package Optimize;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import IR.Node.*;
import IR.Node.Module;

public class SSAOptimizer {
    private HashSet<Var> globals = new HashSet<>();
    private HashMap<Var, Register> local2reg = new HashMap<>();
    private HashMap<Register, Register> use2reg = new HashMap<>();

    public void constructSSA(Module m) {
        for (var func : m.funcDefs) {
            globals.clear();
            local2reg.clear();
            use2reg.clear();
            func.calcRPO();
            func.calcDomTree();
            func.calcDomFrontier();
            findGlobals(func);
            insertPhi(func);
            rename(func.blocks.get(0), func);
        }
    }

    public void destroySSA(Module m) {
        for (var f : m.funcDefs) {
            removePhi(f);
            f.calcRPO();
        }
    }

    public void optimize(Module m) {
        for (var f : m.funcDefs) {
            deadCodeElimination(f);
        }
    }

    // engineering-a-compiler figure 9.9(a)
    private void findGlobals(FuncDef func) {
        var startBB = func.blocks.get(0);
        for (var arg : func.args)
            startBB.defs.add(arg);
        for (var b : func.blocks) {
            for (var i : b.phiMap.values()) {
                var def = i.getDef();
                var uses = i.getUse();
                b.defs.add(def);
                for (var u : uses)
                    b.uses.add(u);
            }
            for (var i : b.ins) {
                var def = i.getDef();
                var uses = i.getUse();
                if (i instanceof Store) {
                    var store = (Store) i;
                    if (func.locals.contains(store.ptr)) {
                        def = store.ptr;
                        uses.remove(def);
                    }
                }
                if (uses != null)
                    for (var u : uses) {
                        b.uses.add(u);
                        if (!b.defs.contains(u) && func.locals.contains(u))
                            globals.add(u);
                    }
                if (def != null) {
                    b.defs.add(def);
                    if (func.locals.contains(def)) {
                        if (def.info == null) {
                            def.info = new Var.VarInfo();
                            var storeIns = (Store) i;
                            def.info.ty = storeIns.value.ty;
                        }
                        def.info.containBB.add(b);
                    }
                }
            }
            var uses = b.exitins.getUse();
            if (uses != null)
                for (var u : uses) {
                    b.uses.add(u);
                    if (!b.defs.contains(u) && func.locals.contains(u))
                        globals.add(u);
                }
        }
    }

    // engineering-a-compiler figure 9.9(b)
    private void insertPhi(FuncDef func) {
        Queue<BasicBlock> worklist = new LinkedList<>();
        HashSet<BasicBlock> visit = new HashSet<>();
        for (var x : globals) {
            worklist.clear();
            visit.clear();
            worklist.addAll(x.info.containBB);
            while (!worklist.isEmpty()) {
                var b = worklist.remove();
                if (visit.contains(b)) continue;
                visit.add(b);
                for (var d : b.DF)
                    if (!d.phiMap.containsKey(x)) {
                        d.addPhi(x, x.info.ty);
                        worklist.add(d);
                    }
            }
        }
    }

    // engineering-a-compiler figure 9.12
    private void rename(BasicBlock b, FuncDef func) {
        // traverse phi
        for (var phi : b.phiMap.values()) {
            if (func.locals.contains(phi.res)) {
                phi.res = phi.res.newName();
                local2reg.put(phi.res, phi.res);
            }
        }
        // traverse others
        var it = b.ins.iterator();
        while (it.hasNext()) {
            var i = it.next();
            var def = i.getDef();
            var uses = i.getUse();
            if (i instanceof Alloca) {
                it.remove();
                continue;
            }
            if (i instanceof Store) {
                var store = (Store) i;
                if (func.locals.contains(store.ptr)) {
                    local2reg.put(store.ptr.newName(), store.value);
                    continue;
                }
            }
            if (i instanceof Load && uses != null && !uses.isEmpty() && func.locals.contains(uses.get(0))) {
                var load = (Load) i;
                use2reg.put(def, local2reg.get(load.ptr.top()));
                it.remove();
                continue;
            }
            if (uses != null)
                for (var u : uses)
                    if (use2reg.containsKey(u))
                        i.replace(u, resolveUse(u));
        }
        // rewrite exitIns
        var exitins = b.exitins;
        var exitUses = exitins.getUse();
        if (exitUses != null)
            for (var u : exitUses)
                if (use2reg.containsKey(u))
                    exitins.replace(u, resolveUse(u));
        // fill in phi in CFG successor
        for (var s : b.succ)
            for (var p : s.phiMap.entrySet()) {
                var old = p.getKey();
                if (globals.contains(old)) {
                    var top = old.top();
                    if (top != null)
                        p.getValue().add(resolveUse(local2reg.get(top)), b);
                    else
                        p.getValue().add(Register.defaultValue(old.info.ty), b);
                }
            }
        // rename DTSon
        for (var s : b.DTSon)
            rename(s, func);
        // pop
        it = b.ins.iterator();
        while (it.hasNext()) {
            var i = it.next();
            if (i instanceof Store) {
                var def = ((Store)i).ptr;
                if (func.locals.contains(def)) {
                    def.pop();
                    it.remove();
                }
            }
        }
        for (var v : b.phiMap.keySet())
            if (globals.contains(v))
                v.pop();
    }

    Register resolveUse(Register u) {
        while (use2reg.containsKey(u)) {
            u = use2reg.get(u);
        }
        return u;
    }


    private void removePhi(FuncDef func) {
        HashMap<BasicBlock, BasicBlock> alterMap = new HashMap<>();
        for (var b : func.getRPO()) {
            if (b.phiMap.isEmpty()) continue;
            alterMap.clear();
            var pre = b.pre;
            // deal with critical edge
            for (var p : pre) {
                if (p.succ.size() > 1) { 
                    var toInsert = func.addBB();
                    alterMap.put(p, toInsert);

                    var exitins = (Br)p.exitins;
                    if (exitins.trueBB == b)
                        exitins.trueBB = toInsert;
                    else
                        exitins.falseBB = toInsert;
                    p.succ.remove(b);
                    p.succ.add(toInsert);
                    toInsert.pre.add(p);
                }
            }
            // adjust pre
            var it = pre.iterator();
            while (it.hasNext()) {
                var p = it.next();
                if (alterMap.containsKey(p))
                    it.remove();
            }
            for (var p : alterMap.values())
                p.exit(new Br(b));
            // place move
            for (var phi : b.phiMap.values()) {
                var dest = phi.res;
                for (var p : phi.list) {
                    if (alterMap.containsKey(p.BB))
                        p.BB = alterMap.get(p.BB);
                    if (dest != p.reg)
                        p.BB.add(new Move(dest, p.reg));
                }
            }
            b.phiMap.clear();
        }
    }  

    // structure used by naive dead code elimination
    private static class DCEInfo {
        public Instruction defby;
        public HashSet<Instruction> useby = new HashSet<>();
        public boolean useful = false;
    }
    private HashMap<Var, DCEInfo> DCEMap = new HashMap<>();

    private DCEInfo getDCEInfo(Var v) {
        if (DCEMap.containsKey(v))
            return DCEMap.get(v);
        var ret = new DCEInfo();
        DCEMap.put(v, ret);
        return ret;
    }
    private boolean isUseful(Instruction i) {
        if (i instanceof Store || i instanceof Call)
            return true;
        return false;
    }
    private void collectDCEInfo(FuncDef func) {
        DCEMap.clear();
        for (var b : func.blocks) {
            // phi ins
            for (var phi : b.phiMap.values()) {
                var resInfo = getDCEInfo(phi.res);
                resInfo.defby = phi;
                var uses = phi.getUse();
                for (var u : uses)
                    getDCEInfo(u).useby.add(phi);
            }
            // ordinary ins
            for (var i : b.ins) {
                var def = i.getDef();
                var uses =i.getUse();
                boolean useful = isUseful(i);
                if (def != null)
                    getDCEInfo(def).defby = i;
                if (uses != null)
                    for (var u : uses) {
                        var info = getDCEInfo(u);
                        info.useby.add(i);
                        if (useful) info.useful = true;
                    }
            }
            // exit ins
            var uses = b.exitins.getUse();
            if (uses != null)
                for (var u : uses) {
                    var info = getDCEInfo(u);
                    info.useby.add(b.exitins);
                    info.useful = true;
                }
        }
    }

    private void deadCodeElimination(FuncDef func) {
        collectDCEInfo(func);
        HashSet<Var> worklist = new HashSet<>();
        HashSet<Instruction> toremove = new HashSet<>();
        // mark useful
        for (var item : DCEMap.entrySet())
            if (item.getValue().useful)
                worklist.add(item.getKey());
        while (!worklist.isEmpty()) {
            var it = worklist.iterator();
            var reg = it.next();
            it.remove();
            var info = DCEMap.get(reg);
            if (info.defby != null) {
                var uses = info.defby.getUse();
                if (uses != null)
                    for (var u : uses) {
                        var useInfo = DCEMap.get(u);
                        if (!useInfo.useful) {
                            useInfo.useful = true;
                            worklist.add(u);
                        }
                    }
            }
        }
        // find dead code
        for (var item : DCEMap.values())
            if (!item.useful) {
                if (item.defby != null && !(item.defby instanceof Call))
                    toremove.add(item.defby);
                toremove.addAll(item.useby);
            }
        // remove deadcode
        for (var b : func.blocks) {
            var phiIt = b.phiMap.entrySet().iterator();
            while (phiIt.hasNext()) {
                var p = phiIt.next();
                if (toremove.contains(p.getValue()))
                    phiIt.remove();
            }
            var it = b.ins.iterator();
            while (it.hasNext()) {
                var i = it.next();
                if (toremove.contains(i))
                    it.remove();
            }
        }
    }

}   

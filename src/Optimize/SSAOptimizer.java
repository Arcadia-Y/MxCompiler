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
            transformParaCopy(f);
            f.calcRPO();
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
                        p.BB.paraCopy.add(new Move(dest, p.reg));
                }
            }
            b.phiMap.clear();
        }
    }
    
    // The-SSA-Book Algorithm 3.6
    private void transformParaCopy(FuncDef func) {
        HashMap<Var, Register> preMap = new HashMap<>();
        for (var b : func.blocks) {
            preMap.clear();
            // collect preInfo
            var it = b.paraCopy.iterator();
            while (it.hasNext()) {
                var i = it.next();
                if (i.src == i.dest)
                    it.remove();
                else 
                    preMap.put(i.dest, i.src);
            }
            // sequentialize
            while (true) {
                boolean flag = true;
                while (flag) {
                    flag = false;
                    it = b.paraCopy.iterator();
                    while (it.hasNext()) {
                        var i = it.next();
                        if (!preMap.containsValue(i.dest)) {
                            flag = true;
                            b.add(i);
                            it.remove();
                            preMap.remove(i.dest);
                            continue;
                        }
                    }
                }
                it = b.paraCopy.iterator();
                if (!it.hasNext()) break;
                // break one of the cycles
                var i = it.next();
                var tmpVar = func.newUnname(i.dest.ty);
                var newMove = new Move(tmpVar, i.src);
                b.add(newMove);
                it.remove();
                b.paraCopy.add(newMove);
                preMap.put(i.dest, tmpVar);
            }
        }
    }

}   

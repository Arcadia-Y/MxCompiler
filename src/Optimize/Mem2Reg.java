package Optimize;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import IR.Node.*;
import IR.Node.Module;

public class Mem2Reg {
    private FuncDef func;
    private HashSet<Var> globals = new HashSet<>();
    private HashMap<Var, Register> local2reg = new HashMap<>();
    private HashMap<Register, Register> use2reg = new HashMap<>();

    public void constructSSA(Module m) {
        for (var f : m.funcDefs) {
            func = f;
            globals.clear();
            local2reg.clear();
            use2reg.clear();
            func.calcRPO();
            func.calcDomTree();
            func.calcDomFrontier();
            findGlobals();
            insertPhi();
            rename(func.blocks.get(0));
        }
    }

    private void findGlobals() {
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


    private void insertPhi() {
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

    private void rename(BasicBlock b) {
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
            if (i instanceof Phi) continue;
            if (i instanceof Alloca) {
                it.remove();
                continue;
            }
            if (func.locals.contains(def)) {
                var store = (Store) i;
                local2reg.put(def.newName(), store.value);
                continue;
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
            rename(s);
        // pop
        it = b.ins.iterator();
        while (it.hasNext()) {
            var i = it.next();
            var def = i.getDef();
            if (func.locals.contains(def)) {
                def.pop();
                it.remove();
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

}   

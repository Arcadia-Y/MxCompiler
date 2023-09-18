package Optimize;

import java.util.HashMap;
import java.util.HashSet;

import IR.Node.*;
import IR.Node.Module;

public class DeadCodeElimination {
    private static class DCEInfo {
        public Instruction defby;
        public HashSet<Instruction> useby = new HashSet<>();
        public boolean useful = false;
    }
    private HashMap<Var, DCEInfo> DCEMap = new HashMap<>();

    public void run(Module mod) {
        for (var f : mod.funcDefs) {
            codeElimination(f);
            jumpElimination(f);
            eliminateUnreachable(f);
        }
    }

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

    public boolean codeElimination(FuncDef func) {
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
        return !toremove.isEmpty();
    }

    // engineering-a-compiler figure 10.2
    public boolean jumpElimination(FuncDef func) {
        boolean ret = false;
        boolean changed = true;
        while (changed) {
            func.calcRPO();
            changed = onePass(func);
            ret |= changed;
        }
        return ret;
    }

    private boolean onePass(FuncDef func) {
        boolean changed = false;
        HashSet<BasicBlock> removeBB = new HashSet<>();
        for (var index = func.RPO.size()-1; index >= 0; index--) {
            var i = func.RPO.get(index);
            var exitins = i.exitins;
            if (exitins instanceof Ret) continue;
            var exit = (Br) exitins;

            if (exit.cond != null) {
                if (exit.trueBB == exit.falseBB) { // branch folding
                    changed = true;
                    i.removeExit();
                    exit.trueBB.removePhiBB(i);
                    i.exit(new Br(exit.trueBB));
                } else if (exit.cond instanceof BoolConst) { // definitive branch
                    changed = true;
                    var boolCond = (BoolConst) exit.cond;
                    i.removeExit();
                    if (boolCond.value) {
                        i.exit(new Br(exit.trueBB));
                        exit.falseBB.removePhiBB(i);
                    } else {
                        i.exit(new Br(exit.falseBB));
                        exit.trueBB.removePhiBB(i); 
                    }
                }
            }

            if (exit.cond == null) {
                var destBB = exit.trueBB;
                if (destBB.pre.size() == 1) { // combine blocks
                    changed = true;
                    i.ins.addAll(destBB.ins);
                    destBB.ins.clear();
                    var newexit = destBB.exitins;
                    destBB.removeExit();
                    i.removeExit();
                    if (newexit instanceof Br) {
                        var br = (Br) newexit;
                        i.exit(br);
                        br.trueBB.adjustPhiBB(destBB, i);
                        if (br.falseBB != null)
                            br.falseBB.adjustPhiBB(destBB, i);
                    } else {
                        var ret = (Ret) newexit;
                        i.exit(ret);
                    }
                    removeBB.add(destBB);
                }
            }
        }

        if (!removeBB.isEmpty()) {
            var it = func.blocks.iterator();
            while (it.hasNext()) {
                var bb = it.next();
                if (removeBB.contains(bb))
                    it.remove();
            }
        }
        return changed;
    }

    public boolean eliminateUnreachable(FuncDef func) {
        boolean changed = false;
        var it = func.blocks.iterator();
        it.next();
        while (it.hasNext()) {
            var b = it.next();
            if (b.pre.isEmpty()) {
                changed = true;
                for (var s : b.succ) {
                    s.pre.remove(b);
                    s.removePhiBB(b);
                }
                it.remove();
            }
        }
        return changed;
    }

}

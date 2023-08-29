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
        for (var f : mod.funcDefs)
            deadCodeElimination(f); 
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

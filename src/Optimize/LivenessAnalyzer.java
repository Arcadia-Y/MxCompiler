package Optimize;

import java.util.HashSet;

import IR.Node.Module;
import IR.Node.BasicBlock;
import IR.Node.FuncDef;
import IR.Node.Var;

// engineering-a-compiler figure 8.14
public class LivenessAnalyzer {

    public void run(Module mod) {
        for (var f : mod.funcDefs) {
            initPass(f);
            solvePass(f);
        }
    }

    private void initPass(FuncDef func) {
        var startBB = func.blocks.get(0);
        startBB.defs.clear();
        startBB.uses.clear();
        startBB.defs.addAll(func.args);
        initBB(startBB);
        var it = func.blocks.iterator();
        it.next();
        while (it.hasNext()) {
            var b = it.next();
            b.defs.clear();
            b.uses.clear();
            initBB(b);
        }
    }

    private void solvePass(FuncDef func) {
        boolean changed = true;
        while (changed) {
            changed = false;
            var rpo = func.getRPO();
            var it = rpo.listIterator(rpo.size());
            while (it.hasPrevious()) {
                var b = it.previous();
                if (recompute(b))
                    changed = true;
            }
        }
    }

    private void initBB(BasicBlock b) {
        for (var i : b.ins) {
            var def = i.getDef();
            var uses = i.getUse();
            if (uses != null)
                for (var u : uses)
                    if (!b.defs.contains(u))
                        b.uses.add(u);
            if (def != null) 
                b.defs.add(def);
        }
        var uses = b.exitins.getUse();
        if (uses != null)
            for (var u : uses)
                if (!b.defs.contains(u))
                b.uses.add(u);
    }

    // return if changed
    private boolean recompute(BasicBlock b) {
        HashSet<Var> newIn = new HashSet<>();
        HashSet<Var> newOut = new HashSet<>();
        for (var s : b.succ)
            newOut.addAll(s.livein);
        newIn.addAll(newOut);
        var defs = b.defs;
        for (var d : defs)
            newIn.remove(d);
        newIn.addAll(b.uses);
        if (!newIn.equals(b.livein) || !newOut.equals(b.liveout)) {
            b.livein = newIn;
            b.liveout = newOut;
            return true;
        }
        return false;
    }

}

package Optimize;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import ASM.Storage.Register;
import ASM.Storage.RegisterSet;
import ASM.Storage.StackSlot;
import ASM.Storage.StoreUnit;
import IR.Node.Call;
import IR.Node.FuncDef;
import IR.Node.Module;
import IR.Node.Var;

// linear scan register allocation
public class RegisterAllocator {
    public RegisterSet regSet = new RegisterSet();
    private static class LiveInterval {
        public Var var;
        public int start = Integer.MAX_VALUE;
        public int end = -1;
        public boolean acrossCall = false;
        public StoreUnit pos;
        public LiveInterval(Var variable) {
            var = variable;
        }
        public void updateStart(int x) {
            start = Math.min(start, x);
        }
        public void updateEnd(int x) {
            end = Math.max(end, x);
        }
    }
    private HashMap<Var, LiveInterval> intInfo = new HashMap<>();
    private TreeSet<Integer> callPos = new TreeSet<>();
    private ArrayList<LiveInterval> intList = new ArrayList<>();
    private HashSet<Register> freeCaller = new HashSet<>();
    private HashSet<Register> freeSave = new HashSet<>();
    private HashSet<StackSlot> freeSlot = new HashSet<>();
    private TreeSet<LiveInterval> activeCaller;
    private TreeSet<LiveInterval> activeSave;
    private TreeSet<LiveInterval> activeSlot;

    private FuncDef func;
    private int order = 0;
    private int frameSize = 0;

    private Comparator<LiveInterval> startComp;
    private Comparator<LiveInterval> endComp;

    public RegisterAllocator() {
        startComp = new Comparator<LiveInterval>() {
            @Override
            public int compare(LiveInterval a, LiveInterval b) {
                return Integer.compare(a.start, b.start);
            }
        };
        endComp = new Comparator<LiveInterval>() {
            @Override
            public int compare(LiveInterval a, LiveInterval b) {
                if (a.end != b.end)
                    return Integer.compare(a.end, b.end);
                return Integer.compare(a.start, b.start);
            }
        };
        activeCaller = new TreeSet<>(endComp);
        activeSave = new TreeSet<>(endComp);
        activeSlot = new TreeSet<>(endComp);
    }

    public void run(Module m) {
        for (var f : m.funcDefs)
            dealFunc(f);
    }

    private void dealFunc(FuncDef f) {
        init(f);
        collectInfo();
        linearAllocate();
        writeBack();
    }

    private void init(FuncDef f) {
        func = f;
        frameSize = 0;
        order = 0;
        intInfo.clear();
        intList.clear();
        callPos.clear();
        callPos.add(Integer.MAX_VALUE);
        activeCaller.clear();
        activeSave.clear();
        activeSlot.clear();
        freeCaller.addAll(regSet.callerReg.values());
        freeSave.addAll(regSet.saveReg.values());
        freeSlot.clear();
    }

    private LiveInterval getIntInfo(Var x) {
        var ret = intInfo.get(x);
        if (ret != null) return ret;
        ret = new LiveInterval(x);
        intInfo.put(x, ret);
        return ret;
    }

    private void collectInfo() {
        var rpo = func.getRPO();
        // collect liveIntervals
        for (var arg : func.args)
            getIntInfo(arg).updateStart(-1);
        for (var b : rpo) {
            for (var i : b.ins) {
                var def = i.getDef();
                var uses = i.getUse();
                if (i instanceof Call)
                    callPos.add(order);
                if (def != null)
                    getIntInfo(def).updateStart(order);
                if (uses != null)
                    for (var u : uses)
                        getIntInfo(u).updateEnd(order);
                order++;
            }
            var exitUses = b.exitins.getUse();
            if (exitUses != null)
                for (var u : exitUses)
                    getIntInfo(u).updateEnd(order);
            order++;
            for (var out : b.liveout)
                getIntInfo(out).updateEnd(order);
        }
        intList.addAll(intInfo.values());
        intList.sort(startComp);
        // mark acrossCall
        for (var i : intList)
            if (callPos.ceiling(i.start + 1) != callPos.ceiling(i.end))
                i.acrossCall = true;
    }

    private void linearAllocate() {
        for (var i : intList) {
            expireOldIntervals(i.start);
            if (i.acrossCall) { // save
                if (freeSave.isEmpty())
                    spillAtIntervalSave(i);
                else {
                    var it = freeSave.iterator();
                    var reg = it.next();
                    func.usedSaveReg.add(reg);
                    i.pos = reg;
                    it.remove();
                    activeSave.add(i);
                }
            } else { // caller
                if (freeCaller.isEmpty())
                    spillAtIntervalCaller(i);
                else {
                    var it = freeCaller.iterator();
                    var reg = it.next();
                    i.pos = reg;
                    it.remove();
                    activeCaller.add(i);
                }
            }
        }
    }

    private void expireOldIntervals(int i) {
        var it = activeCaller.iterator();
        while (it.hasNext()) {
            var j = it.next();
            if (j.end > i)
                break;
            it.remove();
            freeCaller.add((Register)j.pos);
        }
        it = activeSave.iterator();
        while (it.hasNext()) {
            var j = it.next();
            if (j.end > i)
                break;
            it.remove();
            freeSave.add((Register)j.pos);
        }
        it = activeSlot.iterator();
        while (it.hasNext()) {
            var j = it.next();
            if (j.end > i)
                break;
            it.remove();
            freeSlot.add((StackSlot)j.pos);
        }
    }

    private void spillAtIntervalSave(LiveInterval i) {
        var last = activeSave.last();
        if (last.end > i.end) {
            i.pos = last.pos;
            last.pos = spillSlot();
            activeSlot.add(last);
            activeSave.remove(last);
            activeSave.add(i);
        } else {
            i.pos = spillSlot();
        }
    }

    private void spillAtIntervalCaller(LiveInterval i) {
        var last = activeCaller.last();
        if (last.end > i.end) {
            i.pos = last.pos;
            last.pos = spillSlot();
            activeSlot.add(last);
            activeCaller.remove(last);
            activeCaller.add(i);
        } else {
            i.pos = spillSlot();
        }
    }

    private StackSlot spillSlot() {
        if (!freeSlot.isEmpty()) {
            var it = freeSlot.iterator();
            var ret = it.next();
            it.remove();
            return ret;
        }
        var ret = new StackSlot(frameSize);
        frameSize += 4;
        return ret;
    }
    
    private void writeBack() {
        for (var it : intList)
            func.regMap.put(it.var, it.pos);
        func.frameSize = frameSize;
    }

}

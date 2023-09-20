package Optimize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import IR.Node.BasicBlock;
import IR.Node.BinaryInst;
import IR.Node.BoolConst;
import IR.Node.FuncDef;
import IR.Node.Instruction;
import IR.Node.IntConst;
import IR.Node.Module;
import IR.Node.Register;
import IR.Node.Var;

public class ConstantPropagation {
    public static enum Metainfo {
        PHI, UNDEF, INTCONST, BOOLCONST
    } 
    public static class CPInfo {
        public Metainfo metainfo = Metainfo.UNDEF;
        public int value;
        public CPInfo() {}
        public CPInfo(Metainfo meta) {
            metainfo = meta;
        }
        public CPInfo(int v) {
            metainfo = Metainfo.INTCONST;
            value = v;
        }
        public CPInfo(boolean v) {
            metainfo = Metainfo.BOOLCONST;
            value = v ? 1 : 0;
        }
    }

    private HashMap<Var, CPInfo> ssaMap = new HashMap<>();
    private HashMap<Var, ArrayList<Instruction>> useMap = new HashMap<>();
    private CPInfo getCPInfo(Var v) {
        var res = ssaMap.get(v);
        if (res != null) return res;
        res = new CPInfo();
        ssaMap.put(v, res);
        return res;
    }
    private ArrayList<Instruction> getUseInfo(Var v) {
        var res = useMap.get(v);
        if (res != null) return res;
        res = new ArrayList<>();
        useMap.put(v, res);
        return res;
    }

    private LinkedList<Var> worklist = new LinkedList<>();
    private HashSet<Instruction> toremove = new HashSet<>();

    public void run(Module mod) {
        for (var f : mod.funcDefs)
            SimpleConstantPropagation(f);
    }

    // engineering-a-compiler figure 9.18
    public boolean SimpleConstantPropagation(FuncDef func) {
        // initialization phase
        worklist.clear();
        toremove.clear();
        ssaMap.clear();
        useMap.clear();
        collectInfo(func);
        // propagation phase
        propagate();
        // remove useless instructions
        for (var b : func.blocks) {
            var it = b.ins.iterator();
            while (it.hasNext()) {
                var i = it.next();
                if (toremove.contains(i))
                    it.remove();
            }
        }
        return !toremove.isEmpty();
    }

    private void propagate() {
        while (!worklist.isEmpty()) {
            var n = worklist.pop();
            var nValue = getCPInfo(n);
            if (nValue.metainfo == Metainfo.UNDEF) continue;
            Register constValue = null;
            if (nValue.metainfo == Metainfo.INTCONST)
                constValue = new IntConst(nValue.value);
            else
                constValue = new BoolConst(nValue.value == 0 ? false : true);
            for (var op : getUseInfo(n)) {
                op.replace(n, constValue);
                var m = op.getDef();
                if (m == null) continue;
                var mValue = getCPInfo(m);
                if (mValue.metainfo == Metainfo.PHI) continue;
                if (op instanceof Interpretable) {
                    var newValue = ((Interpretable)op).interpret();
                    if (newValue.metainfo != Metainfo.UNDEF) {
                        ssaMap.put(m, newValue);
                        worklist.add(m);
                        toremove.add(op);
                    }
                }
            }
        }
    }

    private void collectInfo(FuncDef func) {
        var bblist = func.getRPO();
        for (var b : bblist) {
            collectPhiInfo(b);
            collectInsInfo(b);
            var uses = b.exitins.getUse();
            if (uses != null)
                for (var u : uses)
                    getUseInfo(u).add(b.exitins);
        }
    }

    private void collectPhiInfo(BasicBlock b) {
        for (var phi : b.phiMap.values()) {
            for (var item : phi.list)
                if (item.reg instanceof Var)
                    getUseInfo((Var)item.reg).add(phi);
            ssaMap.put(phi.res, new CPInfo(Metainfo.PHI));
        }
    }

    private void collectInsInfo(BasicBlock b) {
        for (var i : b.ins) {
            var uses = i.getUse();
            if (uses != null)
                for (var u : uses)
                    getUseInfo(u).add(i);
            if (i instanceof Interpretable) {
                var ins = (Interpretable) i;
                var res = ins.interpret();
                if (res.metainfo == Metainfo.UNDEF) continue;
                ssaMap.put(ins.getRes(), res);
                worklist.add(ins.getRes());
                toremove.add(i);
            }
        }
    }

    private static class AddSubInfo {
        public Var variable;
        public int constInt = 0;
        public AddSubInfo(Var var, int constValue) {
            variable = var;
            constInt = constValue;
        }
    }
    private HashMap<Var, AddSubInfo> addSubMap = new HashMap<>();
    public boolean dealAddSub(FuncDef func) {
        boolean changed = false;
        addSubMap.clear();
        for (var bb : func.blocks)
            for (var ins : bb.ins)
                if (ins instanceof BinaryInst) {
                    var bin = (BinaryInst) ins;
                    boolean addFlag = bin.op.equals("add");
                    boolean subFlag = bin.op.equals("sub");
                    if (!addFlag && !subFlag) continue;
                    boolean lFlag = bin.op1 instanceof Var;
                    boolean rFlag = bin.op2 instanceof Var;
                    if (subFlag && rFlag) continue;
                    if (!(lFlag^rFlag)) continue;
                    Var variable = null;
                    int constValue = 0;
                    if (lFlag) {
                        variable = (Var)bin.op1;
                        constValue = ((IntConst)bin.op2).value;
                    } else {
                        variable = (Var)bin.op2;
                        constValue = ((IntConst)bin.op1).value;
                    }
                    if (subFlag) constValue = -constValue;
                    var newInfo = new AddSubInfo(variable, constValue);
                    var info = addSubMap.get(variable);
                    if (info == null) {
                        addSubMap.put(bin.res, newInfo);
                        continue;
                    }
                    changed = true;
                    newInfo.variable = info.variable;
                    newInfo.constInt = info.constInt + constValue;
                    addSubMap.put(bin.res, newInfo);
                    bin.op1 = newInfo.variable;
                    bin.op2 = new IntConst(newInfo.constInt);
                    bin.op = "add";
                }
        return changed;
    }

}

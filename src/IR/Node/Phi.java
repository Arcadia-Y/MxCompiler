package IR.Node;

import java.util.ArrayList;
import java.util.List;

import IR.IRVisitor;
import IR.Type.Type;

public class Phi extends Instruction {
    public Var res;
    public Type ty;
    public static class Pair {
        public Register reg;
        public BasicBlock BB;
        public Pair(Register r, BasicBlock b) {
            reg = r;
            BB = b;
        }
        public String toString() {
            return "[" + reg.valueString() + ", %" + BB.label + "]";
        }
    }
    public ArrayList<Pair> list = new ArrayList<Pair>();
    public Phi(Var r, Type t) {
        res = r;
        ty = t;
    }
    public void add(Register r, BasicBlock b) {
        list.add(new Pair(r, b));
    }
    public String toString() {
        String ret = res.name + " = phi " + ty.toString() + " ";
        ret += list.get(0).toString();
        for (int i = 1; i < list.size(); i++)
            ret += ", " + list.get(i).toString();
        return ret; 
    }
    public void accept(IRVisitor v) {
        v.visit(this);
    }
    @Override
    public Var getDef() {
        return res;
    }
    @Override
    public List<Var> getUse() {
        var ret = new ArrayList<Var>();
        for (var i : list)
            if (i.reg instanceof Var && ((Var)i.reg).name.charAt(0) != '@')
                ret.add((Var)i.reg);
        return ret;
    }
    @Override
    public void replace(Var v, Register r) {
        for (int i = 0; i < list.size(); i++) {
            var it = list.get(i);
            if (it.reg == v)
                it.reg = r;
        }
    }
}

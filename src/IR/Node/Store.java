package IR.Node;

import java.util.ArrayList;
import java.util.List;

import IR.IRVisitor;

public class Store extends Instruction {
    public Register value;
    public Var ptr;
    public Store(Register v, Var p) {
        value = v;
        ptr = p;
    }
    public String toString() {
        return "store " + value.toString() + ", " + ptr.toString();
    }
    public void accept(IRVisitor v) {
        v.visit(this);
    }
    @Override
    public Var getDef() {
        return null;
    }
    @Override
    public List<Var> getUse() {
        var ret = new ArrayList<Var>();
        if (value instanceof Var && ((Var)value).name.charAt(0) != '@')
            ret.add((Var)value);
        if (ptr instanceof Var && ((Var)ptr).name.charAt(0) != '@')
            ret.add((Var)ptr);
        return ret;
    }
    @Override
    public void replace(Var v, Register r) {
        value = r;
    }
}

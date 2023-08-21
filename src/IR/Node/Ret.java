package IR.Node;

import java.util.ArrayList;
import java.util.List;

import IR.IRVisitor;

public class Ret extends Instruction {
    public Register value;
    public Ret(Register r) {
        value = r;
    }
    public String toString() {
        if (value == null)
            return "ret void";
        return "ret " + value.toString();
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
        return ret;
    }
    @Override
    public void replace(Var v, Register r) {
        value = r;
    }
}

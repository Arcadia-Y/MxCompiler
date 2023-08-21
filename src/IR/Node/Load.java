package IR.Node;

import java.util.ArrayList;
import java.util.List;

import IR.IRVisitor;
import IR.Type.Type;

public class Load extends Instruction {
    public Var res;
    public Type ty;
    public Var ptr;
    public Load(Var r, Type t, Var p) {
        res = r;
        ty = t;
        ptr = p;
    }
    public String toString() {
        return res.name + " = load " + ty.toString() + ", " + ptr.toString();
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
        if (ptr.name.charAt(0) != '@')
            ret.add(ptr);
        return ret;
    }
    @Override
    public void replace(Var v, Register r) {
        ptr = (Var)r;
    }
}

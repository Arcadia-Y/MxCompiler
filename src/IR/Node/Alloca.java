package IR.Node;

import java.util.List;

import IR.IRVisitor;
import IR.Type.Type;

public class Alloca extends Instruction {
    public Var res;
    public Type ty;
    public Alloca(Var v, Type t) {
        res = v;
        ty = t;
    }
    public String toString() {
        return res.name + " = alloca " + ty.toString(); 
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
        return null;
    }
    @Override
    public void replace(Var v, Register r) {
        if (res == v)
            res = (Var)r;
    }
}

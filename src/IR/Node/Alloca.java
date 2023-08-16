package IR.Node;

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
}

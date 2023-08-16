package IR.Node;

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
}

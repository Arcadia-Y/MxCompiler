package IR.Node;

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
}

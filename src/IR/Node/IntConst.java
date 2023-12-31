package IR.Node;

import IR.IRVisitor;
import IR.Type.IntType;

public class IntConst extends Register {
    public int value;
    public IntConst(int v) {
        ty = new IntType(32);
        value = v;
    }
    public String toString() {
        return ty.toString() + " " + value;
    }
    public String valueString() {
        return "" + value;
    }
    public void accept(IRVisitor v) {
        v.visit(this);
    }
}

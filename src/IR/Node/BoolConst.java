package IR.Node;

import IR.Type.IntType;

public class BoolConst extends Register {
    public boolean value;
    public BoolConst(boolean v) {
        ty = new IntType(1);
        value = v;
    }
    public String toString() {
        return ty.toString() + " " + value;
    }
    public String valueString() {
        return "" + value;
    }
}

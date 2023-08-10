package IR.Node;

import IR.Type.PtrType;

public class NullConst extends Register {
    public NullConst() {
        ty = new PtrType();
    }
    public String toString() {
        return "ptr null";
    }
    public String valueString() {
        return "null";
    }
}

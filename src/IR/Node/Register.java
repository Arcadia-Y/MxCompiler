package IR.Node;

import IR.IRVisitor;
import IR.Type.IntType;
import IR.Type.Type;

public abstract class Register extends IRNode {
    public Type ty;
    public abstract String toString();
    public abstract String valueString();
    public abstract void accept(IRVisitor v);
    public static Register defaultValue(Type ty) {
        if (ty instanceof IntType) {
            var intType = (IntType) ty;
            if (intType.bits == 1)
                return new BoolConst(false);
            return new IntConst(0);
        }
        return new NullConst();
    }
}

package IR.Node;

import IR.IRVisitor;
import IR.Type.Type;

public abstract class Register extends IRNode {
    public Type ty;
    public abstract String toString();
    public abstract String valueString();
    public abstract void accept(IRVisitor v);
}

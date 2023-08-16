package IR.Node;

import IR.IRVisitor;

public abstract class Instruction extends IRNode {
    public abstract void accept(IRVisitor v);
}

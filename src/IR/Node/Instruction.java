package IR.Node;

import java.util.List;

import IR.IRVisitor;

public abstract class Instruction extends IRNode implements Cloneable {
    public abstract void accept(IRVisitor v);
    public abstract Var getDef();
    public abstract List<Var> getUse();
    // repalce v with r
    public abstract void replace(Var v, Register r);
}

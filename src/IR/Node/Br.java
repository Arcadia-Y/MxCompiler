package IR.Node;

import IR.IRVisitor;

public class Br extends Instruction {
    public Register cond;
    public BasicBlock trueBB, falseBB;
    public Br(Register c, BasicBlock t, BasicBlock f) {
        cond = c;
        trueBB = t;
        falseBB = f;
    }
    public Br(BasicBlock bb) {
        trueBB = bb;
    }
    public String toString() {
        if (cond == null) 
            return "br label %" + trueBB.label;
        return "br " + cond.toString() + ", label %" + trueBB.label + ", label %" + falseBB.label;
    }
    public void accept(IRVisitor v) {
        v.visit(this);
    }
}

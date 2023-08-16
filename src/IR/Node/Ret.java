package IR.Node;

import IR.IRVisitor;

public class Ret extends Instruction {
    public Register value;
    public Ret(Register r) {
        value = r;
    }
    public String toString() {
        if (value == null)
            return "ret void";
        return "ret " + value.toString();
    }
    public void accept(IRVisitor v) {
        v.visit(this);
    }
}

package IR.Node;

public class Br {
    public Register cond;
    public BasicBlock trueBB, falseBB;
    public String toString() {
        if (cond == null) 
            return "br label " + trueBB.label;
        return "br " + cond.toString() + ", label " + trueBB.label + ", label " + falseBB.label;
    }
}

package IR.Node;

import IR.Type.Type;

public class Icmp extends Instruction {
    public Var res;
    public String cond;
    public Type ty;
    public Register op1, op2;
    public Icmp(Var r, String c, Register o1, Register o2) {
        res = r;
        cond = c;
        ty = o1.ty;
        op1 = o1;
        op2 = o2;
    }
    public String toString() {
        return res.name + " = icmp " + cond + " " + op1.toString() + ", " + op2.valueString();
    }
}
package IR.Node;

import IR.Type.Type;

public class BinaryInst extends Instruction {
    public Var res;
    public String op;
    public Type ty;
    public Register op1 ,op2;
    public BinaryInst(Var r, String o, Register o1, Register o2) {
        res = r;
        op = o;
        ty = o1.ty;
        op1 = o1;
        op2 = o2;
    }
    public String toString() {
        return res.name + " = " + op + " " + op1.toString() + ", " + op2.valueString();
    }
}

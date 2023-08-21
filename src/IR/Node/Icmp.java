package IR.Node;

import java.util.ArrayList;
import java.util.List;

import IR.IRVisitor;
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
    public void accept(IRVisitor v) {
        v.visit(this);
    }
    @Override
    public Var getDef() {
        return res;
    }
    @Override
    public List<Var> getUse() {
        ArrayList<Var> ret = new ArrayList<>();
        if (op1 instanceof Var) {
            var v = (Var)op1;
            if (v.name.charAt(0) != '@')
                ret.add(v);
        }
        if (op2 instanceof Var) {
            var v = (Var)op2;
            if (v.name.charAt(0) != '@')
                ret.add(v);
        }
        return ret;
    }
    @Override
    public void replace(Var v, Register r) {
        if (op1 == v)
            op1 = r;
        else if (op2 == v)
            op2 = r;
    }
}

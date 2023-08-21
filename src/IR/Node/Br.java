package IR.Node;

import java.util.ArrayList;
import java.util.List;

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
    @Override
    public Var getDef() {
        return null;
    }
    @Override
    public List<Var> getUse() {
        if (cond instanceof Var) {
            var ret = new ArrayList<Var>();
            var v = (Var)cond;
            if (v.name.charAt(0) != '@')
                ret.add((Var)cond);
            return ret;
        }
        return null;
    }
    @Override
    public void replace(Var v, Register r) {
        cond = r;
    }
}

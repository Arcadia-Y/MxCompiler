package IR.Node;

import java.util.ArrayList;
import java.util.List;

import IR.IRVisitor;
import Optimize.ConstantPropagation.CPInfo;
import Optimize.ConstantPropagation.Metainfo;
import Optimize.Interpretable;

public class Move extends Instruction implements Interpretable {
    public Var dest;
    public Register src;

    public Move(Var destination, Register source) {
        dest = destination;
        src = source;
    }
    @Override
    public void accept(IRVisitor v) {
        v.visit(this);
    }

    @Override
    public Var getDef() {
        return dest;
    }

    @Override
    public List<Var> getUse() {
        ArrayList<Var> ret = new ArrayList<>();
        if (src instanceof Var && ((Var)src).name.charAt(0) != '@')
            ret.add((Var)src);
        return ret;
    }

    @Override
    public void replace(Var v, Register r) {
        if (v == src)
            src = r;
    }

    @Override
    public String toString() {
        return dest.toString() + " = " + src.toString();
    }
    @Override
    public CPInfo interpret() {
        if (src instanceof BoolConst)
            return new CPInfo(((BoolConst)src).value);
        if (src instanceof IntConst)
            return new CPInfo(((IntConst)src).value);
        return new CPInfo(Metainfo.UNDEF);
    }
    @Override
    public Var getRes() {
        return dest;
    }
}

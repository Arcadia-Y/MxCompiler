package IR.Node;

import java.util.ArrayList;
import java.util.List;

import IR.IRVisitor;
import IR.Type.Type;

public class GEP extends Instruction {
    public Var res;
    public Type ty;
    public Var ptr;
    public ArrayList<Register> index = new ArrayList<Register>();
    public GEP(Var r, Type t, Var p) {
        res = r;
        ty = t;
        ptr = p;
    }
    public String toString() {
        String ret = res.name + " = getelementptr " + ty.toString() + ", " + ptr.toString();
        for (var it : index) {
            ret += ", ";
            ret += it.toString();
        }
        return ret;
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
        var ret = new ArrayList<Var>();
        if (ptr.name.charAt(0) != '@')
            ret.add(ptr);
        for (var i : index)
            if (i instanceof Var && ((Var)i).name.charAt(0) != '@')
                ret.add((Var)i);
        return ret;
    }
    @Override
    public void replace(Var v, Register r) {
        if (ptr == v) {
            ptr = (Var)r;
            return;
        }
        for (int id = 0; id < index.size(); id++) {
            var i = index.get(id);
            if (i == v) {
                index.set(id, r);
            }
        }
    }
}

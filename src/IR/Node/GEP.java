package IR.Node;

import java.util.ArrayList;

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
}

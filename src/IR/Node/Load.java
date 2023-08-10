package IR.Node;

import IR.Type.Type;

public class Load extends Instruction {
    Var res;
    Type ty;
    Var ptr;
    public Load(Var r, Type t, Var p) {
        res = r;
        ty = t;
        ptr = p;
    }
    public String toString() {
        return res.name + " = load " + ty.toString() + ", " + ptr.toString();
    }
}

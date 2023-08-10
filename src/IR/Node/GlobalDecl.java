package IR.Node;

public class GlobalDecl {
    public Var reg;
    public Register init; // must be a const
    public GlobalDecl(Var v, Register i) {
        reg = v;
        init = i;
    } 
    public String toString() {
        return reg.name + " = global " + init.toString(); 
    }
}

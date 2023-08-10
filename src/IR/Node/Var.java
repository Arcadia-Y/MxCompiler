package IR.Node;

import IR.Type.Type;

public class Var extends Register {
    public String name;
    public Var(Type t, String n) {
        ty = t;
        name = n;
    }
    public String toString() {
        return ty.toString() + " " + name;
    }
    public String valueString() {
        return name;
    }
}

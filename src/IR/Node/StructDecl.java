package IR.Node;

import java.util.ArrayList;

import IR.Type.Type;

public class StructDecl {
    public String name;
    public ArrayList<Type> mem = new ArrayList<Type>();

    public StructDecl(String n) {
        name = n;
    }
    public String toString() {
        String ret = name + " = type {";
        if (!mem.isEmpty())
            ret += mem.get(0).toString();
        for (int i = 1; i < mem.size(); i++)
            ret += ", " + mem.get(i).toString();
        ret += "}";
        return ret;
    }
}

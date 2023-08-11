package IR.Node;

import java.util.ArrayList;

import IR.Type.*;

public class FuncDecl extends IRNode {
    public Type ty;
    public ArrayList<Type> args = new ArrayList<Type>();
    public String name;
    
    public FuncDecl(Type t, String n) {
        ty = t;
        name = n;
    }
    public String toString() {
        String ret = "declare " + ty.toString() + " @" + name + "(";
        if (!args.isEmpty())
            ret += args.get(0).toString();
        for (int i = 1; i < args.size(); i++)
            ret += ", " + args.get(i).toString();
        ret += ")";
        return ret;
    }
}

package IR.Node;

import java.util.ArrayList;

import Util.Type;

public class FuncDecl extends IRNode {
    public Type ty;
    public ArrayList<Type> args;
    public String name;
    
    public String toString() {
        String ret = "declare " + ty.toString() + " " + name + "(";
        if (!args.isEmpty())
            ret += args.get(0).toString();
        for (int i = 1; i < args.size(); i++)
            ret += ", " + args.get(i).toString();
        ret += ")";
        return ret;
    }
}

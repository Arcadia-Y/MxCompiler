package IR.Node;

import java.util.ArrayList;

import IR.Type.Type;

public class FuncDef extends IRNode {
    public Type ty;
    public String name;
    public ArrayList<Var> args;
    public ArrayList<BasicBlock> blocks;

    public String toString() {
        String ret = "define " + ty.toString() + " " + name + "(";
        if (!args.isEmpty())
            ret += args.get(0).toString();
        for (int i = 1; i < args.size(); i++)
            ret += ", " + args.get(i).toString();
        ret += ") {\n";
        for (var it : blocks)
            ret += it.toString() + "\n";
        ret += "}";
        return ret;
    }
}

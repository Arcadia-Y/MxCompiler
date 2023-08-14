package IR.Node;

import java.util.ArrayList;

import IR.Type.Type;

public class FuncDef extends IRNode {
    public Type ty;
    public String name;
    public ArrayList<Var> args = new  ArrayList<Var>();
    public ArrayList<BasicBlock> blocks = new ArrayList<BasicBlock>();
    private int regCnt = 0;
    private int BBCnt = 0;

    public FuncDef(Type t, String n) {
        ty = t;
        name = n;
    }
    public BasicBlock getLastBlock() {
        return blocks.get(blocks.size()-1);
    }
    public BasicBlock addBB() {
        BasicBlock ret = new BasicBlock("_BB"+BBCnt);
        blocks.add(ret);
        BBCnt++;
        return ret;
    }
    public Var newUnname(Type t) {
        Var ret = new Var(t, "%_" + regCnt);
        regCnt++;
        return ret;
    }
    public String toString() {
        String ret = "define " + ty.toString() + " @" + name + "(";
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

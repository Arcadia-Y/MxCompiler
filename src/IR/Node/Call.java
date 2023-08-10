package IR.Node;

import java.util.ArrayList;

import IR.Type.Type;

public class Call extends Instruction {
    public Var res;
    public Type ty;
    public String name;
    public ArrayList<Register> args = new ArrayList<Register>();
    public Call(Var r, Type t, String n) {
        res = r;
        ty = t;
        name = n;
    }
    public String toString() {
        String ret;
        if (res == null)
            ret = "call void " + name;
        else 
            ret = res.name + " = call " + ty.toString() + " " + name;
        ret += "(";
        if (!args.isEmpty())
            ret += args.get(0).toString();
        for (int i = 1; i < args.size(); i++)
            ret += ", " + args.get(i).toString();
        ret += ")";
        return ret;
    }
}

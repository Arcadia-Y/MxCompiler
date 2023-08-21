package IR.Node;

import java.util.ArrayList;
import java.util.List;

import IR.IRVisitor;
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
            ret = "call void @" + name;
        else 
            ret = res.name + " = call " + ty.toString() + " @" + name;
        ret += "(";
        if (!args.isEmpty())
            ret += args.get(0).toString();
        for (int i = 1; i < args.size(); i++)
            ret += ", " + args.get(i).toString();
        ret += ")";
        return ret;
    }
    public void accept(IRVisitor v) {
        v.visit(this);
    }
    @Override
    public Var getDef() {
        return res;
    }
    @Override
    public List<Var> getUse() {
        var ret = new ArrayList<Var>();
        for (var a : args)
            if (a instanceof Var && ((Var)a).name.charAt(0) != '@')
                ret.add((Var)a);
        return ret;
    }
    @Override
    public void replace(Var v, Register r) {
        for (int i = 0; i < args.size(); i++) {
            var it = args.get(i);
            if (it == v)
                args.set(i, r);
        }
    }
}

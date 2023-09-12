package IR.Node;

import java.util.ArrayList;
import java.util.List;

import IR.IRVisitor;
import IR.Type.Type;
import Optimize.Interpretable;
import Optimize.ConstantPropagation.CPInfo;
import Optimize.ConstantPropagation.Metainfo;

public class BinaryInst extends Instruction implements Interpretable {
    public Var res;
    public String op;
    public Type ty;
    public Register op1 ,op2;
    public BinaryInst(Var r, String o, Register o1, Register o2) {
        res = r;
        op = o;
        ty = o1.ty;
        op1 = o1;
        op2 = o2;
    }
    public String toString() {
        return res.name + " = " + op + " " + op1.toString() + ", " + op2.valueString();
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
        ArrayList<Var> ret = new ArrayList<>();
        if (op1 instanceof Var) {
            var v = (Var)op1;
            if (v.name.charAt(0) != '@')
                ret.add(v);
        }
        if (op2 instanceof Var) {
            var v = (Var)op2;
            if (v.name.charAt(0) != '@')
                ret.add(v);
        }
        return ret;
    }
    @Override
    public void replace(Var v, Register r) {
        if (op1 == v)
            op1 = r;
        else if (op2 == v)
            op2 = r; 
    }
    
    public Var getRes() {
        return res;
    }

    public CPInfo interpret() {
        if (op1 instanceof Var || op2 instanceof Var)
            return new CPInfo(Metainfo.UNDEF);
        int v1 = ((IntConst)op1).value;
        int v2 = ((IntConst)op2).value;
        int res = 0;
        switch (op) {
        case "add":
            res = v1 + v2;
            break;
        case "sub":
            res = v1 - v2;
            break;
        case "mul":
            res = v1 * v2;
            break;
        case "sdiv":
            if (v2 == 0) return new CPInfo(Metainfo.UNDEF);
            res = v1 / v2;
            break;
        case "srem":
            if (v2 == 0) return new CPInfo(Metainfo.UNDEF);
            res = v1 % v2;
            break;
        case "shl":
            res = v1 << v2;
            break;
        case "ashr":
            res = v1 >> v2;
            break;
        case "and":
            res = v1 & v2;
            break;
        case "or":
            res = v1 | v2;
            break;
        case "xor":
            res = v1 ^ v2;
            break;
        }
        return new CPInfo(res);
    }
}

package IR.Node;

import java.util.HashSet;
import java.util.Stack;

import IR.IRVisitor;
import IR.Type.Type;

public class Var extends Register {
    public String name;
    public Var(Type t, String n) {
        ty = t;
        name = n;
    }
    public static class VarInfo {
        public HashSet<BasicBlock> containBB = new HashSet<>();
        public Stack<Var> renameStack = new Stack<>();
        public int cnt = 0;
        public Type ty; // type of the (derefered) local variable
    }
    public VarInfo info = null;
    public String toString() {
        return ty.toString() + " " + name;
    }
    public String valueString() {
        return name;
    }
    public void accept(IRVisitor v) {
        v.visit(this);
    }
    public Var newName() {
        int i = info.cnt;
        info.cnt++;
        Var ret = new Var(info.ty, name + "." + i);
        info.renameStack.push(ret);
        return ret;
    }
    public Var top() {
        if (info.renameStack.empty()) return null;
        return info.renameStack.peek();
    }
    public void pop() {
        info.renameStack.pop();
    }
}

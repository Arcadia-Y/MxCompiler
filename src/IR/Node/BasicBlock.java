package IR.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import IR.Type.Type;

public class BasicBlock extends IRNode {
    public String label;
    public HashMap<Var, Phi> phiMap = new HashMap<>();
    public LinkedList<Instruction> ins = new LinkedList<>();
    public Instruction exitins;

    public ArrayList<BasicBlock> pre = new ArrayList<>();
    public ArrayList<BasicBlock> succ = new ArrayList<>();

    public HashSet<Var> defs = new HashSet<Var>();
    public HashSet<Var> uses = new HashSet<Var>();
    public HashSet<Var> livein = new HashSet<>();
    public HashSet<Var> liveout = new HashSet<>();

    public boolean visited = false;
    public int RPOid;
    public BasicBlock idom;
    public HashSet<BasicBlock> DF = new HashSet<>(); // Dominance Frontier
    public HashSet<BasicBlock> DTSon = new HashSet<>();

    public BasicBlock(String n) {
        label = n;
    }
    public boolean isend() {
        return exitins != null;
    }
    public void add(Instruction i) {
        ins.add(i);
    }
    public Phi addPhi(Var res, Type ty) {
        Phi p = phiMap.get(res);
        if (p != null)
            return p;
        p = new Phi(res, ty);
        phiMap.put(res, p);
        return p;
    }
    public void exit(Br br) {
        exitins = br;
        succ.add(br.trueBB);
        br.trueBB.pre.add(this);
        if (br.cond != null) {
            succ.add(br.falseBB);
            br.falseBB.pre.add(this);
        }
    }
    public void exit(Ret ret) {
        exitins = ret;
    }
    public String toString() {
        String ret = label + ":\n";
        for (var i : phiMap.values()) {
            ret += "    " + i.toString();
            ret += '\n';
        }
        for (var item : ins) {
            ret += "    " + item.toString();
            ret += '\n';
        }
        ret += "    " + exitins.toString();
        ret += '\n';
        return ret;
    }
}

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
    public LinkedList<Move> paraCopy = new LinkedList<>();

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
    public void removeExit() {
        if (exitins instanceof Ret) return;
        Br br = (Br) exitins;
        succ.remove(br.trueBB);
        br.trueBB.pre.remove(this);
        if (br.cond != null) {
            succ.remove(br.falseBB);
            br.falseBB.pre.remove(this);
        }
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

    public void adjustPhiBB(BasicBlock originBB, BasicBlock newBB) {
        for (var phi : phiMap.values())
            for (var pair : phi.list)
                if (pair.BB == originBB)
                    pair.BB = newBB;                    
    }
    public void removePhiBB(BasicBlock target) {
        for (var phi : phiMap.values()) {
            var it = phi.list.iterator();
            while (it.hasNext()) {
                var pair = it.next();
                if (pair.BB == target) {
                    it.remove();
                    break;
                }
            }
        }
        if (pre.size() == 1) {
            var preBB = pre.get(0);
            for (var phi : phiMap.values()) {
                var item = phi.list.get(0);
                preBB.add(new Move(phi.res, item.reg));
            }
        }
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

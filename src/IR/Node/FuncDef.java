package IR.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import ASM.Storage.StoreUnit;
import IR.Type.Type;

public class FuncDef extends IRNode {
    public Type ty;
    public String name;
    public ArrayList<Var> args = new ArrayList<Var>();
    public HashSet<Var> locals = new HashSet<>();
    public HashSet<Var> unnames = new HashSet<>();
    public ArrayList<BasicBlock> blocks = new ArrayList<>();
    public ArrayList<BasicBlock> RPO = null; // Reverse Post Order
    private int regCnt = 0;
    private int BBCnt = 0;

    public HashMap<Var, StoreUnit> regMap = new HashMap<>();
    public HashSet<ASM.Storage.Register> usedSaveReg = new HashSet<>(); 
    public int frameSize;
    public HashSet<Var> deadArg = new HashSet<>();

    public FuncDef(Type t, String n) {
        ty = t;
        name = n;
    }

    public BasicBlock addBB() {
        BasicBlock ret = new BasicBlock("_BB" + BBCnt);
        blocks.add(ret);
        BBCnt++;
        return ret;
    }

    public Var newUnname(Type t) {
        Var ret = new Var(t, "%_" + regCnt);
        regCnt++;
        return ret;
    }

    public Var addUnname(Type t) {
        Var ret = new Var(t, "%_" + regCnt);
        regCnt++;
        unnames.add(ret);
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
            ret += it.toString();
        ret += "}";
        return ret;
    }

    public ArrayList<BasicBlock> getRPO() {
        if (RPO == null)
            calcRPO();
        return RPO;
    }

    public void calcRPO() {
        RPO = new ArrayList<>();
        dfsPostOrder(blocks.get(0));
        Collections.reverse(RPO);
        int i = 0;
        for (var b : RPO) {
            b.RPOid = i;
            b.visited = false;
            i++;
        }
    }

    public void dfsPostOrder(BasicBlock x) {
        x.visited = true;
        for (var i : x.succ)
            if (!i.visited)
                dfsPostOrder(i);
        RPO.add(x);
    }

    // A Simple, Fast Dominance Algorithm
    // by Keith D. Cooper, Timothy J. Harvey, and Ken Kennedy
    public void calcDomTree() {
        BasicBlock startBB = blocks.get(0);
        startBB.idom = startBB;
        var list = blocks.subList(1, blocks.size());
        boolean changed = true;
        while (changed) {
            changed = false;
            for (var node : list) {
                BasicBlock newIdom = null;
                for (var pre : node.pre)
                    if (pre.idom != null) {
                        newIdom = pre;
                        break;
                    }
                for (var pre : node.pre)
                    if (pre != newIdom && pre.idom != null)
                        newIdom = intersect(pre, newIdom);
                if (node.idom != newIdom) {
                    node.idom = newIdom;
                    changed = true;
                }
            }
        }
        startBB.idom = null;
        var it = blocks.iterator();
        it.next();
        while (it.hasNext()) {
            var b = it.next();
            if (b.idom == null)
                it.remove(); // remove unreachable blocks
            else
                b.idom.DTSon.add(b);
        }
    }

    private BasicBlock intersect(BasicBlock b1, BasicBlock b2) {
        while (b1 != b2) {
            while (b1.RPOid > b2.RPOid)
                b1 = b1.idom;
            while (b2.RPOid > b1.RPOid)
                b2 = b2.idom;
        }
        return b1;
    }

    public void calcDomFrontier() {
        var rpo = getRPO();
        for (var b : rpo) {
            if (b.pre.size() < 2)
                continue;
            for (var p : b.pre) {
                var runner = p;
                while (runner != b.idom) {
                    runner.DF.add(b);
                    runner = runner.idom;
                }
            }
        }
    }

}

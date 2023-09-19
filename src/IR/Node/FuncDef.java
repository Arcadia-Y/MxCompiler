package IR.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

import ASM.Storage.StoreUnit;
import IR.Type.Type;

public class FuncDef extends IRNode {
    public Type ty;
    public String name;
    public ArrayList<Var> args = new ArrayList<Var>();
    public HashSet<Var> locals = new HashSet<>();
    public LinkedList<BasicBlock> blocks = new LinkedList<>();
    public ArrayList<BasicBlock> RPO = null; // Reverse Post Order
    private int regCnt = 0;
    private int BBCnt = 0;

    public HashMap<Var, StoreUnit> regMap = new HashMap<>();
    public HashSet<ASM.Storage.Register> usedSaveReg = new HashSet<>(); 
    public int frameSize;
    public HashSet<Var> deadArg = new HashSet<>();

    public int insCnt = 0;
    public int callCnt = 0;

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

    public Var addUnname(Type t) {
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
        for (var b : blocks)
            b.visited = false;
        dfsPostOrder(blocks.get(0));
        Collections.reverse(RPO);
        int i = 0;
        for (var b : RPO) {
            b.RPOid = i;
            b.visited = false;
            i++;
        }
    }

    // use heap memory to avoid stack overflow
    public void dfsPostOrder(BasicBlock start) {
        Stack<BasicBlock> bbStack = new Stack<>();
        Stack<Integer> indexStack = new Stack<>();
        start.visited = true;
        bbStack.push(start);
        indexStack.push(0);
        while (!bbStack.empty()) {
            int id = indexStack.pop();
            var bb = bbStack.peek();
            while (id < bb.succ.size() && bb.succ.get(id).visited) id++;
            if (id == bb.succ.size()) {
                bbStack.pop();
                RPO.add(bb);
                continue;
            }
            indexStack.push(id+1);
            var son = bb.succ.get(id);
            son.visited = true;
            bbStack.push(son);
            indexStack.push(0);
        }
    }

    /*  Reference:
     *  A Simple, Fast Dominance Algorithm
     *  Keith D. Cooper, Timothy J. Harvey and Ken Kennedy
     *  https://web.cse.ohio-state.edu/~rountev.1/788/papers/cooper-spe01.pdf
     */
    public void calcDomTree() {
        for (var b : blocks) {
            b.DF.clear();
            b.DTSon.clear();
            b.idom = null;
            b.defs.clear();
            b.uses.clear();
        }
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
            if (b.idom == null) { // remove unreachable blocks
                b.removeExit();
                it.remove();
            }
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

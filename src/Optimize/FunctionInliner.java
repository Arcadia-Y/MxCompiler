package Optimize;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import IR.Node.*;
import IR.Node.Module;
import IR.Type.PtrType;

public class FunctionInliner {
    private HashMap<String, FuncDef> name2func = new HashMap<>();
    private HashMap<Var, Var> varMap = new HashMap<>();
    private HashMap<Var, Register> argMap = new HashMap<>();
    private HashMap<BasicBlock, BasicBlock> bbMap = new HashMap<>();
    private FuncDef curFunc;
    private int CALLEELIMIT = 300;
    private int CALLERLIMIT = 1500;

    public void run(Module mod) {
        buildMap(mod);
        collectCallCnt(mod);
        boolean changed = true;
        while (changed) {
            changed = false;
            collectInsCnt(mod);
            for (var f : mod.funcDefs)
                changed |= dealFunc(f);
        }
    }

    private Var getNewVar(Var oldVar) {
        var res = varMap.get(oldVar);
        if (res != null) return res;
        if (oldVar.name.charAt(0) == '@') // global variable
            return oldVar;
        res = curFunc.addUnname(oldVar.ty);
        varMap.put(oldVar, res);
        return res;
    }
    private Register getNewReg(Register oldReg) {
        if (oldReg instanceof Var) {
            var oldVar = (Var) oldReg;
            var posArg = argMap.get(oldVar);
            if (posArg != null)
                return posArg;
            return getNewVar(oldVar);
        }
        return oldReg;
    }
    private BasicBlock getNewBB(BasicBlock oldBB) {
        var res = bbMap.get(oldBB);
        if (res != null) return res;
        res = curFunc.addBB();
        bbMap.put(oldBB, res);
        return res;
    }

    private void transPhi(BasicBlock oldBB, BasicBlock newBB) {  
        for (var phi : oldBB.phiMap.values()) {
            var newPhi = newBB.addPhi(getNewVar(phi.res), phi.ty);
            for (var pair : phi.list)
                newPhi.list.add(new Phi.Pair(getNewReg(pair.reg), getNewBB(pair.BB)));
        }
    }
    private Instruction transIns(Instruction oldIns) {
        if (oldIns instanceof Load) {
            var i = (Load)oldIns;
            return new Load(getNewVar(i.res), i.ty, getNewVar(i.ptr));
        }
        if (oldIns instanceof Store) {
            var i = (Store)oldIns;
            return new Store(getNewReg(i.value), getNewVar(i.ptr));
        }
        if (oldIns instanceof BinaryInst) {
            var i = (BinaryInst) oldIns;
            return new BinaryInst(getNewVar(i.res), i.op, getNewReg(i.op1), getNewReg(i.op2));
        }
        if (oldIns instanceof Alloca) {
            var i = (Alloca) oldIns;
            var newVar = getNewVar(i.res);
            curFunc.locals.add(newVar);
            return new Alloca(newVar, i.ty);
        }
        if (oldIns instanceof Icmp) {
            var i = (Icmp)oldIns;
            return new Icmp(getNewVar(i.res), i.cond, getNewReg(i.op1), getNewReg(i.op2));
        }
        if (oldIns instanceof GEP) {
            var i = (GEP)oldIns;
            var res = new GEP(getNewVar(i.res), i.ty, getNewVar(i.ptr));
            for (var item : i.index)
                res.index.add(getNewReg(item));
            return res;
        }
        if (oldIns instanceof Call) { // builtin function call
            var i = (Call) oldIns;
            Call res = null;
            if (i.res == null)
                res = new Call(null, i.ty, i.name);
            else
                res = new Call(getNewVar(i.res), i.ty, i.name);
            for (var arg : i.args)
                res.args.add(getNewReg(arg));
            return res;
        }
        if (oldIns instanceof Move) {
            var i = (Move)oldIns;
            return new Move(getNewVar(i.dest), getNewReg(i.src));
        }
        // should never be reached
        return null;
    }
    private void transBr(Br br, BasicBlock curBB) {
        if (br.cond == null)
            curBB.exit(new Br(getNewBB(br.trueBB)));
        else 
            curBB.exit(new Br(getNewReg(br.cond), getNewBB(br.trueBB), getNewBB(br.falseBB)));
        return;
    }
    private void transRet(Ret ret, BasicBlock curBB, Var retVar, BasicBlock retBB) {
        if (ret.value != null)
            curBB.add(new Store(getNewReg(ret.value), retVar));
        curBB.exit(new Br(retBB));
    }

    // return startBB of inlined function
    private BasicBlock doinline(FuncDef inlineFunc, Var retVar, BasicBlock retBB) {
        var startBB = getNewBB(inlineFunc.blocks.get(0));
        for (var b : inlineFunc.blocks) {
            var curBB = getNewBB(b);
            transPhi(b, curBB);
            for (var i : b.ins)
                curBB.add(transIns(i));
            var exit = b.exitins;
            if (exit instanceof Br)
                transBr((Br)exit, curBB);
            else 
                transRet((Ret)exit, curBB, retVar, retBB);
        }
        return startBB;
    }
    
    private boolean dealFunc(FuncDef func) {
        if (func.callCnt == 0 || func.insCnt > CALLERLIMIT) return false;
        boolean changed = false;
        curFunc = func;
        Queue<BasicBlock> queue = new LinkedList<>();
        for (var b : func.blocks)
            b.visited = false;
        queue.add(func.blocks.getFirst());
        while (!queue.isEmpty()) {
            var bb = queue.remove();
            if (bb.visited) continue;
            bb.visited = true;
            // find call to inline
            FuncDef funcInline = null;
            Call callInline = null;
            var it = bb.ins.iterator();
            while (it.hasNext()) {
                var i = it.next();
                if (i instanceof Call) {
                    var call = (Call) i;
                    var f = name2func.get(call.name);
                    if (f != null && f.callCnt == 0 && f.insCnt <= CALLEELIMIT) {
                        funcInline = f;
                        callInline = call;
                        it.remove();
                        break;
                    }
                }
            }
            // no call to inline
            if (funcInline == null) {
                queue.addAll(bb.succ);
                continue;
            }
            // inline the call
            changed = true;
            func.callCnt--;
            // move remaining ins to newBB
            var newBB = curFunc.addBB();
            while (it.hasNext()) {
                var i = it.next();
                newBB.add(i);
                it.remove();
            }
            var exitIns = bb.exitins;
            for (var succ : bb.succ)
                succ.adjustPhiBB(bb, newBB);
            bb.removeExit();
            if (exitIns instanceof Br)
                newBB.exit((Br)exitIns);
            else 
                newBB.exit((Ret)exitIns);
            // deal with call args & ret
            varMap.clear();
            bbMap.clear();
            argMap.clear();
            for (int i = 0; i < callInline.args.size(); i++) {
                var funcArg = funcInline.args.get(i);
                var callArg = callInline.args.get(i);
                if (funcArg.name.equals("%this"))
                    varMap.put(funcArg, (Var)callArg);
                else
                    argMap.put(funcArg, callArg);  
            }
            Var retVar = null;
            if (callInline.res != null) {
                retVar = func.addUnname(new PtrType());
                func.locals.add(retVar);
                bb.add(new Alloca(retVar, callInline.ty));
                newBB.ins.addFirst(new Load(callInline.res, callInline.ty, retVar));
            }
            var startBB = doinline(funcInline, retVar, newBB);
            bb.exit(new Br(startBB));
            collectFuncIns(func);
            if (func.insCnt > CALLERLIMIT)
                break;
            queue.add(newBB);
        }
        curFunc = null;
        return changed;
    }

    private void buildMap(Module mod) {
        name2func.clear();
        for (var f : mod.funcDefs)
            name2func.put(f.name, f);
    }
    private void collectCallCnt(Module mod) {
        for (var f : mod.funcDefs) {
            f.callCnt = 0;
            for (var b : f.blocks)
                for (var i : b.ins)
                    if (i instanceof Call && name2func.containsKey(((Call)i).name))
                        ++f.callCnt;
        }
    }
    private void collectInsCnt(Module mod) {
        for (var f : mod.funcDefs) {
            collectFuncIns(f);
        }
    }
    private void collectFuncIns(FuncDef f) {
        f.insCnt = 0;
        for (var b : f.blocks) {
            f.insCnt += b.phiMap.size();
            f.insCnt += b.ins.size();
            f.insCnt++;
        }
    } 
    
}

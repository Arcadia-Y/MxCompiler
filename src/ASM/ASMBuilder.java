package ASM;

import java.util.HashMap;

import ASM.Node.*;
import ASM.Node.Instruction;
import ASM.Storage.*;
import ASM.Storage.Register;
import IR.IRVisitor;
import IR.Node.Module;
import IR.Node.*;

public class ASMBuilder implements IRVisitor {
    ASMModule asm;
    Function curFunc;
    Block curBB;
    int BBcnt = 0;

    HashMap<BasicBlock, Block> bb2bb;
    HashMap<Var, StoreUnit> regMap;
    HashMap<Var, Integer> allocMap;
    int frameSize;
    int raOffset, s1Offset;
    boolean methodFlag = false;

    HashMap<Register, StackSlot> saveMap = new HashMap<>();
    HashMap<Integer, StackSlot> argumentMap = new HashMap<>();
    StackSlot raSlot;
    public RegisterSet regSet;
    
    public ASMBuilder(RegisterSet registerSet) {
        regSet = registerSet;
    }

    public ASMModule generateASM(Module it) {
        asm = new ASMModule();
        visit(it);
        return asm;
    }

    /*
     * stack layout:
     * arg 8
     * arg 9
     * ...
     * ra
     * [callee-saved registers]
     * [spill]
     */
    private void getStackLayout(FuncDef func) {
        frameSize = func.frameSize;
        saveMap.clear();
        argumentMap.clear();
        for (var s : func.usedSaveReg) {
            saveMap.put(s, new StackSlot(frameSize));
            frameSize += 4;
        }
        raSlot = new StackSlot(frameSize);
        frameSize += 4;
        for (int i = func.args.size(); i > 7; i--) {
            argumentMap.put(i, new StackSlot(frameSize));
            frameSize += 4;
        }
    }

    // if reg locates in register, return the register
    // else load the reg into dest, retiurn dest
    // only used for NON-STORE instruction
    private Register load(IR.Node.Register reg, Register dest) {
        if (reg instanceof IntConst) {
            curBB.add(new Instruction("li", dest.toString(), ""+((IntConst)reg).value));
            return dest;
        }
        if (reg instanceof BoolConst) {
            boolean val = ((BoolConst)reg).value;
            if (!val)
                return regSet.zero;
            curBB.add(new Instruction("li", dest.toString(), "1"));
            return dest;
        }
        if (reg instanceof NullConst) {
            return regSet.zero;
        }
        Var var = (Var) reg;
        // const string
        if (var.name.charAt(0) == '@') {
            curBB.add(new Instruction("lui", dest.toString(), "%hi(" + var.name.substring(1) + ")"));
            curBB.add(new Instruction("addi", dest.toString(), dest.toString(), "%lo(" + var.name.substring(1) + ")"));
            return dest;
        }
        // local
        var locat = regMap.get(var);
        if (locat instanceof Register)
            return (Register) locat;
        curBB.add(new Instruction("lw", dest.toString(), locat.toString()));
        return dest;
    }

    private void moveReg(Register dest, Register src) {
        if (dest == src) return;
        curBB.add(new Instruction("mv", dest.toString(), src.toString()));    
    }

    @Override
    public void visit(Module it) {
        for (var i : it.globals)
            visit(i);
        for (var i : it.strings)
            visit(i);
        for (var i : it.funcDefs)
            visit(i);
    }

    @Override
    public void visit(GlobalDecl it) {
        GlobalVar glob = new GlobalVar(it.reg.name.substring(1), 0);
        asm.globals.add(glob);
    }

    @Override
    public void visit(StringGlobal it) {
        GlobalStr str = new GlobalStr(it.reg.name.substring(1), it.str);
        asm.strings.add(str);
    }

    @Override
    public void visit(FuncDef it) {
        curFunc = new Function(it.name);
        asm.funcs.add(curFunc);
        bb2bb = new HashMap<BasicBlock, Block>();
        curBB = newBB();
        curFunc.blocks.add(curBB);
        getStackLayout(it);
        regMap = it.regMap;
        curBB.add(new Instruction("addi", "sp", "sp", "-" + frameSize));
        curBB.add(new Instruction("sw", "ra", raSlot.toString()));
        storeSaveReg();
        moveDefArg(it);
        for (var b : it.blocks)
            visit(b);
        curFunc = null;
        curBB = null;
    }

    // move funcDef argument to its allocated location
    private void moveDefArg(FuncDef f) {
        HashMap<Var, Register> posMap = new HashMap<>();
        HashMap<Register, Var> argMap = new HashMap<>();
        // deal with a0 to a7
        for (int i = 0; i < 8 && i < f.args.size(); i++) {
            Var v = f.args.get(i);
            if (f.deadArg.contains(v)) continue;
            var pos = regSet.callerReg.get("a" + i);
            posMap.put(v, pos);
            argMap.put(pos, v);
        }
        for (int i = 0; i < 8 && i < f.args.size(); i++) {
            Var tomove = f.args.get(i);
            if (f.deadArg.contains(tomove)) continue;
            var src = posMap.get(tomove);
            var dest = regMap.get(tomove);
            argMap.remove(src);
            if (dest instanceof StackSlot) {
                curBB.add(new Instruction("sw", src.toString(), dest.toString()));
                continue;
            }
            // check possible collision
            var destReg = (Register) dest;
            if (!argMap.containsKey(destReg)) {
                moveReg(destReg, src);
                continue;
            }
            // swap the location
            Var toSrcVar = argMap.get(destReg);
            moveReg(regSet.t0, destReg);
            moveReg(destReg, src);
            moveReg(src, regSet.t0);
            posMap.put(toSrcVar, src);
            argMap.put(src, toSrcVar);
        }
        // move a8...
        for (int i = 8; i < f.args.size(); i++) {
            var src = argumentMap.get(i);
            var dest = regMap.get(f.args.get(i));
            if (dest instanceof Register) {
                curBB.add(new Instruction("lw", dest.toString(), src.toString()));
                continue;
            }
            curBB.add(new Instruction("lw", "t0", src.toString()));
            curBB.add(new Instruction("sw", "t0", dest.toString()));
        }
    }

    private StoreUnit getCallPos(int i) {
        if (i < 8) return regSet.callerReg.get("a"+i);
        return new StackSlot((7 - i) * 4);
    }

    private void moveCallArg(Call fCall) {
        HashMap<Var, Register> posMap = new HashMap<>();
        HashMap<Var, Integer> argIndex = new HashMap<>();
        HashMap<Register, Var> argMap = new HashMap<>();
        // deal with arguments locting on a0-a7
        for (int i = 0; i < fCall.args.size(); i++) {
            var a = fCall.args.get(i);
            if (a instanceof Var) {
                var pos = regMap.get(a);
                if (pos instanceof Register && ((Register)pos).name.charAt(0) == 'a') {
                    posMap.put((Var)a, (Register)pos);
                    argIndex.put((Var)a, i);
                    argMap.put((Register)pos, (Var)a);
                }
            }
        }
        // similar algorithm with moveDefArg
        for (var a : posMap.keySet()) {
            var src = posMap.get(a);
            var dest = getCallPos(argIndex.get(a));
            argMap.remove(src);
            if (dest instanceof StackSlot) {
                curBB.add(new Instruction("sw", src.toString(), dest.toString()));
                continue;
            }
            // check
            var destReg = (Register) dest;
            if (!argMap.containsKey(destReg)) {
                moveReg(destReg, src);
                continue;
            }
            // swap
            Var toSrcVar = argMap.get(destReg);
            moveReg(regSet.t0, destReg);
            moveReg(destReg, src);
            moveReg(src, regSet.t0);
            posMap.put(toSrcVar, src);
            argMap.put(src, toSrcVar); 
        }
        // put other arguments
        for (int i = 0; i < fCall.args.size() && i < 8; i++) {
            var a = fCall.args.get(i);
            if (argIndex.containsKey(a)) continue;
            var dest = regSet.callerReg.get("a" + i);
            var src = load(a, dest);
            moveReg(dest, src);
        }
        for (int i = 8; i < fCall.args.size(); i++) {
            var a = fCall.args.get(i);
            if (argIndex.containsKey(a)) continue;
            var dest = new StackSlot((7 - i) * 4);
            var src = load(a, regSet.t0);
            curBB.add(new Instruction("sw", src.toString(), dest.toString()));
        }
    }
    
    private void storeSaveReg() {
        for (var i : saveMap.entrySet())
            curBB.add(new Instruction("sw", i.getKey().toString(), i.getValue().toString()));
    }

    private void loadSaveReg() {
        for (var i : saveMap.entrySet())
            curBB.add(new Instruction("lw", i.getKey().toString(), i.getValue().toString()));
    }

    private Block newBB() {
        Block ret = new Block("_BB." + BBcnt);
        BBcnt++;
        return ret;
    }

    private Block findBB(BasicBlock it) {
        if (bb2bb.containsKey(it))
            return bb2bb.get(it);
        Block ret = newBB();
        bb2bb.put(it, ret);
        return ret;
    }

    @Override
    public void visit(BasicBlock it) {
        curBB = findBB(it);
        curFunc.blocks.add(curBB);
        for (var i : it.ins)
            i.accept(this);
        it.exitins.accept(this);
    }

    @Override
    public void visit(Alloca it) {
        return;
    }

    private void addBinaryInst(StoreUnit dest, String op, String op1, String op2) {
        if (dest instanceof Register) {
            curBB.add(new Instruction(op, dest.toString(), op1, op2));
            return;
        }
        curBB.add(new Instruction(op, "t0", op1, op2));
        curBB.add(new Instruction("sw", "t0", dest.toString()));
    }

    private boolean checkRange(int x) {
        return x >= -2048 && x <= 2047;
    }

    @Override
    public void visit(BinaryInst it) {
        String binOp = "";
        boolean immFlag = true;
        boolean shiftFlag = false;
        var dest = regMap.get(it.res);
        switch (it.op) {
        case "mul":
        case "sub":
            immFlag = false;
        case "add":
        case "and":
        case "or":
        case "xor":
            binOp = it.op;
            break;
        case "sdiv":
            binOp = "div";
            immFlag = false;
            break;
        case "srem":
            binOp = "rem";
            immFlag = false;
            break;
        case "shl" :
            binOp = "sll";
            shiftFlag = true;
            break;
        case "ashr":
            binOp = "sra";
            shiftFlag = true;
            break;
        }
        if (immFlag) {
            if (it.op2 instanceof IntConst) {
                var op2Val = ((IntConst)it.op2).value;
                if (checkRange(op2Val)) {
                    var op1Reg = load(it.op1, regSet.t0);
                    addBinaryInst(dest, binOp + "i", op1Reg.toString(), ""+op2Val);
                    return;
                }
            }
            else if (it.op1 instanceof IntConst && !shiftFlag) {
                var op1Val = ((IntConst)it.op1).value;
                if (checkRange(op1Val)) {
                    var op2Reg = load(it.op2, regSet.t0);
                    addBinaryInst(dest, binOp + "i", op2Reg.toString(), ""+op1Val);
                    return;
                }
            } 
        }
        var op1Reg = load(it.op1, regSet.t0);
        var op2Reg = load(it.op2, regSet.ra);
        addBinaryInst(dest, binOp, op1Reg.toString(), op2Reg.toString());
    }

    @Override
    public void visit(BoolConst it) {
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(Br it) {
        if (it.cond == null) {
            Block goal = findBB(it.trueBB);
            curBB.exit(new Instruction("j", goal.label));
            return;
        }
        var condReg = load(it.cond, regSet.t0);
        Block trueBB = findBB(it.trueBB), falseBB = findBB(it.falseBB);
        curBB.exit(new Instruction("bnez", condReg.toString(), trueBB.label));
        curBB.exit(new Instruction("j", falseBB.label));
    }

    @Override
    public void visit(Call it) {
        moveCallArg(it);
        curBB.add(new Instruction("call", it.name));
        if (it.res != null) {
            var dest = regMap.get(it.res);
            if (dest instanceof Register)
                moveReg((Register)dest, regSet.a0);
            else
                curBB.add(new Instruction("sw", "a0", dest.toString()));
        }
    }

    @Override
    public void visit(FuncDecl it) {
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(GEP it) {
        // array
        if (it.index.size() == 1) {
            var ptrReg = load(it.ptr, regSet.t0);
            var indexReg = load(it.index.get(0), regSet.ra);
            if (!it.ty.isBool()) {
                curBB.add(new Instruction("slli", "ra", indexReg.toString(), "2"));
                indexReg = regSet.ra;
            }
            addBinaryInst(regMap.get(it.res), "add", ptrReg.toString(), indexReg.toString());
            return;
        }
        // class
        int offset = ((IntConst)it.index.get(1)).value * 4;
        var ptrReg = load(it.ptr, regSet.t0);
        addBinaryInst(regMap.get(it.res), "addi", ptrReg.toString(), ""+offset);
    }

    @Override
    public void visit(Icmp it) {
        var op1reg = load(it.op1, regSet.t0);
        var op2reg = load(it.op2, regSet.ra);
        var dest = regMap.get(it.res);
        switch (it.cond) {
        case "sle":
            addBinaryInst(regSet.t0, "slt", op2reg.toString(), op1reg.toString());
            addBinaryInst(dest, "xori", "t0", "1");
            break;
        case "sge":
            addBinaryInst(regSet.t0, "slt", op1reg.toString(), op2reg.toString());
            addBinaryInst(dest, "xori", "t0", "1");
            break;
        case "slt":
            addBinaryInst(dest, "slt", op1reg.toString(), op2reg.toString());
            break;
        case "sgt":
            addBinaryInst(dest, "slt", op2reg.toString(), op1reg.toString());
            break;
        case "eq":
            addBinaryInst(regSet.t0, "sub", op1reg.toString(), op2reg.toString());
            addBinaryInst(dest, "sltiu", "t0", "1");
            break;
        case "ne":
            addBinaryInst(regSet.t0, "sub", op1reg.toString(), op2reg.toString());
            addBinaryInst(dest, "sltu", "x0", "t0");
            break;
        }
    }

    @Override
    public void visit(IntConst it) {
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(Load it) {
        var dest = regMap.get(it.res);
        // global
        if (it.ptr.name.charAt(0) == '@') {
            if (dest instanceof Register) {
                if (it.ty.isBool()) curBB.add(new Instruction("lb", dest.toString(), it.ptr.name.substring(1)));
                else    curBB.add(new Instruction("lw", dest.toString(), it.ptr.name.substring(1)));
                return;
            }
            // dest instanceof StackSlot
            if (it.ty.isBool()) curBB.add(new Instruction("lb", "t0", it.ptr.name.substring(1)));
            else    curBB.add(new Instruction("lw", "t0", it.ptr.name.substring(1)));
            curBB.add(new Instruction("sw", "t0", dest.toString()));
            return;
        }
        // ptr
        var ptrReg = load(it.ptr, regSet.t0);
        if (dest instanceof Register) {
            if (it.ty.isBool()) curBB.add(new Instruction("lb", dest.toString(), "0(" + ptrReg + ")"));
            else    curBB.add(new Instruction("lw", dest.toString(), "0(" + ptrReg + ")"));
            return;
        }
        if (it.ty.isBool()) curBB.add(new Instruction("lb", "t0", "0(" + ptrReg + ")"));
        else    curBB.add(new Instruction("lw", "t0", "0(" + ptrReg + ")"));
        curBB.add(new Instruction("sw", "t0", dest.toString()));
        return;
    }

    @Override
    public void visit(NullConst it) {
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(Phi it) {
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(Ret it) {
        if (it.value != null) {
            var resReg = load(it.value, regSet.a0);
            moveReg(regSet.a0, resReg);
        }
        loadSaveReg();
        curBB.add(new Instruction("lw", "ra", raSlot.toString()));
        curBB.add(new Instruction("addi", "sp", "sp", ""+frameSize));
        curBB.exit(new Instruction("ret"));
    }

    @Override
    public void visit(Store it) {
        var value = load(it.value, regSet.t0);
        // global
        if (it.ptr.name.charAt(0) == '@') {
            if (it.value.ty.isBool())
                curBB.add(new Instruction("sb", value.toString(), it.ptr.name.substring(1), "ra"));
            else 
                curBB.add(new Instruction("sw", value.toString(), it.ptr.name.substring(1), "ra"));
            return;
        }
        var ptr = load(it.ptr, regSet.ra);
        // ptr
        if (it.value.ty.isBool())
            curBB.add(new Instruction("sb", value.toString(), "0(" + ptr + ")"));
        else 
            curBB.add(new Instruction("sw", value.toString(), "0(" + ptr + ")"));
        return;
    }

    @Override
    public void visit(StructDecl it) {
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(Var it) {
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(Move it) {
        var dest = regMap.get(it.dest);
        if (dest instanceof Register) {
            var destReg = (Register) dest;
            var src = load(it.src, destReg);
            moveReg(destReg, src);
            return;
        }
        var src = load(it.src, regSet.t0);
        curBB.add(new Instruction("sw", src.toString(), dest.toString()));
    }
    
}

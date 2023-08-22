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

    public ASMModule generateASM(Module it) {
        asm = new ASMModule();
        visit(it);
        return asm;
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
        regAlloc(it);
        curBB.add(new Instruction("addi sp, sp, -" + frameSize));
        curBB.add(new Instruction("sw ra, " + raOffset + "(sp)"));
        for (var b : it.blocks)
            visit(b);
        methodFlag = false;
        curFunc = null;
        curBB = null;
    }

    private Block newBB() {
        Block ret = new Block("_BB." + BBcnt);
        BBcnt++;
        return ret;
    }

    /*
     * stack layout:
     * arguments (if more than 8, top down: 8,9,10...)
     * s1 (caller saved for thisPtr)
     * ra
     * locals
     * unnames
     */
    private void regAlloc(FuncDef it) {
        frameSize = 0;
        regMap = new HashMap<Var, StoreUnit>();
        for (var i : it.unnames) {
            regMap.put(i, new StackSlot(frameSize));
            frameSize += 4;
        }
        allocMap = new HashMap<Var, Integer>();
        for (var i : it.locals) {
            allocMap.put(i, frameSize);
            frameSize += 4;
        }
        raOffset = frameSize;
        frameSize += 4;

        if (!it.args.isEmpty()) {
            var arg = it.args.get(0);
            if (arg.name.equals("%this")) {
                methodFlag = true;
                s1Offset = frameSize;
                frameSize += 4;
                curBB.add(new Instruction("mv s1, a0"));
                regMap.put(arg, new Register("s1"));
            } else {
                regMap.put(arg, new Register("a0"));
            }
        }

        for (int i = 1; i < 8 && i < it.args.size(); i++)
            regMap.put(it.args.get(i), new Register("a" + i));
        for (int i = it.args.size() - 1; i > 7; i--) {
            regMap.put(it.args.get(i), new StackSlot(frameSize));
            frameSize += 4;
        }
    }

    private void loadTo(IR.Node.Register reg, String rd) {
        if (reg instanceof IntConst) {
            curBB.add(new Instruction("li "+ rd + ", " + ((IntConst)reg).value));
            return;
        }
        if (reg instanceof BoolConst) {
            int val = ((BoolConst)reg).value ? 1 : 0;
            curBB.add(new Instruction("li "+ rd + ", " + val));
            return;
        }
        if (reg instanceof NullConst) {
            curBB.add(new Instruction("li "+ rd + ", 0"));
            return;
        }
        Var var = (Var) reg;
        // global string
        if (var.name.charAt(0) == '@') {
            curBB.add(new Instruction("lui " + rd + ", %hi(" + var.name.substring(1) + ")"));
            curBB.add(new Instruction("addi " + rd + ", " + rd + ", %lo(" + var.name.substring(1) + ")"));
            return;
        }
        // local
        var locat = regMap.get(var);
        if (locat instanceof Register) {
            curBB.add(new Instruction("mv " + rd + ", " + ((Register)locat).name));
            return;
        }
        curBB.add(new Instruction("lw " + rd + ", " + ((StackSlot)locat).offset + "(sp)"));
    }

    private void storeTo(IR.Node.Var var, String rd) {
        // global string
        if (var.name.charAt(0) == '@') {
            curBB.add(new Instruction("sw " + rd + ", " + var.name.substring(1) + ", s0"));
            return;
        }
        // local
        var locat = regMap.get(var);
        if (locat instanceof Register) {
            curBB.add(new Instruction("mv " + ((Register)locat).name + ", " + rd));
            return;
        }
        curBB.add(new Instruction("sw " + rd + ", " + ((StackSlot)locat).offset + "(sp)"));
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
        for (var i : it.phiMap.values())
            i.accept(this);
        for (var i : it.ins)
            i.accept(this);
        it.exitins.accept(this);
    }

    @Override
    public void visit(Alloca it) {
        return;
    }

    @Override
    public void visit(BinaryInst it) {
        loadTo(it.op1, "t0");
        loadTo(it.op2, "t1");
        String binOp = "";
        switch (it.op) {
        case "mul":
        case "add":
        case "sub":
        case "and":
        case "or":
        case "xor":
            binOp = it.op;
            break;
        case "sdiv":
            binOp = "div";
            break;
        case "srem":
            binOp = "rem";
            break;
        case "shl" :
            binOp = "sll";
            break;
        case "ashr":
            binOp = "sra";
            break;
        }
        curBB.add(new Instruction(binOp + " t2, t0, t1"));
        storeTo(it.res, "t2");
        
    }

    @Override
    public void visit(BoolConst it) {
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(Br it) {
        if (it.cond == null) {
            Block goal = findBB(it.trueBB);
            curBB.exit(new Instruction("j " + goal.label));
            return;
        }
        loadTo(it.cond, "t0");
        Block trueBB = findBB(it.trueBB), falseBB = findBB(it.falseBB);
        curBB.exit(new Instruction("bnez t0, " + trueBB.label));
        curBB.exit(new Instruction("j " + falseBB.label));
    }

    @Override
    public void visit(Call it) {
        for (int i = 0; i < 8 && i < it.args.size(); i++)
            loadTo(it.args.get(i), "a" + i);
        int argOffset = -4;
        for (int i = 8; i < it.args.size(); i++, argOffset -= 4) {
            var reg = it.args.get(i);
            loadTo(reg, "t0");
            curBB.add(new Instruction("sw t0, " + argOffset + "(sp)"));
        }
        // save s1 for method
        if (methodFlag) {
            curBB.add(new Instruction("sw s1, " + s1Offset + "(sp)"));
            curBB.add(new Instruction("call " + it.name));
            curBB.add(new Instruction("lw s1, " + s1Offset + "(sp)"));
        } else {
            curBB.add(new Instruction("call " + it.name));
        }
        if (it.res != null)
            storeTo(it.res, "a0");
    }

    @Override
    public void visit(FuncDecl it) {
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(GEP it) {
        // array
        if (it.index.size() == 1) {
            loadTo(it.ptr, "t0");
            loadTo(it.index.get(0), "t1");
            if (!it.ty.isBool()) 
                curBB.add(new Instruction("slli t1, t1, 2"));
            curBB.add(new Instruction("add t2, t0, t1"));
            storeTo(it.res, "t2");
            return;
        }
        // class
        int offset = ((IntConst)it.index.get(1)).value * 4;
        loadTo(it.ptr, "t0");
        if (offset != 0)
            curBB.add(new Instruction("addi t0, t0, " + offset));
        storeTo(it.res, "t0");
    }

    @Override
    public void visit(Icmp it) {
        loadTo(it.op1, "t0");
        loadTo(it.op2, "t1");
        switch (it.cond) {
        case "sle":
            curBB.add(new Instruction("slt t2, t1, t0"));
            curBB.add(new Instruction("xori t2, t2, 1"));
            break;
        case "sge":
            curBB.add(new Instruction("slt t2, t0, t1"));
            curBB.add(new Instruction("xori t2, t2, 1"));
            break;
        case "slt":
            curBB.add(new Instruction("slt t2, t0, t1"));
            break;
        case "sgt":
            curBB.add(new Instruction("slt t2, t1, t0"));
            break;
        case "eq":
            curBB.add(new Instruction("sub t2, t0, t1"));
            curBB.add(new Instruction("seqz t2, t2"));
            break;
        case "ne":
            curBB.add(new Instruction("sub t2, t0, t1"));
            curBB.add(new Instruction("snez t2, t2"));
            break;
        }
        storeTo(it.res, "t2");
    }

    @Override
    public void visit(IntConst it) {
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(Load it) {
        // alloc
        if (allocMap.containsKey(it.ptr)) {
            int offset = allocMap.get(it.ptr);
            if (it.ty.isBool())
                curBB.add(new Instruction("lb t0, " + offset + "(sp)"));
            else 
                curBB.add(new Instruction("lw t0, " + offset + "(sp)"));
            storeTo(it.res, "t0");
            return;
        }
        // global
        if (it.ptr.name.charAt(0) == '@') {
            if (it.ty.isBool())
                curBB.add(new Instruction("lb t0, " + it.ptr.name.substring(1)));
            else 
                curBB.add(new Instruction("lw t0, " + it.ptr.name.substring(1)));
            storeTo(it.res, "t0");
            return;
        }
        // ptr
        loadTo(it.ptr, "t0");
        if (it.ty.isBool())
            curBB.add(new Instruction("lb t1, 0(t0)"));
        else 
            curBB.add(new Instruction("lw t1, 0(t0)"));
        storeTo(it.res, "t1");
        return;
    }

    @Override
    public void visit(NullConst it) {
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(Phi it) {
        var oringinBB = curBB;
        for (var i : it.list) {
            curBB = findBB(i.BB);
            loadTo(i.reg, "s0");
            var tomove = curBB.list.get(curBB.list.size()-1);
            curBB.phi(tomove);
            curBB.list.remove(tomove);
        }
        curBB = oringinBB;
        storeTo(it.res, "s0");
    }

    @Override
    public void visit(Ret it) {
        if (it.value != null)
            loadTo(it.value, "a0");
        curBB.add(new Instruction("lw ra, " + raOffset + "(sp)"));
        curBB.add(new Instruction("addi sp, sp, " + frameSize));
        curBB.exit(new Instruction("ret"));
    }

    @Override
    public void visit(Store it) {
        loadTo(it.value, "t0");
         // alloc
         if (allocMap.containsKey(it.ptr)) {
            int offset = allocMap.get(it.ptr);
            if (it.value.ty.isBool())
                curBB.add(new Instruction("sb t0, " + offset + "(sp)"));
            else 
                curBB.add(new Instruction("sw t0, " + offset + "(sp)"));
            return;
        }
        // global
        if (it.ptr.name.charAt(0) == '@') {
            if (it.value.ty.isBool())
                curBB.add(new Instruction("sb t0, " + it.ptr.name.substring(1) + ", t1"));
            else 
                curBB.add(new Instruction("sw t0, " + it.ptr.name.substring(1) + ", t1"));
            return;
        }
        loadTo(it.ptr, "t1");
        // ptr
        if (it.value.ty.isBool())
            curBB.add(new Instruction("sb t0, 0(t1)"));
        else 
            curBB.add(new Instruction("sw t0, 0(t1)"));
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }
    
}

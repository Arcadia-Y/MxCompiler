package IR;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;

import AST.*;
import IR.Node.*;
import IR.Node.BoolConst;
import IR.Node.IntConst;
import IR.Node.Module;
import IR.Node.NullConst;
import IR.Type.*;
import Semantic.Scope;
import Util.ClassType;
import Util.FuncType;
import Util.Type.BaseType;

public class IRBuilder implements ASTVisitor {
    Module mod;
    Register resReg;
    String resFuncName;

    String curClass;
    StructDecl curStruct;
    Scope classScope;
    FuncDef curFunc;
    BasicBlock curBB;

    boolean constructFlag = false;
    boolean methodFlag = false;
    Var thisPtr;
    
    Deque<BasicBlock> breakStack = new ArrayDeque<BasicBlock>();
    Deque<BasicBlock> continueStack = new ArrayDeque<BasicBlock>();
    FuncDef initFunc;
    HashMap<String, Scope> classInfo;
    HashMap<String, Var> name2local;
    HashMap<String, Var> name2global = new HashMap<String, Var>();
    HashMap<String, StructDecl> name2struct = new HashMap<String, StructDecl>();
    HashMap<String, Integer> name2pos = new HashMap<String, Integer>(); // class member name to its position

    int stringCnt = 0;
    HashMap<String, Var> string2var = new HashMap<String, Var>();

    private IntType intType = new IntType(32);
    private IntType boolType = new IntType(1);
    private VoidType voidType = new VoidType();
    private PtrType ptrType = new PtrType();

    private Type transType(Util.Type ty) {
        if (ty.arrayLayer != 0)
            return ptrType;
        switch(ty.baseType) {
        case INT:
            return intType;
        case BOOL:
            return boolType;
        case VOID:
            return voidType;
        default:
            return ptrType;
        }
    }

    public Module getIR(Program it) {
        visit(it);
        return mod;
    }

    @Override
    public void visit(Program it) {
        mod = new Module();
        classInfo = it.scope.classInfo;
        initFunc = new FuncDef(voidType, "_mx_init");
        mod.funcDefs.add(initFunc);
        // struct definition
        for (var item: it.decls)
            if (item instanceof ClassDecl)
                visitStructMem((ClassDecl) item);
        // global variable
        curFunc = initFunc;
        curBB = initFunc.addBB();
        name2local = new HashMap<String, Var>();
        for (var item : it.decls)
            if (item instanceof VarDecl)
                visitGlobalVarDecl((VarDecl) item);
        curBB.exit(new Ret(null));
        curFunc = null;
        curBB = null;
        name2local = null;
        initFunc.getLastBlock().exit(new Ret(null));
        // function
        for (var item : it.decls)
            if (!(item instanceof VarDecl))
                item.accept(this);
    }

    private void visitStructMem(ClassDecl it) {
        curStruct = new StructDecl(it.name);
        mod.structs.add(curStruct);
        name2struct.put(it.name, curStruct);
        int count = 0;
        for (var i : it.mem)
            if (i instanceof VarDecl) {
                var vd = (VarDecl) i;
                Type ty = transType(vd.t.t);
                for (var v : vd.inits) {
                    curStruct.mem.add(ty);
                    name2pos.put(it.name + "." + v.id, count);
                    count++;
                }
            }
        curStruct = null;
    }

    private void visitGlobalVarDecl(VarDecl it) {
        for (var i : it.inits) {
            Var v = new Var(ptrType, "@" + i.id);
            Register reg;
            if (it.t.t.arrayLayer == 0) {
                switch (it.t.t.baseType) {
                case INT:
                    reg = new IntConst(0);
                    break;
                case BOOL:
                    reg = new BoolConst(false);
                    break;
                default:
                    reg = new NullConst();
                }
            } else {
                reg = new NullConst();
            }
            mod.globals.add(new GlobalDecl(v, reg));
            name2global.put(i.id, v);
            if (i.init != null) {
                i.init.accept(this);
                getValue(i.init);
                curBB.add(new Store(resReg, v));
            }
        }
    }

    @Override
    public void visit(VarDecl it) {
        // local variable
        Type ty = transType(it.t.t);
        for (var i : it.inits) {
            Var v = new Var(ptrType, "%" + i.id);
            name2local.put(i.id, v);
            curBB.add(new Alloca(v, ty));
            if (i.init != null) {
                i.init.accept(this);
                getValue(i.init);
                curBB.add(new Store(resReg, v));
            }
        }
    }

    @Override
    public void visit(AST.FuncDecl it) {
        name2local = new HashMap<String, Var>();
        Type retType = transType(it.retType.t);

        if (curClass != null) { // class method
            curFunc = new FuncDef(retType, curClass + "." + it.name);
            thisPtr = new Var(ptrType, "%this");
            curFunc.args.add(thisPtr);
        } else { // global function
            curFunc = new FuncDef(retType, it.name);
        }

        mod.funcDefs.add(curFunc);
        curBB = curFunc.addBB();
        if (it.name.equals("main")) {
            curBB.add(new Call(null, voidType, "_mx_init"));
        }
        
        if (!(retType instanceof VoidType)) { // only for non-void function
            curFunc.retBB = curFunc.addBB();
            curFunc.retVar = new Var(ptrType, "%return");
            curBB.add(new Alloca(curFunc.retVar, retType));
            Var res = curFunc.newUnname(retType);
            curFunc.retBB.add(new Load(res, retType, curFunc.retVar));
            curFunc.retBB.exit(new Ret(res));
        }

        for (var i : it.para) {
            Type ty = transType(i.t.t);
            Var arg = curFunc.newUnname(ty);
            curFunc.args.add(arg);
            Var v = new Var(ptrType, "%" + i.name);
            name2local.put(i.name, v);
            curBB.add(new Alloca(v, ty));
            curBB.add(new Store(arg, v));
        }

        it.block.accept(this);
        if (!curBB.isend()) {
            if (retType instanceof VoidType)
                curBB.exit(new Ret(null));
            else {
                if (curFunc.name.equals("main"))
                    curBB.add(new Store(new IntConst(0), curFunc.retVar));
                curBB.exit(new Br(curFunc.retBB));
            }
        }
        curFunc = null;
        curBB = null;
        name2local = null;
        thisPtr = null;
    }

    @Override
    public void visit(ClassDecl it) {
        curClass = it.name;
        classScope = classInfo.get(curClass);
        curStruct = name2struct.get(curClass);
        for (var item : it.mem)
            if (!(item instanceof VarDecl))
                item.accept(this);
        if (!constructFlag) {
            FuncDef construct = new FuncDef(voidType, curClass + "." + curClass);
            construct.args.add(construct.newUnname(ptrType));
            BasicBlock tmpBB = construct.addBB();
            tmpBB.exit(new Ret(null));
            mod.funcDefs.add(construct);
        }
        constructFlag = false;
        curClass = null;
        classScope = null;
        curStruct = null;
    }

    @Override
    public void visit(ConstructDecl it) {
        constructFlag = true;
        curFunc = new FuncDef(voidType, curClass + "." + it.name);
        mod.funcDefs.add(curFunc);
        curBB = curFunc.addBB();
        name2local = new HashMap<String, Var>();
        thisPtr = new Var(ptrType, "%this");
        curFunc.args.add(thisPtr);
        name2local.put("this", thisPtr);


        it.block.accept(this);
        if (!curBB.isend())
            curBB.exit(new Ret(null));
        curFunc = null;
        curBB = null;
        name2local = null;
        thisPtr = null;
    }

    @Override
    public void visit(BlockStmt it) {
        for (var i : it.stmts) {
            if (curBB.isend()) break;
            i.accept(this);
        }
    }

    @Override
    public void visit(ExprStmt it) {
        if (it.expr != null)
            it.expr.accept(this);
    }

    @Override
    public void visit(IfStmt it) {
        it.cond.accept(this);
        getValue(it.cond);
        Register cond = resReg;
        BasicBlock originBB = curBB;
        if (it.falseStmt == null) {
            BasicBlock trueBB = curFunc.addBB();
            curBB = trueBB;
            it.trueStmt.accept(this);
            BasicBlock endBB = curFunc.addBB();
            if (!curBB.isend())
                curBB.exit(new Br(endBB));
            originBB.exit(new Br(cond, trueBB, endBB));
            curBB = endBB;
            return;
        }
        BasicBlock trueBB = curFunc.addBB();
        BasicBlock falseBB = curFunc.addBB();
        BasicBlock endBB = curFunc.addBB();
        curBB = trueBB;
        it.trueStmt.accept(this);
        if (!curBB.isend())
            curBB.exit(new Br(endBB));
        curBB = falseBB;
        it.falseStmt.accept(this);
        if (!curBB.isend())
            curBB.exit(new Br(endBB));
        originBB.exit(new Br(cond, trueBB, falseBB));
        curBB = endBB;
    }

    @Override
    public void visit(WhileStmt it) {
        BasicBlock condBB = curFunc.addBB();
        curBB.exit(new Br(condBB));
        curBB = condBB;
        it.cond.accept(this);
        getValue(it.cond);
        Register cond = resReg;
        BasicBlock loopBB = curFunc.addBB();
        BasicBlock endBB = curFunc.addBB();
        curBB.exit(new Br(cond, loopBB, endBB));
        breakStack.addLast(endBB);
        continueStack.addLast(condBB);
        curBB = loopBB;
        it.stmt.accept(this);
        if (!curBB.isend())
            curBB.exit(new Br(condBB));
        breakStack.removeLast();
        continueStack.removeLast();
        curBB = endBB;
    }

    @Override
    public void visit(ForStmt it) {
        if (it.init != null) {
            it.init.accept(this);
        }
        BasicBlock condBB = curFunc.addBB();
        BasicBlock loopBB = curFunc.addBB();
        BasicBlock stepBB = curFunc.addBB();
        BasicBlock endBB = curFunc.addBB();

        curBB.exit(new Br(condBB));
        curBB = condBB;
        if (it.cond != null) {
            it.cond.accept(this);
            getValue(it.cond);
            curBB.exit(new Br(resReg, loopBB, endBB));
        } else {
            curBB.exit(new Br(loopBB));
        }

        breakStack.addLast(endBB);
        continueStack.addLast(stepBB);
        curBB = loopBB;
        it.stmt.accept(this);
        if (!curBB.isend())
            curBB.exit(new Br(stepBB));
        breakStack.removeLast();
        continueStack.removeLast();
        
        curBB = stepBB;
        if (it.next != null)
            it.next.accept(this);
        curBB.exit(new Br(condBB));
        curBB = endBB;
    }

    @Override
    public void visit(ContinueStmt it) {
        BasicBlock purpose = continueStack.getLast();
        curBB.exit(new Br(purpose));
    }

    @Override
    public void visit(BreakStmt it) {
        BasicBlock purpose = breakStack.getLast();
        curBB.exit(new Br(purpose));
    }

    @Override
    public void visit(ReturnStmt it) {
        if (it.expr == null) {
            curBB.exit(new Ret(null));
            return;
        }
        it.expr.accept(this);
        getValue(it.expr);
        curBB.add(new Store(resReg, curFunc.retVar));
        curBB.exit(new Br(curFunc.retBB));
    }

    @Override
    public void visit(VarDeclStmt it) {
        it.decl.accept(this);
    }

    @Override
    public void visit(ParameterDecl it) {
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(VarInitDecl it) {
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    // if expr is Lvalue, get its value and store in resReg
    private void getValue(Expr it) {
        if (it.isLvalue) {
            Type ty = transType(it.t);
            Var res = curFunc.newUnname(ty);
            curBB.add(new Load(res, ty, (Var)resReg));
            resReg = res;
        }
    }

    @Override
    public void visit(Postfix it) {
        it.expr.accept(this);
        Var varPtr = (Var) resReg;
        Type intT = intType;
        Var res = curFunc.newUnname(intT);
        curBB.add(new Load(res, intT, varPtr));
        Var newVal = curFunc.newUnname(intT);
        switch (it.op) {
        case "++":
            curBB.add(new BinaryInst(newVal, "add", res, new IntConst(1)));
            break;
        case "--":
            curBB.add(new BinaryInst(newVal, "sub", res, new IntConst(1)));
        }
        curBB.add(new Store(newVal, varPtr));
        resReg = res;
    }

    @Override
    public void visit(FunctionCall it) {
        it.expr.accept(this);
        String funcName = resFuncName;
        FuncType fType = (FuncType) it.expr.t;
        Type rType = transType(fType.retType);
        Var res = null;
        if (!(rType instanceof VoidType))
            res = curFunc.newUnname(rType);
        Call call = new Call(res, rType, funcName);
        // class method
        if (methodFlag) { 
            call.args.add(resReg); // thisPtr 
            methodFlag = false;
        }
        for (var i : it.para) {
            i.accept(this);
            getValue(i);
            call.args.add(resReg);
        }
        curBB.add(call);
        resReg = res;
    }

    @Override
    public void visit(Subscript it) {
        it.array.accept(this);
        Var arr = curFunc.newUnname(ptrType);
        curBB.add(new Load(arr, ptrType, (Var) resReg));
        it.index.accept(this);
        getValue(it.index);
        Register index = resReg;
        Type ty = transType(it.t);
        Var res = curFunc.newUnname(ptrType);
        GEP ins = new GEP(res, ty, arr);
        ins.index.add(index);
        curBB.add(ins);
        resReg = res;
    }

    @Override
    public void visit(MemberAccess it) {
        it.obj.accept(this);
        getValue(it.obj);
        // array
        if (it.obj.t.arrayLayer != 0) {
            resFuncName = "_array.size";
            methodFlag = true;
            return;
        }
        // string
        if (it.obj.t.baseType == BaseType.STRING) {
            resFuncName = "string." + it.memName.name;
            methodFlag = true;
            return;
        }
        // class
        String cName = ((ClassType) it.obj.t).name;
        if (it.t.baseType == BaseType.FUNC) { // method
            resFuncName = cName + "." + it.memName.name;
            methodFlag = true;
            return;
        }
        // member variable
        int pos = name2pos.get(cName + "." + it.memName.name);
        Var res = curFunc.newUnname(ptrType);
        GEP ins = new GEP(res, new StructType(cName), (Var)resReg);
        ins.index.add(new IntConst(0));
        ins.index.add(new IntConst(pos));
        curBB.add(ins);
        resReg = res;
    }

    @Override
    public void visit(UnaryExpr it) {
        it.expr.accept(this);
        switch (it.op) {
        case "!":
        {
            Var res = curFunc.newUnname(boolType);
            getValue(it.expr);
            curBB.add(new BinaryInst(res, "xor", resReg, new BoolConst(true)));
            resReg = res;
            return;
        }
        case "++":
        case "--":
        {
            Var res = curFunc.newUnname(intType);
            Var varPtr = (Var) resReg;
            getValue(it.expr);
            String op = it.op.equals("++") ? "add" : "sub";
            curBB.add(new BinaryInst(res, op, resReg, new IntConst(1)));
            curBB.add(new Store(res, varPtr));
            resReg = varPtr;
            return;
        }
        case "+":
        {
            getValue(it.expr);
            return;
        }
        case "-":
        {
            Var res = curFunc.newUnname(intType);
            getValue(it.expr);
            curBB.add(new BinaryInst(res, "sub", new IntConst(0), resReg));
            resReg = res;
            return;
        }
        case "~":
        {
            Var res = curFunc.newUnname(intType);
            getValue(it.expr);
            curBB.add(new BinaryInst(res, "xor", resReg, new IntConst(-1)));
            resReg = res;
            return;
        }
        default:
            throw new Error("Unknown unary expression");        
        }
    }

    @Override
    public void visit(BinaryExpr it) {
        if (it.l.t.baseType == BaseType.BOOL) {
            it.l.accept(this);
            getValue(it.l);
            Register lReg = resReg;
            BasicBlock originBB = curBB;
            BasicBlock continueBB = curFunc.addBB();
            BasicBlock endBB = curFunc.addBB();
            if (it.op.equals("&&")) 
                curBB.exit(new Br(lReg, continueBB, endBB));
            else 
                curBB.exit(new Br(lReg, endBB, continueBB));
            curBB = continueBB;
            it.r.accept(this);
            getValue(it.r);
            Register rReg = resReg;
            curBB.exit(new Br(endBB));
            continueBB = curBB;
            curBB = endBB;
            Var res = curFunc.newUnname(boolType);
            Phi phiIns = new Phi(res, boolType);
            if (it.op.equals("&&")) 
                phiIns.add(new BoolConst(false), originBB);
            else 
                phiIns.add(new BoolConst(true), originBB);
            phiIns.add(rReg, continueBB);
            curBB.add(phiIns);
            resReg = res;
            return;
        }
        if (it.t.baseType == BaseType.STRING) {
            it.l.accept(this);
            getValue(it.l);
            Register lStr = resReg;
            it.r.accept(this);
            getValue(it.r);
            Register rStr = resReg;
            Var res = curFunc.newUnname(ptrType);
            Call func = null;
            switch(it.op) {
            case "+":
                func = new Call(res, ptrType, "string.add");
                break;
            case "<=":
                func = new Call(res, ptrType, "string.lte");
                break;
            case ">=":
                func = new Call(res, ptrType, "string.gte");
                break;
            case "<":
                func = new Call(res, ptrType, "string.lt");
                break;
            case ">":
                func = new Call(res, ptrType, "string.gt");
                break;
            case "==":
                func = new Call(res, ptrType, "string.eq");
                break;
            case "!=":
                func = new Call(res, ptrType, "string.neq");
                break;
            }
            func.args.add(lStr);
            func.args.add(rStr);
            curBB.add(func);
            resReg = res;
            return;
        }
        // INT or PTR
        it.l.accept(this);
        getValue(it.l);
        Register l = resReg;
        it.r.accept(this);
        getValue(it.r);
        Register r = resReg;
        String binOp = null;
        String icmpOp = null;
        switch (it.op) {
        case "*":
            binOp = "mul";
            break;
        case "/":
            binOp = "sdiv";
            break;
        case "%":
            binOp = "srem";
            break;
        case "+":
            binOp = "add";
            break;
        case "-":
            binOp = "sub";
            break;
        case "<<":
            binOp = "shl";
            break;
        case ">>":
            binOp = "ashr";
            break;
        case "&":
            binOp = "and";
            break;
        case "|":
            binOp = "or";
            break;
        case "^":
            binOp = "xor";
            break;
        case "<=":
            icmpOp = "sle";
            break;
        case ">=":
            icmpOp = "sge";
            break;
        case "<":
            icmpOp = "slt";
            break;
        case ">":
            icmpOp = "sgt";
            break;
        case "==":
            icmpOp = "eq";
            break;
        case "!=":
            icmpOp = "ne";
            break;
        }
        if (binOp != null) {
            Var res = curFunc.newUnname(intType);
            curBB.add(new BinaryInst(res, binOp, l, r));
            resReg = res;
        } else {
            Var res = curFunc.newUnname(boolType);
            curBB.add(new Icmp(res, icmpOp, l, r));
            resReg = res;
        }
    }

    @Override
    public void visit(Ternary it) {
        it.cond.accept(this);
        getValue(it.cond);
        BasicBlock trueBB = curFunc.addBB();
        BasicBlock falseBB = curFunc.addBB();
        curBB.exit(new Br(resReg, trueBB, falseBB));
        curBB = trueBB;
        it.trueExpr.accept(this);
        Register trueReg = resReg;
        trueBB = curBB;
        curBB = falseBB;
        it.falseExpr.accept(this);
        Register falseReg = resReg;
        falseBB = curBB;
        BasicBlock endBB = curFunc.addBB();
        trueBB.exit(new Br(endBB));
        falseBB.exit(new Br(endBB));
        curBB = endBB;
        if (it.t.baseType != BaseType.VOID) {
            Type ty = transType(it.t);
            Var res = curFunc.newUnname(ty);
            Phi ins = new Phi(res, ty);
            ins.add(trueReg, trueBB);
            ins.add(falseReg, falseBB);
            curBB.add(ins);
            resReg = res;
        } else {
            resReg = null;
        }
    }

    @Override
    public void visit(Assignment it) {
        it.r.accept(this);
        getValue(it.r);
        Register r = resReg;
        it.l.accept(this);
        Var l = (Var) resReg;
        curBB.add(new Store(r, l));
        resReg = l;
    }

    @Override
    public void visit(Identifier it) {
        // local variable
        if (name2local.containsKey(it.name)) {
            resReg = name2local.get(it.name);
            return;
        }
        if (curClass != null && classScope.table.containsKey(it.name)) {
            var astType = classScope.table.get(it.name);
            // method
            if (astType.baseType == BaseType.FUNC) {
                resFuncName = curClass + "." + it.name;
                resReg = thisPtr;
                methodFlag = true;
                return;
            }
            // member variable
            int pos = name2pos.get(curClass + "." + it.name);
            Var res = curFunc.newUnname(ptrType);
            GEP ins = new GEP(res, new StructType(curClass), thisPtr);
            ins.index.add(new IntConst(0));
            ins.index.add(new IntConst(pos));
            curBB.add(ins);
            resReg = res;
            return;
        } 
        // global function
        if (it.t.baseType == BaseType.FUNC) {
            resFuncName = it.name;
            return;
        }
        // global variable
        resReg = name2global.get(it.name);
        return;
    }

    @Override
    public void visit(ThisNode it) {
        resReg = thisPtr;
    }

    @Override
    public void visit(StringConst it) {
        if (string2var.containsKey(it.value)) {
            resReg = string2var.get(it.value);
            return;
        }
        Var res = new Var(ptrType, "@.str." + stringCnt);
        stringCnt++;
        mod.strings.add(new StringGlobal(res, it.value));
        string2var.put(it.value, res);
        resReg = res;
    }

    @Override
    public void visit(TypeNode it) {
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(AST.BoolConst it) {
        resReg = new BoolConst(it.value);
    }

    @Override
    public void visit(AST.IntConst it) {
        resReg = new IntConst(it.value);
    }

    @Override
    public void visit(AST.NullConst it) {
        resReg = new NullConst();
    }

    @Override
    public void visit(NewExpr it) {
        if (it.t.arrayLayer == 0) {
            String cName = ((ClassType)it.t).name;
            int size = name2struct.get(cName).size();
            Var res = curFunc.newUnname(ptrType);
            Call malloc = new Call(res, ptrType, "new.malloc");
            malloc.args.add(new IntConst(size));
            curBB.add(malloc);
            Call initCall = new Call(null, voidType, cName + "." + cName);
            initCall.args.add(res);
            curBB.add(initCall);
            resReg = res;
            return;
        }
        ArrayList<Register> inits = new ArrayList<Register>();
        for (var i : it.init) {
            i.accept(this);
            getValue(i);
            inits.add(resReg);
        }
        transNewToFor(it.t.baseType, it.t.arrayLayer, inits);
    }

    private void transNewToFor(Util.Type.BaseType baseType, int arrayLayer, List<Register> inits) {
        if (inits.size() == 1) {
            Register size = inits.get(0);
            Var res = curFunc.newUnname(ptrType);
            Call func;
            if (arrayLayer == 1) {
                switch (baseType) {
                case INT:
                    func = new Call(res, ptrType, "new.intArray");
                    break;
                case BOOL:
                    func = new Call(res, ptrType, "new.boolArray");
                    break;
                default:
                    func = new Call(res, ptrType, "new.ptrArray");
                }
            } else {
                func = new Call(res, ptrType, "new.ptrArray");
            }
            func.args.add(size);
            curBB.add(func);
            resReg = res;
            return;
        }
        Register size0 = inits.get(0);
        Var res = curFunc.newUnname(ptrType);
        Call newFunc = new Call(res, ptrType, "new.ptrArray");
        newFunc.args.add(size0);
        curBB.add(newFunc);

        Var iPtr = curFunc.newUnname(ptrType);
        curBB.add(new Alloca(iPtr, intType));
        curBB.add(new Store(new IntConst(0), iPtr));
        BasicBlock condBB = curFunc.addBB();
        BasicBlock loopBB = curFunc.addBB();
        BasicBlock stepBB = curFunc.addBB();
        BasicBlock endBB = curFunc.addBB();
        curBB.exit(new Br(condBB));
        Var iValue = curFunc.newUnname(intType);
        Var cmpRes = curFunc.newUnname(boolType);
        condBB.add(new Load(iValue, intType, iPtr));
        condBB.add(new Icmp(cmpRes, "slt", iValue, size0));
        condBB.exit(new Br(cmpRes, loopBB, endBB));
        Var nexti = curFunc.newUnname(intType);
        stepBB.add(new BinaryInst(nexti, "add", iValue, new IntConst(1)));
        stepBB.add(new Store(nexti, iPtr));
        stepBB.exit(new Br(condBB));

        curBB = loopBB;
        transNewToFor(baseType, arrayLayer-1, inits.subList(1, inits.size()));
        Var elemPtr = curFunc.newUnname(ptrType);
        GEP gep = new GEP(elemPtr, ptrType, res);
        gep.index.add(iValue);
        curBB.add(gep);
        curBB.add(new Store(resReg, elemPtr));
        curBB.exit(new Br(stepBB));

        curBB = endBB;
        resReg = res;
    }
}

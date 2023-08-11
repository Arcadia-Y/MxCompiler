package IR;

import java.util.HashMap;

import AST.*;
import IR.Node.*;
import IR.Node.BoolConst;
import IR.Node.IntConst;
import IR.Node.Module;
import IR.Node.NullConst;
import IR.Type.*;
import IR.Type.Type;
import Semantic.Scope;

public class IRBuilder implements ASTVisitor {
    Module mod;
    Register resReg;
    String resFuncName;

    String curClass;
    StructDecl curStruct;
    Scope classScope;
    FuncDef curFunc;
    BasicBlock curBB;
    
    FuncDef initFunc;
    HashMap<String, Scope> classInfo;
    HashMap<String, Var> name2local;
    HashMap<String, Var> name2global = new HashMap<String, Var>();
    HashMap<String, Integer> name2pos;// class member name to its position

    private Type transType(Util.Type ty) {
        if (ty.arrayLayer != 0)
            return new PtrType();
        switch(ty.baseType) {
        case INT:
            return new IntType(32);
        case BOOL:
            return new IntType(1);
        case VOID:
            return new VoidType();
        default:
            return new PtrType();
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
        name2pos = it.scope.name2pos;
        initFunc = new FuncDef(new VoidType(), "_init");
        mod.funcDefs.add(initFunc);
        // global variable
        curFunc = initFunc;
        curBB = initFunc.addBB();
        name2local = new HashMap<String, Var>();
        for (var item : it.decls)
            if (item instanceof VarDecl)
                visitGlobalVarDecl((VarDecl) item);
        curFunc = null;
        curBB = null;
        name2local = null;
        for (var item : it.decls)
            if (!(item instanceof VarDecl))
                item.accept(this);
    }

    private void visitGlobalVarDecl(VarDecl it) {
        for (var i : it.inits) {
            Var v = new Var(new PtrType(), "@" + i.id);
            Register reg;
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
            mod.globals.add(new GlobalDecl(v, reg));
            name2global.put(i.id, v);
            if (i.init != null) {
                i.init.accept(this);
                curBB.add(new Store(resReg, v));
            }
        }
    }

    @Override
    public void visit(VarDecl it) {
        Type ty = transType(it.t.t);
        // local variable
        if (curFunc != null) {
            for (var i : it.inits) {
                Var v = new Var(new PtrType(), "%" + i.id);
                name2local.put(i.id, v);
                curBB.add(new Alloca(v, ty));
                if (i.init != null) {
                    i.init.accept(this);
                    curBB.add(new Store(resReg, v));
                }
            }
            return;
        }
        // class member
        for (var i : it.inits) {
            curStruct.mem.add(ty);
        }
        return;
    }

    @Override
    public void visit(AST.FuncDecl it) {
        // global function
        if (curClass == null) {
            curFunc = new FuncDef(transType(it.retType.t), it.name);
            mod.funcDefs.add(curFunc);
            curBB = curFunc.addBB();
            name2local = new HashMap<String, Var>();
            for (var i : it.para) {
                Type ty = transType(i.t.t);
                Var arg = curFunc.newUnname(ty);
                curFunc.args.add(arg);
                Var v = new Var(new PtrType(), "%" + i.name);
                name2local.put(i.name, v);
                curBB.add(new Alloca(v, ty));
                curBB.add(new Store(arg, v));
            }
            for (var i : it.block.stmts)
                i.accept(this);
            curFunc = null;
            curBB = null;
            name2local = null;
            return;
        }
        // class method
        curFunc = new FuncDef(transType(it.retType.t), curClass + "." + it.name);
        mod.funcDefs.add(curFunc);
        curBB = curFunc.addBB();
        name2local = new HashMap<String, Var>();
        Var thisReg = new Var(new PtrType(), "%this");
        curFunc.args.add(thisReg);
        name2local.put("this", thisReg);
        for (var i : it.para) {
            Type ty = transType(i.t.t);
            Var arg = curFunc.newUnname(ty);
            curFunc.args.add(arg);
            Var v = new Var(new PtrType(), "%" + i.name);
            name2local.put(i.name, v);
            curBB.add(new Alloca(v, ty));
            curBB.add(new Store(arg, v));
        }
        for (var i : it.block.stmts)
            i.accept(this);
        curFunc = null;
        curBB = null;
        name2local = null;
    }

    @Override
    public void visit(ClassDecl it) {
        curClass = it.name;
        classScope = classInfo.get(curClass);
        curStruct = new StructDecl("%class." + curClass);
        for (var item : it.mem)
            item.accept(this);
        curClass = null;
        classScope = null;
        curStruct = null;
    }

    @Override
    public void visit(ConstructDecl it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(BlockStmt it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(ExprStmt it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(IfStmt it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(WhileStmt it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(ForStmt it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(ContinueStmt it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(BreakStmt it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(ReturnStmt it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(VarDeclStmt it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(ParameterDecl it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(VarInitDecl it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(Postfix it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(FunctionCall it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(Subscript it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(MemberAccess it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(UnaryExpr it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(NewExpr it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(BinaryExpr it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(Ternary it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(Assignment it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(Identifier it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(ThisNode it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(StringConst it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(TypeNode it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(AST.BoolConst it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(AST.IntConst it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(AST.NullConst it) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }
    
}

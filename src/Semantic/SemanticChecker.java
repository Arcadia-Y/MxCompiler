package Semantic;

import java.util.ArrayList;
import java.util.HashMap;

import AST.*;
import Util.ClassType;
import Util.FuncType;
import Util.Position;
import Util.Type;
import Util.Error.SemanticError;
import Util.Type.BaseType;

public class SemanticChecker implements ASTVisitor{
    GlobalScope global;
    Scope current, parent;
    FuncType currentFunc;
    String currentClass;
    Boolean hasReturn;
    int loopCount = 0;
    HashMap<String, FuncType> bultinMethod;
    HashMap<String, Integer> varCount; // for variable renaming in each function

    public SemanticChecker() {
        bultinMethod = new HashMap<String, FuncType>();
        bultinMethod.put("size", new FuncType(new Type(BaseType.INT), new ArrayList<Type>()));
        bultinMethod.put("length", new FuncType(new Type(BaseType.INT), new ArrayList<Type>()));
        FuncType fType = new FuncType(new Type(BaseType.STRING), new ArrayList<Type>());
        fType.argTypes.add(new Type(BaseType.INT));
        fType.argTypes.add(new Type(BaseType.INT));
        bultinMethod.put("substring", fType);
        bultinMethod.put("parseInt", new FuncType(new Type(BaseType.INT), new ArrayList<Type>()));
        fType = new FuncType(new Type(BaseType.INT), new ArrayList<Type>());
        fType.argTypes.add(new Type(BaseType.INT));
        bultinMethod.put("ord", fType);
    }

    private void checkTypeDef(Type t, Position pos) {
        if (t.baseType == BaseType.CLASS) {
            String obj = ((ClassType)t).name;
            if (!global.classInfo.containsKey(obj))
                throw new SemanticError("type not defined", pos);
        }
    }
    private void checkVarRedef(String id, Position pos) {
        if (current.table.containsKey(id))
            throw new SemanticError("variable redefined", pos);
    }
    private void push(Declaration node) {
        parent = current;
        current = new Scope(parent, node);
    }
    private void push(Statement node) {
        parent = current;
        current = new Scope(parent, node);
    }
    private void pop() {
        current = parent;
        parent = current.parent;
    }
    private String rename(String n) {
        var cnt = varCount.get(n);
        if (cnt == null) {
            current.rename.put(n, 0);
            varCount.put(n, 1);
            return n + ".0";
        } else {
            current.rename.put(n, cnt);
            varCount.put(n, cnt+1);
            return n + "." + cnt;
        }
    }

    @Override
    public void visit(Program it) {
        global = it.scope;
        current = it.scope;
        for (var del : it.decls)
            del.accept(this);
    }
    @Override
    public void visit(VarDecl it) {
        if (it.t.t.baseType == BaseType.VOID)
            throw new SemanticError("variable type cannot be void", it.t.pos);
        checkTypeDef(it.t.t, it.pos);
        // class member variable has already be defined
        if (current.node instanceof ClassDecl)
            return;
        for (var item : it.inits) {
            checkVarRedef(item.id, item.pos);
            if (item.init != null) {
                item.init.accept(this);
                if (!it.t.t.isSame(item.init.t))
                    throw new SemanticError("expression type doesn't match variable type", item.pos);
            }
            current.table.put(item.id, it.t.t);
            if (currentFunc != null)
                item.id = rename(item.id);
        }
    }
    @Override
    public void visit(FuncDecl it) {
        push(it);
        currentFunc = it.getFuncType();
        varCount = new HashMap<String, Integer>();
        hasReturn = false;
        checkTypeDef(currentFunc.retType, it.retType.pos);
        for (var arg : it.para) {
            checkVarRedef(arg.name, arg.pos);
            current.table.put(arg.name, arg.t.t);
            arg.name = rename(arg.name);
        }
        for (var stmt : it.block.stmts)
            stmt.accept(this);
        if (!hasReturn && (currentFunc.retType.baseType != BaseType.VOID)&& !it.name.equals("main"))
            throw new SemanticError("non-void function should return a value", it.pos);
        currentFunc = null;
        varCount = null;
        pop();
    }
    @Override
    public void visit(ClassDecl it) {
        parent = current;
        current = global.classInfo.get(it.name);
        currentClass = it.name;
        for (var item : it.mem)
            item.accept(this);
        currentClass = null;
        pop();
    }
    @Override
    public void visit(ConstructDecl it) {
        push(it);
        currentFunc = new FuncType(new Type(BaseType.VOID), new ArrayList<Type>());
        varCount = new HashMap<String, Integer>();
        for (var stmt : it.block.stmts)
            stmt.accept(this);
        currentFunc = null;
        varCount = null;
        pop();
    }
    @Override
    public void visit(BlockStmt it) {
        push(it);
        for (var stmt : it.stmts)
            stmt.accept(this);
        pop();
    }
    @Override
    public void visit(ExprStmt it) {
        if (it.expr != null)
            it.expr.accept(this);
    }
    @Override
    public void visit(IfStmt it) {
        it.cond.accept(this);
        if (it.cond.t.baseType != BaseType.BOOL || it.cond.t.arrayLayer != 0)
            throw new SemanticError("condition of if statement should be bool expression", it.cond.pos);
        push(it.trueStmt);
        it.trueStmt.accept(this);
        pop();
        if (it.falseStmt != null) {
            push(it.falseStmt);
            it.falseStmt.accept(this);
            pop();
        }
    }
    @Override
    public void visit(WhileStmt it) {
        push(it);
        loopCount++;
        it.cond.accept(this);
        if (it.cond.t.baseType != BaseType.BOOL || it.cond.t.arrayLayer != 0)
            throw new SemanticError("condition of while statement should be bool expression", it.cond.pos);
        it.stmt.accept(this);
        loopCount--;
        pop();
    }
    @Override
    public void visit(ForStmt it) {
        push(it);
        loopCount++;
        if (it.varDecl != null) it.varDecl.accept(this);
        if (it.init != null) it.init.accept(this);
        if (it.cond != null) {
            it.cond.accept(this);
            if (it.cond.t.baseType != BaseType.BOOL || it.cond.t.arrayLayer != 0)
                throw new SemanticError("condition of for statement should not be non-bool expression", it.cond.pos);
        }
        if (it.next != null) it.next.accept(this);
        it.stmt.accept(this);
        loopCount--;
        pop();
    }
    @Override
    public void visit(ContinueStmt it) {
        if (loopCount == 0)
            throw new SemanticError("continue should be in a loop statement", it.pos);
    }
    @Override
    public void visit(BreakStmt it) {
        if (loopCount == 0)
            throw new SemanticError("break should be in a loop statement", it.pos);
    }
    @Override
    public void visit(ReturnStmt it) {
        if (currentFunc == null)
            throw new SemanticError("return statement should be in a function", it.pos);
        if (it.expr == null) {
            if (currentFunc.retType.baseType != BaseType.VOID)
                throw new SemanticError("non-void function should return a value", it.pos);
        } else {
            it.expr.accept(this);
            if (!currentFunc.retType.isSame(it.expr.t))
                throw new SemanticError("return type doesn't match", it.expr.pos);
        }
        hasReturn = true;
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
    @Override
    public void visit(Postfix it) {
        it.expr.accept(this);
        if (it.expr.t.baseType != BaseType.INT || it.expr.t.arrayLayer != 0)
            throw new SemanticError("postfix expression should be int", it.expr.pos);
        if (!it.expr.isLvalue)
            throw new SemanticError("postfix expression is not assignable", it.expr.pos);
        it.t.baseType = BaseType.INT;
    }
    @Override
    public void visit(FunctionCall it) {
        it.expr.accept(this);
        if (it.expr.t.baseType != BaseType.FUNC)
            throw new SemanticError("non-function in function call", it.pos);
        FuncType fType = (FuncType) it.expr.t;
        if (fType.argTypes.size() != it.para.size())
            throw new SemanticError("function arguments size not match", it.pos);
        for (int i = 0; i < it.para.size(); i++) {
            var arg = it.para.get(i);
            arg.accept(this);
            if (!fType.argTypes.get(i).isSame(arg.t))
                throw new SemanticError("function argument type not match", arg.pos);
        }
        it.t = fType.retType;
    }
    @Override
    public void visit(Subscript it) {
        it.array.accept(this);
        if (it.array.t.arrayLayer == 0)
            throw new SemanticError("subscript needs an array type", it.array.pos);
        it.index.accept(this);
        if (it.index.t.arrayLayer != 0 || it.index.t.baseType != BaseType.INT)
            throw new SemanticError("subscript index should be int", it.index.pos);
        it.t = it.array.t.copy();
        it.t.arrayLayer--;
        it.isLvalue = true;
    }
    @Override
    public void visit(MemberAccess it) {
        it.obj.accept(this);
        if (it.obj.t.baseType == BaseType.VOID)
            throw new SemanticError("void can not be accessed", it.pos);
        // array
        if (it.obj.t.arrayLayer != 0) {
            if (!it.memName.name.equals("size"))
                throw new SemanticError("array doesn't have the member", it.memName.pos);
            it.t = bultinMethod.get("size");
            return;
        }
        // string
        if (it.obj.t.baseType == BaseType.STRING) {
            if (it.obj instanceof StringConst)
                throw new SemanticError("const string has no bultin method", it.obj.pos);
            if (it.memName.name.equals("size") || !bultinMethod.containsKey(it.memName.name))
                throw new SemanticError("string doesn't have the member", it.memName.pos);
            it.t = bultinMethod.get(it.memName.name);
            return;
        }
        // non-class
        if (!(it.obj.t instanceof ClassType))
            throw new SemanticError("variable is not a class", it.obj.pos);
        // class
        ClassType cType = (ClassType) it.obj.t;
        Scope cScope = global.classInfo.get(cType.name);
        Type memType = cScope.table.get(it.memName.name);
        if (memType == null)
            throw new SemanticError("class has no such member", it.memName.pos);
        it.t = memType;
        it.isLvalue = true;
    }
    @Override
    public void visit(UnaryExpr it) {
        it.expr.accept(this);
        if (it.op.equals("!")) {
            if (it.expr.t.arrayLayer != 0 || it.expr.t.baseType != BaseType.BOOL)
                throw new SemanticError("expression after \"!\" should be bool", it.expr.pos);
            it.t.baseType = BaseType.BOOL;
            return;
        }
        if (it.expr.t.arrayLayer != 0 || it.expr.t.baseType != BaseType.INT)
            throw new SemanticError( "expression after \"" + it.op + "\" should be int", it.expr.pos);
        if (it.op.equals("++") || it.op.equals("--")) {
            if (!it.expr.isLvalue)
                throw new SemanticError("expression should be an lvalue", it.expr.pos);
            it.t.baseType = BaseType.INT;
            it.isLvalue = true;
            return;
        }
        it.t.baseType = BaseType.INT;
    }
    @Override
    public void visit(NewExpr it) {
        if (it.t.baseType == BaseType.VOID)
            throw new SemanticError("new expression should not be void", it.pos);
        if (it.t.baseType == BaseType.CLASS)
            if (!global.classInfo.containsKey(((ClassType) it.t).name))
                throw new SemanticError("type not defined", it.pos);
        for (var item : it.init) {
            item.accept(this);
            if (item.t.arrayLayer != 0 || item.t.baseType != BaseType.INT)
                throw new SemanticError("new initial expression should be int", item.pos);
        }
    }
    @Override
    public void visit(BinaryExpr it) {
        it.l.accept(this);
        it.r.accept(this);
        if (it.l.t.baseType == BaseType.FUNC)
            throw new SemanticError("function should not be in binary expression", it.pos);
        if (!it.l.t.isSame(it.r.t))
            throw new SemanticError("binary expression should have the same type", it.pos);
        switch (it.op) {
        case "==":
        case "!=":
            if (it.t.baseType == BaseType.VOID)
                throw new SemanticError("binary expression should not be void", it.pos);
            it.t.baseType = BaseType.BOOL;
            break;
        case "+":
            if (it.l.t.arrayLayer != 0 || (it.l.t.baseType != BaseType.INT && it.l.t.baseType != BaseType.STRING))
                throw new SemanticError("expression type for \"" +  it.op + "\" should be int or string", it.pos);
            it.t.baseType = it.l.t.baseType;
            break;
        case ">=":
        case "<=":
        case ">":
        case "<":
            if (it.l.t.arrayLayer != 0 || (it.l.t.baseType != BaseType.INT && it.l.t.baseType != BaseType.STRING))
                throw new SemanticError("expression type for \"" +  it.op + "\" should be int or string", it.pos);
            it.t.baseType = BaseType.BOOL;
            break;
        case "&&":
        case "||":
            if (it.l.t.arrayLayer != 0 || it.l.t.baseType != BaseType.BOOL)
                throw new SemanticError("expression type for \"" +  it.op + "\" should be bool", it.pos);
            it.t.baseType = BaseType.BOOL;
            break;
        default:
            if (it.l.t.arrayLayer != 0 || it.l.t.baseType != BaseType.INT)
                throw new SemanticError("expression type for \"" +  it.op + "\" should be int", it.pos);
            it.t.baseType = BaseType.INT;
            break;
        }
    }
    @Override
    public void visit(Ternary it) {
        it.cond.accept(this);
        if (it.cond.t.arrayLayer != 0 || it.cond.t.baseType != BaseType.BOOL)
            throw new SemanticError("ternary expression condition should be bool", it.cond.pos);
        it.trueExpr.accept(this);
        it.falseExpr.accept(this);
        if (!it.trueExpr.t.isSame(it.falseExpr.t))
            throw new SemanticError("ternary expression should have same type", it.trueExpr.pos);
        it.t = it.trueExpr.t.baseType == BaseType.NULL ? it.falseExpr.t : it.trueExpr.t;
    }
    @Override
    public void visit(Assignment it) {
        it.l.accept(this);
        if (it.l instanceof ThisNode)
            throw new SemanticError("this cannot be assigned", it.l.pos);
        if (!it.l.isLvalue)
            throw new SemanticError("the expression is not assignable", it.l.pos);
        it.r.accept(this);
        if (!it.l.t.isSame(it.r.t))
            throw new SemanticError("assignment type not match", it.r.pos);
        it.t = it.l.t;
        it.isLvalue = true;
    }
    @Override
    public void visit(Identifier it) {
        String n = it.name;
        Scope s = current;
        while (s != null) {
            Type t = s.table.get(n);
            if (t != null) {
                it.t = t;
                it.isLvalue = t.baseType == BaseType.FUNC ? false : true;
                if (s.node instanceof ClassDecl) {
                    if (t.baseType == BaseType.FUNC)
                        it.name = currentClass + "." + it.name;
                    return;
                }
                var cnt = s.rename.get(n);
                if (cnt != null)
                    it.name = n + "." + cnt;
                return;
            }
            s = s.parent;
        }
        throw new SemanticError("identifier not defined", it.pos);
    }
    @Override
    public void visit(ThisNode it) {
        if (currentClass == null)
            throw new SemanticError("this cannot be used outside class", it.pos);
        it.t = new ClassType(currentClass);
        it.isLvalue = false;
    }
    @Override
    public void visit(BoolConst it) {
        return;
    }
    @Override
    public void visit(IntConst it) {
        return;
    }
    @Override
    public void visit(StringConst it) {
        return;
    }
    @Override
    public void visit(NullConst it) {
        return;
    }
    @Override
    public void visit(TypeNode it) {
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }
}

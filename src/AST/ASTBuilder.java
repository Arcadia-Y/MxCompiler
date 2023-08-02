package AST;

import Parser.*;
import Util.Position;
import Util.Type;
import Util.Type.BaseType;

public class ASTBuilder extends MxBaseVisitor<ASTNode> {
    public ASTNode visitProgram(MxParser.ProgramContext ctx) {
        Program prog = new Program(new Position(ctx));
        for (var son : ctx.declaration()) 
            prog.decls.add((Declaration) visit(son));
        return prog;
    }

    // statement
    public ASTNode visitBlockStmt(MxParser.BlockStmtContext ctx) {
        BlockStmt ret = new BlockStmt(new Position(ctx));
        for (var son : ctx.statement())
            ret.stmts.add((Statement) visit(son));
        return ret;
    }
    public ASTNode visitExprStmt(MxParser.ExprStmtContext ctx) {
        ExprStmt ret = new ExprStmt(new Position(ctx));
        ret.expr = (Expr) visit(ctx.expr());
        return ret;
    }
    public ASTNode visitIfStmt(MxParser.IfStmtContext ctx) {
        IfStmt ret = new IfStmt(new Position(ctx));
        ret.cond = (Expr) visit(ctx.cond);
        ret.trueStmt = (Statement) visit(ctx.trueStmt);
        if (ctx.falseStmt != null)
            ret.falseStmt = (Statement) visit(ctx.falseStmt);
        return ret;
    }
    public ASTNode visitWhile(MxParser.WhileContext ctx) {
        WhileStmt ret = new WhileStmt(new Position(ctx));
        ret.cond = (Expr) visit(ctx.expr());
        ret.stmt = (Statement) visit(ctx.statement());
        return ret;
    }
    public ASTNode visitFor(MxParser.ForContext ctx) {
        ForStmt ret = new ForStmt(new Position(ctx));
        if (ctx.init != null)
            ret.init = (Expr) visit(ctx.init.expr());
        else
            ret.varDecl = (VarDecl) visit(ctx.declInit);
        ret.cond = (Expr) visit(ctx.cond);
        ret.next = (Expr) visit(ctx.next);
        ret.stmt = (Statement) visit(ctx.statement());
        return ret;
    }
    public ASTNode visitContinue(MxParser.ContinueContext ctx) {
        return new ContinueStmt(new Position(ctx));
    }
    public ASTNode visitBreak(MxParser.BreakContext ctx) {
        return new BreakStmt(new Position(ctx));
    }
    public ASTNode visitReturn(MxParser.ReturnContext ctx) {
        var ret = new ReturnStmt(new Position(ctx));
        if (ctx.expr() != null)
            ret.expr = (Expr) visit(ctx.expr());
        return ret;
    }
    public ASTNode visitVarDeclStmt(MxParser.VarDeclStmtContext ctx) {
        var ret = new VarDeclStmt(new Position(ctx));
        ret.decl = (VarDecl) visit(ctx.varDecl());
        return ret;
    }

    // declaration
    public ASTNode visitVarDecl(MxParser.VarDeclContext ctx) {
        VarDecl ret = new VarDecl(new Position(ctx));
        ret.t = (TypeNode) visit(ctx.type());
        for (var son: ctx.varInitDecl())
            ret.inits.add((VarInitDecl) visit(son));
        return ret;
    }
    public ASTNode visitFuncDecl(MxParser.FuncDeclContext ctx) {
        FuncDecl ret = new FuncDecl(new Position(ctx));
        ret.retType = (TypeNode) visit(ctx.type());
        ret.name = ctx.Identifier().getText();
        for (var son: ctx.parameterDecl()) {
            ret.para.add((ParameterDecl) visit(son));
        }
        ret.block = (BlockStmt) visit(ctx.blockStmt());
        return ret;
    }
    public ASTNode visitClassDecl(MxParser.ClassDeclContext ctx) {
        ClassDecl ret = new ClassDecl(new Position(ctx));
        ret.name = ctx.Identifier().getText();
        for (var son : ctx.memberDecl())
            ret.mem.add((Declaration) visit(son));
        return ret;
    }
    public ASTNode visitVarInitDecl(MxParser.VarInitDeclContext ctx) {
        var ret = new VarInitDecl(new Position(ctx));
        ret.id = ctx.Identifier().getText();
        if (ctx.expr() != null) {
            ret.init = (Expr) visit(ctx.expr());
        }
        return ret;
    }
    public ASTNode visitType(MxParser.TypeContext ctx) {
        var ret = new TypeNode(new Position(ctx));
        ret.t = new Type();
        if (ctx.nonArray().BultinType() != null) {  
            switch (ctx.nonArray().getText()) {
            case "void":
                ret.t.baseType = BaseType.VOID;
                break;
            case "bool":
                ret.t.baseType = BaseType.BOOL;
                break;
            case "int":
                ret.t.baseType = BaseType.INT;
                break;
            case "string":
                ret.t.baseType = BaseType.STRING;
            }
        } else {
            ret.t.baseType = BaseType.CLASS;
            ret.t.className = ctx.nonArray().getText();
        }
        ret.t.arrayLayer = ctx.LeftBracket().size();
        return ret;
    }
    public ASTNode visitParameterDecl(MxParser.ParameterDeclContext ctx) {
        var ret = new ParameterDecl(new Position(ctx));
        ret.t = (TypeNode) visit(ctx.type());
        ret.name = ctx.Identifier().getText();
        return ret;
    }
    public ASTNode visitConstructDecl(MxParser.ConstructDeclContext ctx) {
        var ret = new ConstructDecl(new Position(ctx));
        ret.name = ctx.Identifier().getText();
        ret.block = (BlockStmt) visit(ctx.blockStmt());
        return ret;
    }

    // expression
    public ASTNode visitSubExpr(MxParser.SubExprContext ctx) {
        return visit(ctx.expr());
    }
    public ASTNode visitPostfix(MxParser.PostfixContext ctx) {
        var ret = new Postfix(new Position(ctx));
        ret.expr = (Expr) visit(ctx.expr());
        ret.op = ctx.op.getText();
        return ret;
    }
    public ASTNode visitFunctionCall(MxParser.FunctionCallContext ctx) {
        var ret = new FunctionCall(new Position(ctx));
        ret.expr = (Expr) visit(ctx.expr(0));
        for (int i = 1; i < ctx.expr().size(); i++) {
            ret.para.add((Expr) visit(ctx.expr(i)));
        }
        return ret;
    }
    public ASTNode visitSubscript(MxParser.SubscriptContext ctx) {
        var ret = new Subscript(new Position(ctx));
        ret.array = (Expr) visit(ctx.expr(0));
        ret.index = (Expr) visit(ctx.index);
        return ret;
    }
    public ASTNode visitMemberAccess(MxParser.MemberAccessContext ctx) {
        var ret = new MemberAccess(new Position(ctx));
        ret.className = (Expr) visit(ctx.expr());
        ret.memName = new Variable(new Position(ctx.Identifier()), ctx.Identifier().getText());
        return ret;
    }
    public ASTNode visitUnaryExpr(MxParser.UnaryExprContext ctx) {
        var ret = new UnaryExpr(new Position(ctx));
        ret.expr = (Expr) visit(ctx.expr());
        ret.op = ctx.op.getText();
        return ret;
    }
    public ASTNode visitNewExpr(MxParser.NewExprContext ctx) {
        var ret = new NewExpr(new Position(ctx));
        ret.t = new Type();
        if (ctx.newItem().nonArray().BultinType() != null) {  
            switch (ctx.newItem().nonArray().getText()) {
            case "void":
                ret.t.baseType = BaseType.VOID;
                break;
            case "bool":
                ret.t.baseType = BaseType.BOOL;
                break;
            case "int":
                ret.t.baseType = BaseType.INT;
                break;
            case "string":
                ret.t.baseType = BaseType.STRING;
            }
        } else {
            ret.t.baseType = BaseType.CLASS;
            ret.t.className = ctx.newItem().nonArray().getText();
        }
        ret.t.arrayLayer = ctx.newItem().LeftBracket().size();
        for (var son: ctx.newItem().expr()) {
            ret.init.add((Expr) visit(son));
        }
        return ret;
    }
    public ASTNode visitBinaryExpr(MxParser.BinaryExprContext ctx) {
        var ret = new BinaryExpr(new Position(ctx));
        ret.l = (Expr) visit(ctx.l);
        ret.r = (Expr) visit(ctx.r);
        ret.op = ctx.op.getText();
        return ret;
    }
    public ASTNode visitTernary(MxParser.TernaryContext ctx) {
        var ret = new Ternary(new Position(ctx));
        ret.cond = (Expr) visit(ctx.expr(0));
        ret.trueExpr = (Expr) visit(ctx.expr(1));
        ret.falseExpr = (Expr) visit(ctx.expr(2));
        return ret;
    }
    public ASTNode visitAssignment(MxParser.AssignmentContext ctx) {
        var ret = new Assignment(new Position(ctx));
        ret.l = (Expr) visit(ctx.expr(0));
        ret.r = (Expr) visit(ctx.expr(1));
        return ret;
    }
    public ASTNode visitVariable(MxParser.VariableContext ctx) {
        return new Variable(new Position(ctx), ctx.getText());
    }
    public ASTNode visitConstant(MxParser.ConstantContext ctx) {
        String text = ctx.literal().getText();
        if (text.equals("true")) {
            return new BoolConst(new Position(ctx), true);
        } else if (text.equals("false")) {
            return new BoolConst(new Position(ctx), false);
        } else if (text.equals("null")) {
            return new NullConst(new Position(ctx));
        } else if (ctx.literal().StringLiteral() != null) {
            return new StringConst(new Position(ctx), text);
        } else {
            return new IntConst(new Position(ctx), Integer.parseInt(text));
        }
    }
}

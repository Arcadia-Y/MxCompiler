package AST;

public interface ASTVisitor {
     void visit(Program it);
     void visit(Declaration it);
     void visit(Statement it);

     void visit(VarDecl it);
     void visit(FuncDecl it);
     void visit(ClassDecl it);

     void visit(BlockStmt it);
     void visit(ExprStmt it);
     void visit(IfStmt it);
     void visit(WhileStmt it);
     void visit(ForStmt it);
     void visit(ContinueStmt it);
     void visit(BreakStmt it);
     void visit(ReturnStmt it);
     void visit(VarDeclStmt it);

     void visit(ParameterDeclList it);
     void visit(ParameterDecl it);
     void visit(VarInitDecl it);

     void visit(Expr it);
     void visit(Postfix it);
     void visit(FunctionCall it);
     void visit(ParameterList it);
     void visit(Subscript it);
     void visit(MemberAccess it);
     void visit(UnaryExpr it);
     void visit(NewExpr it);
     void visit(BinaryExpr it);
     void visit(Ternary it);
     void visit(Assignment it);

     void visit(Variable it);
     void visit(BoolConst it);
     void visit(IntConst it);
     void visit(StringConst it);
     void visit(NullConst it);


     void visit(TypeNode it);
}

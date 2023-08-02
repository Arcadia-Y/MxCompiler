package AST;

import Util.Position;

public class ForStmt extends Statement {
    public Expr init, cond, next;
    public VarDecl varDecl;
    public Statement stmt;

    ForStmt(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

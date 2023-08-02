package AST;

import Util.Position;

public class ExprStmt extends Statement {
    public Expr expr;

    ExprStmt(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

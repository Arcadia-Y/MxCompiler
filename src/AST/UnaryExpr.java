package AST;

import Util.Position;

public class UnaryExpr extends Expr {
    public String op;
    public Expr expr;

    UnaryExpr(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

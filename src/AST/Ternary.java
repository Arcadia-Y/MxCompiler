package AST;

import Util.Position;

public class Ternary extends Expr {
    public Expr cond, trueExpr, falseExpr;

    Ternary(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

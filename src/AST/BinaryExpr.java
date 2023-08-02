package AST;

import Util.Position;

public class BinaryExpr extends Expr {
    public Expr l, r;
    public String op;

    BinaryExpr(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

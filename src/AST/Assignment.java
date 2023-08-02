package AST;

import Util.Position;

public class Assignment extends Expr {
    public Expr l, r;

    Assignment(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

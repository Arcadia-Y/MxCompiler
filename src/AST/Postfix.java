package AST;

import Util.Position;

public class Postfix extends Expr {
    public Expr expr;
    public String op;

    Postfix(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

package AST;

import Util.Position;

public class Subscript extends Expr {
    public Expr array;
    public Expr index;

    Subscript(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

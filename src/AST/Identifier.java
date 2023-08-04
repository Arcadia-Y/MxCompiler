package AST;

import Util.Position;

public class Identifier extends Expr {
    public final String name;

    Identifier(Position pos, String n) {
        super(pos);
        name = n;
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

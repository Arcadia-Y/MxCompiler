package AST;

import Util.Position;

public class Identifier extends Expr {
    public String name;

    Identifier(Position pos, String n) {
        super(pos);
        name = n;
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

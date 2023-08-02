package AST;

import Util.Position;

public class Variable extends Expr {
    public final String name;

    Variable(Position pos, String n) {
        super(pos);
        name = n;
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

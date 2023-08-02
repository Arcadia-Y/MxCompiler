package AST;

import Util.Position;

public class BoolConst extends Expr{
    public final boolean value;

    BoolConst(Position pos, boolean v) {
        super(pos);
        value = v;
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

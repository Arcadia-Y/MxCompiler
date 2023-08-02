package AST;

import Util.Position;

public class IntConst extends Expr {
    public final int value;

    IntConst(Position pos, int v) {
        super(pos);
        value = v;
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

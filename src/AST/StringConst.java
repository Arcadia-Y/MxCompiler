package AST;

import Util.Position;

public class StringConst extends Expr{
    public final String value;

    StringConst(Position pos, String v) {
        super(pos);
        value = v;
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

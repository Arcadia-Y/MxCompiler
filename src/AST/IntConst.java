package AST;

import Util.Position;
import Util.Type.BaseType;

public class IntConst extends Expr {
    public final int value;

    IntConst(Position pos, int v) {
        super(pos);
        t.baseType = BaseType.INT;
        value = v;
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

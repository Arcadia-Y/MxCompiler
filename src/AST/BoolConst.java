package AST;

import Util.Position;
import Util.Type.BaseType;

public class BoolConst extends Expr{
    public final boolean value;

    BoolConst(Position pos, boolean v) {
        super(pos);
        t.baseType = BaseType.BOOL;
        value = v;
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

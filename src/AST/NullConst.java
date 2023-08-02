package AST;

import Util.Position;
import Util.Type.BaseType;

public class NullConst extends Expr{
    NullConst(Position pos) {
        super(pos);
        t.baseType = BaseType.NULL;
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

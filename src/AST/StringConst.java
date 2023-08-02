package AST;

import Util.Position;
import Util.Type.BaseType;

public class StringConst extends Expr{
    public final String value;

    StringConst(Position pos, String v) {
        super(pos);
        t.baseType = BaseType.STRING;
        value = v;
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

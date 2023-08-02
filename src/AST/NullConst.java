package AST;

import Util.Position;

public class NullConst extends Expr{
    NullConst(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

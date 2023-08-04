package AST;

import Util.Position;

public class MemberAccess extends Expr {
    public Expr obj;
    public Identifier memName;

    MemberAccess(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

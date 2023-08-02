package AST;

import Util.Position;

public class ContinueStmt extends Statement {
    ContinueStmt(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

package AST;

import Util.Position;

public class BreakStmt extends Statement {
    BreakStmt(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

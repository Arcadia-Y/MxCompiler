package AST;

import Util.Position;

public class ReturnStmt extends Statement {
    public Expr expr;
    
    ReturnStmt(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

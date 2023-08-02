package AST;

import Util.Position;

public class WhileStmt extends Statement {
    public Expr cond;
    public Statement stmt;
    
    WhileStmt(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

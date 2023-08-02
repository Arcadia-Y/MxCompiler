package AST;

import Util.Position;

public class IfStmt extends Statement {
    public Expr cond;
    public Statement trueStmt;
    public Statement falseStmt;
    
    IfStmt(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

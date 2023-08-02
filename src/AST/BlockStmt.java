package AST;

import java.util.LinkedList;

import Util.Position;

public class BlockStmt extends Statement {
    public LinkedList<Statement> stmts;

    BlockStmt(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

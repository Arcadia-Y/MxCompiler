package AST;

import java.util.ArrayList;

import Util.Position;

public class BlockStmt extends Statement {
    public ArrayList<Statement> stmts = new ArrayList<Statement>();

    BlockStmt(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

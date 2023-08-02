package AST;

import Util.Position;

public class VarDeclStmt extends Statement {
    public VarDecl decl;

    VarDeclStmt(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

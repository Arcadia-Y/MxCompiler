package AST;

import Util.Position;

public class ConstructDecl extends Declaration {
    String name;
    BlockStmt block;

    ConstructDecl(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

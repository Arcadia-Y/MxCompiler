package AST;

import Util.Position;

public class VarInitDecl extends ASTNode {
    public String id;
    public Expr init;    

    VarInitDecl(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

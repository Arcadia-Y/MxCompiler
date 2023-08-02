package AST;

import Util.Position;

public class ParameterDecl extends ASTNode {
    public TypeNode t;
    public String name;

    ParameterDecl(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

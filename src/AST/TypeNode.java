package AST;

import Util.Position;
import Util.Type;

public class TypeNode extends ASTNode {
    public Type t;

    TypeNode(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

package AST;

import Util.Position;

public class ThisNode extends Expr {
    ThisNode(Position pos) {
        super(pos);
    }
    
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

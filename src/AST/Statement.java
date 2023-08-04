package AST;

import Semantic.Scope;
import Util.Position;

public abstract class Statement extends ASTNode {
    public Scope scope;
    
    Statement(Position pos) {
        super(pos);
    }
}

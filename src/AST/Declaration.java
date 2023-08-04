package AST;

import Semantic.Scope;
import Util.Position;

abstract public class Declaration extends ASTNode {
    public Scope scope;
    Declaration(Position pos) {
        super(pos);
    }
}

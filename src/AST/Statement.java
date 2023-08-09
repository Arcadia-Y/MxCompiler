package AST;

import Util.Position;

public abstract class Statement extends ASTNode {
    Statement(Position pos) {
        super(pos);
    }
}

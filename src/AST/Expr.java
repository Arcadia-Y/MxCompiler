package AST;

import Util.Position;
import Util.Type;

public abstract class Expr extends ASTNode {
    Type t;
    Boolean isLvalue;

    Expr(Position pos) {
        super(pos);
    }
}

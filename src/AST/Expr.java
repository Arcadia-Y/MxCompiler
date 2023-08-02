package AST;

import Util.Position;

public abstract class Expr extends ASTNode {
    Type t;
    Boolean isLvalue;

    Expr(Position pos) {
        super(pos);
    }
}

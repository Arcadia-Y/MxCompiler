package AST;

import Util.Position;
import Util.Type;

public abstract class Expr extends ASTNode {
    public Type t = new Type();
    public Boolean isLvalue = false;

    Expr(Position pos) {
        super(pos);
    }
}

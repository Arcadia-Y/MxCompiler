package AST;

import java.util.LinkedList;
import Util.Position;
import Util.Type;

public class NewExpr extends Expr{
    Type t;
    LinkedList<Expr> init = new LinkedList<Expr>();

    NewExpr(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

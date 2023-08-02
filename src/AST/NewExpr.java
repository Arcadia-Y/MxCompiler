package AST;

import java.util.LinkedList;
import Util.Position;

public class NewExpr extends Expr{
    Type t;
    LinkedList<Expr> init;

    NewExpr(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

package AST;

import java.util.ArrayList;
import Util.Position;

public class NewExpr extends Expr{
    public ArrayList<Expr> init = new ArrayList<Expr>();

    NewExpr(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

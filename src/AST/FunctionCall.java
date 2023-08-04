package AST;

import java.util.ArrayList;
import Util.Position;

public class FunctionCall extends Expr {
    public Expr expr;
    public ArrayList<Expr> para = new ArrayList<Expr>();

    FunctionCall(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

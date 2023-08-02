package AST;

import java.util.LinkedList;
import Util.Position;

public class FunctionCall extends Expr {
    public Expr expr;
    public LinkedList<Expr> para = new LinkedList<Expr>();

    FunctionCall(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

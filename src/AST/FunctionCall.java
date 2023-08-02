package AST;

import Util.Position;

public class FunctionCall extends Expr {
    public Expr expr;
    public ParameterList para;

    FunctionCall(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

package AST;

import java.util.LinkedList;

import Util.Position;

public class ParameterList extends ASTNode {
    public LinkedList<Expr> list;
    
    ParameterList(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

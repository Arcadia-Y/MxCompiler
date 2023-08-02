package AST;

import java.util.LinkedList;

import Util.Position;

public class ParameterDeclList extends ASTNode {
    public LinkedList<ParameterDecl> list = new LinkedList<ParameterDecl>();

    ParameterDeclList(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

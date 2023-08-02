package AST;

import java.util.LinkedList;

import Util.Position;

public class ClassDecl extends Declaration {
    String name;
    LinkedList<Declaration> mem = new LinkedList<Declaration>();

    ClassDecl(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

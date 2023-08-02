package AST;

import java.util.LinkedList;

import Util.Position;

public class ClassDecl extends Declaration {
    String name;
    LinkedList<Declaration> mem;

    ClassDecl(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

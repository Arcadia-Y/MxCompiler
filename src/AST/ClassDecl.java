package AST;

import java.util.ArrayList;

import Util.Position;

public class ClassDecl extends Declaration {
    public String name;
    public ArrayList<Declaration> mem = new ArrayList<Declaration>();

    public ClassDecl(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

package AST;

import Util.Position;
import java.util.LinkedList;

public class VarDecl extends Declaration {
    public TypeNode t;
    public LinkedList<VarInitDecl> inits = new LinkedList<VarInitDecl>();

    VarDecl(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

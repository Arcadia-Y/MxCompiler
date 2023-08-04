package AST;

import Util.Position;
import java.util.ArrayList;

public class VarDecl extends Declaration {
    public TypeNode t;
    public ArrayList<VarInitDecl> inits = new ArrayList<VarInitDecl>();

    VarDecl(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

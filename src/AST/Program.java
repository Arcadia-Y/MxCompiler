package AST;

import java.util.LinkedList;
import Util.Position;

public class Program extends ASTNode {
    public LinkedList<Declaration> decls = new LinkedList<Declaration>();
    
    Program(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

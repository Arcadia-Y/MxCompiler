package AST;

import java.util.ArrayList;

import Semantic.GlobalScope;
import Util.Position;

public class Program extends ASTNode {
    public ArrayList<Declaration> decls = new ArrayList<Declaration>();
    public GlobalScope scope;
    
    Program(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

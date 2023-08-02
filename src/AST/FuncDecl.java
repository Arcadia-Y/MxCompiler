package AST;

import java.util.LinkedList;
import Util.Position;

public class FuncDecl extends Declaration {
    public TypeNode retType;
    public String name;
    public LinkedList<ParameterDecl> para = new LinkedList<ParameterDecl>();
    public BlockStmt block;

    FuncDecl(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

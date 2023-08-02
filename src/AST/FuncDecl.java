package AST;

import Util.Position;

public class FuncDecl extends Declaration {
    public TypeNode retType;
    public String name;
    public ParameterDeclList para;
    public BlockStmt block;

    FuncDecl(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

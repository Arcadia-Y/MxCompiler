package AST;

import java.util.ArrayList;

import Util.FuncType;
import Util.Position;

public class FuncDecl extends Declaration {
    public TypeNode retType;
    public String name;
    public ArrayList<ParameterDecl> para = new ArrayList<ParameterDecl>();
    public BlockStmt block;
    public FuncType funcType;

    FuncDecl(Position pos) {
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    public FuncType getFuncType() {
        return funcType;
    }
}

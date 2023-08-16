package IR;

import IR.Node.Module;
import IR.Node.*;

public interface IRVisitor {
    public void visit(Alloca it);
    public void visit(BasicBlock it);
    public void visit(BinaryInst it);
    public void visit(BoolConst it);
    public void visit(Br it);
    public void visit(Call it);
    public void visit(FuncDecl it);
    public void visit(FuncDef it);
    public void visit(GEP it);
    public void visit(GlobalDecl it);
    public void visit(Icmp it);
    public void visit(IntConst it);
    public void visit(Load it);
    public void visit(Module it);
    public void visit(NullConst it);
    public void visit(Phi it);
    public void visit(Ret it);
    public void visit(Store it);
    public void visit(StringGlobal it);
    public void visit(StructDecl it);
    public void visit(Var it);
}

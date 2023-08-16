package IR.Node;

import java.util.ArrayList;

import IR.Type.*;

public class Module extends IRNode {
    public ArrayList<FuncDecl> funcDecls = new ArrayList<FuncDecl>();
    public ArrayList<FuncDef> funcDefs = new ArrayList<FuncDef>();
    public ArrayList<GlobalDecl> globals = new ArrayList<GlobalDecl>();
    public ArrayList<StringGlobal> strings = new ArrayList<StringGlobal>();
    public ArrayList<StructDecl> structs = new ArrayList<StructDecl>();
    private IntType intType = new IntType(32);
    private IntType boolType = new IntType(1);
    private VoidType voidType = new VoidType();
    private PtrType ptrType = new PtrType();

    public Module() {
        // bultin function
        FuncDecl f = new FuncDecl(voidType, "print");
        f.args.add(ptrType);
        funcDecls.add(f);

        f = new FuncDecl(voidType, "println");
        f.args.add(ptrType);
        funcDecls.add(f);

        f = new FuncDecl(voidType, "printInt");
        f.args.add(intType);
        funcDecls.add(f);

        f = new FuncDecl(voidType, "printlnInt");
        f.args.add(intType);
        funcDecls.add(f);

        f = new FuncDecl(ptrType, "getString");
        funcDecls.add(f);

        f = new FuncDecl(intType, "getInt");
        funcDecls.add(f);

        f = new FuncDecl(ptrType, "toString");
        f.args.add(intType);
        funcDecls.add(f);

        f = new FuncDecl(intType, "string.length");
        f.args.add(ptrType);
        funcDecls.add(f);

        f = new FuncDecl(ptrType, "string.substring");
        f.args.add(ptrType);
        f.args.add(intType);
        f.args.add(intType);
        funcDecls.add(f);

        f = new FuncDecl(intType, "string.parseInt");
        f.args.add(ptrType);
        funcDecls.add(f);

        f = new FuncDecl(intType, "string.ord");
        f.args.add(ptrType);
        f.args.add(intType);
        funcDecls.add(f);

        f = new FuncDecl(ptrType, "string.add");
        f.args.add(ptrType);
        f.args.add(ptrType);
        funcDecls.add(f);

        f = new FuncDecl(boolType, "string.eq");
        f.args.add(ptrType);
        f.args.add(ptrType);
        funcDecls.add(f);

        f = new FuncDecl(boolType, "string.neq");
        f.args.add(ptrType);
        f.args.add(ptrType);
        funcDecls.add(f);

        f = new FuncDecl(boolType, "string.gt");
        f.args.add(ptrType);
        f.args.add(ptrType);
        funcDecls.add(f);

        f = new FuncDecl(boolType, "string.gte");
        f.args.add(ptrType);
        f.args.add(ptrType);
        funcDecls.add(f);

        f = new FuncDecl(boolType, "string.lt");
        f.args.add(ptrType);
        f.args.add(ptrType);
        funcDecls.add(f);

        f = new FuncDecl(boolType, "string.lte");
        f.args.add(ptrType);
        f.args.add(ptrType);
        funcDecls.add(f);

        f = new FuncDecl(intType, "_array.size");
        f.args.add(ptrType);
        funcDecls.add(f);

        f = new FuncDecl(ptrType, "new.malloc");
        f.args.add(intType);
        funcDecls.add(f);

        f = new FuncDecl(ptrType, "new.intArray");
        f.args.add(intType);
        funcDecls.add(f);

        f = new FuncDecl(ptrType, "new.boolArray");
        f.args.add(intType);
        funcDecls.add(f);

        f = new FuncDecl(ptrType, "new.ptrArray");
        f.args.add(intType);
        funcDecls.add(f);
    }
    public String toString() {
        String ret = "";
        for (var it : structs) 
            ret += it.toString() + "\n";
        for (var it : globals)
            ret += it.toString() + "\n";
        for (var it : strings)
            ret += it.toString() + "\n";
        ret += "\n";
        for (var it : funcDecls)
            ret += it.toString() + "\n";
        ret += "\n";
        for (var it : funcDefs)
            ret += it.toString() + "\n\n";
        return ret;
    }
}

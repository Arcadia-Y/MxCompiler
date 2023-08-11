package IR.Node;

import java.util.ArrayList;

import IR.Type.*;

public class Module extends IRNode {
    public ArrayList<FuncDecl> funcDecls = new ArrayList<FuncDecl>();
    public ArrayList<FuncDef> funcDefs = new ArrayList<FuncDef>();
    public ArrayList<GlobalDecl> globals = new ArrayList<GlobalDecl>();
    public ArrayList<StructDecl> structs = new ArrayList<StructDecl>();
    
    public Module() {
        // bultin function
        FuncDecl f = new FuncDecl(new VoidType(), "print");
        f.args.add(new PtrType());
        funcDecls.add(f);

        f = new FuncDecl(new VoidType(), "println");
        f.args.add(new PtrType());
        funcDecls.add(f);

        f = new FuncDecl(new VoidType(), "printInt");
        f.args.add(new IntType(32));
        funcDecls.add(f);

        f = new FuncDecl(new VoidType(), "printlnInt");
        f.args.add(new IntType(32));
        funcDecls.add(f);

        f = new FuncDecl(new PtrType(), "getString");
        funcDecls.add(f);

        f = new FuncDecl(new IntType(32), "getInt");
        funcDecls.add(f);

        f = new FuncDecl(new PtrType(), "toString");
        f.args.add(new IntType(32));
        funcDecls.add(f);

        f = new FuncDecl(new IntType(32), "string.length");
        funcDecls.add(f);

        f = new FuncDecl(new PtrType(), "string.substring");
        f.args.add(new IntType(32));
        f.args.add(new IntType(32));
        funcDecls.add(f);

        f = new FuncDecl(new IntType(32), "string.parseInt");
        funcDecls.add(f);

        f = new FuncDecl(new IntType(32), "string.ord");
        f.args.add(new IntType(32));
        funcDecls.add(f);

        f = new FuncDecl(new PtrType(), "new.malloc");
        f.args.add(new IntType(32));
        funcDecls.add(f);
    }
    public String toString() {
        String ret = "";
        for (var it : structs) 
            ret += it.toString() + "\n";
        for (var it : globals)
            ret += it.toString() + "\n";
        for (var it : funcDecls)
            ret += it.toString() + "\n";
        for (var it : funcDefs)
            ret += it.toString() + "\n";
        return ret;
    }
}

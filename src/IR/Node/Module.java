package IR.Node;

import java.util.ArrayList;

public class Module extends IRNode {
    public ArrayList<FuncDecl> funcDecls = new ArrayList<FuncDecl>();
    public ArrayList<FuncDef> funcDefs = new ArrayList<FuncDef>();
    public ArrayList<GlobalDecl> globals = new ArrayList<GlobalDecl>();
    public ArrayList<StructDecl> structs = new ArrayList<StructDecl>();
    
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

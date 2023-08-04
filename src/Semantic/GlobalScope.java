package Semantic;

import java.util.ArrayList;
import java.util.HashMap;

import AST.ASTNode;
import Util.Type;
import Util.Type.BaseType;
import Util.FuncType;

public class GlobalScope extends Scope {
    public HashMap<String, Scope> classInfo = new HashMap<String, Scope>();

    GlobalScope(Scope s, ASTNode n) {
        super(s, n);
        ArrayList<Type> list = new ArrayList<Type>();
        table.put("getString", new FuncType(new Type(BaseType.STRING), list));
        table.put("getInt", new FuncType(new Type(BaseType.INT), list));
        list.add(new Type(BaseType.STRING)); 
        table.put("print", new FuncType(new Type(BaseType.VOID), list));
        table.put("println", new FuncType(new Type(BaseType.VOID), list));
        list.clear();
        list.add(new Type(BaseType.INT));
        table.put("printInt", new FuncType(new Type(BaseType.VOID), list));
        table.put("printlnInt", new FuncType(new Type(BaseType.VOID), list));
        table.put("toString", new FuncType(new Type(BaseType.STRING), list));
    } 
}

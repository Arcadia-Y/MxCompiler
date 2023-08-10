package Semantic;

import AST.*;
import Util.FuncType;
import Util.Error.SemanticError;
import Util.Type.BaseType;

public class SymbolCollector {
    GlobalScope global;

    public void visit(Program node) {
        global = new GlobalScope(null, node);
        node.scope = global;
        for (var del: node.decls) {
            if (del instanceof FuncDecl) {
                FuncDecl func = (FuncDecl) del;
                if (global.table.containsKey(func.name) || global.classInfo.containsKey(func.name))
                    throw new SemanticError("function name redefined", func.pos);
                global.table.put(func.name, func.getFuncType());
            } else if (del instanceof ClassDecl) {
                ClassDecl cla = (ClassDecl) del;
                if (global.classInfo.containsKey(cla.name) || global.table.containsKey(cla.name))
                    throw new SemanticError("class name redefined", cla.pos);
                global.classInfo.put(cla.name, visit(cla));
            }
        }
        FuncType mainFuncType = (FuncType) global.table.get("main");
        if (mainFuncType == null)
            throw new SemanticError("no main function", node.pos);
        if (mainFuncType.retType.baseType != BaseType.INT || mainFuncType.retType.arrayLayer != 0)
            throw new SemanticError("main function should return int", node.pos);
        if (mainFuncType.argTypes.size() != 0)
            throw new SemanticError("main function should have no argument", node.pos);
    }
    public Scope visit(ClassDecl node) {
        var ret = new Scope(global, node);
        int pos = 0;
        int construtCount = 0;
        for (var del : node.mem) {
            if (del instanceof FuncDecl) {
                FuncDecl func = (FuncDecl) del;
                if (ret.table.containsKey(func.name))
                    throw new SemanticError("member function name redefined", func.pos);
                if (func.name.equals(node.name))
                    throw new SemanticError("construct function should have no return type", func.pos);
                ret.table.put(func.name, func.getFuncType());
            } else if (del instanceof ConstructDecl) {
                ConstructDecl func = (ConstructDecl) del;
                if (!func.name.equals(node.name))
                    throw new SemanticError("construct function should have the same name as the class", func.pos);
                construtCount++;
                if (construtCount > 1)
                    throw new SemanticError("construct function redefined", func.pos);
            } else {
                VarDecl varDecls = (VarDecl) del;
                for (var item : varDecls.inits) {
                    if (ret.table.containsKey(item.id))
                        throw new SemanticError("member variable name redefined", item.pos);
                    ret.table.put(item.id, varDecls.t.t);
                    global.name2pos.put(node.name + "." + item.id, pos);
                    pos++;
                }
            }
        }
        return ret;
    }
}

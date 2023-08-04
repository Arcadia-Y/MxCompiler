package Semantic;

import java.util.HashMap;

import AST.ASTNode;
import Util.Type;

public class Scope {
    public HashMap<String, Type> table = new HashMap<String, Type>();
    public ASTNode node;
    public Scope parent;
    
    Scope(Scope p, ASTNode n) {
        parent = p;
        node = n;
    }
}

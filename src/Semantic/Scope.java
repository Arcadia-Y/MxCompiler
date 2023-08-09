package Semantic;

import java.util.HashMap;

import AST.ASTNode;
import Util.Type;

public class Scope {
    public HashMap<String, Type> table = new HashMap<String, Type>();
    public HashMap<String, Integer> rename = new HashMap<String, Integer>(); // store <.n> for variables needing renameing
    public ASTNode node;
    public Scope parent;
    
    Scope(Scope p, ASTNode n) {
        parent = p;
        node = n;
    }
}

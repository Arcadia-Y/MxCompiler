package ASM.Node;

import java.util.ArrayList;

public class Function {
    public String name; 
    public ArrayList<Block> blocks = new ArrayList<Block>();
    public Function(String Name) {
        name = Name;
    }
    public String toString() {
        String ret = "\t.globl\t" + name + "\n";
        ret += "\t.type\t" + name + ",@function\n";
        ret += name + ":\n";
        for (var b : blocks)
            ret += b.toString();
        return ret;
    } 
}

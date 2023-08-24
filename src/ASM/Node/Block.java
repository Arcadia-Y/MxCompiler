package ASM.Node;

import java.util.LinkedList;

public class Block {
    public String label;
    public LinkedList<Instruction> list = new LinkedList<Instruction>();
    public LinkedList<Instruction> exitIns = new LinkedList<Instruction>();
    public Block(String name) {
        label = name;
    }
    public void add(Instruction ins) {
        list.add(ins);
    }
    public void exit(Instruction ins) {
        exitIns.add(ins);
    }
    public void phi(Instruction ins) {
        exitIns.add(0, ins);
    }
    public String toString() {
        String ret = label + ":\n";
        for (var i : list)
            ret += "\t" + i.toString() + "\n";
        for (var i : exitIns)
            ret += "\t" + i.toString() + "\n";
        return ret;
    } 
}

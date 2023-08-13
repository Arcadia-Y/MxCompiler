package IR.Node;

import java.util.ArrayList;

public class BasicBlock extends IRNode {
    public String label;
    public ArrayList<Instruction> ins = new ArrayList<Instruction>();
    private Instruction exitins;

    public BasicBlock(String n) {
        label = n;
    }
    public boolean isend() {
        return exitins != null;
    }
    public void add(Instruction i) {
        ins.add(i);
    }
    public void exit(Instruction i) {
        exitins = i;
    }
    public String toString() {
        String ret = label + ":\n";
        for (var item : ins) {
            ret += item.toString();
            ret += '\n';
        }
        ret += exitins.toString();
        ret += '\n';
        return ret;
    }
}

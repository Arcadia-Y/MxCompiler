package IR.Node;

import java.util.ArrayList;

public class BasicBlock extends IRNode {
    public String label;
    public ArrayList<Instruction> ins;

    public void add(Instruction i) {
        ins.add(i);
    }
    public String toString() {
        String ret = label + ":\n";
        for (var item : ins) {
            ret += item.toString();
            ret += '\n';
        }
        return ret;
    }
}

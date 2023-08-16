package ASM.Node;

public class GlobalVar {
    public String name;
    public int value;
    public GlobalVar(String Name, int Value) {
        name = Name;
        value = Value;
    }
    public String toString() {
        String ret = "\t.globl\t" + name + "\n";
        ret += name + ":\n";
        ret += "\t.word\t" + value + "\n";
        return ret;
    }
}

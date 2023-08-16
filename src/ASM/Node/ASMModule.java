package ASM.Node;

import java.util.ArrayList;

public class ASMModule {
    public ArrayList<Function> funcs = new ArrayList<Function>();
    public ArrayList<GlobalVar> globals = new ArrayList<GlobalVar>();
    public ArrayList<GlobalStr> strings = new ArrayList<GlobalStr>();
    public String toString() {
        String ret = "\t.text\n";
        for (var f : funcs)
            ret += f.toString() + "\n";
        if (!globals.isEmpty())
            ret += "\t.data\n";
        for (var v : globals)
            ret += v.toString() + "\n";
        if (!strings.isEmpty())
            ret += "\t.rodata\n";
        for (var s : strings)
            ret += s.toString() + "\n";
        return ret;
    }
}

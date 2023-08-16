package ASM.Node;

public class GlobalStr {
    public String label;
    public String content;
    public GlobalStr(String Label, String Content) {
        label = Label;
        content = Content;
    }
    public String toString() {
        String ret = label + ":\n";
        ret += "\t.asciz\t\"";
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            switch (c) {
            case '\n':
                ret += "\\n";
                break;
            case '\\':
                ret += "\\\\";
                break;
            case '\"':
                ret += "\\\"";
                break;
            default:
                ret += c;
            }
        }
        ret += "\"\n";
        ret += "\t.size\t" + label + ", " + (content.length() + 1) + "\n";
        return ret;
    }
}

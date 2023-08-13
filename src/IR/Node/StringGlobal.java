package IR.Node;

public class StringGlobal {
    Var reg;
    String str;
    public StringGlobal(Var register, String content) {
        reg = register;
        str = content;
    }
    public String toString() {
        String ret = reg.name + " = private unnamed_addr constant [";
        ret += str.length() + 1;
        ret += " x i8] c\"";
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
            case '\n':
                ret += "\\0A";
                break;
            case '\\':
                ret += "\\\\";
                break;
            case '\"':
                ret += "\\22";
                break;
            default:
                ret += c;
            }
        }
        ret += "\\00\"";
        return ret;
    }
}

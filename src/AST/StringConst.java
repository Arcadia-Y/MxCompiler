package AST;

import Util.Position;
import Util.Type.BaseType;

public class StringConst extends Expr{
    public String value = "";

    StringConst(Position pos, String v) {
        super(pos);
        t.baseType = BaseType.STRING;
        for (int i = 1; i < v.length() - 1; i++) {
            char ch = v.charAt(i);
            if (ch != '\\') {
                value += ch;
                continue;
            }
            i++;
            ch = v.charAt(i);
            switch (ch) {
            case 'n':
                value += '\n';
                break;
            case '"':
                value += '\"';
                break;
            case '\\':
                value += '\\';
            }
        }
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

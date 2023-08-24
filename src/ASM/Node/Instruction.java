package ASM.Node;

import java.util.ArrayList;

public class Instruction {
    public String ins;
    public ArrayList<String> tokens = new ArrayList<>();
    public Instruction(String content) {
        ins = content;
        tokens.add(ins);
    }
    public Instruction(String op, String rd) {
        tokens.add(op);
        tokens.add(rd);
        ins = op + " " + rd;
    }
    public Instruction(String op, String rd, String rs) {
        tokens.add(op);
        tokens.add(rd);
        tokens.add(rs);
        ins = op + " " + rd + ", " + rs;
    }
    public Instruction(String op, String rd, String op1, String op2) {
        tokens.add(op);
        tokens.add(rd);
        tokens.add(op1);
        tokens.add(op2);
        ins = op + " " + rd + ", " + op1 + ", " + op2;
    }
    public String toString() {
        return ins;
    } 
}

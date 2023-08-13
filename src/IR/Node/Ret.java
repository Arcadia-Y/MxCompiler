package IR.Node;

public class Ret extends Instruction {
    Register value;
    public Ret(Register r) {
        value = r;
    }
    public String toString() {
        if (value == null)
            return "ret void";
        return "ret " + value.toString();
    }
}

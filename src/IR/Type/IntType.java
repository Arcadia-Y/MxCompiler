package IR.Type;

public class IntType extends Type{
    int bits;

    public IntType(int b) {
        bits = b;
    }
    public String toString() {
        return "i" + bits;
    }
}

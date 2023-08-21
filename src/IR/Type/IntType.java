package IR.Type;

public class IntType extends Type{
    public int bits;

    public IntType(int b) {
        bits = b;
    }
    public String toString() {
        return "i" + bits;
    }
    @Override
    public boolean isBool() {
        return bits == 1;
    }
}

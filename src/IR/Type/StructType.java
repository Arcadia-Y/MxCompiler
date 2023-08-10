package IR.Type;

public class StructType extends Type {
    String name;
    public StructType(String n) {
        name = n;
    }
    public String toString() {
        return name;
    }
}

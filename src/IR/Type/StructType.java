package IR.Type;

// only for GEP

public class StructType extends Type {
    String name;
    public StructType(String n) {
        name = "%class." + n;
    }
    public String toString() {
        return name;
    }
}

package Util;

public class ClassType extends Type {
    public String name;

    public ClassType(String n) {
        baseType = BaseType.CLASS;
        name = n;
    }
    @Override public boolean isSame(Type other) {
        if (!super.isSame(other)) return false;
        if (other.baseType == BaseType.NULL) return true;
        return this.name.equals(((ClassType) other).name);
    }
    @Override public Type copy() {
        ClassType ret = new ClassType(name);
        ret.arrayLayer = arrayLayer;
        return ret;
    }
}

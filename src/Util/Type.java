package Util;

public class Type {
    public enum BaseType {
        INT, BOOL, STRING, CLASS, VOID, NULL
    }
    public BaseType baseType;
    public String className;
    public int arrayLayer = 0;
    public boolean isRef() {
        if (arrayLayer > 0 || baseType== BaseType.STRING || baseType == BaseType.CLASS || baseType == BaseType.NULL) {
            return true;
        }
        return false;
    }
    public boolean isSame(Type other) {
        if (baseType == BaseType.NULL) {
            return other.isRef() ? true : false;
        }
        if (other.baseType == BaseType.NULL) {
            return isRef() ? true : false;
        }
        if (baseType != other.baseType) {
            return false;
        }
        if (arrayLayer != other.arrayLayer) {
            return false;
        }
        if (baseType == BaseType.CLASS) {
            return className == other.className;
        }
        return true;
    }
}

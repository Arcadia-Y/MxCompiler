package Util;

public class Type {
    public enum BaseType {
        INT, BOOL, STRING, CLASS, VOID, FUNC, NULL
    }
    public BaseType baseType;
    public int arrayLayer = 0;

    public Type() {}
    public Type(BaseType t) {
        baseType = t;
    }
    public boolean isRef() {
        if (arrayLayer > 0 || baseType == BaseType.CLASS || baseType == BaseType.NULL) {
            return true;
        }
        return false;
    }
    public boolean isSame(Type other) {
        if (baseType == BaseType.NULL) {
            return other.isRef();
        }
        if (other.baseType == BaseType.NULL) {
            return isRef();
        }
        if (baseType != other.baseType) {
            return false;
        }
        if (arrayLayer != other.arrayLayer) {
            return false;
        }
        return true;
    }
    public Type copy() {
        Type ret = new Type(baseType);
        ret.arrayLayer = arrayLayer;
        return ret;
    }
}

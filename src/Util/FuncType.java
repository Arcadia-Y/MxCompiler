package Util;

import java.util.ArrayList;

public class FuncType extends Type {
    public Type retType;
    public ArrayList<Type> argTypes = new ArrayList<Type>();

    public FuncType() {
        baseType = BaseType.FUNC;
    }
    public FuncType(Type ret, ArrayList<Type> list) {
        baseType = BaseType.FUNC;
        retType = ret;
        for (var item: list) {
            argTypes.add(item);
        }
    }
    @Override
    public boolean isSame(Type other) {
        return false;
    }
}

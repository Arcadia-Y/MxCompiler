package ASM.Storage;

public class Register extends StoreUnit {
    public String name;
    public Register(String id) {
        name = id;
    }
    public String toString() {
        return name;
    }
}

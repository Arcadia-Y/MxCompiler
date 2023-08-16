package ASM.Storage;

public class StackSlot extends StoreUnit {
    public int offset;
    public StackSlot(int offset_) {
        offset = offset_;
    }
}

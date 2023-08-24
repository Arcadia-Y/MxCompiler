package Optimize;

import ASM.Storage.RegisterSet;
import IR.Node.Module;

public interface RegisterAllocator {
    public void run(Module m);
    public RegisterSet getRegSet();
}

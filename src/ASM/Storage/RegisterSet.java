package ASM.Storage;

import java.util.HashMap;

public class RegisterSet {
    public HashMap<String, Register> callerReg = new HashMap<>();
    public HashMap<String, Register> saveReg = new HashMap<>();
    public Register ra = new Register("ra"), sp = new Register("sp"), t0 = new Register("t0"), zero = new Register("zero"), a0 = new Register("a0");
    public RegisterSet() {
        callerReg.put("t1", new Register("t1"));
        callerReg.put("t2", new Register("t2"));
        callerReg.put("t3", new Register("t3"));
        callerReg.put("t4", new Register("t4"));
        callerReg.put("t5", new Register("t5"));
        callerReg.put("t6", new Register("t6"));
        callerReg.put("a0", a0);
        callerReg.put("a1", new Register("a1"));
        callerReg.put("a2", new Register("a2"));
        callerReg.put("a3", new Register("a3"));
        callerReg.put("a4", new Register("a4"));
        callerReg.put("a5", new Register("a5"));
        callerReg.put("a6", new Register("a6"));
        callerReg.put("a7", new Register("a7"));
        saveReg.put("s0", new Register("s0"));
        saveReg.put("s1", new Register("s1"));
        saveReg.put("s2", new Register("s2"));
        saveReg.put("s3", new Register("s3"));
        saveReg.put("s4", new Register("s4"));
        saveReg.put("s5", new Register("s5"));
        saveReg.put("s6", new Register("s6"));
        saveReg.put("s7", new Register("s7"));
        saveReg.put("s8", new Register("s8"));
        saveReg.put("s9", new Register("s9"));
        saveReg.put("s10", new Register("s10"));
        saveReg.put("s11", new Register("s11"));
        saveReg.put("gp", new Register("gp"));
        saveReg.put("tp", new Register("tp"));
    }
}

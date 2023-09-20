package Optimize;

import java.util.HashMap;
import java.util.HashSet;

import IR.Node.*;
import IR.Node.Module;

public class GlobalVariableOptimizer {
    private static class GlobalInfo {
        public boolean isStore = false;
        public Register value;
    }
    private HashMap<Var, GlobalInfo> global2info = new HashMap<>();
    private HashMap<Var, Register> useMap = new HashMap<>();
    private HashSet<String> builtinFunc = new HashSet<>();
    private GlobalInfo getInfo(Var var) {
        var ret = global2info.get(var);
        if (ret != null) return ret;
        ret = new GlobalInfo();
        global2info.put(var, ret);
        return ret;
    }

    public void run(Module mod) {
        for (var f : mod.funcDecls)
            builtinFunc.add(f.name);
        for (var f : mod.funcDefs) {
            useMap.clear();
            for (var b : f.blocks)
                dealBlock(b);
        }
    }

    private void dealBlock(BasicBlock bb) {
        global2info.clear();
        var it = bb.ins.listIterator();
        while (it.hasNext()) {
            var i = it.next();
            if (i instanceof Load) {
                var load = (Load)i;
                if (load.ptr.name.charAt(0) == '@') {
                    if (!global2info.containsKey(load.ptr)) { // first load
                        var info = new GlobalInfo();
                        global2info.put(load.ptr, info);
                        info.value = load.res;
                        continue;
                    }
                    // later load
                    useMap.put(load.res, global2info.get(load.ptr).value);
                    it.remove();
                    continue;
                }
            }
            if (i instanceof Store) {
                var store = (Store) i;
                if (store.ptr.name.charAt(0) == '@') {
                    var info = getInfo(store.ptr);
                    info.isStore = true;
                    var mapValue = useMap.get(store.value);
                    if (mapValue != null)
                        store.value = mapValue;
                    info.value = store.value;
                    it.remove();
                    continue;
                }
            }
            if (i instanceof Call) {
                var call = (Call) i;
                for (int index = 0; index < call.args.size(); index++) {
                    var arg = call.args.get(index);
                    if (useMap.containsKey(arg))
                        call.args.set(index, useMap.get(arg));
                }
                if (builtinFunc.contains(call.name))
                    continue;
                it.previous();
                for (var pair : global2info.entrySet())
                    if (pair.getValue().isStore)
                        it.add(new Store(pair.getValue().value, pair.getKey()));
                global2info.clear();
                it.next();
                continue;
            }
            // other ins
            var list = i.getUse();
            if (list != null)
                for (var u : list) {
                    var value = useMap.get(u);
                    if (value != null)
                        i.replace(u, value);
                }
        }
        // before Br
        for (var pair : global2info.entrySet())
            if (pair.getValue().isStore)
                bb.ins.add(new Store(pair.getValue().value, pair.getKey()));
        if (bb.exitins instanceof Br) {
            var br = (Br) bb.exitins;
            var value = useMap.get(br.cond);
            if (value != null)
                br.cond = value;
        } else {
            var ret = (Ret)bb.exitins;
            var value = useMap.get(ret.value);
            if (value != null)
                ret.value = value;
        }
        global2info.clear();
        return;
    }
}

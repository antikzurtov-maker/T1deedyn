package dev.t1deedyn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Small registry; module instances are created once during client initialization. */
public final class ModuleManager {
    private static final List<Module> MODULES=new ArrayList<>(8);
    private ModuleManager(){}
    public static void register(Module module){MODULES.add(module);}
    public static List<Module> all(){return Collections.unmodifiableList(MODULES);}
    public static void tick(){for(int i=0;i<MODULES.size();i++){Module m=MODULES.get(i);if(m.isEnabled())try{m.onTick();}catch(Throwable e){m.setEnabled(false);T1deedynClient.LOGGER.error("Module {} auto-disabled",m.id,e);}}}
}

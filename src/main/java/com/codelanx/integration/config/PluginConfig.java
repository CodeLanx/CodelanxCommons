package com.codelanx.integration.config;

import com.codelanx.commons.config.DataHolder;
import com.codelanx.commons.config.RelativePath;
import com.codelanx.commons.data.FileDataType;
import com.codelanx.commons.data.types.Yaml;
import com.codelanx.commons.util.Reflections;
import com.codelanx.integration.util.ReflectBukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Stream;

//a dynamic per-plugin config
@RelativePath("clconfig.yml")
public enum PluginConfig implements Config {

    DISABLED_COMMANDS("disabled-commands", new ArrayList<>()),
    ;

    private static final DataHolder<Yaml> DATA = new DataHolder<>(Yaml.class);
    private final String key;
    private final Object def;

    private PluginConfig(String key, Object def) {
        this.key = key;
        this.def = def;
    }

    @Override
    public String getPath() {
        return this.key;
    }

    @Override
    public Object getDefault() {
        return this.def;
    }

    @Override
    public DataHolder<? extends FileDataType> getData() {
        return DATA;
    }

    @Override
    public File getFileLocation() {
        Plugin p = ReflectBukkit.getCallingPlugin(PluginConfig.getOffset());
        if (!Reflections.hasAnnotation(PluginConfig.class, RelativePath.class)) {
            throw new IllegalStateException("'" + PluginConfig.class.getName() + "' is missing either PluginClass or RelativePath annotations");
        }
        File folder = p.getDataFolder();
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return new File(folder, PluginConfig.class.getAnnotation(RelativePath.class).value());
    }

    //TODO: Implement
    private static int getOffset() {
        StackTraceElement[] elems = Thread.currentThread().getStackTrace();
        StackTraceElement elem = null;
        int offset = 1;
        for (int i = 0; i < elems.length; i++) {
            //search for the first non-config / non-java class
            if (Stream.of(elems[i].getClassName())
                    .filter(k -> !k.startsWith("java"))
                    .filter(k -> !k.startsWith("com.codelanx.codelanxlib.config"))
                    .filter(k -> !k.startsWith("com.codelanx.integration.config")) //TODO: Remove
                    .filter(k -> !k.startsWith("com.codelanx.commons.config"))
                    .findFirst().isPresent()) {
                elem = elems[i];
                offset = i;
                break;
            }
        }
        if (elem == null) {
            System.out.println("No match found, getting most recent caller");
            elem = Reflections.getCaller(1);
        }
        System.out.println("Caller: " + elem);
        /*
        int offset;
        if (!elem.getClassName().equals(InfoFile.class.getName())) { //wrong, but left as an example of the idea
            offset = 0;
        } else if (elem.getMethodName().equals("save")) {
            offset = 2;
        } else {
            //assume it's through a config #get call etc
        }*/
        System.out.println("Offset: " + offset + " (from file: " + (offset - 1) + ")");
        return 0;
    }
}
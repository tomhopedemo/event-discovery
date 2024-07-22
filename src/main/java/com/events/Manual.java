package com.events;

import java.util.HashMap;
import java.util.Map;

import static com.events.ExecutionBlock.*;

class Manual {
    static String city = "london";
    static String ref = "Strongroom Bar";
    static String executionBlock = "out";
    static Map<String, ExecutionBlock> blocks = createExecutionBlocks();

    static Map<String, ExecutionBlock> createExecutionBlocks() {
        Map<String, ExecutionBlock> blocks = new HashMap<>();
        blocks.put("a", new ManualAllBlock(ref));
        blocks.put("out", new OutputBlock(ref));
        blocks.put("html", new HtmlBlock());
        blocks.put("server", new ApiServerBlock());
        return blocks;
    }

    public static void main(String[] args) {
        Context.GOOGLE_OVERRIDE = true;
        BaseDirs baseDirs = new BaseDirs(Util.EnvType.LOCAL);
        WebReader.setCaches(baseDirs.getWebcacheDir(), "/");
        Local.setLocalWebParameters();
        blocks.get(executionBlock).execute(baseDirs, city);
        System.exit(0);
    }
}
package com.events;

import java.util.HashMap;
import java.util.Map;

import static com.events.ExecutionBlock.*;

/**
 * 1. run only venues with 1000
 * 2. output data in Json format exposed via rest
 * 3. next.js app to access rest endpoint
 * 4. package structure to become com.events -> events
 * 5 (from datetime-regex, additional package structure)
 */

class Control {
    static String city = "london";
    static String ref = "Bush Theatre";
    static String executionBlock = "a";
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
        BaseDirs baseDirs = new BaseDirs(Util.EnvType.LOCAL);
        WebReader.setCaches(baseDirs.getWebcacheDir(), "/");
        Local.setLocalWebParameters();
        blocks.get(executionBlock).execute(baseDirs, city);
        System.exit(0);
    }
}
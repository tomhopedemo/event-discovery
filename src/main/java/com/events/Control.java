package com.events;

import java.util.HashMap;
import java.util.Map;

import static com.events.ExecutionBlock.*;

/**
 * 1. next.js app to access rest endpoint
 * 2. package structure to become com.events -> events
 * 3 (from datetime-regex, additional package structure)
 * 4 (output - exclusions to appear before the displayed events )
 * 5 refactor execution blocks as control records / remote/local incorporated/deleted
 */

public class Control {
    static String city = "london";
    static String ref = "The Barbican Centre Theatre";
    static String executionBlock = "api";
    static Map<String, ExecutionBlock> blocks = createExecutionBlocks();

    static Map<String, ExecutionBlock> createExecutionBlocks() {
        Map<String, ExecutionBlock> blocks = new HashMap<>();
        blocks.put("a", new ManualAllBlock(ref));
        blocks.put("out", new OutputBlock(null));
        blocks.put("html", new HtmlBlock());
        blocks.put("api", new ApiServerBlock());
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
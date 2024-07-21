package com.events;

import com.events.ExecutionBlock.ApiServerBlock;
import com.events.ExecutionBlock.LocalBlock;

import java.util.Map;

import static com.events.Util.list;
import static com.events.Util.map;

class Local {
    static String executionKey = "local";
    static final String city = "london";
    static Map<String, ExecutionBlock> executionBlocks = createExecutionBlocks();

    static Map<String, ExecutionBlock> createExecutionBlocks() {
        Map<String, ExecutionBlock> executionBlocks = map();
        executionBlocks.put("local", new LocalBlock()); //very useful
        executionBlocks.put("api", new ApiServerBlock()); //initial testing
        return executionBlocks;
    }

    public static void main(String[] args) {
        BaseDirs baseDirs = new BaseDirs(Util.EnvType.LOCAL);
        WebReader.setCaches(baseDirs.getWebcacheDir(), "/");
        setLocalWebParameters();
        executionBlocks.get(executionKey).execute(baseDirs, city);
        System.exit(0);
    }

    static void setWebParameters() {
        WebReader.UriExtension.specialUrls = list("https://www.npg.org.uk", "https://www.sjss.org.uk", "https://canalcafetheatre.com", "http://www.slimjimsliquorstore.com", "https://riocinema.org.uk/RioCinema.dll", "https://phoenixcinema.co.uk/PhoenixCinema.dll", "https://genesiscinema.co.uk/GenesisCinema.dll", "https://www.menierchocolatefactory.com/Online", "http://www.thetaprooms.co.uk", "http://theatrotechnis.com", "http://www.acesandeightssaloonbar.com", "http://www.cafe1001.co.uk/cgi-bin", "http://www.thelexington.co.uk", "http://electricbrixton.uk.com");
        WebUtils.HASH_SUFFIX_URLS.add("www.rcseng.ac.uk");
    }

    static void setLocalWebParameters() {
        WebReader.THROTTLE_VALUE = 500L;
        setWebParameters();
    }
}
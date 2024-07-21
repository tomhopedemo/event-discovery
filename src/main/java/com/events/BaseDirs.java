package com.events;

import static com.events.Util.EnvType.LOCAL;

class BaseDirs {
    final Util.EnvType type;

    BaseDirs(Util.EnvType type) {
        this.type = type;
    }

    String getRoot() {
        if (LOCAL.equals(type)) {
            return "/Users/tom/";
        } else {
            return "/usr/local/";
        }
    }

    String getBaseDir() {
        return getRoot() + "londonoo" + "/";
    }

    String getInputDir() {
        return getBaseDir() + "input" + "/";
    }

    String getDynamicConfigDir() {
        return getBaseDir() + "dynconfig" + "/";
    }

    String getPhantomDir() {
        if (LOCAL.equals(type)) {
            return getRoot() + "phantomjs" + "/" + "bin" + "/";
        } else {
            return "/usr/lib/phantomjs/bin/";
        }
    }

    String getGoogleScript() {
        if (LOCAL.equals(type)) {
            return getRoot() + "googlechrome" + "/" + "headless.sh";
        } else {
            return "/usr/lib/googlechrome/headless.sh";
        }
    }

    String getWebcacheDir() {
        return getRoot() + "webcache" + "/";
    }
}
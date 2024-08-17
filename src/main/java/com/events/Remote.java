package com.events;

import com.events.ExecutionBlock.*;

import static com.events.Util.sout;

class Remote {
    public static void main(String[] args) {
        BaseDirs baseDirs = new BaseDirs(Util.EnvType.SERVER);
        WebReader.setCaches(baseDirs.getWebcacheDir(), "/");
        Local.setWebParameters();
        execute(args, baseDirs);
        System.exit(0);
    }

    static void execute(String[] args, BaseDirs baseDirs) {
        if (Util.empty(args)) {
            sout("No args - Terminating");
            return;
        }
        String executionKey = args[0];
        ExecutionBlock block = null;         // ----- Single Argument Executions
        if ("areas".equals(executionKey)) {
            block = new HtmlAreasBlock();
        } else if ("westend".equals(executionKey)) {
            block = new HtmlWestEndBlock();
        } else if ("api".equals(executionKey)) {
            block = new ApiServerBlock();
        } else if ("out".equals(executionKey)) {
            block = new OutputBlock(null);
        } else if ("now".equals(executionKey)) {
            block = new HtmlBlock();
        } else if ("htmlall".equals(executionKey)) {
            block = new HtmlAreasLondonBlock(null);
        } else if ("htmltypes".equals(executionKey)) {
            block = new HtmlTypesBlock();
        } else if ("stale".equals(executionKey)) {
            block = staleParse(args);
        } else if ("check".equals(executionKey)) {
            block = new CombinationCheckBlock();
        } else if ("htmlmonth".equals(executionKey)) {
            block = new HtmlMonthBlock();
        } else if ("merge".equals(executionKey)) {
            block = new SecondaryBlock();
        } else if ("manual".equals(executionKey)) {
            block = manualParse(args);
        } else if ("htmlarea".equals(executionKey)) {
            block = htmlAreaParse(args);
        } else {
            sout("Unregistered Execution Key - Terminating");
        }
        if (block != null) {
            block.execute(baseDirs, "london");
        }
    }

    static ExecutionBlock staleParse(String[] args) {
        if (args.length != 3) {
            sout("Stale arguments not set - terminating");
            return null;
        }
        try {
            return new RerunBlock(Double.valueOf(args[2]), true);
        } catch (NumberFormatException e) {
            sout("Unable to parse arguments - terminating");
            return null;
        }
    }

    static ExecutionBlock manualParse(String[] args) {
        if (args.length != 3) {
            sout("Control arguments not set - terminating");
            return null;
        }
        return new ManualRerunBlock(args[2]);
    }

    static ExecutionBlock htmlAreaParse(String[] args) {
        if (args.length != 3) {
            sout("htmlarea arguments not set - terminating");
            return null;
        }
        return new HtmlAreasLondonBlock(args[2]);
    }
}
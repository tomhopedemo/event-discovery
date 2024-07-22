package com.events;

import com.events.date.Calendar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.events.Util.*;

interface ExecutionBlock {
    void execute(BaseDirs dirs, String city);

    abstract class AbstractDaysBlock implements ExecutionBlock {
        Double numDays;

        AbstractDaysBlock(Double numDays) {
            this.numDays = numDays;
        }
    }

    abstract class AbstractDaysFoundBlock extends AbstractDaysBlock {
        AtomicBoolean found;

        AbstractDaysFoundBlock(Double numDaysStale, AtomicBoolean found) {
            super(numDaysStale);
            this.found = found;
        }
    }

    abstract class AbstractRefBlock implements ExecutionBlock {
        String ref;

        AbstractRefBlock(String ref) {
            this.ref = ref;
        }
    }

    class StaleLocalBlock extends AbstractDaysBlock {
        StaleLocalBlock(Double numDaysStale) {
            super(numDaysStale);
        }

        public void execute(BaseDirs baseDirs, String city) {
            InputData inputData = new InputData(city, baseDirs);
            Runner.rerun12(inputData.getLocal(), numDays, new Dirs(baseDirs, new Util.FileContext("local", "", ""), city), inputData);
        }
    }

    class ExportLocalBlock implements ExecutionBlock {
        boolean completed = false;
        Process process = null;

        public void execute(BaseDirs baseDirs, String city) {
            try {
                String exportLocalExecutable = "/Users/tom/londonoo/scripts/localexport.sh";
                ProcessBuilder builder = new ProcessBuilder(Util.list(exportLocalExecutable));
                sout("exporting local files");
                process = builder.start();
                process.waitFor();
                sout("export completed");
            } catch (Exception e) {
                sout("export failed");
            } finally {
                end();
            }
        }

        synchronized void end() {
            if (completed) return;
            if (process != null) {
                process.destroy();
            }
            completed = true;
        }
    }

    class RerunBlock extends AbstractDaysBlock {
        boolean rerunB;

        RerunBlock(double days, boolean rerunB) {
            super(days);
            this.rerunB = rerunB;
        }

        public void execute(BaseDirs baseDirs, String city) {
            Dirs dirs = new Dirs(baseDirs, city);
            InputData inputData = new InputData(city, baseDirs);
            List<String> references = inputData.getRefs();
            List<String> rerun = Stale.getUnmergedA(inputData.getAFestivals(references), numDays, dirs, inputData);
            Runner.run12(rerun, dirs, inputData);
            if (rerunB) {
                Runner.rerun12(inputData.getBx(references), numDays, dirs, inputData);
            }
        }
    }

    class ManualRerunBlock extends AbstractRefBlock {
        ManualRerunBlock(String ref) {
            super(ref);
        }

        public void execute(BaseDirs baseDirs, String city) {
            Output.WRITE_TO_SCREEN = true;
            WebReader.DISABLE_WRITE_CACHE = true;
            InputData inputData = new InputData(city, baseDirs);
            List<String> refs = inputData.getABFestivals(Util.list(ref));
            Runner.run123(refs, new Dirs(baseDirs, city), inputData);
        }
    }

    class CombinationCheckBlock implements ExecutionBlock {
        public void execute(BaseDirs baseDirs, String city) {
            AtomicBoolean found = new AtomicBoolean(false);
            new FestivalCheckBlock(10d, found).execute(baseDirs, city);
            new BCheckBlock(28d, found).execute(baseDirs, city);
            new CheckBlock(1.5d, found).execute(baseDirs, city);
            if (found.get()) {
                sout("^^^^^^ " + city + " ^^^^^^^");
            }
        }
    }

    class LocalBlock implements ExecutionBlock {
        public void execute(BaseDirs baseDirs, String city) {
            new StaleLocalBlock(2d).execute(baseDirs, city);
            new ExportLocalBlock().execute(baseDirs, city);
        }
    }

    class ApiServerBlock implements ExecutionBlock {
        public void execute(BaseDirs baseDirs, String cityNotUsed) {
            new ApiServer(new Dirs(baseDirs, cityNotUsed)).run();
        }
    }

    class CheckBlock implements ExecutionBlock {
        Double numDaysStale;
        AtomicBoolean found;

        CheckBlock(Double numDaysStale, AtomicBoolean found) {
            this.numDaysStale = numDaysStale;
            this.found = found;
        }

        public void execute(BaseDirs baseDirs, String city) {
            Dirs dirs = new Dirs(baseDirs, city);
            InputData inputData = new InputData(city, baseDirs);
            List<String> refs = inputData.getStatus().getAB(inputData.getRefs());
            List<String> stale = Stale.getUnmergedA(refs, numDaysStale, dirs, inputData);
            if (!Util.empty(stale)) {
                sout("---- STALE A -----");
                sout(stale);
                found.set(true);
            }
        }
    }

    class BCheckBlock extends AbstractDaysFoundBlock {
        BCheckBlock(Double baseNumDaysStale, AtomicBoolean found) {
            super(baseNumDaysStale, found);
        }

        public void execute(BaseDirs baseDirs, String city) {
            InputData inputData = new InputData(city, baseDirs);
            List<String> staleb = Runner.staleB(inputData.getRefs(), numDays, new Dirs(baseDirs, city), inputData);
            if (!Util.empty(staleb)) {
                sout("----- STALE B -----");
                sout(staleb);
                found.set(true);
            }
        }
    }

    class FestivalCheckBlock extends AbstractDaysFoundBlock {
        FestivalCheckBlock(Double numDaysInFuture, AtomicBoolean found) {
            super(numDaysInFuture, found);
        }

        public void execute(BaseDirs baseDirs, String city) {
            InputData inputData = new InputData(city, baseDirs);
            List<String> strings = Runner.executeFestivalstale(inputData.getRefs(), numDays, baseDirs, inputData);
            if (!Util.empty(strings)) {
                found.set(true);
            }
        }
    }

    class OutputBlock implements ExecutionBlock {
        String refOverride;

        OutputBlock(String refOverride) {
            this.refOverride = refOverride;
        }

        public void execute(BaseDirs baseDirs, String city) {
            List<String> refs;
            InputData inputData = new InputData(city, baseDirs);
            if (Util.empty(refOverride)) {
                refs = inputData.ABFestivalLocalLong();
            } else {
                Output.WRITE_TO_SCREEN = true;
                refs = inputData.getABFestivalLocalLong(Util.list(refOverride));
            }
            Output.execute(refs, new Dirs(baseDirs, city), inputData);
        }
    }

    List<String> types = Util.list("theatre", "comedy", "music", "film");

    class ManualAllBlock implements ExecutionBlock {
        String ref;

        ManualAllBlock(String ref) {
            this.ref = ref;
        }

        public void execute(BaseDirs baseDirs, String city) {
            Output.WRITE_TO_SCREEN = true;
            Dirs dirs = new Dirs(baseDirs, city);
            InputData inputData = new InputData(city, baseDirs);
            Runner.run123(Util.list(ref), dirs, inputData);
        }
    }

    class HtmlBlock implements ExecutionBlock {
        public void execute(BaseDirs baseDirs, String city) {
            Html.HtmlContext ctx = new Html.HtmlContext();
            new Html(new Dirs(baseDirs, city), new InputData(city, baseDirs)).execute(ctx);
        }
    }

    class HtmlAreasBlock implements ExecutionBlock {
        public void execute(BaseDirs baseDirs, String cityNotUsed) {
            Dirs noCityDirs = new Dirs(baseDirs, new Util.FileContext(null, null, "areas"), null);
            new Html.HtmlAreasLondon(noCityDirs, null).execute();
        }
    }

    class HtmlWestEndBlock implements ExecutionBlock {
        public void execute(BaseDirs baseDirs, String cityNotUsed) {
            InputData londonInputData = new InputData("london", baseDirs);
            new Html.HtmlWestEndLondon(new Dirs(baseDirs, "london"), londonInputData).execute();
        }
    }

    class HtmlAreasLondonBlock implements ExecutionBlock {
        String areaFilter;

        HtmlAreasLondonBlock(String areaFilter) {
            this.areaFilter = areaFilter;
        }

        public void execute(BaseDirs baseDirs, String cityNotUsed) {
            Dirs dirs = new Dirs(baseDirs, new Util.FileContext(null, null, null), "london");
            List<String> areasToRun = new ArrayList<>(London.areasDisplay());
            areasToRun.add("anywhere");
            InputData inputData = new InputData("london", baseDirs);
            if (!Util.empty(areaFilter)) {
                areasToRun = areasToRun.stream().filter(a -> areaFilter.equals(a)).collect(Collectors.toList());
                if (Util.empty(areasToRun)) {
                    sout("area not exist: " + areaFilter);
                    return;
                }
            } else {
                write(dirs.getBaseDirs().getDynamicConfigDir() + "areaswritten.txt", areasToRun);
            }
            for (String area : areasToRun) {
                Html.HtmlContext ctx = new Html.HtmlContext();
                ctx.area = area;
                new Html(dirs, inputData).execute(ctx);
            }
        }
    }

    class HtmlTypesBlock implements ExecutionBlock {
        public void execute(BaseDirs baseDirs, String city) {
            Dirs dirs = new Dirs(baseDirs, new Util.FileContext(null, null, "types"), city);
            InputData inputData = new InputData(city, baseDirs);
            for (String type : types) {
                Html.HtmlContext ctx = new Html.HtmlContext();
                ctx.type = type;
                new Html(dirs, inputData).execute(ctx);
            }
        }
    }

    class HtmlMonthBlock implements ExecutionBlock {
        public void execute(BaseDirs baseDirs, String city) {
            Dirs dirs = new Dirs(baseDirs, new Util.FileContext(null, null, "month"), city);
            Html.HtmlContext ctx = new Html.HtmlContext();
            ctx.extended = true;
            InputData inputData = new InputData(city, baseDirs);
            new Html(dirs, inputData).execute(ctx);
        }
    }

    class SecondaryBlock implements ExecutionBlock {
        public void execute(BaseDirs baseDirs, String city) {
            InputData inputData = new InputData(city, baseDirs);
            Dirs dirs = new Dirs(baseDirs, city);
            List<String> refs = inputData.getABFestivals(inputData.getRefs());
            Secondary.execute(refs, dirs, inputData);
        }
    }

    class Runner {
        static void rerun12(List<String> refs, double numDaysStale, Dirs dirs, InputData inputData) {
            List<String> rerun = Stale.getUmmerged(refs, numDaysStale, dirs);
            run12(rerun, dirs, inputData);
        }

        static void run12(List<String> refs, Dirs dirs, InputData inputData) {
            Primary.execute(refs, dirs, inputData);
            Secondary.execute(refs, dirs, inputData);
        }

        static void run123(List<String> refs, Dirs dirs, InputData inputData) {
            run12(refs, dirs, inputData);
            Output.execute(refs, dirs, inputData);
        }

        static List<String> staleB(List<String> allRefs, double numDaysStale, Dirs dirs, InputData inputData) {
            Status qualities = inputData.getStatus();
            List<String> refs = qualities.getBx(allRefs);
            List<String> staleb = Stale.getUmmerged(refs, numDaysStale, dirs);
            List<String> refsB2 = qualities.get(staleb, "B2");
            List<String> refsB3 = qualities.get(staleb, "B3");
            List<String> refsB4 = qualities.get(staleb, "B4");
            List<String> refsB6 = qualities.get(staleb, "B6");
            List<String> b2 = new ArrayList<>(Util.intersect(refsB2, staleb));
            List<String> b3 = new ArrayList<>(Util.intersect(refsB3, staleb));
            List<String> b4 = new ArrayList<>(Util.intersect(refsB4, staleb));
            List<String> b6 = new ArrayList<>(Util.intersect(refsB6, staleb));
            List<String> staleRefsB2 = Stale.getUmmerged(b2, numDaysStale * 2, dirs);
            List<String> staleRefsB3 = Stale.getUmmerged(b3, numDaysStale * 3, dirs);
            List<String> staleRefsB4 = Stale.getUmmerged(b4, numDaysStale * 4, dirs);
            List<String> staleRefsB6 = Stale.getUmmerged(b6, numDaysStale * 6, dirs);
            List<String> toOutput = new ArrayList<>();
            for (String ref : staleb) {
                if (refsB6.contains(ref)) {
                    if (staleRefsB6.contains(ref)) {
                        toOutput.add(ref);
                    }
                } else if (refsB4.contains(ref)) {
                    if (staleRefsB4.contains(ref)) {
                        toOutput.add(ref);
                    }
                } else if (refsB3.contains(ref)) {
                    if (staleRefsB3.contains(ref)) {
                        toOutput.add(ref);
                    }
                } else if (refsB2.contains(ref)) {
                    if (staleRefsB2.contains(ref)) {
                        toOutput.add(ref);
                    }
                } else {
                    toOutput.add(ref);
                }
            }
            return toOutput;
        }

        static List<String> executeFestivalstale(List<String> allRefs, double numDaysInFuture, BaseDirs dirs, InputData inputData) {
            List<String> stale = new ArrayList<>();
            List<String> refs = inputData.getStatus().getFestivalStale(allRefs);
            for (String ref : refs) {
                Context ctx = Context.make(ref, inputData);
                Util.StringMutable clean = new Util.StringMutable(ctx.primaryFestivalDate());
                if (Util.empty(clean)) continue;
                clean.string = Util.lowercase(clean.string);
                List<Calendar.Date> dates = DateMatcher.match(clean, ctx.lang, ctx.date);
                if (Util.empty(dates)) continue;
                for (Calendar.Date date : dates) {
                    if (Calendar.Date.daysBetween(date, Calendar.currentDate()) < numDaysInFuture) {
                        stale.add(ref);
                        break;
                    }
                }
            }
            if (!Util.empty(stale)) {
                sout("---- STALE FESTIVALS");
                sout(stale);
                return stale;
            }
            return null;
        }
    }

    class Stale {
        static List<String> getUnmergedA(List<String> refs, double numDaysStale, Dirs dirs, InputData inputData) {
            List<String> stale = getUmmerged(refs, numDaysStale, dirs);
            List<String> toKeep = inputData.getAFestivals(refs);
            stale.removeIf(s -> !toKeep.contains(s));
            return stale;
        }

        static List<String> getUmmerged(List<String> refs, double numDaysStale, Dirs dirs) {
            return getStaleFiles(refs, dirs.getUnmergedDir(), numDaysStale);
        }

        static List<String> getStaleFiles(List<String> refs, String location, double numDaysStale) {
            List<String> stale = list();
            List<String> created = list();
            File[] files = new File(location).listFiles();
            if (Util.empty(files)) return refs;
            long millis = System.currentTimeMillis();
            double numMillisStale = 1000 * 60 * 60 * 24 * numDaysStale;
            for (File file : files) {
                if (millis - file.lastModified() < numMillisStale) {
                    created.add(file.getName().replaceAll("\\.txt", ""));
                }
            }
            for (String ref : refs) {
                if (!created.contains(ref)) {
                    stale.add(ref);
                }
            }
            return stale;
        }
    }
}
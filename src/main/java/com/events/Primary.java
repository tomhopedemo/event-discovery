package com.events;

import java.util.List;

import static com.events.Page.generateUrls;
import static com.events.Util.sout;

public class Primary {
    public static final Util.MapList<String, Event> REF_EVENTS = new Util.MapList<>();

    static void execute(List<String> refs, Dirs dirs, InputData inputData) {
        execute(refs, null, dirs, inputData);
    }

    static void execute(List<String> refs, String additionalCtxNotes, Dirs dirs, InputData inputData) {
        sout("... Program D - " + refs.size() + " references to process...");
        for (String ref : refs) {
            try {
                sout("PROGRAM D: " + ref);
                Context ctx = Context.make(ref, additionalCtxNotes, inputData);
                ctx.setUrls(generateUrls(ctx, inputData));
                PrimaryWorker.make(ctx, dirs);
                List<Event> events = REF_EVENTS.get(ref);
                sout(Util.size(events) + " events generated for " + ref);
                if (Util.empty(events)) continue;
                Util.write(dirs.getUnmergedDir() + ref + ".txt", Util.string(Event.saveFormat(events), Shared.DELIMITER_RECORD));
            } catch (Exception e) {
                e.printStackTrace();
                sout("PROGRAM D FAILURE REFERENCE: " + ref);
            } finally {
                REF_EVENTS.mapList.remove(ref);
                WebReader.clearMemoryCaches();
            }
        }
        sout("PROGRAM D COMPLETE");
    }
}
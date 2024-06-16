package com.arcta.events;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Optional;

import static com.arcta.events.Util.list;

class Iframe {
    static String make(String link, Document doc, Context ctx) {
        Elements iframes = doc.getElementsByTag("iframe");
        if (Util.empty(iframes)) return null;
        Element element = iframes.get(0);
        if (!Util.empty(ctx.iframeClass())) {
            Optional<Element> first = iframes.stream().filter(i -> i.hasClass(ctx.iframeClass())).findFirst();
            if (first.isPresent()) {
                element = first.get();
            } else {
                return null;
            }
        }
        String src = element.attr("src");
        if (Util.empty(src)) {
            String dataReplace = element.attr("data-replace");
            if (Util.empty(dataReplace)) {
                return null;
            } else {
                src = dataReplace;
            }
        }
        if (Util.contains(src, list("googletagmanager", "recaptcha"))) return null;
        String iframeUrl = WebReader.UriExtension.toFullUrl(link, src);
        if (Util.empty(iframeUrl)) return null;
        if (Util.contains(iframeUrl, list("about:blank", "facebook.com/plugins", "youtube.com/embed"))) return null;
        return iframeUrl;
    }
}
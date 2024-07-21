package com.events;

import com.events.date.DateMatcher;
import org.jsoup.nodes.Element;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.events.Util.list;

class Link {
    static final List<String> BAD_HREF_ENDINGS = list("jpg", ".ics", ".mp3", "=ical");
    static final List<String> BAD_HREF = list("paypal.com", "twitter.com", "javascript", "google.co.uk/maps", "google.com/maps", "maps.google.com", "youtube.com", "youtu.be/", "calendar.google.com", "google.com/calendar", "admin.designmynight", "linkedin.com", "gmail.com", "addtocalendar.com", "facebook.com/sharer", "fb-messenger:/", "mailto:", "/email-protection", "whatsapp:", "plus.google.com", "www.instagram.com", "wikipedia.org", "/news/", "/past_exhibitions/", "/past_exhibitions.", "/ics.php", "_ics_download", "/vcs.php");
    static final List<String> POOR_HREF = list("facebook.com", "/contact-us/", "/membership/", "/category/", "/event_category/", "search.aspx?", "designmynight.com", "tel:0");
    static final List<String> GOOD_TEXT = list("more info", "tell me more", "read more", "show details");
    static final List<String> POOR_TEXT = list("book now", "buy tickets", "book tickets", "book ticket", "add to calendar", "exhibitor info", "find tickets");
    static final List<String> POOR_TEXT_EQUALS = list("performance", "film", "films", "event", "opening", "study room event", "events", "lada screens", "book");
    static final List<String> POOR_TEXT_BEGINNINGS = list("book ");

    static Util.Multi<String, String> make(String url, Collection<Element> parentElements, Element classElement, Context ctx) {
        String badHrefSpecific = ctx.linkBadHref();
        String poorHrefSpecific = ctx.linkPoorHref();
        String goodHrefSpecific = ctx.linkGoodHref();
        String goodTextSpecific = ctx.linkGoodText();
        Map<String, String> hrefText = WebUtils.hrefText(classElement);
        if (!Util.empty(hrefText)) {
            hrefText.entrySet().removeIf(e -> Util.endsWith(e.getKey(), BAD_HREF_ENDINGS));
            for (String href : hrefText.keySet()) {            // -------------- GOOD ----------------
                String text = hrefText.get(href);
                if (!Util.empty(goodHrefSpecific)) {
                    if (href.contains(goodHrefSpecific)) {
                        return new Util.Multi<>(WebReader.UriExtension.toFullUrl(url, href), text);
                    }
                }
                if (Util.empty(text)) continue;
                if (Util.contains(text.toLowerCase(), GOOD_TEXT)) {
                    return new Util.Multi<>(WebReader.UriExtension.toFullUrl(url, href), text);
                }
                if (Util.contains(text.toLowerCase(), goodTextSpecific)) {
                    return new Util.Multi<>(WebReader.UriExtension.toFullUrl(url, href), text);
                }
            }
            List<String> lowPriorityHrefs = list();             // ------------- IDENTIFY LOW PRIORITY URLS FOR NEXT SECTION --------------
            for (String href : hrefText.keySet()) {
                String text = hrefText.get(href);
                if (Util.contains(href, badHrefSpecific)) continue;
                if (Util.startsWith(text.toLowerCase(), POOR_TEXT_BEGINNINGS)) {
                    lowPriorityHrefs.add(href);
                } else if (Util.contains(text.toLowerCase(), POOR_TEXT)) {
                    lowPriorityHrefs.add(href);
                } else if (POOR_TEXT_EQUALS.contains(text.toLowerCase())) {
                    lowPriorityHrefs.add(href);
                } else if (ctx.ref.equals(text.toLowerCase())) {
                    lowPriorityHrefs.add(href);
                } else if (Util.contains(href.toLowerCase(), POOR_HREF)) {
                    lowPriorityHrefs.add(href);
                } else if (Util.contains(href.toLowerCase(), poorHrefSpecific)) {
                    lowPriorityHrefs.add(href);
                } else if (Util.safeNull(text).length() < 15 && Util.safeNull(text).length() > 4) {
                    Util.StringMutable clean = new Util.StringMutable(Util.lowercase(text));
                    DateMatcher.match(clean, ctx.lang, ctx.date);
                    if (Util.empty(clean.string)) {
                        lowPriorityHrefs.add(href);
                    }
                }
            }
            for (String href : hrefText.keySet()) {            // ------------- NOT POOR/BAD -----------------
                if (Util.endsWith(href, BAD_HREF_ENDINGS)) continue;
                if (Util.contains(href, BAD_HREF)) continue;
                if (Util.contains(href, badHrefSpecific)) continue;
                if (href.equals("#") || href.equals(url + "/#") || href.equals(url + "#")) continue;
                if (lowPriorityHrefs.contains(href)) continue;
                return new Util.Multi<>(WebReader.UriExtension.toFullUrl(url, href), hrefText.get(href));
            }
            for (String href : lowPriorityHrefs) {             // --------------- NOT BAD --------------------
                if (Util.endsWith(href, BAD_HREF_ENDINGS)) continue;
                if (Util.contains(href, BAD_HREF)) continue;
                if (href.equals("#") || href.equals(url + "/#") || href.equals(url + "#")) continue;
                return new Util.Multi<>(WebReader.UriExtension.toFullUrl(url, href), hrefText.get(href));
            }
        }
        if (parentElements == null)
            return null;         // -----LOOK FOR INDICATIVE URL IN PARENT SO LONG AS THE PARENT DOES NOT ALSO HAVE OTHER CHILDREN WITH // ----------------------------------THE SAME CLASS NAME -----------------------------------------
        if (!Util.intersection(classElement.siblingElements(), parentElements)) {
            Map<String, String> hrefTextParent = WebUtils.hrefText(classElement.parent());
            if (!Util.empty(hrefTextParent)) {
                String hrefParent = hrefTextParent.keySet().iterator().next();
                return new Util.Multi<>(WebReader.UriExtension.toFullUrl(url, hrefParent), hrefTextParent.get(hrefParent));
            }
        }
        return null;
    }
}
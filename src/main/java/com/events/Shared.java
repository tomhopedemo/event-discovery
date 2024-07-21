package com.events;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

class Shared {
    static final String DELIMITER_INLINE = "#ARCTA#";
    static final String DELIMITER_RECORD = "\n#RECORD#";
    static final String DELIMITER_RECORD_READABLE = DELIMITER_RECORD + "\n";

    static Util.Multi3<String, String, String> getStructuralParameters(Context ctx) {
        String identifier = null;
        String identifierType = null;
        if (ctx.primaryTag() != null) {
            identifier = ctx.primaryTag();
            identifierType = "TAG";
        } else if (ctx.primaryListTag() != null) {
            identifier = ctx.primaryListTag();
            identifierType = "TAG";
        } else if (ctx.primaryId() != null) {
            identifier = ctx.primaryId();
            identifierType = "ID";
        } else if (ctx.primaryListId() != null) {
            identifier = ctx.primaryListId();
            identifierType = "ID";
        } else if (ctx.primaryClassholder() != null) {
            identifier = ctx.primaryClassholder();
            identifierType = "CLASSHOLD";
        } else if (ctx.primaryClassholdId() != null) {
            identifier = ctx.primaryClassholdId();
            identifierType = "CLASSHOLDID";
        } else if (ctx.primaryClass() != null) {
            identifier = ctx.primaryClass();
            identifierType = "CLASS";
        } else if (ctx.primaryListClass() != null) {
            identifier = ctx.primaryListClass();
            identifierType = "CLASS";
        }
        return new Util.Multi3<>(identifier, getMethod(ctx), identifierType);
    }

    static String getMethod(Context ctx) {
        String method;
        if (ctx.primaryList()) {
            method = "L";
        } else if (ctx.primaryPartial()) {
            method = "P";
        } else if (ctx.primaryTable()) {
            method = "T";
        } else {
            method = "C";
        }
        return method;
    }

    static List<Element> generateElements(Context ctx, String identifier, String identifierType, Element doc) {
        List<Element> elements;
        doc = restrictDoc(ctx, doc);
        if ("ID".equals(identifierType)) {
            elements = getElementsForId(identifier, doc);
        } else if ("TAG".equals(identifierType)) {
            elements = doc.getElementsByTag(identifier);
        } else if ("CLASSHOLD".equals(identifierType)) {
            elements = getElementsClasshold(identifier, doc);
        } else if ("CLASSHOLDID".equals(identifierType)) {
            elements = Util.list();
            elements.addAll(doc.getElementById(identifier).children());
        } else {
            elements = getElementsByClass(ctx, identifier, doc);
        }
        applyElementFilters(ctx, elements);
        return elements;
    }

    static void applyElementFilters(Context ctx, List<Element> elements) {
        Util.Multi<String, String> keyValue = ctx.primaryRequiredAttribute();
        if (!Util.empty(elements) && keyValue != null) {
            elements.removeIf(e -> !e.attr(keyValue.a).equals(keyValue.b));
        }
        String except = ctx.primaryExceptClass();
        if (!Util.empty(elements) && !Util.empty(except)) {
            elements.removeIf(e -> Util.safeNull(e.classNames()).contains(except));
        }
    }

    static List<Element> getElementsByClass(Context ctx, String identifier, Element doc) {
        List<Element> elements;
        if (!Util.empty(ctx.parentId())) doc = doc.getElementById(ctx.parentId());
        if (doc != null) {
            elements = Jutil.getElementsByClassFallback(doc, identifier);
        } else {
            elements = Util.list();
        }
        return elements;
    }

    static List<Element> getElementsClasshold(String identifier, Element doc) {
        List<Element> elements = Util.list();
        for (Element parent : doc.getElementsByClass(identifier)) {
            Util.addAll(elements, parent.children());
        }
        return elements;
    }

    static List<Element> getElementsForId(String identifier, Element doc) {
        return Util.list(doc.getElementById(identifier));
    }

    static Element restrictDoc(Context ctx, Element doc) {
        if (!Util.empty(ctx.primaryIdRestriction())) {
            Element element = doc.getElementById(ctx.primaryIdRestriction());
            if (element != null) doc = element;
        }
        if (!Util.empty(ctx.primaryClassRestriction())) {
            Element element = Util.get(doc.getElementsByClass(ctx.primaryClassRestriction()), 0);
            if (element != null) doc = element;
        }
        if (!Util.empty(ctx.primaryClassRemoval())) {
            Elements elementsByClass = doc.getElementsByClass(ctx.primaryClassRemoval());
            for (Element elt : elementsByClass) {
                elt.remove();
            }
        }
        return doc;
    }
}
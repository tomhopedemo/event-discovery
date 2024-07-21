package com.events;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.events.Util.list;
import static com.events.Util.set;

class Jutil {
    static Element childElementByTag(Element parent, String tag) {
        if (parent == null || Util.empty(tag)) return null;
        List<Element> elements = childElementsByTag(parent, tag);
        if (Util.empty(elements)) return null;
        return elements.get(0);
    }

    static List<Element> childElementsByTag(Element parent, String tag) {
        if (tag == null || parent == null) return null;
        List<Element> childrenTag = list();
        Elements children = parent.children();
        for (Element child : children) {
            if (tag.equals(child.tagName())) childrenTag.add(child);
        }
        return childrenTag;
    }

    static boolean hasClass(Element element, String className) {
        List<String> list = classesRecursive(element.children(), list());
        return list.contains(className);
    }

    static List<String> classesRecursive(Elements elements, List<String> foundClasses) {
        if (Util.empty(elements)) return foundClasses;
        for (Element element : elements) {
            if (element == null) continue;
            Set<String> classNames = element.classNames();
            if (!Util.empty(classNames)) {
                foundClasses.addAll(classNames);
            }
            classesRecursive(element.children(), foundClasses);
        }
        return foundClasses;
    }

    static List<Element> getElementsByClassFallback(Element doc, String identifier) {
        List<Element> elements = doc.getElementsByClass(identifier);
        if (!Util.empty(elements)) return elements;
        if (Util.empty(identifier)) return elements;
        if (identifier.contains("-")) {
            elements = doc.getElementsByClass(identifier.replaceAll("\\-", "_"));
        } else if (identifier.contains("_")) {
            elements = doc.getElementsByClass(identifier.replaceAll("_", "-"));
        }
        return elements;
    }

    static final List<String> HEADING_TAGS = list("h1", "h2", "h3", "h4");
    static final List<String> MAJOR_GROUPINGS = list("h1", "h2", "h3", "h4", "h5", "h6", "p");
    static final List<String> TITLE_CLASSES = list("title", "name", "summary-title", "event-type");
    static final String TITLE_CLASS_ENDING = "-tags";

    static Util.MultiList<Integer, Element> indexElementByTag(Element document, String classIdentifier) {
        Elements children = document.children();
        Util.MultiList<Integer, Element> indexElements = new Util.MultiList<>();
        indexElementByTagRecursive(children, classIdentifier, indexElements, new Util.IntegerMutable());
        return indexElements;
    }

    static void indexElementByTagRecursive(List<Element> elements, String tagIdentifier, Util.MultiList<Integer, Element> indexElements, Util.IntegerMutable currentIndex) {
        for (Element element : elements) {
            if (tagIdentifier.equals(element.tagName())) {
                indexElements.add(new Util.Multi(currentIndex.integer, element));
            }
            currentIndex.integer = currentIndex.integer + 1;
            indexElementByTagRecursive(element.children(), tagIdentifier, indexElements, currentIndex);
        }
    }

    static Util.MultiList<Integer, Element> indexElement(Element document, String classIdentifier) {
        Elements children = document.children();
        Util.MultiList<Integer, Element> indexElements = new Util.MultiList<>();
        indexElementRecursive(children, classIdentifier, indexElements, new Util.IntegerMutable());
        return indexElements;
    }

    static void indexElementRecursive(List<Element> elements, String classIdentifier, Util.MultiList<Integer, Element> indexElements, Util.IntegerMutable currentIndex) {
        for (Element element : elements) {
            if (element.classNames().contains(classIdentifier)) {
                indexElements.add(new Util.Multi(currentIndex.integer, element));
            }
            currentIndex.integer = currentIndex.integer + 1;
            indexElementRecursive(element.children(), classIdentifier, indexElements, currentIndex);
        }
    }

    static Util.Multi<String, Util.MultiList<Integer, Element>> text(List<Node> elements, List<String> excludedTags, List<String> excludedClasses, Util.Multi<String, String> excludedAttributes, List<String> restrictedToTags, List<String> restrictedToClasses, List<String> restrictedToIds, Util.Multi<String, String> restrictedToAttribute, String gapClass, String gapTag) {
        Util.MultiList<String, Element> stringElements = textFinderRecursive(elements, new Util.MultiList<>(), excludedTags, excludedClasses, excludedAttributes, restrictedToTags, restrictedToClasses, restrictedToIds, restrictedToAttribute, gapClass, gapTag);
        StringBuilder sb = new StringBuilder();
        Util.MultiList<Integer, Element> indexElement = new Util.MultiList<>();
        for (Util.Multi<String, Element> stringElement : stringElements.underlying) {
            String string = stringElement.a;
            if (string.length() == 0 && stringElement.b != null) {
                indexElement.add(new Util.Multi<>(sb.length(), stringElement.b));
            } else {
                indexElement.add(new Util.Multi<>(sb.length(), stringElement.b));
                sb.append(string);
                sb.append(" ");
            }
        }
        String string = (sb.length() == 0) ? "" : sb.substring(0, sb.length() - 1);
        return new Util.Multi<>(string, indexElement);
    }

    static Util.Multi<String, Util.MultiList<Integer, Element>> text(Element element, List<String> excludedTags, List<String> excludedClasses, Util.Multi<String, String> exclusionAttribute, List<String> restrictedToTags, List<String> restrictedToClasses, List<String> restrictedToIds, Util.Multi<String, String> restrictedToAttribute, String gapClass, String gapTag) {
        if (element == null) return null;
        List<Node> childNodes = element.childNodes();
        if (Util.intersection(element.classNames(), excludedClasses)) return null;
        if (childNodes == null || childNodes.size() == 0) {
            HashMap<Integer, Element> map = new HashMap<>();
            map.put(0, element);
            return new Util.Multi(element.text(), map);
        }
        return text(childNodes, excludedTags, excludedClasses, exclusionAttribute, restrictedToTags, restrictedToClasses, restrictedToIds, restrictedToAttribute, gapClass, gapTag);
    }

    static Util.MultiList<String, Element> textFinderRecursive(List<Node> elements, Util.MultiList<String, Element> list, List<String> excludedTags, List<String> excludedClasses, Util.Multi<String, String> excludeAttribute, List<String> restrictedToTags, List<String> restrictedToClasses, List<String> restrictedToIds, Util.Multi<String, String> restrictedToAttribute, String gapClass, String gapTag) {
        for (Node node : elements) {
            if (node instanceof Element) {
                Element element = (Element) node; //Indicate the start of a Header Element
                if (HEADING_TAGS.contains(element.tagName())) {
                    list.add(new Util.Multi<>("\u2021", element));
                }
                if (element.tagName().equals("a") && !Util.empty(element.attributes().get("href"))) {
                    list.add(new Util.Multi<>("", element));
                }
                if (element.tagName().equals("br")) list.add(new Util.Multi<>("\u2021", null));
                if (excludedTags != null && excludedTags.contains((element).tagName())) continue;
                if (excludedClasses != null && Util.intersection(Util.safeNull(element.classNames()), excludedClasses)) continue;
                if (excludeAttribute != null && element.attr(excludeAttribute.a).equals(excludeAttribute.b)) continue;
                if ("element-invisible".equals(element.className())) continue;
                if (element.childNodes() == null || element.childNodes().size() == 0) {
                    String ownText = element.ownText();
                    if (ownText != null && !"".equals(ownText.trim())) {
                        if (!Util.empty(restrictedToClasses)) {
                            if (!Util.intersection(allParentClasses(element), restrictedToClasses)) continue;
                        }
                        if (!Util.empty(restrictedToTags)) {
                            if (!Util.intersection(allParentTags(element), restrictedToTags)) continue;
                        }
                        if (!Util.empty(restrictedToIds)) {
                            if (!Util.intersection(allParentIds(element), restrictedToIds)) continue;
                        }
                        if (!Util.empty(restrictedToAttribute)) {
                            if (!allParentAttr(element, restrictedToAttribute.a).contains(restrictedToAttribute.b)) continue;
                        }
                        list.add(new Util.Multi<>(ownText, null));
                    }
                } else {
                    textFinderRecursive(element.childNodes(), list, excludedTags, excludedClasses, excludeAttribute, restrictedToTags, restrictedToClasses, restrictedToIds, restrictedToAttribute, gapClass, gapTag);
                } //Indicate the end of a Header Element - insert additional text
                if (HEADING_TAGS.contains(element.tagName()) || Util.intersection(Util.lowercase(element.classNames()), TITLE_CLASSES)) {
                    list.add(new Util.Multi<>("\u2021", element));
                } else if (Util.endswithReverse(TITLE_CLASS_ENDING, Util.lowercase(element.classNames()))) {
                    list.add(new Util.Multi<>("\u2021", element)); //indicate the end of another type of major grouping element -
                } else if (MAJOR_GROUPINGS.contains(element.tagName())) {
                    list.add(new Util.Multi<>("\u2021", null));
                } else if (gapClass != null && Util.safeNull(element.classNames()).contains(gapClass)) {
                    list.add(new Util.Multi<>("\u2021", null));
                } else if (gapTag != null && Util.safeNull(element.tagName()).equals(gapTag)) {
                    list.add(new Util.Multi<>("\u2021", null));
                } //indicate end of a href
                if (element.tagName().equals("a") && !Util.empty(element.attributes().get("href"))) {
                    list.add(new Util.Multi<>("", element));
                }
            } else if (node instanceof TextNode) {
                if (!Util.empty(restrictedToClasses)) {
                    if (!Util.intersection(allParentClassesNode(node), restrictedToClasses)) continue;
                }
                if (!Util.empty(restrictedToTags)) {
                    if (!Util.intersection(allParentTagsNode(node), restrictedToTags)) continue;
                }
                if (!Util.empty(restrictedToIds)) {
                    if (!Util.intersection(allParentIdsNode(node), restrictedToIds)) continue;
                }
                String text = ((TextNode) node).text();
                if (text != null && !"".equals(text.trim())) {
                    list.add(new Util.Multi<>(text, null));
                }
            }
        }
        return list;
    }

    static Set<String> allParentClassesNode(Node node) {
        Set<String> classNames = set();
        Element parentElement = (Element) node.parent();
        Util.addAll(classNames, parentElement.classNames());
        for (Element parent : parentElement.parents()) {
            Util.addAll(classNames, parent.classNames());
        }
        return classNames;
    }

    static Set<String> allParentTagsNode(Node node) {
        Set<String> tagNames = set();
        Element parentElement = (Element) node.parent();
        Util.addIfNotEmpty(tagNames, parentElement.tagName());
        for (Element parent : parentElement.parents()) {
            Util.addIfNotEmpty(tagNames, parent.tagName());
        }
        return tagNames;
    }

    static Set<String> allParentClasses(Element element) {
        Set<String> classNames = set();
        Util.addAll(classNames, element.classNames());
        for (Element parent : element.parents()) {
            Util.addAll(classNames, parent.classNames());
        }
        return classNames;
    }

    static Set<String> allParentIdsNode(Node node) {
        Set<String> idNames = set();
        Element parentElement = (Element) node.parent();
        Util.addIfNotEmpty(idNames, parentElement.id());
        for (Element parent : parentElement.parents()) {
            Util.addIfNotEmpty(idNames, parent.id());
        }
        return idNames;
    }

    static Set<String> allParentIds(Element element) {
        Set<String> idNames = set();
        Util.addIfNotEmpty(idNames, element.id());
        for (Element parent : element.parents()) {
            Util.addIfNotEmpty(idNames, parent.id());
        }
        return idNames;
    }

    static Set<String> allParentTags(Element element) {
        Set<String> tagNames = set();
        Util.addIfNotEmpty(tagNames, element.tagName());
        for (Element parent : element.parents()) {
            Util.addIfNotEmpty(tagNames, parent.tagName());
        }
        return tagNames;
    }

    static Set<String> allParentAttr(Element element, String attr) {
        Set<String> attrValues = set();
        Util.addIfNotEmpty(attrValues, element.attr(attr));
        for (Element parent : element.parents()) {
            Util.addIfNotEmpty(attrValues, parent.attr(attr));
        }
        return attrValues;
    }

    static String textOnly(Element element) {
        if (element == null) return null;
        Util.Multi<String, Util.MultiList<Integer, Element>> text = text(element);
        return Util.safeA(text);
    }

    static Util.Multi<String, Util.MultiList<Integer, Element>> text(Element element) {
        return text(element, null, null, null, null, null, null, null, null, null);
    }
}
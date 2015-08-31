package com.bc.fiduceo.reader;

import org.jdom2.Element;

import java.io.IOException;
import java.util.StringTokenizer;


class EosCoreMetaParser {

     Element parseFromString(String text) throws IOException {

        Element rootElem = new Element("odl");
        Element current = rootElem;
        StringTokenizer lineFinder = new StringTokenizer(text, "\n");

        while (lineFinder.hasMoreTokens()) {
            String line = lineFinder.nextToken();
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("GROUP")) {
                if (line.contains("GROUPTYPE")) {
                    continue;
                }
                current = this.startGroup(current, line);
            } else if (line.startsWith("OBJECT")) {
                current = this.startObject(current, line);
            } else if (line.startsWith("END_OBJECT")) {
                this.endObject(current, line);
                current = current.getParentElement();
            } else if (line.startsWith("END_GROUP")) {
                this.endGroup(current, line);
                current = current.getParentElement();
            } else {
                this.addField(current, line);
            }
        }
        return rootElem;
    }

    private Element startGroup(Element parent, String line) throws IOException {
        StringTokenizer stoke = new StringTokenizer(line, "=");
        String toke = stoke.nextToken();
        assert toke.equals("GROUP");
        String name = stoke.nextToken();
        Element group = new Element(name);
        parent.addContent(group);
        return group;
    }

    private void endGroup(Element current, String line) throws IOException {
        StringTokenizer stoke = new StringTokenizer(line, "=");
        String toke = stoke.nextToken();
        assert toke.equals("END_GROUP");
        String name = stoke.nextToken();
        assert name.equals(current.getName());
    }

    private Element startObject(Element parent, String line) throws IOException {
        StringTokenizer stoke = new StringTokenizer(line, "=");
        String toke = stoke.nextToken();
        assert toke.equals("OBJECT");
        String name = stoke.nextToken();
        Element obj = new Element(name);
        parent.addContent(obj);
        return obj;
    }

    private void endObject(Element current, String line) throws IOException {
        StringTokenizer stoke = new StringTokenizer(line, "=");
        String toke = stoke.nextToken();
        assert toke.equals("END_OBJECT");
        String name = stoke.nextToken();
        assert name.equals(current.getName()) : name + " !+ " + current.getName();
    }

    private void addField(Element parent, String line) throws IOException {
        StringTokenizer stoke = new StringTokenizer(line, "=");
        String name = stoke.nextToken();
        if (stoke.hasMoreTokens()) {
            Element field = new Element(name);
            parent.addContent(field);
            String value = stoke.nextToken();
            if (value.startsWith("(")) {
                this.parseValueCollection(field, value);
                return;
            }
            value = this.stripQuotes(value);
            field.addContent(value);
        }

    }

    private void parseValueCollection(Element field, String value) {
        if (value.startsWith("(")) {
            value = value.substring(1);
        }

        if (value.endsWith(")")) {
            value = value.substring(0, value.length() - 1);
        }

        StringTokenizer stoke = new StringTokenizer(value, "\",");

        while (stoke.hasMoreTokens()) {
            field.addContent((new Element("value")).addContent(this.stripQuotes(stoke.nextToken())));
        }

    }

    private String stripQuotes(String name) {
        if (name.startsWith("\"")) {
            name = name.substring(1);
        }

        if (name.endsWith("\"")) {
            name = name.substring(0, name.length() - 1);
        }

        return name;
    }

}


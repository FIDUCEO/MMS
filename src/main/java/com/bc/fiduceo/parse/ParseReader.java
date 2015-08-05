package com.bc.fiduceo.parse;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.util.IO;

import java.io.IOException;
import java.io.OutputStream;
import java.util.StringTokenizer;

/**
 * Created by autobox on 8/3/2015.
 */
public class ParseReader {
    private static Logger log = LoggerFactory.getLogger(ParseReader.class);
    private Document doc;
    private boolean debug = false;
    private boolean showRaw = false;
    private boolean show = false;

    public ParseReader() {
    }

    void showDoc(OutputStream out) {
        XMLOutputter fmt = new XMLOutputter(Format.getPrettyFormat());
        try {
            fmt.output(this.doc, out);
        } catch (IOException var4) {
            var4.printStackTrace();
        }
    }

    void parseFile(String filename) throws IOException {
        String text = new String(IO.readFileToByteArray(filename));
        Element element = this.parseFromString(text);
    }

    public Element parseFromString(String text) throws IOException {
        if(this.showRaw) {
            System.out.println("Raw ODL=\n" + text);
        }

        Element rootElem = new Element("odl");
        this.doc = new Document(rootElem);
        Element current = rootElem;
        StringTokenizer lineFinder = new StringTokenizer(text, "\n");

        while(lineFinder.hasMoreTokens()) {
            String line = lineFinder.nextToken();
            line = line.trim();
            if(line.isEmpty()){
                continue;
            }
            if(line.startsWith("GROUP")) {
                if (line.contains("GROUPTYPE")) {
                    continue;
                }
                current = this.startGroup(current, line);
            } else if(line.startsWith("OBJECT")) {
                current = this.startObject(current, line);
            } else if(line.startsWith("END_OBJECT")) {
                this.endObject(current, line);
                current = current.getParentElement();
            } else if(line.startsWith("END_GROUP")) {
                this.endGroup(current, line);
                current = current.getParentElement();
            } else {
                this.addField(current, line);
            }
        }

        if(this.show) {
            this.showDoc(System.out);
        }
        return rootElem;
    }

    Element startGroup(Element parent, String line) throws IOException {
        StringTokenizer stoke = new StringTokenizer(line, "=");
        String toke = stoke.nextToken();

        assert toke.equals("GROUP");

        String name = stoke.nextToken();
        Element group = new Element(name);
        parent.addContent(group);
        return group;
    }

    void endGroup(Element current, String line) throws IOException {
        StringTokenizer stoke = new StringTokenizer(line, "=");
        String toke = stoke.nextToken();

        assert toke.equals("END_GROUP");

        String name = stoke.nextToken();
        if(this.debug) {
            System.out.println(line + " -> " + current);
        }

        assert name.equals(current.getName());

    }

    Element startObject(Element parent, String line) throws IOException {
        StringTokenizer stoke = new StringTokenizer(line, "=");
        String toke = stoke.nextToken();

        assert toke.equals("OBJECT");

        String name = stoke.nextToken();
        Element obj = new Element(name);
        parent.addContent(obj);
        return obj;
    }

    void endObject(Element current, String line) throws IOException {
        StringTokenizer stoke = new StringTokenizer(line, "=");
        String toke = stoke.nextToken();

        assert toke.equals("END_OBJECT");

        String name = stoke.nextToken();
        if(this.debug) {
            System.out.println(line + " -> " + current);
        }

        assert name.equals(current.getName()) : name + " !+ " + current.getName();

    }

    void addField(Element parent, String line) throws IOException {
        StringTokenizer stoke = new StringTokenizer(line, "=");
        String name = stoke.nextToken();
        if(stoke.hasMoreTokens()) {
            Element field = new Element(name);
            parent.addContent(field);
            String value = stoke.nextToken();
            if(value.startsWith("(")) {
                this.parseValueCollection(field, value);
                return;
            }

            value = this.stripQuotes(value);
            field.addContent(value);
        }

    }

    void parseValueCollection(Element field, String value) {
        if(value.startsWith("(")) {
            value = value.substring(1);
        }

        if(value.endsWith(")")) {
            value = value.substring(0, value.length() - 1);
        }

        StringTokenizer stoke = new StringTokenizer(value, "\",");

        while(stoke.hasMoreTokens()) {
            field.addContent((new Element("value")).addContent(this.stripQuotes(stoke.nextToken())));
        }

    }

    String stripQuotes(String name) {
        if(name.startsWith("\"")) {
            name = name.substring(1);
        }

        if(name.endsWith("\"")) {
            name = name.substring(0, name.length() - 1);
        }

        return name;
    }

    public static void main(String[] args) throws IOException {
        ParseReader p = new ParseReader();
        p.parseFile("c:/tmp/struct.txt");
    }
}


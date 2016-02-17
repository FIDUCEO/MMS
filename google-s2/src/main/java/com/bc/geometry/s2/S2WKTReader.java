/*
 * Copyright (C) 2015 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.geometry.s2;

import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Loop;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2Polyline;
import com.google.common.geometry.S2Region;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class S2WKTReader {
    private static final String EMPTY = "EMPTY";
    private static final String COMMA = ",";
    private static final String L_PAREN = "(";
    private static final String R_PAREN = ")";
    private static final String NAN_SYMBOL = "NaN";

    private StreamTokenizer tokenizer;


    /**
     * Reads a Well-Known Text representation of a {@link S2Region}
     * from a {@link String}.
     *
     * @param wellKnownText one or more <Geometry Tagged Text>strings (see the OpenGIS
     *                      Simple Features Specification) separated by whitespace
     * @return a <code>S2Region</code> specified by <code>wellKnownText</code>
     * @throws IllegalArgumentException if a parsing problem occurs
     */
    public Object read(String wellKnownText) throws IllegalArgumentException {
        try (StringReader reader = new StringReader(wellKnownText)) {
            return read(reader);
        }
    }

    /**
     * Reads a Well-Known Text representation of a {@link S2Region}
     * from a {@link Reader}.
     *
     * @param reader a Reader which will return a <Geometry Tagged Text>
     *               string (see the OpenGIS Simple Features Specification)
     * @return a <code>S2Region</code> read from <code>reader</code>
     * @throws IllegalArgumentException if a parsing problem occurs
     */
    public Object read(Reader reader) throws IllegalArgumentException {
        tokenizer = new StreamTokenizer(reader);
        // set tokenizer to NOT parse numbers
        tokenizer.resetSyntax();
        tokenizer.wordChars('a', 'z');
        tokenizer.wordChars('A', 'Z');
        tokenizer.wordChars(128 + 32, 255);
        tokenizer.wordChars('0', '9');
        tokenizer.wordChars('-', '-');
        tokenizer.wordChars('+', '+');
        tokenizer.wordChars('.', '.');
        tokenizer.whitespaceChars(0, ' ');
        tokenizer.commentChar('#');

        try {
            return readGeometryTaggedText();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns the next array of <code>Coordinate</code>s in the stream.
     *
     * @return the next array of <code>Coordinate</code>s in the
     * stream, or an empty array if EMPTY is the next element returned by
     * the stream.
     * @throws IOException    if an I/O error occurs
     * @throws ParseException if an unexpected token was encountered
     */
    private List<S2Point> getPoints() throws IOException, IllegalArgumentException {
        String nextToken = getNextEmptyOrOpener();
        if (EMPTY.equals(nextToken)) {
            return Collections.emptyList();
        }
        ArrayList<S2Point> points = new ArrayList<>();
        points.add(getPreciseCoordinate());
        nextToken = getNextCloserOrComma();
        while (COMMA.equals(nextToken)) {
            points.add(getPreciseCoordinate());
            nextToken = getNextCloserOrComma();
        }
        return points;
    }

    private S2Point getPreciseCoordinate() throws IOException, IllegalArgumentException {
        double x = getNextNumber();
        double y = getNextNumber();
        if (isNumberNext()) {
            getNextNumber();
        }
        return S2LatLng.fromDegrees(y, x).toPoint();
    }

    private boolean isNumberNext() throws IOException {
        int type = tokenizer.nextToken();
        tokenizer.pushBack();
        return type == StreamTokenizer.TT_WORD;
    }

    /**
     * Parses the next number in the stream.
     * Numbers with exponents are handled.
     * <tt>NaN</tt> values are handled correctly, and
     * the case of the "NaN" symbol is not significant.
     *
     * @return the next number in the stream
     * @throws IllegalArgumentException if the next token is not a valid number
     * @throws IOException              if an I/O error occurs
     */
    private double getNextNumber() throws IOException, IllegalArgumentException {
        int type = tokenizer.nextToken();
        switch (type) {
            case StreamTokenizer.TT_WORD: {
                if (tokenizer.sval.equalsIgnoreCase(NAN_SYMBOL)) {
                    return Double.NaN;
                } else {
                    try {
                        return Double.parseDouble(tokenizer.sval);
                    } catch (NumberFormatException ex) {
                        parseErrorWithLine("Invalid number: " + tokenizer.sval);
                    }
                }
            }
        }
        parseErrorExpected("number");
        return 0.0;
    }

    /**
     * Returns the next EMPTY or L_PAREN in the stream as uppercase text.
     *
     * @return the next EMPTY or L_PAREN in the stream as uppercase
     * text.
     * @throws IllegalArgumentException if the next token is not EMPTY or L_PAREN
     * @throws IOException              if an I/O error occurs
     */
    private String getNextEmptyOrOpener() throws IOException, IllegalArgumentException {
        String nextWord = getNextWord();
        if (EMPTY.equals(nextWord) || L_PAREN.equals(nextWord)) {
            return nextWord;
        }
        parseErrorExpected(EMPTY + " or " + L_PAREN);
        return null;
    }

    /**
     * Returns the next R_PAREN or COMMA in the stream.
     *
     * @return the next R_PAREN or COMMA in the stream
     * @throws IllegalArgumentException if the next token is not R_PAREN or COMMA
     * @throws IOException              if an I/O error occurs
     */
    private String getNextCloserOrComma() throws IOException, IllegalArgumentException {
        String nextWord = getNextWord();
        if (COMMA.equals(nextWord) || R_PAREN.equals(nextWord)) {
            return nextWord;
        }
        parseErrorExpected(COMMA + " or " + R_PAREN);
        return null;
    }

    /**
     * Returns the next word in the stream.
     *
     * @return the next word in the stream as uppercase text
     * @throws IllegalArgumentException if the next token is not a word
     * @throws IOException              if an I/O error occurs
     */
    private String getNextWord() throws IOException, IllegalArgumentException {
        int type = tokenizer.nextToken();
        switch (type) {
            case StreamTokenizer.TT_WORD:

                String word = tokenizer.sval;
                if (word.equalsIgnoreCase(EMPTY))
                    return EMPTY;
                return word;

            case '(':
                return L_PAREN;
            case ')':
                return R_PAREN;
            case ',':
                return COMMA;
        }
        parseErrorExpected("word");
        return null;
    }

    /**
     * Throws a formatted ParseException reporting that the current token
     * was unexpected.
     *
     * @param expected a description of what was expected
     * @throws IllegalArgumentException
     * @throws IllegalStateException    if an invalid token is encountered
     */
    private void parseErrorExpected(String expected) throws IllegalArgumentException {
        // throws Asserts for tokens that should never be seen
        if (tokenizer.ttype == StreamTokenizer.TT_NUMBER) {
            throw new IllegalStateException("Should never reach here: Unexpected NUMBER token");
        }
        if (tokenizer.ttype == StreamTokenizer.TT_EOL) {
            throw new IllegalStateException("Should never reach here: Unexpected EOL token");
        }

        String tokenStr = tokenString();
        parseErrorWithLine("Expected " + expected + " but found " + tokenStr);
    }

    private void parseErrorWithLine(String msg) {
        throw new IllegalArgumentException(msg + " (line " + tokenizer.lineno() + ")");
    }

    /**
     * Gets a description of the current token
     *
     * @return a description of the current token
     */
    private String tokenString() {
        switch (tokenizer.ttype) {
            case StreamTokenizer.TT_NUMBER:
                return "<NUMBER>";
            case StreamTokenizer.TT_EOL:
                return "End-of-Line";
            case StreamTokenizer.TT_EOF:
                return "End-of-Stream";
            case StreamTokenizer.TT_WORD:
                return "'" + tokenizer.sval + "'";
        }
        return "'" + (char) tokenizer.ttype + "'";
    }

    /**
     * Creates a <code>Geometry</code> using the next token in the stream.
     *
     * @return a <code>Geometry</code> specified by the next token
     * in the stream
     * @throws IOException if an I/O error occurs
     */
    private Object readGeometryTaggedText() throws IOException {
        String type;

        try {
            type = getNextWord();
        } catch (IOException e) {
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }

        if ("LINESTRING".equalsIgnoreCase(type)) {
            return readLineStringText();
        } else if ("LINEARRING".equalsIgnoreCase(type)) {
            return readLinearRingText();
        } else if ("MULTILINESTRING".equalsIgnoreCase(type)) {
            return readMultiLineString();
        } else if ("POLYGON".equalsIgnoreCase(type)) {
            return readPolygonText();
        } else if ("MULTIPOLYGON".equalsIgnoreCase(type)) {
            return readMultiPolygonText();
        } else if ("POINT".equalsIgnoreCase(type)) {
            return readPointText();
        }
        parseErrorWithLine("Unknown geometry type: " + type);
        // should never reach here
        return null;
    }

    /**
     * Creates a <code>LineString</code> using the next token in the stream.
     *
     * @return a <code>LineString</code> specified by the next
     * token in the stream
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if an unexpected token was encountered
     */
    private S2Polyline readLineStringText() throws IOException, IllegalArgumentException {
        return new S2Polyline(getPoints());
    }

    /**
     * Creates a <code>LinearRing</code> using the next token in the stream.
     *
     * @return a <code>LinearRing</code> specified by the next
     * token in the stream
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if the coordinates used to create the <code>LinearRing</code>
     *                                  do not form a closed linestring, or if an unexpected token was
     *                                  encountered
     */
    private S2Loop readLinearRingText() throws IOException, IllegalArgumentException {
        List<S2Point> points = getPoints();
        if (points.size() > 1 && points.get(0).equals(points.get(points.size() - 1))) {
            points.remove(points.size() - 1);
        }
        return new S2Loop(points);
    }


    /**
     * Creates a <code>Polygon</code> using the next token in the stream.
     *
     * @return a <code>Polygon</code> specified by the next token
     * in the stream
     * @throws ParseException if the coordinates used to create the <code>Polygon</code>
     *                        shell and holes do not form closed linestrings, or if an unexpected
     *                        token was encountered.
     * @throws IOException    if an I/O error occurs
     */
    private S2Polygon readPolygonText() throws IOException, IllegalArgumentException {
        String nextToken = getNextEmptyOrOpener();
        if (EMPTY.equals(nextToken)) {
            return new S2Polygon();
        }
        ArrayList<S2Loop> loops = new ArrayList<>();
        S2Loop shell = readLinearRingText();
        shell.normalize();
        loops.add(shell);
        nextToken = getNextCloserOrComma();
        while (COMMA.equals(nextToken)) {
            S2Loop e = readLinearRingText();
            loops.add(e);
//            if (!loops.contains(e)) {
//                loops.add(e);
//            }
            nextToken = getNextCloserOrComma();
        }
        return new S2Polygon(loops);
    }

    private S2Point readPointText() throws IOException {
        final List<S2Point> points = getPoints();
        return points.get(0);
    }

    private List<S2Point> getMultiPolygonPoints() throws IOException, IllegalArgumentException {
        ArrayList<S2Point> points = new ArrayList<>();
        points.add(getPreciseCoordinate());
        String nextToken = getNextCloserOrComma();
        while (COMMA.equals(nextToken)) {
            points.add(getPreciseCoordinate());
            nextToken = getNextCloserOrComma();
        }
        return points;
    }


    private List<S2Polygon> readMultiPolygonText() throws IOException, IllegalArgumentException {
        List<S2Polygon> s2PolygonList = new ArrayList<>();
        while (true) {
            int i = tokenizer.nextToken();
            if (StreamTokenizer.TT_WORD == i) {
                tokenizer.pushBack();
                List<S2Point> points = getMultiPolygonPoints();
                S2Loop s2Loop = new S2Loop(points);
                s2Loop.normalize();
                s2PolygonList.add(new S2Polygon(s2Loop));
            }
            if (i == ')') {
                int i1 = tokenizer.nextToken();
                if (i1 == StreamTokenizer.TT_EOF) {
                    break;
                }
                tokenizer.pushBack();
            }
        }
        return s2PolygonList;
    }


    private List<S2Polyline> readMultiLineString() throws IOException, IllegalArgumentException {
        List<S2Polyline> s2LoopList = new ArrayList<>();
        while (true) {
            int i = tokenizer.nextToken();
            if (StreamTokenizer.TT_WORD == i) {
                tokenizer.pushBack();
                S2Polyline polyline = getMultiLineStringPoints();
                s2LoopList.add(polyline);
            }
            if (i == ')') {
                int i1 = tokenizer.nextToken();
                if (i1 == StreamTokenizer.TT_EOF) {
                    break;
                }
                tokenizer.pushBack();
            }
        }
        return s2LoopList;
    }


    private S2Polyline getMultiLineStringPoints() throws IOException, IllegalArgumentException {
        ArrayList<S2Point> points = new ArrayList<>();
        points.add(getPreciseCoordinate());
        String nextToken = getNextCloserOrComma();
        while (COMMA.equals(nextToken)) {
            points.add(getPreciseCoordinate());
            nextToken = getNextCloserOrComma();
        }

        if (points.size() > 1 && points.get(0).equals(points.get(points.size() - 1))) {
            points.remove(points.size() - 1);
        }
        return new S2Polyline(points);
    }
}

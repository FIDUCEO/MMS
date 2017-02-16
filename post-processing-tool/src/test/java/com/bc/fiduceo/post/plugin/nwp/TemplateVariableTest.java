package com.bc.fiduceo.post.plugin.nwp;


import org.junit.Before;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TemplateVariableTest {

    private TemplateVariable templateVariable;

    @Before
    public void setUp() {
        templateVariable = new TemplateVariable("name", "orig_name", DataType.SHORT, "left down");
    }

    @Test
    public void testConstructAndGet() {
        assertEquals("name", templateVariable.getName());
        assertEquals("orig_name", templateVariable.getOriginalName());
        assertEquals(DataType.SHORT, templateVariable.getDataType());
        assertEquals("left down", templateVariable.getDimensions());

        final List<Attribute> attributes = templateVariable.getAttributes();
        assertEquals(0, attributes.size());
    }

    @Test
    public void testAddGetAttribute_number() {
        templateVariable.addAttribute("integer", -2);
        templateVariable.addAttribute("float", 3.87f);
        templateVariable.addAttribute("double", 118.99732);

        final List<Attribute> attributes = templateVariable.getAttributes();
        assertEquals(3, attributes.size());

        Attribute attribute = attributes.get(0);
        assertEquals("integer", attribute.getShortName());
        assertEquals(-2, attribute.getNumericValue().intValue());

        attribute = attributes.get(1);
        assertEquals("float", attribute.getShortName());
        assertEquals(3.87f, attribute.getNumericValue().floatValue(), 1e-8);

        attribute = attributes.get(2);
        assertEquals("double", attribute.getShortName());
        assertEquals(118.99732, attribute.getNumericValue().doubleValue(), 1e-8);
    }

    @Test
    public void testAddGetAttribute_string() {
        templateVariable.addAttribute("string", "we_can");

        final List<Attribute> attributes = templateVariable.getAttributes();
        assertEquals(1, attributes.size());

        Attribute attribute = attributes.get(0);
        assertEquals("string", attribute.getShortName());
        assertEquals("we_can", attribute.getStringValue());
    }

}

package com.bc.fiduceo.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NodeTypeTest {

    @Test
    public void testToId() {
        assertEquals(0, NodeType.ASCENDING.toId());
        assertEquals(1, NodeType.DESCENDING.toId());
        assertEquals(2, NodeType.UNDEFINED.toId());
    }

    @Test
    public void testFromId() {
         assertEquals(NodeType.ASCENDING, NodeType.fromId(0));
         assertEquals(NodeType.DESCENDING, NodeType.fromId(1));
         assertEquals(NodeType.UNDEFINED, NodeType.fromId(2));
         assertEquals(NodeType.UNDEFINED, NodeType.fromId(9));
    }
}

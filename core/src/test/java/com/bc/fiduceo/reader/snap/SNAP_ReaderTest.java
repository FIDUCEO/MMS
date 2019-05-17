package com.bc.fiduceo.reader.snap;

import com.bc.fiduceo.core.Interval;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SNAP_ReaderTest {

    @Test
    public void testCreatProductData() {
        ProductData productData = SNAP_Reader.createProductData(DataType.FLOAT, 4);
        assertEquals(ProductData.TYPE_FLOAT32, productData.getType());
        assertEquals(4, productData.getNumElems());

        productData = SNAP_Reader.createProductData(DataType.INT, 5);
        assertEquals(ProductData.TYPE_INT32, productData.getType());
        assertEquals(5, productData.getNumElems());

        productData = SNAP_Reader.createProductData(DataType.SHORT, 6);
        assertEquals(ProductData.TYPE_INT16, productData.getType());
        assertEquals(6, productData.getNumElems());

        productData = SNAP_Reader.createProductData(DataType.BYTE, 7);
        assertEquals(ProductData.TYPE_INT8, productData.getType());
        assertEquals(7, productData.getNumElems());
    }

    @Test
    public void testCreatProductData_unsupportesType() {
        try {
            SNAP_Reader.createProductData(DataType.SEQUENCE, 4);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetGeophysicalNoDataValue_fromNode() {
        final RasterDataNode dataNode = mock(RasterDataNode.class);

        when(dataNode.isNoDataValueUsed()).thenReturn(true);
        when(dataNode.getGeophysicalNoDataValue()).thenReturn(-12.7);

        final double noDataValue = SNAP_Reader.getGeophysicalNoDataValue(dataNode);
        assertEquals(-12.7, noDataValue, 1e-8);
    }

    @Test
    public void testGetGeophysicalNoDataValue_fromDefault() {
        final RasterDataNode dataNode = mock(RasterDataNode.class);

        when(dataNode.isNoDataValueUsed()).thenReturn(false);
        when(dataNode.getDataType()).thenReturn(ProductData.TYPE_INT16);

        final double noDataValue = SNAP_Reader.getGeophysicalNoDataValue(dataNode);
        assertEquals(-32767.0, noDataValue, 1e-8);
    }


    @Test
    public void testGetNoDataValue_fromNode() {
        final RasterDataNode dataNode = mock(RasterDataNode.class);

        when(dataNode.isNoDataValueUsed()).thenReturn(true);
        when(dataNode.getNoDataValue()).thenReturn(23.987);

        final double noDataValue = SNAP_Reader.getNoDataValue(dataNode);
        assertEquals(23.987, noDataValue, 1e-8);
    }

    @Test
    public void testGetNoDataValue_fromDefault() {
        final RasterDataNode dataNode = mock(RasterDataNode.class);

        when(dataNode.isNoDataValueUsed()).thenReturn(false);
        when(dataNode.getDataType()).thenReturn(ProductData.TYPE_INT32);

        final double noDataValue = SNAP_Reader.getNoDataValue(dataNode);
        assertEquals(-2.147483647E9, noDataValue, 1e-8);
    }

    @Test
    public void testCreateReadingArray() {
        final int[] shape = {2, 3};

        Array array = SNAP_Reader.createReadingArray(DataType.FLOAT, shape);
        assertNotNull(array);
        assertEquals(DataType.FLOAT, array.getDataType());
        assertArrayEquals(shape, array.getShape());

        array = SNAP_Reader.createReadingArray(DataType.SHORT, shape);
        assertNotNull(array);
        assertEquals(DataType.INT, array.getDataType());
        assertArrayEquals(shape, array.getShape());

        array = SNAP_Reader.createReadingArray(DataType.BYTE, shape);
        assertNotNull(array);
        assertEquals(DataType.INT, array.getDataType());
        assertArrayEquals(shape, array.getShape());
    }

    @Test
    public void testCreateReadingArray_unsupported() {
        final int[] shape = {2, 3};

        try {
            SNAP_Reader.createReadingArray(DataType.DOUBLE, shape);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }

        try {
            SNAP_Reader.createReadingArray(DataType.LONG, shape);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testGetShape() {
        final Interval interval = new Interval(12, 23);

        final int[] shape = SNAP_Reader.getShape(interval);
        assertEquals(2, shape.length);
        assertEquals(23, shape[0]);
        assertEquals(12, shape[1]);
    }
}

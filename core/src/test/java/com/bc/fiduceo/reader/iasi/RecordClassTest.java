package com.bc.fiduceo.reader.iasi;

import org.junit.Test;

import javax.imageio.stream.ImageInputStream;

import java.io.IOException;

import static com.bc.fiduceo.reader.iasi.RecordClass.IPR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecordClassTest {

    @Test
    public void testReadRecordClass() throws IOException {
        final ImageInputStream imageInputStream = mock(ImageInputStream.class);
        when(imageInputStream.readByte()).thenReturn((byte)3);

        final RecordClass recordClass = RecordClass.readRecordClass(imageInputStream);
        assertEquals(IPR, recordClass);
    }

    @Test
    public void testReadRecordClass_valueOutOfRange() throws IOException {
        final ImageInputStream imageInputStream = mock(ImageInputStream.class);

        when(imageInputStream.readByte()).thenReturn((byte)-1);
        try {
            RecordClass.readRecordClass(imageInputStream);
            fail("IOException expected");
        } catch (IOException expected) {
        }

        when(imageInputStream.readByte()).thenReturn((byte)9);
        try {
            RecordClass.readRecordClass(imageInputStream);
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }
}

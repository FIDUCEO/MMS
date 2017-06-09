package com.bc.fiduceo.reader.iasi;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;

class InternalPointerRecord {

    GenericRecordHeader grh;
    RecordClass targetRecordClass;
    InstrumentGroup targetInstrumentGroup;
    byte targetRecordSubclass;
    long targetRecordOffset;

    static InternalPointerRecord readInternalPointerRecord(ImageInputStream iis) throws IOException {
        final InternalPointerRecord ipr = new InternalPointerRecord();

        ipr.grh = GenericRecordHeader.readGenericRecordHeader(iis);

        if (ipr.grh.recordClass != RecordClass.IPR  || ipr.grh.instrumentGroup != InstrumentGroup.GENERIC) {
            throw new IOException("Illegal Generic Record Header");
        }

        ipr.targetRecordClass = RecordClass.readRecordClass(iis);
        ipr.targetInstrumentGroup = InstrumentGroup.readInstrumentGroup(iis);
        ipr.targetRecordSubclass = iis.readByte();
        ipr.targetRecordOffset = iis.readUnsignedInt();

        return ipr;
    }
}
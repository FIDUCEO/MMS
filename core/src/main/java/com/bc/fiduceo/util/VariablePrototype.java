/*
 * Copyright (C) 2016 Brockmann Consult GmbH
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

package com.bc.fiduceo.util;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.CDMSort;
import ucar.nc2.Dimension;
import ucar.nc2.EnumTypedef;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.ProxyReader;
import ucar.nc2.Structure;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.util.CancelTask;
import ucar.nc2.util.Indent;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.util.Formatter;
import java.util.List;
import java.util.Map;

// This class should be used when driving special purpose classes from the NetCDF Variable class. Overwriting
// all methods with a throws implementation ensures that methods that should be overridden are really overridden;
// calls into the not completely initialized base class are not possible this was tb 2016-09-26

@SuppressWarnings("deprecation")
public class VariablePrototype extends Variable {

    public VariablePrototype() {
    }

    @Override
    public DataType getDataType() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int[] getShape() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int getShape(int index) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public List<Attribute> getAttributes() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int[] getShapeNotScalar() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public long getSize() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int getElementSize() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int getRank() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Group getParentGroup() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isMetadata() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isScalar() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isVariableLength() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isUnsigned() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setUnsigned(boolean b) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isUnlimited() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public List<Dimension> getDimensions() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Dimension getDimension(int i) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getDimensionsString() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int findDimensionIndex(String name) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Attribute findAttribute(String name) {
        return null;
    }

    @Override
    public Attribute findAttributeIgnoreCase(String name) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getDescription() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getUnitsString() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public List<Range> getRanges() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Section getShapeAsSection() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public ProxyReader getProxyReader() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setProxyReader(ProxyReader proxyReader) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Variable section(List<Range> ranges) throws InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Variable section(Section subsection) throws InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Variable slice(int dim, int value) throws InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Variable reduce(List<Dimension> dims) throws InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String lookupEnumString(int val) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setEnumTypedef(EnumTypedef enumTypedef) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public EnumTypedef getEnumTypedef() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array read(int[] origin, int[] shape) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array read(String sectionSpec) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array read(List<Range> ranges) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array read(Section section) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array read() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public byte readScalarByte() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public short readScalarShort() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int readScalarInt() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public long readScalarLong() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public float readScalarFloat() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public double readScalarDouble() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String readScalarString() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array reallyRead(Variable client, CancelTask cancelTask) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array reallyRead(Variable client, Section section, CancelTask cancelTask) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public long readToByteChannel(Section section, WritableByteChannel wbc) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public long readToStream(Section section, OutputStream out) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getNameAndDimensions() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getNameAndDimensions(boolean strict) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void getNameAndDimensions(StringBuilder buf) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void getNameAndDimensions(StringBuffer buf) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void getNameAndDimensions(StringBuilder buf, boolean useFullName, boolean strict) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void getNameAndDimensions(Formatter buf, boolean useFullName, boolean strict) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String toString() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String writeCDL(boolean useFullName, boolean strict) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String toStringDebug() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getDatasetLocation() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean equals(Object oo) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int hashCode() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void hashCodeShow(Indent indent) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int compareTo(VariableSimpleIF o) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setDataType(DataType dataType) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String setName(String shortName) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setParentGroup(Group group) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setElementSize(int elementSize) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Attribute addAttribute(Attribute att) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void addAll(Iterable<Attribute> atts) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean remove(Attribute a) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean removeAttribute(String attName) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean removeAttributeIgnoreCase(String attName) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setDimensions(List<Dimension> dims) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void resetShape() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setDimensions(String dimString) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void resetDimensions() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setDimensionsAnonymous(int[] shape) throws InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setIsScalar() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setDimension(int idx, Dimension dim) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Variable setImmutable() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isImmutable() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Object getSPobject() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setSPobject(Object spiObject) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int getSizeToCache() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setSizeToCache(int sizeToCache) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setCaching(boolean caching) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isCaching() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void invalidateCache() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setCachedData(Array cacheData) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setCachedData(Array cacheData, boolean isMetadata) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void createNewCache() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean hasCachedData() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setValues(int npts, double start, double incr) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setValues(List<String> values) throws IllegalArgumentException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public List<Dimension> getDimensionsAll() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int[] getShapeAll() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isCoordinateVariable() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isUnknownLength() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public CDMSort getSort() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setSort(CDMSort sort) {
        super.setSort(sort);
    }

    @Override
    public String getShortName() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setShortName(String name) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Group getGroup() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Structure getParentStructure() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setParentStructure(Structure parent) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isMemberOfStructure() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean getImmutable() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setImmutable(boolean tf) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getDODSName() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setDODSName(String name) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getFullName() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getFullNameEscaped() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getName() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int localhash() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Map<String, Object> getAnnotations() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Object getAnnotation(String key) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Object annotate(String key, Object value) {
        return super.annotate(key, value);
    }
}

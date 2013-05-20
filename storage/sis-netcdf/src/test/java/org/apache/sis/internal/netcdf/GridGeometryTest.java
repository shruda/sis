/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.internal.netcdf;

import java.io.IOException;
import org.apache.sis.storage.netcdf.AttributeNames;
import org.apache.sis.test.DependsOn;
import org.apache.sis.test.DependsOnMethod;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.apache.sis.test.TestUtilities.getSingleton;


/**
 * Tests the {@link GridGeometry} implementation. The default implementation tests
 * {@link org.apache.sis.internal.netcdf.ucar.GridGeometryWrapper} since the UCAR
 * library is our reference implementation. However subclasses can override the
 * {@link #createDecoder(String)} method in order to test a different implementation.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3
 * @version 0.3
 * @module
 */
@DependsOn(VariableTest.class)
public strictfp class GridGeometryTest extends TestCase {
    /**
     * Tests {@link GridGeometry#getSourceDimensions()} and {@link GridGeometry#getTargetDimensions()}.
     *
     * @throws IOException If an error occurred while reading the NetCDF file.
     */
    @Test
    public void testDimensions() throws IOException {
        final GridGeometry geometry = getSingleton(selectDataset(NCEP).getGridGeometries());
        assertEquals("getSourceDimensions()", 3, geometry.getSourceDimensions());
        assertEquals("getTargetDimensions()", 3, geometry.getTargetDimensions());
    }

    /**
     * Tests {@link GridGeometry#getAxes()}.
     *
     * @throws IOException If an error occurred while reading the NetCDF file.
     */
    @Test
    @DependsOnMethod("testDimensions")
    public void testAxes() throws IOException {
        final Axis[] axes = getSingleton(selectDataset(NCEP).getGridGeometries()).getAxes();
        assertEquals(3, axes.length);
        final Axis x = axes[2];
        final Axis y = axes[1];
        final Axis t = axes[0];

        assertSame(AttributeNames.LONGITUDE, x.attributeNames);
        assertSame(AttributeNames.LATITUDE,  y.attributeNames);
        assertSame(AttributeNames.TIME,      t.attributeNames);

        assertArrayEquals(new int[] {2}, x.sourceDimensions);
        assertArrayEquals(new int[] {1}, y.sourceDimensions);
        assertArrayEquals(new int[] {0}, t.sourceDimensions);

        assertArrayEquals(new int[] {73}, x.sourceSizes);
        assertArrayEquals(new int[] {73}, y.sourceSizes);
        assertArrayEquals(new int[] { 1}, t.sourceSizes);
    }
}

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
package org.apache.sis.storage.netcdf;

import java.io.IOException;
import ucar.nc2.dataset.NetcdfDataset;
import org.opengis.metadata.Metadata;
import org.opengis.wrapper.netcdf.IOTestCase;
import org.apache.sis.internal.netcdf.TestCase;
import org.apache.sis.internal.netcdf.Decoder;
import org.apache.sis.internal.netcdf.ucar.DecoderWrapper;
import org.apache.sis.internal.netcdf.impl.ChannelDecoderTest;
import org.apache.sis.test.DependsOn;
import org.junit.Test;

import static org.apache.sis.test.Assert.*;


/**
 * Tests {@link MetadataReader}. This tests uses the SIS embedded implementation and the UCAR library
 * for reading NetCDF attributes.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3
 * @version 0.3
 * @module
 */
@DependsOn({
    ChannelDecoderTest.class,
    org.apache.sis.internal.netcdf.impl.VariableInfoTest.class
})
public final strictfp class MetadataReaderTest extends IOTestCase {
    /**
     * Reads the metadata using the NetCDF decoder embedded with SIS,
     * and compares its string representation with the expected one.
     *
     * @throws IOException Should never happen.
     */
    @Test
    public void testEmbedded() throws IOException {
        final Metadata metadata;
        try (Decoder input = ChannelDecoderTest.createChannelDecoder(NCEP)) {
            metadata = new MetadataReader(input).read();
        }
        compareToExpected(metadata);
    }

    /**
     * Reads the metadata using the UCAR library and compares
     * its string representation with the expected one.
     *
     * @throws IOException Should never happen.
     */
    @Test
    public void testUCAR() throws IOException {
        final Metadata metadata;
        try (Decoder input = new DecoderWrapper(TestCase.LISTENERS, new NetcdfDataset(open(NCEP)))) {
            metadata = new MetadataReader(input).read();
        }
        compareToExpected(metadata);
    }

    /**
     * Compares the string representation of the given metadata object with the expected one.
     * The given metadata shall have been created from the {@link #NCEP} dataset.
     */
    static void compareToExpected(final Metadata actual) {
        assertMultilinesEquals(
            "DefaultMetadata\n" +
            "  ├─File identifier………………………………………………………………………… edu.ucar.unidata:NCEP/SST/Global_5x2p5deg/SST_Global_5x2p5deg_20050922_0000.nc\n" +
            "  ├─Hierarchy level………………………………………………………………………… Dataset\n" +
            "  ├─Contact\n" +
            "  │   ├─Individual name……………………………………………………………… NOAA/NWS/NCEP\n" +
            "  │   └─Role…………………………………………………………………………………………… Point of contact\n" +
            "  ├─Metadata standard name……………………………………………………… ISO 19115-2 Geographic Information - Metadata Part 2 Extensions for imagery and gridded data\n" +
            "  ├─Metadata standard version……………………………………………… ISO 19115-2:2009(E)\n" +
            "  ├─Spatial representation info\n" +
            "  │   ├─Number of dimensions………………………………………………… 3\n" +
            "  │   ├─Axis dimension properties (1 of 3)\n" +
            "  │   │   ├─Dimension name……………………………………………………… Column\n" +
            "  │   │   └─Dimension size……………………………………………………… 73\n" +
            "  │   ├─Axis dimension properties (2 of 3)\n" +
            "  │   │   ├─Dimension name……………………………………………………… Row\n" +
            "  │   │   └─Dimension size……………………………………………………… 73\n" +
            "  │   ├─Axis dimension properties (3 of 3)\n" +
            "  │   │   ├─Dimension name……………………………………………………… Time\n" +
            "  │   │   └─Dimension size……………………………………………………… 1\n" +
            "  │   ├─Cell geometry…………………………………………………………………… Area\n" +
            "  │   └─Transformation parameter availability…… false\n" +
            "  ├─Identification info\n" +
            "  │   ├─Spatial representation type……………………………… Grid\n" +
            "  │   ├─Extent\n" +
            "  │   │   ├─Geographic element\n" +
            "  │   │   │   ├─West bound longitude…………………………… -180.0\n" +
            "  │   │   │   ├─East bound longitude…………………………… 180.0\n" +
            "  │   │   │   ├─South bound latitude…………………………… -90.0\n" +
            "  │   │   │   ├─North bound latitude…………………………… 90.0\n" +
            "  │   │   │   └─Extent type code……………………………………… true\n" +
            "  │   │   └─Vertical element\n" +
            "  │   │       ├─Minimum value……………………………………………… 0.0\n" +
            "  │   │       └─Maximum value……………………………………………… 0.0\n" +
            "  │   ├─Abstract………………………………………………………………………………… NCEP SST Global 5.0 x 2.5 degree model data\n" +
            "  │   ├─Citation\n" +
            "  │   │   ├─Title……………………………………………………………………………… Sea Surface Temperature Analysis Model\n" +
            "  │   │   ├─Date\n" +
            "  │   │   │   ├─Date……………………………………………………………………… Thu Sep 22 02:00:00 CEST 2005\n" +
            "  │   │   │   └─Date type………………………………………………………… Creation\n" +
            "  │   │   ├─Identifier\n" +
            "  │   │   │   ├─Code……………………………………………………………………… NCEP/SST/Global_5x2p5deg/SST_Global_5x2p5deg_20050922_0000.nc\n" +
            "  │   │   │   └─Authority\n" +
            "  │   │   │       └─Title………………………………………………………… edu.ucar.unidata\n" +
            "  │   │   └─Cited responsible party\n" +
            "  │   │       ├─Individual name………………………………………… NOAA/NWS/NCEP\n" +
            "  │   │       └─Role……………………………………………………………………… Originator\n" +
            "  │   ├─Descriptive keywords\n" +
            "  │   │   ├─Keyword………………………………………………………………………… EARTH SCIENCE > Oceans > Ocean Temperature > Sea Surface Temperature\n" +
            "  │   │   ├─Type………………………………………………………………………………… Theme\n" +
            "  │   │   └─Thesaurus name\n" +
            "  │   │       └─Title…………………………………………………………………… GCMD Science Keywords\n" +
            "  │   ├─Point of contact\n" +
            "  │   │   ├─Individual name…………………………………………………… NOAA/NWS/NCEP\n" +
            "  │   │   └─Role………………………………………………………………………………… Point of contact\n" +
            "  │   └─Resource constraints\n" +
            "  │       └─Use limitation……………………………………………………… Freely available\n" +
            "  ├─Content info\n" +
            "  │   └─Dimension\n" +
            "  │       ├─Descriptor………………………………………………………………… Sea temperature\n" +
            "  │       └─Sequence identifier………………………………………… SST\n" +
            "  └─Data quality info\n" +
            "      └─Lineage\n" +
            "          └─Statement…………………………………………………………………… 2003-04-07 12:12:50 - created by gribtocdl" +
            "              2005-09-26T21:50:00 - edavis - add attributes for dataset discovery\n",
        actual.toString());
    }
}

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
package org.apache.sis.internal.referencing.provider;

import javax.xml.bind.annotation.XmlTransient;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.apache.sis.metadata.iso.citation.Citations;
import org.apache.sis.parameter.ParameterBuilder;
import org.apache.sis.internal.referencing.Formulas;
import org.apache.sis.internal.util.Constants;
import org.apache.sis.internal.util.Numerics;
import org.apache.sis.measure.Latitude;
import org.apache.sis.measure.Units;


/**
 * The provider for <cite>"Polar Stereographic (Variant A)"</cite> projection (EPSG:9810).
 *
 * @author  Rueben Schulz (UBC)
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.6
 * @version 0.8
 * @module
 *
 * @see <a href="http://www.remotesensing.org/geotiff/proj_list/polar_stereographic.html">Polar Stereographic on RemoteSensing.org</a>
 */
@XmlTransient
public final class PolarStereographicA extends AbstractStereographic {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 538262714055500925L;

    /**
     * The EPSG name for this projection.
     */
    public static final String NAME = "Polar Stereographic (variant A)";

    /**
     * The EPSG identifier, to be preferred to the name when available.
     */
    public static final String IDENTIFIER = "9810";

    /**
     * The operation parameter descriptor for the <cite>Latitude of natural origin</cite> (φ₀) parameter value.
     * Valid values can be -90° or 90° only. There is no default value.
     */
    public static final ParameterDescriptor<Double> LATITUDE_OF_ORIGIN = LambertConformal1SP.LATITUDE_OF_ORIGIN;

    /**
     * The operation parameter descriptor for the <cite>Longitude of natural origin</cite> (λ₀) parameter value.
     * Valid values range is [-180 … 180]° and default value is 0°.
     */
    public static final ParameterDescriptor<Double> LONGITUDE_OF_ORIGIN;

    /**
     * The operation parameter descriptor for the <cite>Scale factor at natural origin</cite> (k₀) parameter value.
     * Valid values range is (0 … ∞) and default value is 1.
     */
    public static final ParameterDescriptor<Double> SCALE_FACTOR = Mercator1SP.SCALE_FACTOR;

    /**
     * The group of all parameters expected by this coordinate operation.
     */
    private static final ParameterDescriptorGroup PARAMETERS;
    static {
        final ParameterBuilder builder = builder();
        LONGITUDE_OF_ORIGIN = createLongitude(builder
                .addNamesAndIdentifiers(ObliqueStereographic.LONGITUDE_OF_ORIGIN)
                .reidentify(Citations.GEOTIFF, "3095")
                .rename(Citations.GEOTIFF, "StraightVertPoleLong"));

        PARAMETERS = builder
                .addIdentifier(             IDENTIFIER)
                .addName(                   NAME)
                .addName(Citations.OGC,     "Polar_Stereographic")
                .addName(Citations.GEOTIFF, "CT_PolarStereographic")
                .addName(Citations.PROJ4,   "stere")
                .addIdentifier(Citations.GEOTIFF, "15")
                .createGroupForMapProjection(
                        LATITUDE_OF_ORIGIN,     // Can be only ±90°
                        LONGITUDE_OF_ORIGIN,
                        SCALE_FACTOR,
                        FALSE_EASTING,
                        FALSE_NORTHING);
    }

    /**
     * Constructs a new provider.
     */
    public PolarStereographicA() {
        super(PARAMETERS);
    }

    /**
     * If the given parameter values are those of a Universal Polar Stereographic projection,
     * returns -1 for South pole or +1 for North pole. Otherwise returns 0. It is caller's
     * responsibility to verify that the operation method is {@value #NAME}.
     *
     * @param  group  the Transverse Mercator projection parameters.
     * @return zone number (positive if North, negative if South),
     *         or 0 if the given parameters are not for a zoned projection.
     *
     * @since 0.8
     */
    public static int isUPS(final ParameterValueGroup group) {
        if (Numerics.epsilonEqual(group.parameter(Constants.SCALE_FACTOR)    .doubleValue(Units.UNITY),  1, Numerics.COMPARISON_THRESHOLD) &&
            Numerics.epsilonEqual(group.parameter(Constants.FALSE_EASTING)   .doubleValue(Units.METRE),  0, Formulas.LINEAR_TOLERANCE) &&
            Numerics.epsilonEqual(group.parameter(Constants.FALSE_NORTHING)  .doubleValue(Units.METRE),  0, Formulas.LINEAR_TOLERANCE) &&
            Numerics.epsilonEqual(group.parameter(Constants.CENTRAL_MERIDIAN).doubleValue(Units.DEGREE), 0, Formulas.ANGULAR_TOLERANCE))
        {
            final double φ = group.parameter(Constants.LATITUDE_OF_ORIGIN).doubleValue(Units.DEGREE);
            if (Numerics.epsilonEqual(φ, Latitude.MAX_VALUE, Formulas.ANGULAR_TOLERANCE)) return +1;
            if (Numerics.epsilonEqual(φ, Latitude.MIN_VALUE, Formulas.ANGULAR_TOLERANCE)) return -1;
        }
        return 0;
    }
}

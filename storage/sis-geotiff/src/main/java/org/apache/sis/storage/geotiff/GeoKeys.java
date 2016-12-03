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
package org.apache.sis.storage.geotiff;

import java.lang.reflect.Field;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;


/**
 * GeoTIFF keys associated to values needed for building {@link CoordinateReferenceSystem} instances
 * and {@link MathTransform} "grid to CRS". In this class, field names are close to GeoTIFF key names
 * with the {@code "GeoKey"} suffix omitted. For that reason, many of those field names do not follow
 * usual Java convention for constants.
 *
 * @author  Rémi Maréchal (Geomatys)
 * @since   0.8
 * @version 0.8
 * @module
 */
final class GeoKeys {
    /**
     * Do not allow instantiation of this class.
     */
    private GeoKeys() {
    }

    // 6.2.1 GeoTIFF Configuration Keys
    /** Section 6.3.1.1 Codes. */ public static final short ModelType                = 1024;
    /** Section 6.3.1.2 Codes. */ public static final short RasterType               = 1025;
    /** Documentation.         */ public static final short Citation                 = 1026;

    // 6.2.2 Geographic CS Parameter Keys
    /** Section 6.3.2.1 Codes. */ public static final short GeographicType           = 2048;
    /** Documentation.         */ public static final short GeogCitation             = 2049;
    /** Section 6.3.2.2 Codes. */ public static final short GeogGeodeticDatum        = 2050;
    /** Section 6.3.2.4 codes. */ public static final short GeogPrimeMeridian        = 2051;
    /** Section 6.3.1.3 Codes. */ public static final short GeogLinearUnits          = 2052;
    /** Relative to meters.    */ public static final short GeogLinearUnitSize       = 2053;
    /** Section 6.3.1.4 Codes. */ public static final short GeogAngularUnits         = 2054;
    /** Relative to radians.   */ public static final short GeogAngularUnitSize      = 2055;
    /** Section 6.3.2.3 Codes. */ public static final short GeogEllipsoid            = 2056;
    /** In GeogLinearUnits.    */ public static final short GeogSemiMajorAxis        = 2057;
    /** In GeogLinearUnits.    */ public static final short GeogSemiMinorAxis        = 2058;
    /** A ratio.               */ public static final short GeogInvFlattening        = 2059;
    /** Section 6.3.1.4 Codes. */ public static final short GeogAzimuthUnits         = 2060;
    /** In GeogAngularUnit.    */ public static final short GeogPrimeMeridianLong    = 2061;

    // 6.2.3 Projected CS Parameter Keys
    /** Section 6.3.3.1 codes. */ public static final short ProjectedCSType          = 3072;
    /** Documentation.         */ public static final short PCSCitation              = 3073;
    /** Section 6.3.3.2 codes. */ public static final short Projection               = 3074;
    /** Section 6.3.3.3 codes. */ public static final short ProjCoordTrans           = 3075;
    /** Section 6.3.1.3 codes. */ public static final short ProjLinearUnits          = 3076;
    /** Relative to meters.    */ public static final short ProjLinearUnitSize       = 3077;
    /** In GeogAngularUnit.    */ public static final short ProjStdParallel1         = 3078;
    /** In GeogAngularUnit.    */ public static final short ProjStdParallel2         = 3079;
    /** In GeogAngularUnit.    */ public static final short ProjNatOriginLong        = 3080;
    /** In GeogAngularUnit.    */ public static final short ProjNatOriginLat         = 3081;
    /** In ProjLinearUnits.    */ public static final short ProjFalseEasting         = 3082;
    /** In ProjLinearUnits.    */ public static final short ProjFalseNorthing        = 3083;
    /** In GeogAngularUnit.    */ public static final short ProjFalseOriginLong      = 3084;
    /** In GeogAngularUnit.    */ public static final short ProjFalseOriginLat       = 3085;
    /** In ProjLinearUnits.    */ public static final short ProjFalseOriginEasting   = 3086;
    /** In ProjLinearUnits.    */ public static final short ProjFalseOriginNorthing  = 3087;
    /** In GeogAngularUnit.    */ public static final short ProjCenterLong           = 3088;
    /** In GeogAngularUnit.    */ public static final short ProjCenterLat            = 3089;
    /** In ProjLinearUnits.    */ public static final short ProjCenterEasting        = 3090;
    /** In ProjLinearUnits.    */ public static final short ProjCenterNorthing       = 3091;
    /** A ratio.               */ public static final short ProjScaleAtNatOrigin     = 3092;
    /** A ratio.               */ public static final short ProjScaleAtCenter        = 3093;
    /** In GeogAzimuthUnit.    */ public static final short ProjAzimuthAngle         = 3094;
    /** In GeogAngularUnit.    */ public static final short ProjStraightVertPoleLong = 3095;

    // 6.2.4 Vertical CS Keys
    /** Section 6.3.4.1 codes. */ public static final short VerticalCSType           = 4096;
    /** Documentation.         */ public static final short VerticalCitation         = 4097;
    /** Section 6.3.4.2 codes. */ public static final short VerticalDatum            = 4098;
    /** Section 6.3.1.3 codes. */ public static final short VerticalUnits            = 4099;

    /**
     * Returns the name of the given key. Implementation of this method is inefficient,
     * but it should rarely be invoked (mostly for formatting error messages).
     */
    static String name(final short tag) {
        try {
            for (final Field field : GeoKeys.class.getFields()) {
                if (field.getType() == Short.TYPE) {
                    if (field.getShort(null) == tag) {
                        return field.getName();
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);        // Should never happen because we asked only for public fields.
        }
        return Integer.toHexString(Short.toUnsignedInt(tag));
    }
}
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
package org.apache.sis.referencing.gazetteer;

import org.opengis.util.FactoryException;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.operation.Projection;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.apache.sis.internal.referencing.provider.TransverseMercator;
import org.apache.sis.internal.referencing.provider.PolarStereographicA;
import org.apache.sis.referencing.crs.DefaultProjectedCRS;
import org.apache.sis.referencing.cs.AxesConvention;
import org.apache.sis.referencing.IdentifiedObjects;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.CRS;
import org.apache.sis.math.MathFunctions;
import org.apache.sis.util.resources.Errors;
import org.apache.sis.util.StringBuilders;
import org.apache.sis.util.Utilities;


/**
 * Conversions from direct positions to Military Grid Reference System (MGRS) labels.
 * Each {@code MGRSEncoder} instance is configured for one {@code DirectPosition} CRS.
 * If a position is given in another CRS, another {@code MGRSEncoder} instance must be created.
 *
 * <div class="section">Immutability and thread safety</div>
 * This class is <strong>not</strong> thread-safe. A new instance must be created for each thread,
 * or synchronization must be applied by the caller.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.8
 * @version 0.8
 * @module
 *
 * @see <a href="https://en.wikipedia.org/wiki/Military_Grid_Reference_System">Military Grid Reference System on Wikipedia</a>
 */
final class MGRSEncoder {
    /**
     * Height of latitude bands, in degrees.
     * Those bands are labeled from {@code 'C'} to {@code 'X'} inclusive, excluding {@code 'I'} and {@code 'O'}.
     */
    private static final double LATITUDE_BAND_HEIGHT = 8;

    /**
     * Southernmost bound of the first latitude band ({@code 'C'}).
     */
    private static final double UTM_SOUTH_BOUNDS = -80;

    /**
     * Northernmost bound of the last latitude band ({@code 'X'}).
     */
    private static final double UTM_NORTH_BOUNDS = 84;

    /**
     * Special {@link #crsZone} value for the UPS South (Universal Polar Stereographic) projection.
     */
    private static final int SOUTH_POLE = -1000;

    /**
     * Special {@link #crsZone} value for the UPS North (Universal Polar Stereographic) projection.
     */
    private static final int NORTH_POLE = 1000;

    /**
     * Size of the 100,000-meter squares.
     */
    static final double GRID_SQUARE_SIZE = 100_000;

    /**
     * The number of digits in a one-meter precision when formatting MGRS labels.
     *
     * <p><b>Invariant:</b> the following relationship must hold:
     * {@code GRID_SQUARE_SIZE == Math.pow(10, METRE_PRECISION_DIGITS)}
     */
    static final int METRE_PRECISION_DIGITS = 5;

    /**
     * The first of the two letters ({@code 'I'} and {@code 'O'}) excluded in MGRS notation.
     * This letter and all following letters shall be shifted by one character. Example:
     *
     * {@preformat java
     *     char band = ...;
     *     if (band >= EXCLUDE_I) {
     *         band++;
     *         if (band >= EXCLUDE_O) band++;
     *     }
     * }
     *
     * or equivalently:
     *
     * {@preformat java
     *     char band = ...;
     *     if (band >= EXCLUDE_I && ++band >= EXCLUDE_O) band++;
     * }
     */
    private static final char EXCLUDE_I = 'I';

    /**
     * The second of the two letters ({@code 'I'} and {@code 'O'}) excluded in MGRS notation.
     */
    private static final char EXCLUDE_O = 'O';

    /**
     * The datum of the CRS given at construction time, or {@code null} if unsupported.
     * Only the datums enumerated in {@link CommonCRS} are currently supported.
     */
    private final CommonCRS datum;

    /**
     * UTM zone of position CRS (negative for South hemisphere), or {@value #NORTH_POLE} or {@value #SOUTH_POLE}
     * if the CRS is a Universal Polar Stereographic projection, or 0 if the CRS is not a recognized projection.
     * Note that this is not necessarily the same zone than the one to use for formatting any given coordinate in
     * that projected CRS, since the {@link #zone(int, double, char)} method has special rules for some latitudes.
     */
    private final int crsZone;

    /**
     * Coordinate conversion from the position CRS to a CRS of the same type but with normalized axes.
     * Axis directions are (East, North) and axis units are metres or degrees, depending on the CRS type.
     *
     * <p>This transform should perform only simple operation like swapping axis order an unit conversions.
     * It should not perform more complex operations that would require to go back to geographic coordinates.</p>
     */
    private final MathTransform toNormalized;

    /**
     * Coordinate conversion from the <em>normalized</em> position CRS to a geographic CRS.
     * Axis directions are (North, East) as in EPSG geodetic dataset and axis units are degrees.
     */
    private final MathTransform toGeographic;

    /**
     * Creates a new converter from direct positions to MGRS labels.
     */
    MGRSEncoder(final CoordinateReferenceSystem crs) throws FactoryException, TransformException {
        if (crs == null) {
            throw new IllegalArgumentException(Errors.format(Errors.Keys.UnspecifiedCRS));
        }
        datum = CommonCRS.forDatum(crs);
        if (datum == null) {
            throw new TransformException("Unsupported datum");      // TODO: localize
        }
        if (crs instanceof ProjectedCRS) {
            ProjectedCRS  projCRS = (ProjectedCRS) crs;
            Projection projection = projCRS.getConversionFromBase();
            final OperationMethod method = projection.getMethod();
            if (IdentifiedObjects.isHeuristicMatchForName(method, TransverseMercator.NAME)) {
                crsZone = TransverseMercator.Zoner.UTM.zone(projection.getParameterValues());
            } else if (IdentifiedObjects.isHeuristicMatchForName(method, PolarStereographicA.NAME)) {
                crsZone = NORTH_POLE * PolarStereographicA.isUPS(projection.getParameterValues());
            } else {
                crsZone = 0;                                    // Neither UTM or UPS projection.
            }
            if (crsZone != 0) {
                /*
                 * Usually, the projected CRS already has (E,N) axis orientations with metres units,
                 * so we let 'toNormalized' to null. In the rarer cases where the CRS axes do not
                 * have the expected orientations and units, then we build a normalized version of
                 * that CRS and compute the transformation to that CRS.
                 */
                DefaultProjectedCRS normalized;
                projCRS = normalized = DefaultProjectedCRS.castOrCopy(projCRS);
                normalized = normalized.forConvention(AxesConvention.NORMALIZED);
                if (normalized != projCRS) {
                    toNormalized = CRS.findOperation(projCRS, normalized, null).getMathTransform();
                    projection = normalized.getConversionFromBase();
                } else {
                    toNormalized = null;            // ProjectedCRS (UTM or UPS) is already normalized.
                }
            } else {
                toNormalized = null;    // ProjectedCRS is neither UTM or UPS — will need full reprojection.
            }
            /*
             * We will also need the transformation from the normalized projected CRS to latitude and
             * longitude (in that order) in degrees. We can get this transform directly from the
             * projected CRS if its base CRS already has the expected axis orientations and units.
             */
            GeographicCRS geographic = projCRS.getBaseCRS();
            GeographicCRS standard = datum.geographic();
            if (Utilities.equalsIgnoreMetadata(geographic.getCoordinateSystem(), standard.getCoordinateSystem())) {
                toGeographic = projection.getMathTransform().inverse();
            } else {
                toGeographic = CRS.findOperation(projCRS, standard, null).getMathTransform();
            }
        } else {
            crsZone      = 0;
            toNormalized = null;
            toGeographic = null;
            // TODO
        }
    }

    /**
     * Returns the band letter for the given latitude. It is caller responsibility to ensure that the
     * given latitude is between {@value #UTM_SOUTH_BOUNDS} and {@value #UTM_NORTH_BOUNDS} inclusive.
     * The returned letter will be one of {@code "CDEFGHJKLMNPQRSTUVWX"} (note that I and O letters
     * are excluded). All bands are 8° height except the X band which is 12° height.
     *
     * @param  φ  the latitude in degrees for which to get the band letter.
     * @return the band letter for the given latitude.
     */
    static char latitudeBand(final double φ) {
        int band = 'C' + (int) ((φ - UTM_SOUTH_BOUNDS) / LATITUDE_BAND_HEIGHT);
        if (band >= EXCLUDE_I && ++band >= EXCLUDE_O && ++band == 'Y') {
            band = 'X';         // Because the last latitude band ('X') is 12° height instead of 8°.
        }
        return (char) band;
    }

    /**
     * Computes the UTM zone for the given longitude and latitude band.
     * Those zones are normally the same than UTM, except for Norway and
     * Svalbard which have special rules.
     *
     * @param  zone  the value of {@code TransverseMercator.Zoner.UTM.zone(λ)}.
     * @param  band  the latitude band computed by {@link #latitudeBand(double)}.
     * @param  λ     the longitude for which to compute the UTM zone.
     * @return the UTM zone for the given longitude.
     */
    static int zone(int zone, final double λ, final char band) {
        switch (band) {
            /*
             * Zone 32 has been widened to 9° (at the expense of zone 31)
             * between latitudes 56° and 64° to accommodate southwest Norway.
             */
            case 'V': {
                if (zone == 31 && λ >= 3) zone++;           // 3° is the central meridian of zone 31.
                break;
            }
            /*
             * Between 72° and 84°, zones 33 and 35 have been widened to 12° to accommodate Svalbard.
             * To compensate for these 12° wide zones, zones 31 and 37 are widened to 9° and zones 32,
             * 34, and 36 are eliminated.
             */
            case 'X': {
                switch (zone) {
                    case 32: if (λ >=  9) zone++; else zone--; break;   //  9° is zone 32 central meridian.
                    case 34: if (λ >= 21) zone++; else zone--; break;   // 21° is zone 34 central meridian.
                    case 36: if (λ >= 33) zone++; else zone--; break;   // 33° is zone 36 central meridian.
                }
                break;
            }
        }
        return zone;
    }

    /**
     * Encodes the given position into a MGRS label. It is caller responsibility to ensure that the
     * position CRS is the same than the CRS specified at this {@code MGRSEncoder} creation time.
     *
     * @param  position  the direct position to format as a MGRS label.
     * @param  digits    number of digits to use for formatting the numerical part of a MGRS label.
     * @param  buffer    where to format the direct position.
     */
    void encode(DirectPosition position, final int digits, final StringBuilder buffer) throws TransformException {
        if (toNormalized != null) {
            position = toNormalized.transform(position, null);
        }
        final DirectPosition geographic;
        if (crsZone != 0) {
            geographic = toGeographic.transform(position, null);
            final double φ = geographic.getOrdinate(0);
            if (φ >= UTM_SOUTH_BOUNDS && φ <= UTM_NORTH_BOUNDS) {
                final boolean isNorth = MathFunctions.isPositive(φ);
                final char    band    = latitudeBand(φ);
                final double  λ       = geographic.getOrdinate(1);
                final int     utmZone = TransverseMercator.Zoner.UTM.zone(λ);
                final int     zone    = zone(utmZone, λ, band);
                if ((isNorth ? zone : -zone) != crsZone) try {
                    final double cl = TransverseMercator.Zoner.UTM.centralMeridian(zone);
                    position = CRS.findOperation(datum.geographic(), datum.UTM(φ, cl), null)
                            .getMathTransform().transform(geographic, null);
                } catch (FactoryException e) {
                    throw new TransformException(e.toString(), e);
                }
                buffer.append(zone).append(band);
                if (digits >= 0) {
                    /*
                     * Specification said that 100,000-meters columns are lettered from A through Z (omitting I and O)
                     * starting at the 180° meridian, proceeding easterly for 18°, and repeating for each 18° intervals.
                     * Since a UTM zone is 6° width, a 18° interval is exactly 3 standard UTM zones (not the zone number
                     * modified by the zone(…) method). Columns in zone 1 are A-H, zone 2 are J-R (skipping O), zone 3
                     * are S-Z, then repeating every 3 zones.
                     */
                    final double x = position.getOrdinate(0);
                    final double y = position.getOrdinate(1);
                    final double cx = Math.floor(x / GRID_SQUARE_SIZE);
                    final double cy = Math.floor(y / GRID_SQUARE_SIZE);
                    int col = (int) cx;
                    if (col < 1 || col > 8) {
                        /*
                         * UTM northing values at the equator range from 166021 to 833979 meters approximatively
                         * (WGS84 ellipsoid). Consequently 'cx' ranges from approximatively 1.66 to 8.34, so 'c'
                         * should range from 1 to 8.
                         */
                        throw new TransformException(Errors.format(Errors.Keys.OutsideDomainOfValidity));
                    }
                    switch (utmZone % 3) {                          // First A-H sequence starts at zone number 1.
                        case 1: col += ('A' - 1); break;
                        case 2: col += ('J' - 1); if (col >= EXCLUDE_O) col++; break;
                        case 0: col += ('S' - 1); break;
                    }
                    /*
                     * Rows in odd  zones are ABCDEFGHJKLMNPQRSTUV
                     * Rows in even zones are FGHJKLMNPQRSTUVABCDE
                     * Those 20 letters are repeated in a cycle.
                     */
                    int row = (int) cy;
                    if ((zone & 1) == 0) {
                        row += ('F' - 'A');
                    }
                    row = 'A' + (row % 20);
                    if (row >= EXCLUDE_I && ++row >= EXCLUDE_O) row++;
                    buffer.append((char) col).append((char) row);
                    /*
                     * Numerical location at the given precision.
                     * The specification requires us to truncate the number, not to round it.
                     */
                    if (digits > 0) {
                        final double precision = MathFunctions.pow10(METRE_PRECISION_DIGITS - digits);
                        append(buffer, (int) ((x - cx * GRID_SQUARE_SIZE) / precision), digits);
                        append(buffer, (int) ((y - cy * GRID_SQUARE_SIZE) / precision), digits);
                    }
                }
            }
        }
    }

    /**
     * Appends the given value in the given buffer, padding with zero digits in order to get
     * the specified total amount of digits.
     */
    private static void append(final StringBuilder buffer, final int value, int digits) throws TransformException {
        if (value >= 0) {
            final int p = buffer.length();
            digits -= (buffer.append(value).length() - p);
            if (digits >= 0) {
                StringBuilders.repeat(buffer, p, '0', digits);
                return;
            }
        }
        throw new TransformException(Errors.format(Errors.Keys.OutsideDomainOfValidity));
    }
}

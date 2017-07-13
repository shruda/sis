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
package org.apache.sis.internal.netcdf.ucar;

import org.apache.sis.internal.netcdf.DiscreteSampling;
import ucar.nc2.ft.FeatureCollection;

// Branch-dependent imports
import org.apache.sis.internal.jdk8.Stream;
import org.opengis.feature.Feature;


/**
 * A wrapper around the UCAR {@code ucar.nc2.ft} package.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 0.8
 * @since   0.8
 * @module
 */
final class FeaturesWrapper extends DiscreteSampling {
    /**
     * The feature dataset provided by the UCAR library.
     */
    private final FeatureCollection features;

    /**
     * Creates a new discrete sampling parser.
     */
    FeaturesWrapper(final FeatureCollection features) {
        this.features = features;
    }


    /**
     * Returns the stream of features.
     */
    @Override
    public Stream<Feature> features() {
        throw new UnsupportedOperationException();      // TODO
    }
}

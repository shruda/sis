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
package org.apache.sis.parameter;

import java.util.Map;
import org.opengis.util.InternationalString;
import org.opengis.parameter.ParameterDirection;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.apache.sis.referencing.AbstractIdentifiedObject;


/**
 * Base class of parameter descriptors.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.5
 * @version 0.5
 * @module
 */
abstract class AbstractParameterDescriptor extends AbstractIdentifiedObject implements GeneralParameterDescriptor {

    protected AbstractParameterDescriptor(final Map<String,?> properties) {
        super(properties);
    }

    protected AbstractParameterDescriptor(final GeneralParameterDescriptor other) {
        super(other);
    }

    @Override
    public ParameterDirection getDirection() {
        return ParameterDirection.IN;
    }

    @Override
    public InternationalString getDescription() {
        return null;
    }

    /**
     * The minimum number of times that values for this parameter group or parameter are required.
     * The default value is 1. A value of 0 means an optional parameter.
     *
     * @return The minimum occurrence.
     */
    @Override
    public int getMinimumOccurs() {
        return 1;
    }

    /**
     * The maximum number of times that values for this parameter group or parameter can be included.
     * The default value is 1. A value grater than 1 means a repeatable parameter.
     *
     * @return The maximum occurrence.
     */
    @Override
    public int getMaximumOccurs() {
        return 1;
    }
}
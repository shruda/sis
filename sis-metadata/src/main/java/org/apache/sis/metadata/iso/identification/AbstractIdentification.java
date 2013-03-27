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
package org.apache.sis.metadata.iso.identification;

import java.util.Collection;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.constraint.Constraints;
import org.opengis.metadata.distribution.Format;
import org.opengis.metadata.identification.AggregateInformation;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.identification.DataIdentification;
import org.opengis.metadata.identification.ServiceIdentification;
import org.opengis.metadata.identification.BrowseGraphic;
import org.opengis.metadata.identification.Keywords;
import org.opengis.metadata.identification.Progress;
import org.opengis.metadata.identification.Usage;
import org.opengis.metadata.maintenance.MaintenanceInformation;
import org.opengis.util.InternationalString;
import org.apache.sis.metadata.iso.ISOMetadata;
import org.apache.sis.util.iso.Types;


/**
 * Basic information required to uniquely identify a resource or resources.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @author  Touraïvane (IRD)
 * @author  Cédric Briançon (Geomatys)
 * @since   0.3 (derived from geotk-2.1)
 * @version 0.3
 * @module
 */
@XmlType(name = "AbstractMD_Identification_Type", propOrder = {
    "citation", "abstract", "purpose", "credits", "status", "pointOfContacts",
    "resourceMaintenances", "graphicOverviews", "resourceFormats", "descriptiveKeywords",
    "resourceSpecificUsages", "resourceConstraints", "aggregationInfo"
})
@XmlRootElement(name = "MD_Identification")
@XmlSeeAlso({
    DefaultDataIdentification.class,
    DefaultServiceIdentification.class
})
public class AbstractIdentification extends ISOMetadata implements Identification {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 5794381277658853611L;

    /**
     * Citation data for the resource(s).
     */
    private Citation citation;

    /**
     * Brief narrative summary of the content of the resource(s).
     */
    private InternationalString abstracts;

    /**
     * Summary of the intentions with which the resource(s) was developed.
     */
    private InternationalString purpose;

    /**
     * Recognition of those who contributed to the resource(s).
     */
    private Collection<String> credits;

    /**
     * Status of the resource(s).
     */
    private Collection<Progress> status;

    /**
     * Identification of, and means of communication with, person(s) and organizations(s)
     * associated with the resource(s).
     */
    private Collection<ResponsibleParty> pointOfContacts;

    /**
     * Provides information about the frequency of resource updates, and the scope of those updates.
     */
    private Collection<MaintenanceInformation> resourceMaintenances;

    /**
     * Provides a graphic that illustrates the resource(s) (should include a legend for the graphic).
     */
    private Collection<BrowseGraphic> graphicOverviews;

    /**
     * Provides a description of the format of the resource(s).
     */
    private Collection<Format> resourceFormats;

    /**
     * Provides category keywords, their type, and reference source.
     */
    private Collection<Keywords> descriptiveKeywords;

    /**
     * Provides basic information about specific application(s) for which the resource(s)
     * has/have been or is being used by different users.
     */
    private Collection<Usage> resourceSpecificUsages;

    /**
     * Provides information about constraints which apply to the resource(s).
     */
    private Collection<Constraints> resourceConstraints;

    /**
     * Provides aggregate dataset information.
     */
    private Collection<AggregateInformation> aggregationInfo;

    /**
     * Constructs an initially empty identification.
     */
    public AbstractIdentification() {
    }

    /**
     * Creates an identification initialized to the specified values.
     *
     * @param citation  The citation data for the resource(s), or {@code null} if none.
     * @param abstracts A brief narrative summary of the content of the resource(s), or {@code null} if none.
     */
    public AbstractIdentification(final Citation citation, final CharSequence abstracts) {
        this.citation = citation;
        this.abstracts = Types.toInternationalString(abstracts);
    }

    /**
     * Returns a SIS metadata implementation with the same values than the given arbitrary
     * implementation. If the given object is {@code null}, then this method returns {@code null}.
     * Otherwise if the given object is already a SIS implementation, then the given object is
     * returned unchanged. Otherwise a new SIS implementation is created and initialized to the
     * property values of the given object, using a <cite>shallow</cite> copy operation
     * (i.e. properties are not cloned).
     *
     * <p>This method checks for the {@link DataIdentification} and {@link ServiceIdentification}
     * sub-interfaces. If one of those interfaces is found, then this method delegates to
     * the corresponding {@code castOrCopy} static method. If the given object implements more
     * than one of the above-cited interfaces, then the {@code castOrCopy} method to be used is
     * unspecified.</p>
     *
     * @param  object The object to get as a SIS implementation, or {@code null} if none.
     * @return A SIS implementation containing the values of the given object (may be the
     *         given object itself), or {@code null} if the argument was null.
     */
    public static AbstractIdentification castOrCopy(final Identification object) {
        if (object instanceof DataIdentification) {
            return DefaultDataIdentification.castOrCopy((DataIdentification) object);
        }
        if (object instanceof ServiceIdentification) {
            return DefaultServiceIdentification.castOrCopy((ServiceIdentification) object);
        }
        if (object == null || object instanceof AbstractIdentification) {
            return (AbstractIdentification) object;
        }
        final AbstractIdentification copy = new AbstractIdentification();
        copy.shallowCopy(object);
        return copy;
    }

    /**
     * Returns the citation data for the resource(s).
     */
    @Override
    @XmlElement(name = "citation", required = true)
    public synchronized Citation getCitation() {
        return citation;
    }

    /**
     * Sets the citation data for the resource(s).
     *
     * @param newValue The new citation.
     */
    public synchronized void setCitation(final Citation newValue) {
        checkWritePermission();
        citation = newValue;
    }

    /**
     * Returns a brief narrative summary of the content of the resource(s).
     */
    @Override
    @XmlElement(name = "abstract", required = true)
    public synchronized InternationalString getAbstract() {
        return abstracts;
    }

    /**
     * Sets a brief narrative summary of the content of the resource(s).
     *
     * @param newValue The new abstract.
     */
    public synchronized void setAbstract(final InternationalString newValue) {
        checkWritePermission();
        abstracts = newValue;
    }

    /**
     * Returns a summary of the intentions with which the resource(s) was developed.
     */
    @Override
    @XmlElement(name = "purpose")
    public synchronized InternationalString getPurpose() {
        return purpose;
    }

    /**
     * Sets a summary of the intentions with which the resource(s) was developed.
     *
     * @param newValue The new purpose.
     */
    public synchronized void setPurpose(final InternationalString newValue) {
        checkWritePermission();
        purpose = newValue;
    }

    /**
     * Returns the recognition of those who contributed to the resource(s).
     */
    @Override
    @XmlElement(name = "credit")
    public synchronized Collection<String> getCredits() {
        return credits = nonNullCollection(credits, String.class);
    }

    /**
     * Sets the recognition of those who contributed to the resource(s).
     *
     * @param newValues The new credits.
     */
    public synchronized void setCredits(final Collection<? extends String> newValues) {
        credits = writeCollection(newValues, credits, String.class);
    }

    /**
     * Returns the status of the resource(s).
     */
    @Override
    @XmlElement(name = "status")
    public synchronized Collection<Progress> getStatus() {
        return status = nonNullCollection(status, Progress.class);
    }

    /**
     * Sets the status of the resource(s).
     *
     * @param newValues The new status.
     */
    public synchronized void setStatus(final Collection<? extends Progress> newValues) {
        status = writeCollection(newValues, status, Progress.class);
    }

    /**
     * Returns the identification of, and means of communication with, person(s) and organizations(s)
     * associated with the resource(s).
     */
    @Override
    @XmlElement(name = "pointOfContact")
    public synchronized Collection<ResponsibleParty> getPointOfContacts() {
        return pointOfContacts = nonNullCollection(pointOfContacts, ResponsibleParty.class);
    }

    /**
     * Sets the point of contacts.
     *
     * @param newValues The new points of contacts.
     */
    public synchronized void setPointOfContacts(final Collection<? extends ResponsibleParty> newValues) {
        pointOfContacts = writeCollection(newValues, pointOfContacts, ResponsibleParty.class);
    }

    /**
     * Provides information about the frequency of resource updates, and the scope of those updates.
     */
    @Override
    @XmlElement(name = "resourceMaintenance")
    public synchronized Collection<MaintenanceInformation> getResourceMaintenances() {
        return resourceMaintenances = nonNullCollection(resourceMaintenances, MaintenanceInformation.class);
    }

    /**
     * Sets information about the frequency of resource updates, and the scope of those updates.
     *
     * @param newValues The new resource maintenance info.
     */
    public synchronized void setResourceMaintenances(final Collection<? extends MaintenanceInformation> newValues) {
        resourceMaintenances = writeCollection(newValues, resourceMaintenances, MaintenanceInformation.class);
    }

    /**
     * Provides a graphic that illustrates the resource(s) (should include a legend for the graphic).
     */
    @Override
    @XmlElement(name = "graphicOverview")
    public synchronized Collection<BrowseGraphic> getGraphicOverviews() {
        return graphicOverviews = nonNullCollection(graphicOverviews, BrowseGraphic.class);
    }

    /**
     * Sets a graphic that illustrates the resource(s).
     *
     * @param newValues The new graphics overviews.
     */
    public synchronized void setGraphicOverviews(final Collection<? extends BrowseGraphic> newValues) {
        graphicOverviews = writeCollection(newValues, graphicOverviews, BrowseGraphic.class);
    }

    /**
     * Provides a description of the format of the resource(s).
     */
    @Override
    @XmlElement(name = "resourceFormat")
    public synchronized Collection<Format> getResourceFormats() {
        return resourceFormats = nonNullCollection(resourceFormats, Format.class);
    }

    /**
     * Sets a description of the format of the resource(s).
     *
     * @param newValues The new resource format.
     */
    public synchronized void setResourceFormats(final Collection<? extends Format> newValues) {
        resourceFormats = writeCollection(newValues, resourceFormats, Format.class);
    }

    /**
     * Provides category keywords, their type, and reference source.
     */
    @Override
    @XmlElement(name = "descriptiveKeywords")
    public synchronized Collection<Keywords> getDescriptiveKeywords() {
        return descriptiveKeywords = nonNullCollection(descriptiveKeywords, Keywords.class);
    }

    /**
     * Sets category keywords, their type, and reference source.
     *
     * @param newValues The new descriptive keywords.
     */
    public synchronized void setDescriptiveKeywords(final Collection<? extends Keywords> newValues) {
        descriptiveKeywords = writeCollection(newValues, descriptiveKeywords, Keywords.class);
    }

    /**
     * Provides basic information about specific application(s) for which the resource(s)
     * has/have been or is being used by different users.
     */
    @Override
    @XmlElement(name = "resourceSpecificUsage")
    public synchronized Collection<Usage> getResourceSpecificUsages() {
        return resourceSpecificUsages = nonNullCollection(resourceSpecificUsages, Usage.class);
    }

    /**
     * Sets basic information about specific application(s).
     *
     * @param newValues The new resource specific usages.
     */
    public synchronized void setResourceSpecificUsages(final Collection<? extends Usage> newValues) {
        resourceSpecificUsages = writeCollection(newValues, resourceSpecificUsages, Usage.class);
    }

    /**
     * Provides information about constraints which apply to the resource(s).
     */
    @Override
    @XmlElement(name = "resourceConstraints")
    public synchronized Collection<Constraints> getResourceConstraints() {
        return resourceConstraints = nonNullCollection(resourceConstraints, Constraints.class);
    }

    /**
     * Sets information about constraints which apply to the resource(s).
     *
     * @param newValues The new resource constraints.
     */
    public synchronized void setResourceConstraints(final Collection<? extends Constraints> newValues) {
        resourceConstraints = writeCollection(newValues, resourceConstraints, Constraints.class);
    }

    /**
     * Provides aggregate dataset information.
     */
    @Override
    @XmlElement(name = "aggregationInfo")
    public synchronized Collection<AggregateInformation> getAggregationInfo() {
        return aggregationInfo = nonNullCollection(aggregationInfo, AggregateInformation.class);
    }

    /**
     * Sets aggregate dataset information.
     *
     * @param newValues The new aggregation info.
     */
    public synchronized void setAggregationInfo(final Collection<? extends AggregateInformation> newValues) {
        aggregationInfo = writeCollection(newValues, aggregationInfo, AggregateInformation.class);
    }
}

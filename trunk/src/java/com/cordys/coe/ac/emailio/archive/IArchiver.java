package com.cordys.coe.ac.emailio.archive;

import com.cordys.coe.ac.emailio.config.IEmailIOConfiguration;
import com.cordys.coe.ac.emailio.exception.ArchiverException;
import com.cordys.coe.ac.emailio.objects.ContextContainer;
import com.cordys.coe.ac.emailio.objects.IStorageProviderQueryManager;

import com.eibus.management.IManagedComponent;

import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.List;

/**
 * This interface describes the methods that an archiver needs. The archiver is responsible for
 * moving data from the operational environment to a 'parked' environment.
 *
 * @author  pgussow
 */
public interface IArchiver
{
    /**
     * Holds the name of the attribute which holds the parameter name.
     */
    String ATTRIBUTE_NAME = "name";
    /**
     * Holds the name of the attribute type.
     */
    String ATTRIBUTE_TYPE = "type";
    /**
     * Holds the name of the tag that identifies a parameter.
     */
    String ELEMENT_PARAMETER = "parameter";
    /**
     * Holds the name of the tag params which wraps the parameters.
     */
    String ELEMENT_PARAMETERS = "parameters";

    /**
     * This method starts the actual archiving of the passed on context containers.
     *
     * @param   lccContainers  The containers to archive.
     * @param   spqmManager    The QueryManager to use.
     *
     * @throws  ArchiverException  In case of any exceptions.
     */
    void doArchive(List<ContextContainer> lccContainers, IStorageProviderQueryManager spqmManager)
            throws ArchiverException;

    /**
     * This method initializes the archiver if needed.
     *
     * @param   iParameters       The parameters for this archiver.
     * @param   xmi               The XpathMetaInfo. the prefix ns contains the proper namespace.
     * @param   iecConfiguration  The configuration of the connector.
     * @param   sOrganization     The organization the archiver runs in.
     * @param   mcParent          The managed component parent.
     *
     * @throws  ArchiverException  In case of any exceptions.
     */
    void initialize(int iParameters, XPathMetaInfo xmi, IEmailIOConfiguration iecConfiguration,
                    String sOrganization, IManagedComponent mcParent)
             throws ArchiverException;
}

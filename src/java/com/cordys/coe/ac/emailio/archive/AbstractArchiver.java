package com.cordys.coe.ac.emailio.archive;

import com.cordys.coe.ac.emailio.config.IEmailIOConfiguration;
import com.cordys.coe.ac.emailio.exception.ArchiverException;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.management.IManagedComponent;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This base class is used for the archivers. It will already parse the parameters passed on.
 *
 * @author  pgussow
 */
public abstract class AbstractArchiver
    implements IArchiver
{
    /**
     * Holds the configuration of the connector.
     */
    private IEmailIOConfiguration m_iecConfig;
    /**
     * Holds the parameters and their value.
     */
    private Map<String, Object> m_mParameters = new LinkedHashMap<String, Object>();

    /**
     * This method gets the configuration of the connector.
     *
     * @return  The configuration of the connector.
     */
    public IEmailIOConfiguration getConfiguration()
    {
        return m_iecConfig;
    }

    /**
     * Returns a map of the parameters that were present in the configuration block.
     *
     * <p>This method is public primarily to make testing easier. It should not be called by
     * external code under normal conditions.</p>
     *
     * @return  The map of the parameters that were present in the configuration block.
     */
    public Map<String, Object> getParameters()
    {
        return new LinkedHashMap<String, Object>(m_mParameters);
    }

    /**
     * This method initializes the archiver if needed.
     *
     * @param   iParameters    The parameters for this archiver.
     * @param   xmi            The XpathMetaInfo. the prefix ns contains the proper namespace.
     * @param   iecConfig      The configuration of the connector.
     * @param   sOrganization  The organization the archiver runs in.
     * @param   mcParent       The managed component parent.
     *
     * @throws  ArchiverException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.archive.IArchiver#initialize(int, XPathMetaInfo,
     *          IEmailIOConfiguration, String, IManagedComponent)
     */
    @Override public void initialize(int iParameters, XPathMetaInfo xmi,
                                     IEmailIOConfiguration iecConfig, String sOrganization,
                                     IManagedComponent mcParent)
                              throws ArchiverException
    {
        m_iecConfig = iecConfig;

        if (iParameters > 0)
        {
            int[] aiParameters = XPathHelper.selectNodes(iParameters, "./ns:" + ELEMENT_PARAMETER,
                                                         xmi);

            for (int iParameter : aiParameters)
            {
                String sName = Node.getAttribute(iParameter, ATTRIBUTE_NAME);
                String sType = Node.getAttribute(iParameter, ATTRIBUTE_TYPE, "string");
                Object oValue = null;

                if ("string".equals(sType))
                {
                    oValue = Node.getDataWithDefault(iParameter, "");
                }
                else if ("xml".equals(sType))
                {
                    oValue = new Integer(Node.getFirstElement(iParameter));
                }

                if ((sName != null) && (sName.length() > 0))
                {
                    m_mParameters.put(sName, oValue);
                }
            }
        }

        postInit(iParameters, xmi, sOrganization, mcParent);
    }

    /**
     * This method gets the string value for a parameter.
     *
     * @param   sName  The name of the parameter.
     *
     * @return  The string value for a parameter.
     */
    protected String getStringParameter(String sName)
    {
        return (String) m_mParameters.get(sName);
    }

    /**
     * This method gets the value of the XML parameter.
     *
     * @param   sName  The name of the parameter.
     *
     * @return  The value of the XML parameter.
     */
    protected int getXMLParameter(String sName)
    {
        int iReturn = 0;

        Object oTemp = m_mParameters.get(sName);

        if ((oTemp != null) && (oTemp instanceof Integer))
        {
            iReturn = (Integer) oTemp;
        }

        return iReturn;
    }

    /**
     * Adapter method for extending classes. Equivalent of the initialize method.
     *
     * @param   iParameters    The passed on parameters.
     * @param   xmi            The namespace/prefix mapping.
     * @param   sOrganization  The organization the archiver runs in.
     * @param   mcParent       The managed component parent.
     *
     * @throws  ArchiverException  In case of any exceptions.
     */
    protected void postInit(int iParameters, XPathMetaInfo xmi, String sOrganization,
                            IManagedComponent mcParent)
                     throws ArchiverException
    {
    }
}

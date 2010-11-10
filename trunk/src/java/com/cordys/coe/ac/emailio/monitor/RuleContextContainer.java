package com.cordys.coe.ac.emailio.monitor;

import com.cordys.coe.ac.emailio.config.rule.IRuleContext;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This container stores the.
 *
 * @author  pgussow
 */
public class RuleContextContainer extends ArrayList<RuleContext>
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(RuleContextContainer.class);
    /**
     * Holds the unique ID that the storage engine assigned to this container.
     */
    private String m_sStorageID;

    /**
     * Creates a new RuleContextContainer object.
     */
    public RuleContextContainer()
    {
        super();
    }

    /**
     * Creates a new RuleContextContainer object.
     *
     * @param  cCollection  The source collection.
     */
    public RuleContextContainer(Collection<RuleContext> cCollection)
    {
        super(cCollection);
    }

    /**
     * Creates a new RuleContextContainer object.
     *
     * @param  iInitialCapacity  The initial capacity for the collection.
     */
    public RuleContextContainer(int iInitialCapacity)
    {
        super(iInitialCapacity);
    }

    /**
     * This method adds the container detail XML to the current context. If multiple messages are
     * part of the container, then the XML is copied to the other rule contexts as well.
     *
     * @param  containerNode  The XML that contains the detail XML.
     */
    public void addContainerDetailsXML(int containerNode)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Adding container Details XML:\n" + Node.writeToString(containerNode, false));
        }

        boolean clone = false;

        synchronized (this)
        {
            for (RuleContext context : this)
            {
                context.putValue(IRuleContext.SYS_XML_STORAGE_DETAILS,
                                 clone ? Node.duplicate(containerNode) : containerNode);
                clone = true;
            }
        }
    }

    /**
     * This method clears the context.
     *
     * @see  com.cordys.coe.ac.emailio.config.rule.IRuleContext#clear()
     */
    @Override public void clear()
    {
        // All NOM nodes in this list have to be freed.
        synchronized (this)
        {
            for (RuleContext rc : this)
            {
                rc.clear();
            }

            // Remove all entries from the internal list.
            clear();
        }
    }

    /**
     * This method gets the unique ID that the storage engine assigned to this container.
     *
     * @return  The unique ID that the storage engine assigned to this container.
     */
    public String getStorageID()
    {
        return m_sStorageID;
    }

    /**
     * This method sets the unique ID that the storage engine assigned to this container.
     *
     * @param  sStorageID  The unique ID that the storage engine assigned to this container.
     */
    public void setStorageID(String sStorageID)
    {
        m_sStorageID = sStorageID;
    }
}

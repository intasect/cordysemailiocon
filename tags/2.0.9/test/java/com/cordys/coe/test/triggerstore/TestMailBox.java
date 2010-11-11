package com.cordys.coe.test.triggerstore;

import com.cordys.coe.ac.emailio.config.EmailBox;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;

/**
 * DOCUMENTME.
 *
 * @author  $author$
 */
public class TestMailBox extends EmailBox
{
    /**
     * Creates a new TestMailBox object.
     *
     * @param   node  DOCUMENTME
     *
     * @throws  EmailIOConfigurationException  DOCUMENTME
     */
    public TestMailBox(int node)
                throws EmailIOConfigurationException
    {
        super(node, 0, "", false);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    @Override public String[] getEmailFolders()
    {
        return new String[0];
    }
}

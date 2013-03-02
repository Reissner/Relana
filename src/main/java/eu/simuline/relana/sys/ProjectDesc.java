package eu.simuline.relana.sys;

import eu.simuline.relana.model.ClassLocator;
import eu.simuline.relana.model.InstanceLocator;

import java.net.URL;

import java.util.Set;

/**
 * ProjectDesc.java
 *
 *
 * Created: Thu Apr 28 21:52:33 2005
 *
 * @author <a href="mailto:ernst@local">Ernst Reissner</a>
 * @version 1.0
 */

public interface ProjectDesc {

    /**
     * Describe <code>getLibrary</code> method here.
     *
     * @return an <code>URL</code> value
     */
    URL getLibrary();

    /**
     * Describe <code>getLocator</code> method here.
     *
     * @return a <code>ClassLocator</code> value
     */
    ClassLocator getBaseClass();

    /**
     * Describe <code>getOutputEffects</code> method here.
     *
     * @return a <code>Set</code> value
     */
    Set<InstanceLocator> getOutputEffects();
    
}// ProjectDesc

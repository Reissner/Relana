package eu.simuline.relana.sys;

import eu.simuline.relana.model.ClassLocator;
import eu.simuline.relana.model.InstanceLocator;
import eu.simuline.relana.model.CClass;// for javadoc only 
import eu.simuline.relana.model.SClass;// for javadoc only 

import java.net.URL;

import java.util.Set;

/**
 * Represents a relana project as specified in a project file 
 * like <code>src/test/resources/eu/simuline/relana/proj.rml</code>. 
 * This comprises the specification of the location of a library as an url 
 * and a base class located within this library. 
 * In addition, the project file comprises a set of output effects 
 * which may be empty. 
 * A library consists of files <code>*.ccl</code> 
 * specifying the {@link CClass}es and of files <code>*.scl</code> 
 * specifying the {@link SClass}es. 
 * The base class is always a {@link CClass}. 
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

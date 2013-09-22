package eu.simuline.relana.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Locates a class within a library. 
 *
 *
 * Created: Thu Apr 14 23:42:03 2005
 *
 * @author <a href="mailto:ernst@local">Ernst Reissner</a>
 * @version 1.0
 */
public final class InstanceLocator {

    /* -------------------------------------------------------------------- *
     * attributes.                                                          *
     * -------------------------------------------------------------------- */

    /**
     * The name of the class to be located. 
     * Declared final to ensure that {@link #hashCode()} remains unchanged. 
     */
    protected final List<String> path;

    /* -------------------------------------------------------------------- *
     * constructors.                                                        *
     * -------------------------------------------------------------------- */

    public InstanceLocator(List<String> path) {
	if (path.size() == 0) {
	    throw new IllegalArgumentException
		("Found empty location: []. ");
	}
	for (String name : path) {
	    if (name.length() == 0) {
		throw new IllegalArgumentException
		    ("Found empty name in locator: " + path + ". ");
	    }
	    
	}
	
	this.path = Collections.unmodifiableList(path);
    } // InstanceLocator constructor
    
    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    public static InstanceLocator getLocator(List<String> path) {
	return new InstanceLocator(path);
    }

    public static InstanceLocator getLocator(String path) {
	return getLocator
	    (new ArrayList<String>(Arrays.asList(path.split("[.]",-1))));
    }


    public List<String> getPath() {
	return this.path;
    }

    public boolean equals(Object other) {
	if (!(other instanceof InstanceLocator)) {
	    return false;
	}
	return this.path.equals(((InstanceLocator)other).getPath());
    }

    // immutable to allow InstanceLocators as elements of hashsets. 
    public int hashCode() {
	return getPath().hashCode();
    }

    public String toString() {
	return "<InstanceLocator path=\"" + this.path + "\"/>";
    }

} // InstanceLocator

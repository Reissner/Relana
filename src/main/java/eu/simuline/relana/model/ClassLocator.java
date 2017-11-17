package eu.simuline.relana.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Locates a class within a library. 
 *
 *
 * Created: Thu Apr 14 23:42:03 2005
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
public final class ClassLocator {

    /* -------------------------------------------------------------------- *
     * attributes.                                                          *
     * -------------------------------------------------------------------- */

    /**
     * The name of the class to be located. 
     */
    // must be final to guarantee a valid {@link #hashCode()}. 
    protected final String name;

    /**
     * The package of the class to be located. 
     */
    private Package pkg;

    /* -------------------------------------------------------------------- *
     * constructors.                                                        *
     * -------------------------------------------------------------------- */

    public ClassLocator(String name, Package pkg) {
	this.name = name;
	this.pkg = pkg;
    } // ClassLocator constructor

    public ClassLocator(ClassLocator other) {
	this(other.name, other.pkg);
    } // ClassLocator constructor

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    public static ClassLocator getLocator(List<String> path) {
	int idxLast = path.size() - 1;
	String name = path.get(idxLast); // ****
	return new ClassLocator(name,
				Package.getPackage(path.subList(0, idxLast)));
    }

    public static ClassLocator getLocator(String path) {
	return getLocator
	    (new ArrayList<String>(Arrays.asList(path.split("[.]", -1))));
    }

    public String getName() {
	return this.name;
    }

    public Package getPackage() {
	return this.pkg;
    }

    public List<String> getPath() {
	List<String> path = getPackage().getPath();
	path.add(getName());
	return path;
    }

    public boolean equals(Object other) {
	if (!(other instanceof ClassLocator)) {
	    return false;
	}
	return getName().equals(((ClassLocator) other).getName());
    }

    public int hashCode() {
	return getName().hashCode();
    }

    public String toString() {
	return "<ClassLocator name=\"" + this.name + 
	    "\" package=\"" + this.pkg + "\"/>";
    }

} // ClassLocator

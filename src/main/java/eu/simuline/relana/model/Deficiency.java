package eu.simuline.relana.model;

/**
 * Represents a Deficiency. 
 *
 *
 * Created: Thu Apr 14 19:38:02 2005
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
public final class Deficiency {

    public static final Deficiency UNDET = new Deficiency("UNDET");

    /* -------------------------------------------------------------------- *
     * attributes.                                                          *
     * -------------------------------------------------------------------- */

    // must be final to guarantee a valid {@link #hashCode()}. 
    private final String name;

    /* -------------------------------------------------------------------- *
     * constructors.                                                        *
     * -------------------------------------------------------------------- */

    public Deficiency(String name) {
	this.name = name;
    }

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    public String getName() {
	return this.name;
    }

    public String toString() {
	return this.name;
	//return "\n<Deficiency>" + this.name + "</Deficiency>";
    }

    public boolean equals(Object obj) {
	if (!(obj instanceof Deficiency)) {
	    return false;
	}
	return getName().equals(((Deficiency) obj).getName());
    }

    // HashCode is immutable. 
    public int hashCode() {
	return getName().hashCode();
    }
} // Deficiency

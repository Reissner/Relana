package eu.simuline.relana.model;

/**
 * Represents a Deficiency. 
 *
 *
 * Created: Thu Apr 14 19:38:02 2005
 *
 * @author <a href="mailto:ernst@local">Ernst Reissner</a>
 * @version 1.0
 */
public class Deficiency {

    public final static Deficiency UNDET = new Deficiency("UNDET");

    /* -------------------------------------------------------------------- *
     * attributes.                                                          *
     * -------------------------------------------------------------------- */

    private final String name;

    /* -------------------------------------------------------------------- *
     * constructors.                                                        *
     * -------------------------------------------------------------------- */

    public Deficiency(String name) {
	this.name = name;
    } // Deficiency constructor
    
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
	return getName().equals(((Deficiency)obj).getName());
    }

    public int hashCode() {
	return getName().hashCode();
    }
} // Deficiency

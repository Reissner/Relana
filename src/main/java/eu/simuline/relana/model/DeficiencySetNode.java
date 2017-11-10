package eu.simuline.relana.model;

import java.math.BigDecimal;

import java.util.Set;
import java.util.Map;

/**
 * Resolvation of a property within an {@link SClass} 
 * into elementary stochastically independent properties. 
 *
 * Created: Mon Apr 25 15:18:32 2005
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
public final class DeficiencySetNode {

    /* -------------------------------------------------------------------- *
     * attributes.                                                          *
     * -------------------------------------------------------------------- */

    /**
     * The <code>Deficiency</code> to be resolved. 
     */
    private final Deficiency deficiency;

    /**
     * The set of elementary <code>Deficiency</code>s 
     * resolving {@link #deficiency}. 
     */
    private final Set<Deficiency> deficiencySet;


    /* -------------------------------------------------------------------- *
     * constructors.                                                        *
     * -------------------------------------------------------------------- */

    public DeficiencySetNode(Deficiency deficiency,
			     Set<Deficiency> deficiencySet) {
	this.deficiency    = deficiency;
	this.deficiencySet = deficiencySet;
    }


    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    /**
     * Returns the <code>Deficiency</code> to be resolved. 
     *
     * @return 
     *    the <code>Deficiency</code> to be resolved. 
     */
    public Deficiency getDeficiency() {
	return this.deficiency;
    }

    /**
     * Returns the set of elementary <code>Deficiency</code>s 
     * resolving {@link #deficiency}. 
     *
     * @return 
     *    the set of elementary <code>Deficiency</code>s 
     *    resolving {@link #deficiency}. 
     */
    public Set<Deficiency> getDeficiencySet() {
	return this.deficiencySet;
    }

    /**
     * Returns the probability of the wrapped deficiency {@link #deficiency} 
     * if replaced by the set {@link #deficiencySet} of deficiencies 
     * which are assumed to be independent. 
     *
     * @param def2prob
     *    a probability distribution 
     *    the key set of which contains {@link #deficiencySet}. 
     * @return 
     *    the <code>double</code> value 
     *    representing the probability of the deficiency {@link #deficiency} 
     *    or equivalently the probability of the simultaneous occurence of 
     *    {@link #deficiencySet}, independence assumed. 
     */
    public BigDecimal getProb(Map<Deficiency, BigDecimal> def2prob) {
	BigDecimal res = BigDecimal.ONE;
	for (Deficiency def : this.deficiencySet) {
	    res = res.multiply(def2prob.get(def));
	}
	return res;
    }

    public String toString() {
	StringBuffer res = new StringBuffer();
	res.append("<DeficiencySetNode deficiency=\"");
	res.append(this.deficiency);
	res.append("\">");
	res.append(this.deficiencySet);
	res.append("</DeficiencySetNode>");
	return res.toString();
    }
} // DeficiencySetNode

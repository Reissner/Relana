package eu.simuline.relana.model;

import eu.simuline.relana.expressions.Type;
import eu.simuline.relana.expressions.Formula;

import java.util.Set;

/**
 * SInstance.java
 *
 *
 * Created: Thu Apr 14 22:51:12 2005
 *
 * @author <a href="mailto:ernst@local">Ernst Reissner</a>
 * @version 1.0
 */
public final class SInstance {

    /* -------------------------------------------------------------------- *
     * attributes.                                                          *
     * -------------------------------------------------------------------- */

    /**
     * The <code>SClass</code> of this instance. 
     */
    private final Type type;
    private final ProbDistr distr;
    private Formula form;
    String name;// for debugging

    /* -------------------------------------------------------------------- *
     * constructors.                                                        *
     * -------------------------------------------------------------------- */


    public SInstance(Type type, ProbDistr distr, String name) {
	this.type = type;
	this.distr = distr;
	this.name = name;
	assert !type.equals(Type.getEmpty());
	
    } // SInstance constructor
    
    public void setFormula(Formula form) {
	this.form = form;
    }

    public SInstance(SInstance other) {
	this.type  = other.type;
	this.distr = other.distr;
	this.form  = other.form;
	this.name = other.name;
    } // SInstance constructor

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    public Type getType() {
	return this.type;
    }

    public ProbDistr getDistr() {
	return this.distr;
    }

    public Formula getFormula() {
	return this.form;
    }

    public Set<Deficiency> getMin() {
	return getType().getMin();
    }

    /**
     * Returns the <code>SInstance</code> arising from this one 
     * by removing <code>def</code> 
     * in a way that means that <code>def</code> does not occur. 
     * This means that all deficiencies above <code>def</code> 
     * are removed as well. 
     * CAUTION: This may return <code>null</code>. 
     *
     * @param def 
     *    a <code>Deficiency</code> 
     *    minimal within the type of this effect. 
     *    **** is this unique or not? **** 
     * @return 
     *    the <code>SInstance</code> arising from this one 
     *    by assuming that <code>def</code> does not occur for this effect. 
     *    Note that if <code>def</code> is minimal 
     *    within the type of this effect, 
     *    the resulting type is empty. 
     *    Since this is not allowed for <code>SInstance</code>s, 
     *    this method must return <code>null</code>. 
     */
    SInstance remove(Deficiency def) {
	if ((this.distr == null) && (this.form != null)) {
	    throw new IllegalArgumentException("****");
	}
	Type newType = getType().removeAndAbove(def);
	if (newType.equals(Type.getEmpty())) {
	    return null;
	}
	
	SInstance res = new SInstance(newType,
				      this.distr.add(def),
				      this.name);
	return res;
    }

    /**
     * Returns the <code>SInstance</code> arising from this one 
     * by removing <code>def</code> 
     * in a way that means that <code>def</code> occurs. 
     *
     * @param def 
     *    a <code>Deficiency</code> 
     *    minimal within the type of this effect. 
     *    **** is this unique or not? **** 
     * @return 
     *    the <code>SInstance</code> arising from this one 
     *    by assuming that <code>def</code> does occurs for this effect. 
     */
    SInstance add(Deficiency def) {
	if ((this.distr == null) && (this.form != null)) {
	    throw new IllegalArgumentException("****");
	}
	SInstance res = new SInstance(getType() .remove(def),
				      this.distr.remove(def),
				      this.name);

	return res;
    }

    SInstance substitute(SInstance serv, Formula form) {
	SInstance res = new SInstance(this);
	if (this.form != null) {
	    Formula newForm = res.form.substitute(serv, form);
	    res.setFormula(newForm);
	}
	return res;
    }

    // prevent overwriting: enable for (Weak)HashSet/Map 
    public boolean equals(Object obj) {//final 
	return super.equals(obj);
    }

    // enable for (Weak)HashSet/Map 
    public int hashCode() {
	return super.hashCode();
    }

    public String toString() {
	StringBuffer res = new StringBuffer(50);
	res.append("\n<SInstance name=\"");
	res.append(this.name);
	res.append("\">\nformula: ");
	res.append(this.form == null ? "null" : this.form);
	res.append("\n</SInstance>");

	return res.toString();
    }

} // SInstance

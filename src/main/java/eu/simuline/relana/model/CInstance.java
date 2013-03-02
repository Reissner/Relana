package eu.simuline.relana.model;

//import eu.simuline.relana.expressions.Formula;
//import eu.simuline.relana.expressions.Operation;
//import eu.simuline.relana.expressions.Operation;
//import eu.simuline.relana.expressions.Type;

//import java.math.BigDecimal;

//import java.util.Set;
//import java.util.HashSet;
import java.util.Map;
//import java.util.HashMap;
import java.util.TreeMap;
//import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
//import java.util.Comparator;


/**
 * Instance of Component. 
 *
 *
 * Created: Thu Apr 14 23:02:05 2005
 *
 * @author <a href="mailto:ernst@local">Ernst Reissner</a>
 * @version 1.0
 */
public class CInstance {


    /* -------------------------------------------------------------------- *
     * attributes.                                                          *
     * -------------------------------------------------------------------- */

    /**
     * Maps the names of the effects to their instances. 
     */
    private final Map<String,SInstance> effects;

    /**
     * Maps the names of the subcomponents to their instances. 
     */
    private final Map<String,CInstance> components;


    /* -------------------------------------------------------------------- *
     * constructors.                                                        *
     * -------------------------------------------------------------------- */

    /**
     * Creates a new <code>CInstance</code> instance 
     * without effects and subcomponents 
     * (only for class {@link CClass#COMPONENT}). 
     */
    public CInstance() {
	this.effects   = new TreeMap<String,SInstance>();
	this.components = new TreeMap<String,CInstance>();
    } // CInstance constructor

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    /**
     * Adds the given effect under the given name. 
     *
     * @param name 
     *    a <code>String</code> representing the name of <code>sInst</code>. 
     * @param sInst 
     *    an <code>SInstance</code> saved under the given name. 
     */
    void addEffect(String name,SInstance sInst) {
	this.effects.put(name,sInst);
    }

    /**
     * Adds the given component under the given name. 
     *
     * @param name 
     *    a <code>String</code> representing the name of <code>cInst</code>. 
     * @param cInst 
     *    an <code>SInstance</code> saved under the given name. 
     */
    void addComponent(String name,CInstance cInst) {
	this.components.put(name,cInst);
    }

    Map<String,SInstance> getEffects() {
	return this.effects;
    }


    public SInstance getEffect(List<String> path) {
	CInstance comp = this;
	for (int i = 0; i < path.size()-1; i++) {
	    comp = comp.components.get(path.get(i));
	}
	
	return comp.effects.get(path.get(path.size()-1));
    }



    public FlatCInstance flatten() {
	Map<List<String>,SInstance> longName2effect = 
	    new TreeMap<List<String>,SInstance>(FlatCInstance.PATH_CMP);

	// flatten subcomponents 
	String prefix;
	List<String> longName;
	Map<List<String>,SInstance> effects;
	for (Map.Entry<String,CInstance> cEntry 
		 : this.components.entrySet()) {
	    effects = cEntry.getValue().flatten().getEffects();
	    prefix = cEntry.getKey();
	    for (Map.Entry<List<String>,SInstance> sEntry 
		     : effects.entrySet()) {
		
		    longName = new ArrayList<String>(sEntry.getKey());
		    longName.add(0,prefix);
		    longName2effect.put(longName,sEntry.getValue());
	    }
	}
	
	// add top-level effects 
	for (Map.Entry<String,SInstance> sEntry : this.effects.entrySet()) {
	    longName = new ArrayList<String>();
	    longName.add(sEntry.getKey());
	    longName2effect.put(longName,sEntry.getValue());
	}
	
	return new FlatCInstance(longName2effect);
    }

    public String toString() {
	StringBuffer res = new StringBuffer(48);
	res.append("\n<CInstance><Effects>");
	res.append(this.effects);
	res.append("</Effects>\n</CInstance>\n");

	return res.toString();
    }

} // CInstance

package eu.simuline.relana.model;

import eu.simuline.relana.expressions.Formula;
import eu.simuline.relana.expressions.Operation;
//import eu.simuline.relana.expressions.Operation;
//import eu.simuline.relana.expressions.Type;

import java.math.BigDecimal;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
//import java.util.HashMap;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.List;
//import java.util.ArrayList;
import java.util.Comparator;


/**
 * Instance of Component. 
 *
 *
 * Created: Thu Apr 14 23:02:05 2005
 *
 * @author <a href="mailto:ernst@local">Ernst Reissner</a>
 * @version 1.0
 */
public class FlatCInstance {

    /* -------------------------------------------------------------------- *
     * constants.                                                           *
     * -------------------------------------------------------------------- */

    final static Comparator<List<String>> PATH_CMP = 
    new Comparator<List<String>>() {
	    public int compare(List<String> list1,List<String> list2) {
		for (int i = 0; i < Math.min(list1.size(),list2.size()); i++) {
		    int res = list1.get(i).compareTo(list2.get(i));
		    if (res != 0) {
			return res;
		    }
		}
		
		return list1.size()-list2.size();
	    }
	};

    /* -------------------------------------------------------------------- *
     * attributes.                                                          *
     * -------------------------------------------------------------------- */

    /**
     * Maps the names of the effects to their instances. 
     */
    private final Map<List<String>,SInstance> effects;

    /* -------------------------------------------------------------------- *
     * constructors.                                                        *
     * -------------------------------------------------------------------- */

    public FlatCInstance(Map<List<String>,SInstance> effects) {
	this.effects = effects;
    } // FlatCInstance constructor

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    Map<List<String>,SInstance> getEffects() {
	return this.effects;
    }

    public SInstance getEffect(InstanceLocator loc) {
	return getEffect(loc.getPath());
    }

    public SInstance getEffect(List<String> path) {
	return this.effects.get(path);
    }


    /**
     * Returns the <code>FlatCInstance</code> arising from this one 
     * by assuming that <code>def</code> does not occur 
     * within <code>serv</code>. 
     *
     * @param serv 
     *    a <code>SInstance</code> with probability distribution. 
     *    <code>serv.distr != null</code>. 
     * @param def 
     *    a <code>Deficiency</code> 
     *    minimal within the type of <code>serv</code>. 
     *    **** is this unique or not? **** 
     * @return 
     *    a <code>FlatCInstance</code> arising from this one by assuming 
     *    that <code>def</code> does not occur within <code>serv</code>. 
     */
    public FlatCInstance remove(SInstance serv, Deficiency def) {
	SInstance newServ = serv.remove(def);
	if (newServ == null) {
	    // remove would cause empty set. 
	    return substitute(serv, Formula.EMPTY_EXPRESSION);
	}

	Formula newVar = new Formula.Var(newServ,serv.name);
	return substitute(serv,newVar);
    }

    /**
     * Returns the <code>FlatCInstance</code> arising from this one 
     * by assuming that <code>def</code> occurs within <code>serv</code>. 
     *
     * @param serv 
     *    a <code>SInstance</code> with probability distribution. 
     *    <code>serv.distr != null</code>. 
     * @param def 
     *    a <code>Deficiency</code> 
     *    minimal within the type of <code>serv</code>. 
     *    **** is this unique or not? **** 
     * @return 
     *    a <code>FlatCInstance</code> arising from this one 
     *    by assuming that <code>def</code> occurs within <code>serv</code>. 
     */
    public FlatCInstance add(SInstance serv, Deficiency def) {
	
	// create the set {def} with type serv.getType() 
	// This works, because def is minimal within serv.getType() 
	Set<Deficiency> defSet = 
	    new HashSet<Deficiency>();
	defSet.add(def);
	Formula newConst = new Formula.Const(defSet,serv.getType());
	assert serv.getType().isValid(defSet);

	if (serv.getType().asSet().size() == 1) {
	    // def is the only remaining effect 
	    // and removing it implies removing the whole effect. 
	    return substitute(serv,newConst);
	}
	// Here, serv may assume more than one non-empty set. 

	// replace formula "serv" by formula "|({def},newServ)", 
	// where newServ evolves out of serv by changing the type: 
	// removing def and all deficiencies above. 

	// newServ is the effect that occurs by REMOVING def 
	SInstance newServ = serv.add(def);
	Formula newVar = new Formula.Var(newServ,serv.name);
	Set<Formula> args = new HashSet<Formula>();
	args.add(newConst);
	args.add(newVar);
	Formula newComp = Formula.
	    getFormula(Operation.getOperation(Operation.BaseOps.Union)
		       .getEval(null), args);
	
	return substitute(serv,newComp);
    }

    /**
     * Returns the <code>FlatCInstance</code> arising from this one 
     * by substituting <code>serv</code> by <code>form</code> in all effects. 
     *
     * @param serv 
     *    a <code>SInstance</code>. 
     * @param form 
     *    a <code>Formula</code> of appropriate type. 
     *    **** 
     * @return 
     *    a <code>FlatCInstance</code> arising from this one 
     *    by substituting <code>serv</code> by <code>form</code> 
     *    in all effects using {@link SInstance#substitute}. 
     */
    FlatCInstance substitute(SInstance serv, Formula form) {
	Map<List<String>,SInstance> effects = 
	    new TreeMap<List<String>,SInstance>(PATH_CMP);
	for (Map.Entry<List<String>,SInstance> entry 
		 : this.effects.entrySet()) {
	    effects.put(entry.getKey(),
			 entry.getValue().substitute(serv,form));
	}
	
	return new FlatCInstance(effects);
    }

    /**
     * A container comprising an {@link SInstance} 
     * and of one of its minimal {@link Deficiency}s. 
     * This is needed for probability computations: 
     * The variable of the {@link SInstance} is replaced by another one, 
     * eliminating the given {@link Deficiency}. 
     */
    static class InstDef {
	SInstance serv;
	Deficiency def;
	InstDef(SInstance serv, Deficiency def) {
	    this.serv = serv;
	    this.def = def;
	}
    } // class InstDef 

    /**
     * Maps the given effect which is given by a formula, 
     * onto an {@link InstDef} 
     * consisting of a variable with probability distribution 
     * occuring in the formula and its minimal deficiency. 
     * If no such variable exists, the variables within the formula 
     * (which are then all associated with formulae) 
     * are substituted within the root formula, 
     * by their associated formulae. 
     *
     * @param serv 
     *    an <code>SInstance</code> given by a formula. 
     *    **** what if no formula is present? **** 
     * @return 
     *    an <code>InstDef</code> 
     *    consisting of a variable occuring in the formula 
     *    and its minimal deficiency. 
     *    If this does not exist, <code>null</code> is returned. 
     */
    private InstDef instDefic(SInstance serv) {
//System.out.println("+serv: "+serv);

	Formula form = serv.getFormula();
	assert form != null;
// **** no good: impossible to handle effects with distr given directly
	Set<SInstance> vars;

	// in each loop variables with probability distributions are searched 
	// and if none are present, another variable is substituted 
	// by its formula. 
	// This may yield new aspects, unless alll effects are substituted. 
	// ***** the current algorithm which is NOT PERFORMANT **** 
	// loops up to <code>this.effects.keySet().size()</code> times. 
	for (int ind = 0; ind < this.effects.keySet().size(); ind++) {
	    vars = form.getVars();
//System.out.println("form: "+form);
//System.out.println("vars: "+vars);
	    // look for variables with probability distribution 
	    // and essentially return the first found. 
	    for (SInstance var: vars) {
		if (var.getDistr() != null) {
		    // Here, found variable var within formula form with distr. 
		    //System.out.println("var: "+var);
		    //System.out.println("var: "+var.getType());

		    Set<Deficiency> minDefs = var.getType().getMin();
		    assert !minDefs.isEmpty();
		    Iterator<Deficiency> iter = minDefs.iterator();
		    //System.out.println("+form: "+form);
		    return new InstDef(var,iter.next());
		}
	    }
	    // found no variables with probability distribution. 

	    // substitute all variables within form 
	    // (which are all associated with a formula) with their formulae 
	    Formula varForm;
	    for (SInstance var: vars) {
		varForm = var.getFormula();
		form = form.substitute(var,varForm);
	    }
	    serv.setFormula(form);
	}
	// Here, it is sure that form 
	// has no variables with probability distribution 
	// even if other variables are substituted recursively. 
	return null;
    }



    /**
     * Returns the probability 
     * that the effect specified by <code>sPath</code> 
     * is not the empty set. 
     * It is intended to be applied primarily to Boolean effects, 
     * i.e. to effects isomorphic to Boolean ones, 
     * i.e. to one-point effects. 
     * Then this method returns the probability for <code>true</code>. 
     *
     * @param sPath
     *    identifies a effect. 
     * @return 
     *    the probability described above as a <code>BigDecimal</code> value. 
     */
    public BigDecimal getProb(List<String> sPath) {
	SInstance serv = getEffect(sPath);
	// Fetch a effect instance inst within the formula attached with serv 
	// such that s is associated with a probability distribution 
	// with a certain deficiency def. 
	// Then instDef comprises both, inst and def. 
	InstDef instDef = instDefic(serv);
	if (instDef == null) {
	    // the formula attached with serv is constant 
	    // (at least after substitution done by instDefic) 
	    Set<Deficiency> defs = serv.getFormula().getConst();
	    assert defs != null;// i.e. serv.getFormula() is a constant. 
	    return defs.isEmpty() ? BigDecimal.ZERO : BigDecimal.ONE;
	}
	// Here, instDef != null 
	
	// cond is the probability that instDef.def occurs. 
	BigDecimal cond = instDef.serv.getDistr().getProb(instDef.def);

	// create copies of this FlatCInstance 
	// by assuming that def occurs within serv 
	// and that it does not occur, respectively. 
	FlatCInstance cInstP = add   (instDef.serv,instDef.def);
	FlatCInstance cInstM = remove(instDef.serv,instDef.def);
//System.out.println("cInstP: "+cInstP);
//System.out.println("cInstM: "+cInstM);
//System.out.println("this: "+this);
//throw new IllegalStateException();

	return                           cond .multiply(cInstP.getProb(sPath))
	    .add(BigDecimal.ONE.subtract(cond).multiply(cInstM.getProb(sPath)));
    }

    public String toString() {
	StringBuffer res = new StringBuffer(60);
	res.append("\n<FlatCInstance><Effects>");
	res.append(this.effects);
	res.append("</Effects>\n</FlatCInstance>\n");

	return res.toString();
    }

} // FlatCInstance

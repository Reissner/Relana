package eu.simuline.relana.model;

import eu.simuline.relana.expressions.Type;

import java.math.BigDecimal;
import java.math.MathContext;

import java.util.Map;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Stack;
import java.util.Iterator;

/**
 * Represents a probability distribution. 
 *
 *
 * Created: Mon Apr 25 00:33:44 2005
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
public final class ProbDistr {

    /* -------------------------------------------------------------------- *
     * inner classes.                                                       *
     * -------------------------------------------------------------------- */

    /**
     * Enumeration of inverters: the trivial inverter 
     * and the canonical inverter. 
     */
    enum Inverter {
	Identity() {
	    BigDecimal filterProb(BigDecimal prob) {
		return prob;
	    }
	    Set<DeficiencyNode> getSuccsPreds(DeficiencyNode node) {
		return node.getSuccessors();
	    }
	    void addSuccsPreds(DeficiencyNode source,
			       DeficiencyNode target) {
		target.addSuccessors(source.getSuccessors());
	    }
	    String andOr() {
		return "AND";
	    }
	    String condEventName(String event, SortedSet<String> conds) {
		return neg() + event + "|" + andOr() + conds;
	    }
	    String neg() {
		return "";
	    }
	},
	Invert  () {
	    BigDecimal filterProb(BigDecimal prob) {
		return BigDecimal.ONE.subtract(prob);
	    }
	    Set<DeficiencyNode> getSuccsPreds(DeficiencyNode node) {
		return node.getPredecessors();
	    }
	    void addSuccsPreds(DeficiencyNode source,
			       DeficiencyNode target) {
		target.addPredecessors(source.getPredecessors());
	    }
	    String andOr() {
		return "OR";
	    }
	    String condEventName(String event, SortedSet<String> conds) {
		return andOr() + conds + "|" + neg() + event;
	    }
	    String neg() {
		return "~";
	    }
	};

	abstract BigDecimal filterProb(BigDecimal prob);
	abstract Set<DeficiencyNode> getSuccsPreds(DeficiencyNode node);
	abstract void addSuccsPreds(DeficiencyNode source,
				    DeficiencyNode target);
	abstract String andOr();
	abstract String condEventName(String event, SortedSet<String> conds);
	abstract String neg();

    } // enum Inverter 

    /**
     * Contains some information on validity and degeneracy 
     * of a {@link ProbDistr} and also the fundamental figures 
     * to compute probabilities in case of validity and non-degenracy. 
     */
    static class Validator {

	/* ---------------------------------------------------------------- *
	 * attributes.                                                      *
	 * ---------------------------------------------------------------- */

	/**
	 * Maps a deficiency to essentially a set of deficiencies 
	 * it depends on. 
	 */
	private final Map<Deficiency, DeficiencySetNode> extOrdering;

	/**
	 * Maps a deficiency which is either minimal or conditional 
	 * to its probability. 
	 */
	private final Map<Deficiency, BigDecimal> def2prob;

	/**
	 * The <code>Deficiency</code>s in the key set of {@link #def2prob} 
	 * which map to a value greater than <code>1</code>. 
	 */
	private final Set<Deficiency> invalids;

	/**
	 * The <code>Deficiency</code>s in the key set of {@link #def2prob} 
	 * which map to exactly <code>1</code>. 
	 */
	private final Set<Deficiency> degenerates;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	Validator(Map<Deficiency, BigDecimal> def2prob) {
	    this.extOrdering = new HashMap<Deficiency, DeficiencySetNode>();
	    this.def2prob = new HashMap<Deficiency, BigDecimal>(def2prob);
	    this.invalids = new HashSet<Deficiency>();
	    this.degenerates = new HashSet<Deficiency>();
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	void put(Deficiency def, BigDecimal prob) {
	    this.def2prob.put(def, prob);
	    int cmp = prob.compareTo(BigDecimal.ONE);
	    if (cmp == 0) {
		this.degenerates.add(def);
	    }
	    if (cmp > 0) {
		this.invalids.add(def);
	    }
	}

	void put(Deficiency def, DeficiencySetNode dsn) {
	    this.extOrdering.put(def, dsn);
	}

	DeficiencySetNode get(Deficiency def) {
	    return this.extOrdering.get(def);
	}

	boolean isValid() {
	    return this.invalids.isEmpty();
	}

	boolean isDegenerate() {
	    return !this.degenerates.isEmpty();
	}

	Map<Deficiency, BigDecimal> error() {
	    return warningError(this.invalids);
	}

	Map<Deficiency, BigDecimal> warning() {
	    return warningError(this.degenerates);
	}

	Map<Deficiency, BigDecimal> warningError(Set<Deficiency> invOrDeg) {
	    Map<Deficiency, BigDecimal> def2prob = 
		new HashMap<Deficiency, BigDecimal>();
	    for (Deficiency def : invOrDeg) {
		def2prob.put(def, this.def2prob.get(def));
	    }

	    return def2prob;
	}

	BigDecimal getProb(Set<Deficiency> defs) {
	    Set<Deficiency> elDefs = new HashSet<Deficiency>();
	    for (Deficiency def: defs) {
		elDefs.addAll(this.extOrdering.get(def).getDeficiencySet());
	    }
	    BigDecimal result = BigDecimal.ONE;
	    for (Deficiency def: elDefs) {
		result = result.multiply(this.def2prob.get(def));
	    }
	    return result;
	}

	public String toString() {
	    StringBuffer res = new StringBuffer();
	    res.append("<Validator>\nextOrdering: ");
	    res.append(extOrdering);
	    res.append("\ndef2prob: ");
	    res.append(def2prob);
	    res.append("\ninvalids: ");
	    res.append(invalids);
	    res.append("\ndegenerates: ");
	    res.append(degenerates);
	    res.append("</Validator>");
	    return res.toString();
	}
    } // class Validator 

    /* ---------------------------------------------------------------- *
     * attributes.                                                      *
     * ---------------------------------------------------------------- */

    /**
     * The type of this ProbDistr. 
     * Note that this is not  updated by methods such as 
     * {@link #remove} or {@link #add} but only set and used 
     * in the initialization process. 
     */
    private Type type;

    private Map<Deficiency, BigDecimal> def2prob;

    private Map<Inverter, Validator> validators;

    /* ---------------------------------------------------------------- *
     * constructors.                                                    *
     * ---------------------------------------------------------------- */

    public ProbDistr(Type type,
		     Map<Deficiency, BigDecimal> def2prob) {

	checkIn01(def2prob);

	this.type = type;
	this.def2prob = def2prob;
    }

    // already checked by parser: 
    // old2ProbDistr 
    // keys exactly the inner classes, 
    // also distributions fit with inner classes. 
    //
    // also, keys of old2ProbDistr and of def2prob are disjoint 
    // finally, union of keys are exactly deficiencies of type sClass. 
    public ProbDistr(Type type,
		     Map<Deficiency, ProbDistr> old2ProbDistr,
		     Map<Deficiency, BigDecimal> def2prob) {
	this(type, def2prob);
	for (ProbDistr distr : old2ProbDistr.values()) {
	    this.def2prob.putAll(distr.def2prob);
	}
    }

    private static void checkIn01(Map<Deficiency, BigDecimal> def2prob) {
	for (BigDecimal prob : def2prob.values()) {
	    if (prob.compareTo(BigDecimal.ONE ) >= 0 || 
		prob.compareTo(BigDecimal.ZERO) <= 0) {
		throw new IllegalArgumentException
		("Expected probability in (0, 1) but found " + prob + ". ");
	    }
	}
    }

    /* ---------------------------------------------------------------- *
     * methods.                                                         *
     * ---------------------------------------------------------------- */

    // **** needed by getValidator 
    /**
     * Describe <code>init</code> method here.
     *
     * @param inv
     *    determines whether analysis is bottom up or top down. 
     *    Here: just exchanges min and max. 
     * @param minDefs
     *    Invoked with empty set; method collects the minimal Deficiencys. 
     * @param nMinNodes
     *    Invoked with empty set; 
     *    method collects sort of deep copies 
     *    of the non-minimal DeficiencyNodes here. 
     * @return 
     *    a raw <code>Validator</code> for this distribution. 
     *    Here, raw means that nothing is done 
     *    but invocation of the constructor. 
     */
    private Validator init(Inverter inv,
			   Stack<Deficiency> minDefs,
			   Set<DeficiencyNode> nMinNodes) {
	// look for minimum and maximum. 
	for (DeficiencyNode node
		 : getType()
		 .getDeficiency2ordering()
		 .values()) {
	    if (inv.getSuccsPreds(node).isEmpty()) {
		minDefs.push(node.getDeficiency());
	    } else {
		// deep copy 
		DeficiencyNode cpNode = 
		    new DeficiencyNode(node.getDeficiency());
		inv.addSuccsPreds(node, cpNode);
		//cpNode.addSuccessors(node.getSuccessors());
		nMinNodes.add(cpNode);
	    }
	}
	// Here, minDefs/nMinNodes contains the Deficiency(Node)s 
	// with(out) successors to be processed 

	// copy the probabilities of the minimal deficiencies 
	Map<Deficiency, BigDecimal> minDef2prob = 
	    new HashMap<Deficiency, BigDecimal>();
	for (Deficiency minDef : minDefs) {
	    minDef2prob.put(minDef, inv.filterProb(getProb(minDef)));
	}

	return new Validator(minDef2prob);
    }

    // adds intermediate nodes of two classes: 
    // and-nodes and conditional nodes 
    // fill the resulting Map with DeficiencySetNodes 
    // **** currently needed by validate only. 
    /**
     *
     * @param inv
     *    determines whether analysis is bottom up or top down. 
     * @return
     *    a <code>Validator</code>. 
     */
   Validator getValidator(Inverter inv) {
	// minDefs are the Deficiencies 
	// all successors of which are already processed. 
	// In the beginning this is just the minimal Deficiency. 
	Stack<Deficiency> minDefs = new Stack<Deficiency>();
	// The DeficiencyNodes with at least one unprocessed successor. 
	// the predeccessors are irrelevant, 
	// the successors are just the unprocessed ones. 
	// in the very beginning, this includes all but the "minimal" node. 
	Set<DeficiencyNode> nMinNodes = new HashSet<DeficiencyNode>();
	// the resulting map 
	Validator val = init(inv, minDefs, nMinNodes);
	// Here, all variables above are initialized. 

	while (!minDefs.isEmpty()) {
	    // pop element from minDefs and create corresponding 
	    // DeficiencySetNode. 

	    // pop current Deficiency and get proper node....
	    DeficiencyNode node = getType().getDeficiency2ordering()
		.get(minDefs.pop());
	    // ... unwrap its successors (1st iter.: predDefs.isEmpty()). 
	    Set<Deficiency> predDefs = DeficiencyNode.unwrap
		(inv.getSuccsPreds(node));

	    // between node n and its successors s1, ..., sn 
	    // we have to insert &(s1, ..., sn) and n|&(s1, ..., sn) 

	    // of the elements in predDefs get the names and 
	    // union subseqDefs of their representations as sets. 
	    SortedSet<String> namesBelow = new TreeSet<String>();
	    Set<Deficiency> subseqDefs = new HashSet<Deficiency>();
	    for (Deficiency def : predDefs) {
		namesBelow.add(inv.neg() + def.getName());
		// extOrdering.get(def) is defined, i.e. not null 
		subseqDefs.addAll(val.get(def).getDeficiencySet());
	    }
	    // Here, namesBelow are the names and subseqDefs 
	    // the union of the representatives of the entries of predDefs

	    // create the and-node and its probability
	    DeficiencySetNode andDefN = 
		new DeficiencySetNode(new Deficiency(inv.andOr() + namesBelow),
				      subseqDefs);

	    BigDecimal andProb = andDefN.getProb(val.def2prob);
	    // maybe in later implementations andDefN is also added, 
	    // but for now, andDefN is no longer needed. 

	    // add cond-node 
	    String name = inv.condEventName(node.getDeficiency().getName(),
					    namesBelow);
	    Deficiency condDef = new Deficiency(name);
	    BigDecimal condProb = inv.filterProb(getProb(node.getDeficiency()))
		.divide(andProb, MathContext.DECIMAL128);
	    val.put(condDef, condProb);

	    // add DeficiencySetNode for node 
	    Set<Deficiency> subseqCondDefs = 
		new HashSet<Deficiency>(subseqDefs);
	    subseqCondDefs.add(condDef);
	    DeficiencySetNode condDefN = 
		new DeficiencySetNode(node.getDeficiency(), subseqCondDefs);
	    val.put(node.getDeficiency(), condDefN);

	    // update minDefs and nMinNode 
	    Iterator<DeficiencyNode> iter = nMinNodes.iterator();
	    Set<DeficiencyNode> succs;
	    while (iter.hasNext()) {
		DeficiencyNode nMinNode = iter.next();
		succs = inv.getSuccsPreds(nMinNode);
		// remove node
		if (succs.remove(node) && succs.isEmpty()) {
		    iter.remove();
		    minDefs.add(nMinNode.getDeficiency());
		} // else, succs is not empty 
		// Here, node is removed from succs if it was present. 
	    }
	    // Here, node does not occur as a successor 
	    // within a DeficiencyNode in nMinNodes 

	    // also minDefs is the set of deficiencies for nodes 
	    // all successors of which are already processed. 
	} // stil something to be done 

	return val;
    }

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    private Type getType() {
	return this.type;
    }

    // **** what if not defined? 
    public BigDecimal getProb(Deficiency def) {
	BigDecimal res = this.def2prob.get(def);
	if (res == null) {
	    throw new IllegalArgumentException
		("Deficiency \"" + def + "\" is unknown. "); 
	}
	return res;
    }

    public void validate() {
	this.validators = new EnumMap<Inverter, Validator>(Inverter.class);
	validateUp(Inverter.Identity);
	validateUp(Inverter.Invert);
    }

    private void validateUp(Inverter inv) {
	Validator val = getValidator(inv);
	this.validators.put(inv, getValidator(inv));
	if (!val.isValid()) {
	    throw new IllegalStateException("invalid: " + val.error());
	}
	if (val.isDegenerate()) {
	    System.out.println("degenerate: " + val.warning());
	}
   }

    // ****
    ProbDistr remove(Deficiency def) {
	return this;
    }

    // ****
    ProbDistr add(Deficiency def) {
	return this;
    }

    public String toString() {
	StringBuffer res = new StringBuffer();
	res.append("\n<ProbDistr>");
	res.append(this.def2prob);
	res.append("\n</ProbDistr>");
	return res.toString();
    }
} // ProbDistr

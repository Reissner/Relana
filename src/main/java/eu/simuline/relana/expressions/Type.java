package eu.simuline.relana.expressions;

import eu.simuline.relana.model.Deficiency;
import eu.simuline.relana.model.DeficiencyNode;
//import eu.simuline.relana.model.VerifyException;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;

/**
 * Type.java
 *
 *
 * Created: Fri Apr 29 11:18:57 2005
 *
 * @author <a href="mailto:ernst@local">Ernst Reissner</a>
 * @version 1.0
 */
public final class Type {

    public final static Type EMPTY = new Type();
    public final static Type BOOLEAN;

    static {
	Map<Deficiency,DeficiencyNode> deficiency2ordering = 
	    new HashMap<Deficiency,DeficiencyNode>();
	deficiency2ordering.put(Deficiency.UNDET,
				new DeficiencyNode(Deficiency.UNDET));
	BOOLEAN = new Type(deficiency2ordering);
    } // static 

    /* -------------------------------------------------------------------- *
     * attributes.                                                          *
     * -------------------------------------------------------------------- */

    /**
     * Maps declared <code>Deficiency</code>s **********
     * (see 
     * {@link eu.simuline.relana.model.SClass#getDeclaredDeficiency2ordering}) 
     * to their nodes 
     * which determine their predecessors and their successors. 
     * It is required that this relation extends that 
     * given by {@link eu.simuline.relana.model.SClass#superClass}. 
     */
    Map<Deficiency,DeficiencyNode> deficiency2ordering;
    // **** valid after verification only {@link #initMMDefics} 
    Set<Deficiency> minDefs;
    // **** valid after verification only {@link #initMMDefics} 
    Set<Deficiency> maxDefs;


    /* -------------------------------------------------------------------- *
     * constructors.                                                        *
     * -------------------------------------------------------------------- */

    public Type(Map<Deficiency,DeficiencyNode> deficiency2ordering) {
	this.deficiency2ordering = deficiency2ordering;
	assert consistency();
	initMMDefics();
    } // Type constructor

    // deep copy constructor. 
    public Type(Type other) {
	this(other.copy());
    }

    // empty type 
    public Type() {
	this(new HashMap<Deficiency,DeficiencyNode>());
    }

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    /**
     * Returns wheter the successors or the predecessors 
     * of an value of {@link #deficiency2ordering} are all 
     * in the value set of {@link #deficiency2ordering}. 
     */
    private boolean consistency() {
	for (DeficiencyNode node : this.deficiency2ordering.values()) {
	    for (DeficiencyNode succ : node.getSuccessors()) {
		if (this.deficiency2ordering.get(succ.getDeficiency()) != 
		    succ) {
System.out.println("succ.getDeficiency(): "+succ.getDeficiency());
System.out.println("succ: "+ succ);
		    return false;
		}
	    }
	    for (DeficiencyNode pred : node.getPredecessors()) {
		if (this.deficiency2ordering.get(pred.getDeficiency()) != 
		    pred) {
System.out.println("pred.getDeficiency(): "+pred.getDeficiency());
System.out.println("pred: "+ pred);
		    return false;
		}
	    }
	}
	return true;
    }

    public Map<Deficiency,DeficiencyNode> getDeficiency2ordering() {
	return this.deficiency2ordering;
    }


    // **** also initializes fields minDefs and maxDefs
    protected void initMMDefics() {
	this.minDefs = new HashSet<Deficiency>();
	this.maxDefs = new HashSet<Deficiency>();
	
	for (DeficiencyNode node : this.deficiency2ordering.values()) {
	    if (node.getSuccessors().isEmpty()) {
		this.minDefs.add(node.getDeficiency());
	    }
	    if (node.getPredecessors().isEmpty()) {
		this.maxDefs.add(node.getDeficiency());
	    }
	}
    }

    public static Type getEmpty() {
	return new Type();
    }

    protected Map<Deficiency,DeficiencyNode> copy() {
	Map<Deficiency,DeficiencyNode> newDeficiency2ordering = 
	    new HashMap<Deficiency,DeficiencyNode>();
	for (Map.Entry<Deficiency,DeficiencyNode> entry 
		 : this.deficiency2ordering.entrySet()) {
	    newDeficiency2ordering.put(entry.getKey(),
				       new DeficiencyNode(entry.getValue()));
	    assert        newDeficiency2ordering.get(entry.getKey())
		.equals(this.deficiency2ordering.get(entry.getKey()));
	}

	DeficiencyNode node;
	Set<DeficiencyNode> predSuccs, predSuccsNew;
	for (Map.Entry<Deficiency,DeficiencyNode> entry 
		 : newDeficiency2ordering.entrySet()) {
	    node = entry.getValue();
	    
	    predSuccs = node.getPredecessors();
	    predSuccsNew = new HashSet<DeficiencyNode>();
	    for (DeficiencyNode pred : predSuccs) {
		predSuccsNew
		    .add(newDeficiency2ordering.get(pred.getDeficiency()));
	    }
	    predSuccs.clear();
	    predSuccs.addAll(predSuccsNew);

	    predSuccs = node.getSuccessors();
	    predSuccsNew = new HashSet<DeficiencyNode>();
	    for (DeficiencyNode succ : predSuccs) {
		predSuccsNew
		    .add(newDeficiency2ordering.get(succ.getDeficiency()));
	    }
	    predSuccs.clear();
	    predSuccs.addAll(predSuccsNew);
	}


	return newDeficiency2ordering;
    }

    protected DeficiencyNode remove(Map<Deficiency,DeficiencyNode> def2ord,
				    Deficiency def) {
	DeficiencyNode node = def2ord.remove(def);
	if (node == null) {
	    throw new IllegalArgumentException
		("Tried to remove deficiency \"" + def + 
		 "\" which is not present in type " + this + ". ");
	}
	if (!node.getSuccessors().isEmpty()) {
	    throw new IllegalArgumentException
		("Tried to remove deficiency which is not minimal: " + 
		 node.getSuccessors() + " are below. ");
	}
	// Remove link to node in predecessors 
	for (DeficiencyNode pred : node.getPredecessors()) {
	    pred.getSuccessors().remove(node);
	}
	return node;
    }


    /**
     * Returns whether the given set of deficiencies 
     * is allowed by this type. 
     *
     * @param set 
     *    a set of deficiencies for which to decide 
     *    whether this type allows that set. 
     * @return 
     *    a <code>boolean</code> value describing 
     *    whether the given set of deficiencies 
     *    is allowed by this type. 
     *    This includes <code>asSet().containsAll(set)</code>. 
     */
    public boolean isValid(Set<Deficiency> set) {
	DeficiencyNode node;
//	Set<DeficiencyNode> successors;
	for (Deficiency definSet : set) {
	    node = this.deficiency2ordering.get(definSet);
	    if (node == null) {
		return false;
	    }
	    for (DeficiencyNode succ : node.getSuccessors()) {
		if (!set.contains(succ.getDeficiency())) {
		    return false;
		}
	    }
	}
	return true;
    }

    /**
     * Returns <em>a copy</em> to be modified without affecting the original 
     * of the maximal set of deficiencies of this type. 
     *
     * @return
     *    <em>a copy</em> to be modified without affecting the original 
     *    of the maximal set of deficiencies of this type. 
     */
    public Set<Deficiency> asSet() {
	return this.deficiency2ordering.keySet();
    }


    public Type getInverse() {
	Map<Deficiency,DeficiencyNode> invDeficiency2ordering = 
	    new HashMap<Deficiency,DeficiencyNode>();
	for (Map.Entry<Deficiency,DeficiencyNode> entry 
		 : this.deficiency2ordering.entrySet()) {
	    invDeficiency2ordering.put(entry.getKey(),
				       entry.getValue().getInverse());
	}
	return new Type(invDeficiency2ordering);
    }

    public Set<Deficiency> getMin() {
	return this.minDefs;
    }

    public Set<Deficiency> getMax() {
	return this.maxDefs;
    }

    public void addAll(Map<Deficiency,DeficiencyNode> deficiency2ordering) {
	for (Map.Entry<Deficiency,DeficiencyNode> entry 
		 : deficiency2ordering.entrySet()) {
	    DeficiencyNode node = 
		this.deficiency2ordering.get(entry.getKey());

	    if (node == null) {
		node = new DeficiencyNode(entry.getKey());
	    } else {
		node = new DeficiencyNode(node);
	    }
	    
	    node.addAll(entry.getValue());
	    this.deficiency2ordering.put(entry.getKey(),node);
	}
	initMMDefics();
    }

    public void replace(Deficiency oldDef, 
			Deficiency newDefMin,
			Deficiency newDefMax,
			Type type) {
	DeficiencyNode nodeMin = type.getDeficiency2ordering().get(newDefMin);
	DeficiencyNode nodeMax = type.getDeficiency2ordering().get(newDefMax);
	
	boolean assertn = true;
	assertn &= nodeMin.getSuccessors  ().isEmpty();
	assertn &= nodeMax.getPredecessors().isEmpty();

	DeficiencyNode node = this.deficiency2ordering.remove(oldDef);
	for (DeficiencyNode pred : node.getPredecessors()) {
	    assertn &= pred.getSuccessors().remove(node);
	    assertn &= pred.getSuccessors().add(nodeMax);
	}
	nodeMax.addPredecessors(node.getPredecessors());

	for (DeficiencyNode pred : node.getSuccessors()) {
	    assertn &= pred.getPredecessors().remove(node);
	    assertn &= pred.getPredecessors().add(nodeMin);
	}
	nodeMin.addSuccessors(node.getSuccessors());

	assert assertn;
	this.deficiency2ordering.putAll(type.getDeficiency2ordering());
	initMMDefics();
    }





    public Type remove(Deficiency def) {
	Map<Deficiency,DeficiencyNode> newDef2ord = copy();
	remove(newDef2ord,def);
	return new Type(newDef2ord);
    }


    /**
     * Returns a copy of this type 
     * where the given Deficiency, and all Deficiencies above it 
     * are removed. 
     *
     * @param def 
     *    a <code>Deficiency</code> which is assumed 
     *    to be minimal within this type. 
     * @return 
     *    a <code>Type</code> value
     */
    public Type removeAndAbove(Deficiency def) {
	
	Map<Deficiency,DeficiencyNode> newDef2ord = copy();

	Stack<Deficiency> defsToBeRemoved = new Stack<Deficiency>();
	defsToBeRemoved.push(def);
	while (!defsToBeRemoved.empty()) {
	    def = defsToBeRemoved.pop();
	    DeficiencyNode node = remove(newDef2ord,def);
	    for (DeficiencyNode pred : node.getPredecessors()) {
		defsToBeRemoved.push(pred.getDeficiency());
	    }
	}

	return new Type(newDef2ord);
    }

    public boolean implies(Deficiency def1, Deficiency def2) {
	DeficiencyNode node = this.deficiency2ordering.get(def1);
	if (node == null) {
	    throw new IllegalArgumentException
		("Deficiency \"" + def1 + 
		 "\" does not occur within type " + this + ". ");
	}
	Stack<DeficiencyNode> search = new Stack<DeficiencyNode>();
	search.push(node);
	while (!search.empty()) {
	    node = search.pop();
	    if (node.getDeficiency().equals(def2)) {
		return true;
	    }
	    search.addAll(node.getSuccessors());
	}
	return false;
    }

    public Set<Deficiency> getCone(Deficiency def) {
	Set<Deficiency> result = new HashSet<Deficiency>();
	Stack<DeficiencyNode> toBeAdded = new Stack<DeficiencyNode>();
	toBeAdded.push(this.deficiency2ordering.get(def));
	DeficiencyNode node;
	while (!toBeAdded.empty()) {
	    node = toBeAdded.pop();
	    
	    result.add(node.getDeficiency());
	    toBeAdded.addAll(node.getSuccessors());
	}
	
	return result;
    }

    // **** this works properly only if deficiency2ordering is minimal. 
    public boolean equals(Object obj) {
	if (!(obj instanceof Type)) {
	    return false;
	}

	Type other = (Type)obj;
	if (!asSet().equals(other.asSet())) {
	    return false;
	}
	// Here, the types coincide as types. 

	for (Map.Entry<Deficiency,DeficiencyNode> entry 
		 : this.deficiency2ordering.entrySet()) {
	    if (!other.deficiency2ordering.get(entry.getKey())
		.equals(entry.getValue())) {
		return false;
	    }
	}
	// Here, found no deviations 
	return true;
    }

    public int hashCode() {
	return this.deficiency2ordering.hashCode();
    }

    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append("\n<Type>");
	buf.append(this.deficiency2ordering);
	buf.append("</Type>");
	return buf.toString();
    }
} // Type

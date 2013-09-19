package eu.simuline.relana.model;

import java.util.Set;
import java.util.HashSet;

/**
 * Wraps a {@link Deficiency} and serves as a node in the graph 
 * describing the relation <code>\implies</code>. 
 *
 *
 * Created: Thu Apr 14 19:49:45 2005
 *
 * @author <a href="mailto:ernst@local">Ernst Reissner</a>
 * @version 1.0
 */

public class DeficiencyNode {

    /* -------------------------------------------------------------------- *
     * attributes.                                                          *
     * -------------------------------------------------------------------- */

    private Set<DeficiencyNode> predecessors;

    private Set<DeficiencyNode> successors;

    // must be final because of {@link #hashCode()} 
    private final Deficiency deficiency;

    /* -------------------------------------------------------------------- *
     * constructors.                                                        *
     * -------------------------------------------------------------------- */

    public DeficiencyNode(Deficiency deficiency) {
	this(deficiency,
	     new HashSet<DeficiencyNode>(),
	     new HashSet<DeficiencyNode>());
    }

    public DeficiencyNode(Deficiency deficiency,
			  Set<DeficiencyNode> predecessors,
			  Set<DeficiencyNode> successors) {
	this.deficiency = deficiency;
	this.predecessors = predecessors;
	this.successors = successors;
    } // DeficiencyNode constructor 

    public DeficiencyNode(DeficiencyNode node) {
	this(node.deficiency,
	     new HashSet<DeficiencyNode>(node.predecessors),
	     new HashSet<DeficiencyNode>(node.successors));
    } // DeficiencyNode constructor 


    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */


    public static Set<Deficiency> unwrap(Set<DeficiencyNode> defNodes) {
	Set<Deficiency> res = new HashSet<Deficiency>();
	for (DeficiencyNode node : defNodes) {
	    res.add(node.getDeficiency());
	}
	
	return res;
    }

    public Deficiency getDeficiency() {
	return this.deficiency;
    }

    public Set<DeficiencyNode> getPredecessors() {
	return this.predecessors;
    }

    public Set<DeficiencyNode> getSuccessors() {
	return this.successors;
    }

    public void addPredecessor(DeficiencyNode deficiencyNode) {
	this.predecessors.add(deficiencyNode);
    }
    
    public void addSuccessor(DeficiencyNode deficiencyNode) {
	this.successors.add(deficiencyNode);
    }
    
    public void addPredecessors(Set<DeficiencyNode> nodes) {
	this.predecessors.addAll(nodes);
    }
    
    public void addSuccessors(Set<DeficiencyNode> nodes) {
	this.successors.addAll(nodes);
    }
    
    public void addAll(DeficiencyNode other) {
	if (!other.getDeficiency().equals(getDeficiency())) {
	    throw new IllegalArgumentException
		("Deficiencies" + getDeficiency() + " and " + 
		    other.getDeficiency() + " should coincide. ");
	}
	
	this.predecessors.addAll(other.getPredecessors());
	this.  successors.addAll(other.getSuccessors  ());
    }

    public DeficiencyNode getInverse() {
	return new DeficiencyNode(this.deficiency,
				  this.successors,
				  this.predecessors);
    }

    public String toString() {
	StringBuffer res = new StringBuffer(90);
	res.append("\n<DeficiencyNode deficiency=\"");
	res.append(this.deficiency);
	res.append("\">");
	if (!this.predecessors.isEmpty()) {
	    res.append("<Predecessors>");
	    for (DeficiencyNode node : this.predecessors) {
		res.append(node.getDeficiency().getName());
	    }
	    res.append("</Predecessors>\n");

	}
	
	if (!this.successors.isEmpty()) {
	    res.append("<Successors>");
	    for (DeficiencyNode node : this.successors) {
		res.append(node.getDeficiency().getName());
	    }
	    res.append("</Successors>\n");
	}
	res.append("</DeficiencyNode>");
	return res.toString();
    }

    public boolean equals(Object obj) {
	if (!(obj instanceof DeficiencyNode)) {
	    return false;
	}
	DeficiencyNode other = (DeficiencyNode)obj;
	return 
	    getDeficiency  ().equals(other.getDeficiency  ()) && 
	    getPredecessors().equals(other.getPredecessors()) && 
	    getSuccessors  ().equals(other.getSuccessors  ());
    }

    public int hashCode() {
	return 
	    getDeficiency().hashCode();
    }

} // DeficiencyNode

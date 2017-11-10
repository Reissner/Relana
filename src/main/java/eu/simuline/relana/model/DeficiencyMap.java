package eu.simuline.relana.model;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Describes a map between <code>Deficiency</code>'s 
 * of one type to another one. 
 *
 * Created: Thu Apr 21 14:47:33 2005
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
public final class DeficiencyMap {

    /* -------------------------------------------------------------------- *
     * constants.                                                           *
     * -------------------------------------------------------------------- */

    /* -------------------------------------------------------------------- *
     * attributes.                                                          *
     * -------------------------------------------------------------------- */

    /**
     * The set of deficiencies all mapped to the same deficiency. 
     * The values are assumed to be pairwise different 
     * and the keys to be pairwise disjoint. 
     * The keys are subsets of the set represented by {@link #source} 
     * whereas the values are elements 
     * of the set represented by {@link #target}. 
     */
    private final Map<Set<Deficiency>, Deficiency> setOfNew2old;

    /**
     * The source of this map. 
     * @see #domain
     */
    private SClass source;

    /**
     * The target of this map. 
     * @see #range
     */
    private SClass target;

    /**
     * The elements of {@link #source} 
     * for which {@link #map} yields a non-<code>null</code>-result. 
     * @see #source
     */
    private final Set<Deficiency> domain;

    /**
     * The elements of {@link #target} 
     * for which {@link #getInverseImage} yields a non-empty result. 
     * @see #target
     */
    private final Set<Deficiency> range;

    /**
     * The set of deficiencies mapped identically. 
     * As a consequence, this is a subset of both 
     * the set represented by {@link #source} 
     * and the set represented by {@link #target}. 
     */
    private Set<Deficiency> idDom;

    /* -------------------------------------------------------------------- *
     * constructors.                                                        *
     * -------------------------------------------------------------------- */

    public static 
    DeficiencyMap getSubclassMap(Map<Set<Deficiency>, Deficiency> setOfNew2old,
				 SClass source,
				 SClass target) {

	DeficiencyMap result = 
	    new DeficiencyMap(setOfNew2old,
			      source,
			      target,
			      new HashSet<Deficiency>());

	Set<Deficiency> sourceId = 
	    new HashSet<Deficiency>(source.getType().asSet());
	sourceId.removeAll(result.getDomain());
	Set<Deficiency> rangeId = 
	    new HashSet<Deficiency>(target.getType().asSet());
	rangeId.removeAll(result.getRange());
	if (!sourceId.equals(rangeId)) {
	    throw new IllegalArgumentException
		("No subclass map extending " + result + ". ");
	}
	result.idDom = rangeId;
	
	return result;
    }

    public DeficiencyMap(Map<Set<Deficiency>, Deficiency> setOfNew2old,
			 SClass source,
			 SClass target,
			 Set<Deficiency> idDom) {
	this.setOfNew2old = setOfNew2old;
	this.source = source;
	this.target = target;
	this.idDom = idDom;

	// initialize domain and range 
	this.domain = new HashSet<Deficiency>(this.idDom);
	for (Set<Deficiency> setDef : this.setOfNew2old.keySet()) {
	    this.domain.addAll(setDef);
	}
	this.range  = new HashSet<Deficiency>(this.idDom);
	this.range.addAll(this.setOfNew2old.values());

	checkInvImg01();

	// Check that the domain is within source. 
	Set<Deficiency> domain = getDomain();
	if (!this.source.getType().asSet().containsAll(domain)) {
	    throw new IllegalArgumentException
		("Domain " + domain + " not inside type range " + 
		 this.source.getType().asSet() + ". ");
	}

	// check whether the key set of setOfNew2old and idDom 
	// are pairwise disjoint
	int num = this.idDom.size();
	for (Set<Deficiency> setDef : this.setOfNew2old.keySet()) {
	    num += setDef.size();
	}
	if (num != domain.size()) {
	    throw new IllegalArgumentException
		("Inverse images " + this.setOfNew2old.keySet() + 
		 " and identity domain " + this.idDom + 
		 " are not pairwise disjoint. ");
	}

	// Check that the range is within target. 
	Set<Deficiency> range = getRange();
	if (!this.target.getType().asSet().containsAll(range)) {
	    throw new IllegalArgumentException
		("Range " + range + " not inside type range " + 
		 this.target.getType().asSet() + ". ");
	}

	// check whether the key set of setOfNew2old and idDom 
	// are pairwise disjoint
	if (this.setOfNew2old.size() + this.idDom.size() != range.size()) {
	    throw new IllegalArgumentException
		("Images " + this.setOfNew2old.values() + 
		 " and identity domain " + this.idDom + 
		 " are not pairwise disjoint. ");
	}
    } // DeficiencyMap constructor


    void checkInvImg01() {
	// check inverse images of size 0 or 1. 
	Iterator<Set<Deficiency>> iter = this.setOfNew2old.keySet().iterator();
	Set<Deficiency> setDef;
	Deficiency def;
	while (iter.hasNext()) {
	    setDef = iter.next();
	    switch (setDef.size()) { // NOPMD
		case 0:
		    // size == 0: this is superfluous. 
		    throw new IllegalArgumentException
			("Found empty inverse image for " + 
			 this.setOfNew2old.get(setDef) + ". ");
		case 1:
		    // size == 1: if identity, remove and add to idDomain. 
		    def = this.setOfNew2old.get(setDef);
		    if (setDef.contains(def)) {
			// Here, inverse image and image coincide. 
			iter.remove();
			this.idDom.add(def);
		    }
		    continue;
		default:
		    // size is at least two: nothing to do. 
		    continue;
	    }
	}
    }

/*
    public static DeficiencyMap getBooleanMap(SClass source,
					      Set<Deficiency> domain) {
	Map<Set<Deficiency>, Deficiency> setOfNew2old = 
	    new HashMap<Set<Deficiency>, Deficiency>();
	setOfNew2old.put(domain, Deficiency.UNDET);
	return new DeficiencyMap(setOfNew2old,
				 source,
				 SClass.BOOLEAN,
				 domain);
    }
*/

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    /**
     * Returns the identity map with the given source and target 
     * with full domain and range. 
     *
     * @param sourceTarget 
     *    an <code>SClass</code> which is both 
     *    {@link #source} and {@link #target} 
     *    and {@link #idDom} comprises all its deficiencies. 
     * @return 
     *    a <code>DeficiencyMap</code> which is the identity 
     *    on the set of deficiencies of <code>sourceTarget</code>. 
     */
    public static DeficiencyMap identity(SClass sourceTarget) {
	System.out.println("sourceTarget: " + sourceTarget);
	DeficiencyMap map = sourceTarget.getDeficiencyMap();
	Set<Deficiency> idDom = (map == null) 
	    ? new HashSet<Deficiency>
	    (sourceTarget.getDeclaredDeficiency2ordering().keySet())
	    : map.getDomain();

	return new DeficiencyMap(new HashMap<Set<Deficiency>, Deficiency>(),
				 sourceTarget,
				 sourceTarget,
				 idDom);
    }

    /**
     * Returns the inverse of this map provided it exists. 
     *
     * @return 
     *    a <code>DeficiencyMap</code> which is the inverse of this one. 
     * @throws UnsupportedOperationException
     *    if this map is not invertible. 
     */
    public DeficiencyMap getInverse() {
	// check invertibility 
	if (getDomain().size() != getRange().size()) {
	    throw new UnsupportedOperationException
		("This map is not invertible: " + this + ". ");
	}
	
	Map<Set<Deficiency>, Deficiency> invSetOfNew2old = 
	    new HashMap<Set<Deficiency>, Deficiency>();
	Set<Deficiency> oneDefSet;
	Deficiency def;
	for (Map.Entry<Set<Deficiency>, Deficiency> entry 
		 : this.setOfNew2old.entrySet()) {
	    assert entry.getKey().size() == 1;
	    oneDefSet = new HashSet<Deficiency>();
	    oneDefSet.add(entry.getValue());
	    def = entry.getKey().iterator().next();
	    invSetOfNew2old.put(oneDefSet, def);
	}
	
	return new DeficiencyMap(invSetOfNew2old,
				 this.target, // exchanged. 
				 this.source,
				 this.idDom);
    }


    /**
     * Returns the composition of this map and <code>second</code> 
     * (in this order). 
     *
     * @param second 
     *    another <code>DeficiencyMap</code>. 
     * @return 
     *    the composition of this <code>DeficiencyMap</code> 
     *    and <code>second</code>. 
     * @throws IllegalArgumentException
     *    if <code>this.target != second.source</code>. 
     */
    public DeficiencyMap compose(DeficiencyMap second) {
	if (this.target != second.source) {
	    throw new IllegalArgumentException
		("Composition of maps is allowed only " + 
		 "if source and target coincide but found " + 
		 second.source.getName() + " and " + 
		 this.target  .getName() + ". ");
	}
	
	Map<Set<Deficiency>, Deficiency> setOfNew2old = 
	    new HashMap<Set<Deficiency>, Deficiency>();
	// handle all images in the value set of second.setOfNew2old 
	Set<Deficiency> invImg;
	for (Map.Entry<Set<Deficiency>, Deficiency> entry 
		 : second.setOfNew2old.entrySet()) {

	    invImg = this.cont(entry.getKey());
	    setOfNew2old.put(invImg, entry.getValue());
	}
	// Handle all images in second.idDom 
	// which are in the image set of this.setOfNew2old 
	for (Map.Entry<Set<Deficiency>, Deficiency> entry 
		 : this.setOfNew2old.entrySet()) {
	    if (!second.idDom.contains(entry.getValue())) {
		continue;
	    }
	    // Here, entry.getValue() is in second.idDom
	    setOfNew2old.put(entry.getKey(), entry.getValue());
	}
	// handle all images this.idDom and in second.idDom 
	Set<Deficiency> idDom = new HashSet<Deficiency>(this.idDom);
	idDom.retainAll(second.idDom);

	// move from setOfNew2old to idDom whatever possible 
	Deficiency cand;
	Iterator<Map.Entry<Set<Deficiency>, Deficiency>> iter = 
	    setOfNew2old.entrySet().iterator();
	Map.Entry<Set<Deficiency>, Deficiency> entry;
	while (iter.hasNext()) {
	    entry = iter.next();
	    if (entry.getKey().size() != 1) {
		continue;
	    }
	    assert entry.getKey().size() == 1;
	    cand = entry.getKey().iterator().next();
	    if (!cand.equals(entry.getValue())) {
		continue;
	    }

	    idDom.add(cand);
	    iter.remove();
	}

	return new DeficiencyMap(setOfNew2old,
				 this.source,
				 second.target,
				 idDom);
    }

    // **** null if def is not in the domain. 
    /**
     * Performs the mapping. 
     *
     * @param def 
     *    the <code>Deficiency</code> to be mapped. 
     * @return 
     *    the image of <code>def</code> with respect to this map. 
     *    If and only if <code>def</code> is outside the domain, 
     *    returns <code>null</code>. 
     */
    public Deficiency map(Deficiency def) {
	if (this.idDom.contains(def)) {
	    return def;
	}
	
	for (Map.Entry<Set<Deficiency>, Deficiency> entry 
		 : this.setOfNew2old.entrySet()) {
	    if (entry.getKey().contains(def)) {
		return entry.getValue();
	    }
	}
	// def was nowhere found. 
	return null;
    }

    // 
    /**
     * Returns the inverse image of the given deficiency 
     * with respect to this map. 
     *
     * @param def 
     *    the <code>Deficiency</code> 
     *    for which the inverse image is to be determined. 
     * @return 
     *    the inverse image of <code>def</code> with respect to this map. 
     *    This may well be an empty set. 
     */
    Set<Deficiency> getInverseImage(Deficiency def) {
	Set<Deficiency> result = new HashSet<Deficiency>();
	if (this.idDom.contains(def)) {
	    result.add(def);
	    return result;
	}

	for (Map.Entry<Set<Deficiency>, Deficiency> entry 
		 : this.setOfNew2old.entrySet()) {
	    if (entry.getValue().equals(def)) {
		return entry.getKey();
	    }
	}
	
	return  result;
    }

    /**
     * Returns the domain of this map. 
     *
     * @return 
     *    the domain of this map. 
     */
    Set<Deficiency> getDomain() {
	return this.domain;
    }

    /**
     * Returns the range of this map. 
     *
     * @return 
     *    the range of this map. 
     */
    Set<Deficiency> getRange() {
	return this.range;
    }

    /**
     * Returns the source of this map. 
     * @see #source
     */
    public SClass getSource() {
	return this.source;
    }

    /**
     * Returns the target of this map. 
     * @see #target
     */
    public SClass getTarget() {
	return this.target;
    }

    /**
     * Returns whether this map is isotone. 
     *
     * @return 
     *    whether this map is isotone. 
     */
    public boolean isIsotone() {
	Set<Deficiency> cone;
	for (Deficiency def1 : getDomain()) {
	    cone = this.source.getType().getCone(def1);
	    if (!getDomain().containsAll(cone)) {
		return false;
	    }
	    for (Deficiency def2 : cone) {
		if (!this.target.getType().implies(map(def1), map(def2))) {
		    return false;
		}
	    }
	}
	return true;
    }

    /**
     * Returns whether this map is twist-isotone. 
     *
     * @return 
     *    whether this map is twist-isotone. 
     */
    public boolean isTwistIsotone() {
	Set<Deficiency> cone, coneT;
	for (Deficiency def1 : getDomain()) {
	    cone  = this.source.getType().getCone(    def1);
	    coneT = this.target.getType().getCone(map(def1));
	    for (Deficiency defT2 : coneT) {
		Set<Deficiency> inter = 
		    new HashSet<Deficiency>(getInverseImage(defT2));
		inter.retainAll(cone);
		if (inter.isEmpty()) {
		    return false;
		}
	    }
	}
	return true;
    }

    /**
     * Returns the image of <code>defs</code> under this map. 
     *
     * @param defs
     *    a set of <code>Deficiency</code>s to be mapped. 
     * @return 
     *    the image of <code>defs</code> under this map. 
     */
    public Set<Deficiency> cov(Set<Deficiency> defs) {
	assert this.source.getType().isValid(defs);
	Set<Deficiency> defsToMap = new HashSet<Deficiency>(defs);
	defsToMap.retainAll(this.domain);
	Set<Deficiency> result = new HashSet<Deficiency>();

	Set<Deficiency> inter;
	for (Map.Entry<Set<Deficiency>, Deficiency> entry 
		 : this.setOfNew2old.entrySet()) {
	    inter = new HashSet<Deficiency>(entry.getKey());
	    inter.retainAll(defsToMap);
	    if (!inter.isEmpty()) {
		result.add(entry.getValue());
	    }
	}

	defsToMap.retainAll(this.target.getType().asSet());
	result.addAll(defsToMap);
	assert this.target.getType().isValid(result);
	return result;
    }

    /**
     * Returns the inverse image of <code>defs</code> under this map. 
     *
     * @param defs
     *    a set of <code>Deficiency</code>s to be "reverse mapped'. 
     * @return 
     *    the inverse image of <code>defs</code> under this map. 
     */
    public Set<Deficiency> cont(Set<Deficiency> defs) {
	assert this.target.getType().isValid(defs);
	Set<Deficiency> result = new HashSet<Deficiency>();
	for (Map.Entry<Set<Deficiency>, Deficiency> entry 
		 : this.setOfNew2old.entrySet()) {
	    if (defs.contains(entry.getValue())) {
		result.addAll(entry.getKey());
	    }
	}

	Set<Deficiency> defsToMap = 
	    new HashSet<Deficiency>(this.source.getType().asSet());
	defsToMap.retainAll(defs);
	result.addAll(defsToMap);
	assert this.source.getType().isValid(result);
	return result;
    }


    public String toString() {
	StringBuffer res = new StringBuffer();
	res.append("<DeficiencyMap source=\"");
	res.append(source.getPathName());
	res.append("\" target=\"");
	res.append(target.getPathName());
	res.append("\">\n");
	res.append(this.setOfNew2old);
	res.append('\n');
	res.append(this.idDom);
	res.append("\n</DeficiencyMap>");
	return res.toString();
    }

} // DeficiencyMap

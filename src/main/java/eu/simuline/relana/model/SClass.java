package eu.simuline.relana.model;

import eu.simuline.relana.expressions.Type;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
//import java.util.Collections;
import java.util.Iterator;

/**
 * Describes classes of Effects ('services'). 
 * Like a java-class this has a name {@link #sName}, 
 * a package {@link #pkg} and a superclass {@link #superClass} 
 * which is again an {@link SClass}. 
 *
 *
 * Created: Thu Apr 14 19:35:08 2005
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
public final class SClass {

    public static final String BOOL_S_CLASS_NAME = "B";
    public static final SClass BOOLEAN = new SClass();

    /* -------------------------------------------------------------------- *
     * attributes.                                                          *
     * -------------------------------------------------------------------- */

    /**
     * The name of this <code>SClass</code>. 
     */
    private final String sName;

    /**
     * The package of this <code>SClass</code>. 
     */
    private final Package pkg;

    /**
     * The superclass of this <code>SClass</code>. 
     * This is <code>null</code> if and only if 
     * <code>this=={@link #BOOLEAN}</code>. 
     */
    private final SClass superClass;

    private final Map<Deficiency, SClass> oldDef2innerClasses;

    private final DeficiencyMap map;

    /**
     * Maps declared <code>Deficiency</code>s 
     * (see {@link #getDeclaredDeficiency2ordering}) to their nodes 
     * which determine their predecessors and their successors. 
     * It is required that this relation extends that 
     * given by {@link #superClass}. 
     */
    private final Map<Deficiency, DeficiencyNode> deficiency2ordering;

    private final Type type;


    /* -------------------------------------------------------------------- *
     * constructors.                                                        *
     * -------------------------------------------------------------------- */

    // yields the Boolean Class
    private SClass() {
	this.sName = BOOL_S_CLASS_NAME;
	this.pkg = Package.BUILD_IN;
	this.superClass = null;
	this.oldDef2innerClasses = new HashMap<Deficiency, SClass>();
	this.deficiency2ordering = new HashMap<Deficiency, DeficiencyNode>();
	DeficiencyNode node = new DeficiencyNode(Deficiency.UNDET);
	this.deficiency2ordering.put(Deficiency.UNDET, node);

	this.type = Type.BOOLEAN;
	verifyMMDefics();
	this.map = null; // DeficiencyMap.ID_BOOL;
    }

    private SClass(String sName, //**** common superclass with CClass 
		   Package pkg,
		   SClass superClass,
		   Map<Deficiency, SClass> oldDef2innerClasses,
		   Map<Deficiency, DeficiencyNode> deficiency2ordering) {
	this.sName = sName;
	this.pkg = pkg;
	this.superClass = superClass;
	this.oldDef2innerClasses = oldDef2innerClasses;
	this.deficiency2ordering = deficiency2ordering;
	
	this.type = createType();
	verifyMMDefics();
	if (isInner()) {
	    Map<Set<Deficiency>, Deficiency> mapAll2Undet = 
		new HashMap<Set<Deficiency>, Deficiency>();
	    mapAll2Undet.put(deficiency2ordering.keySet(),
			     Deficiency.UNDET);
	    this.map = DeficiencyMap
		.getSubclassMap(mapAll2Undet,
				this,
				getSuperClass());

	} else {
	    this.map = DeficiencyMap
		.getSubclassMap(getMap(oldDef2innerClasses),
				this,
				getSuperClass());

	}

    } // SClass constructor 

    // **** maybe better in class DeficiencyMap ?
    /**
     * Converts a map from the overwritten deficiencies 
     * to the overwriting classes, 
     * to a map from the set of deficiencies in the class 
     * back to the original deficiency. 
     *
     * @param oldDef2intCls
     *    maps a deficiency in outer class 
     *    to the inner class overwriting this deficiency. 
     * @return
     *    a map from the set of deficiencies of a class 
     *    back to the overwritten deficiency in the enclosing class. 
     */
    private static Map<Set<Deficiency>, Deficiency> 
	getMap(Map<Deficiency, SClass> oldDef2intCls) {

	Map<Set<Deficiency>, Deficiency> setOfNew2old = 
	    new HashMap<Set<Deficiency>, Deficiency>();
	for (Map.Entry<Deficiency, SClass> entry : 
		 oldDef2intCls.entrySet()) {
	    Deficiency oldDef = entry.getKey();
	    Set<Deficiency> newDefs = 
		entry.getValue().getDeclaredDeficiency2ordering().keySet();
	    setOfNew2old.put(newDefs, oldDef);

	}
	return setOfNew2old;
    }

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    public String getName() {
	return this.sName;
    }

    public Package getPackage() {
	return this.pkg;
    }

    public Package asPackage() {
	List<String> path = getPackage().getPath();
	path.add(getName());
	return Package.getPackage(path);
    }

    public String getPathName() {
	String res = getPackage().getPathName();
	return res + getName();
    }

    // **** This means it is an inner class itself. 
    public boolean isInner() {
	return (getSuperClass().equals(SClass.BOOLEAN) && 
		getDeclaredInnerClasses().keySet().isEmpty());
    }

    public Type getType() {
	return this.type;
    }
	
    private Type createType() {
	if (getSuperClass() == null) {
	    // Here, the class is the Boolean one. 
	    return new Type(Type.BOOLEAN);
	}
	// For inner classes, 
	// the superclass is Boolean but UNDET is replaced implicitly 
	// otherwise we have to start with a copy 
	// of the type of the superclass. 
	Type result = isInner() 
	    ? new Type() 
	    : new Type(getSuperClass().getType());
	// Here, we replace the properties overwritten by inner classes. 
	SClass innerCls;
	Type innerType;
	Set<Deficiency> inter;
	for (Map.Entry<Deficiency, SClass> entry 
		 : getDeclaredInnerClasses().entrySet()) {
	    innerCls = entry.getValue();

	    // namespace check 
	    innerType = new Type(innerCls.getType());
	    inter = new HashSet<Deficiency>(result.asSet());
	    inter.retainAll(innerType.asSet());
	    if (!inter.isEmpty()) {
		throw new IllegalArgumentException
		    ("Found duplicate property \"" + inter + 
		     "\" in enclosing class \"" + getPathName() +
		     "\" overwritten by inner class. ");
		// **** should be fixed by introducing separate namespaces 
		// for each SClass. 
	    }
	    result.replace(entry.getKey(),
			   innerCls.getMinDefic(),
			   innerCls.getMaxDefic(),
			   innerType);
	} // for all inner classes 

	// add the relations explicitly added in the relations section. 
	result.addAll(getDeclaredDeficiency2ordering());
	return result;
    }

    // is null exactly for {@link #BOOL_S_CLASS}. 
    public SClass getSuperClass() {
	return this.superClass;
    }

    public Map<Deficiency, SClass> getDeclaredInnerClasses() {
	return this.oldDef2innerClasses;
    }

    // may not return null ****
    public SClass getDeclaredInnerClass(Deficiency def) {
	SClass res = this.oldDef2innerClasses.get(def);
	if (res == null) {
	    throw new IllegalArgumentException
		("Found no inner class for key \"" + def.getName() + "\". ");
	}
	
	return res;
    }

    public DeficiencyMap getDeficiencyMap() {
	return this.map;
    }

    // **** used internally and by ProbDistr. 
    Map<Deficiency, DeficiencyNode> getDeclaredDeficiency2ordering() {
	return this.deficiency2ordering;
    }












    // **** valid after verification only {@link #verifyMMDefics} 
    private Deficiency minDef;
    // **** valid after verification only {@link #verifyMMDefics} 
    private Deficiency maxDef;

    // **** also initializes fields minDefs and maxDefs
    private void verifyMMDefics() {
	Set<Deficiency> minDefs = getType().getMin();
	Set<Deficiency> maxDefs = getType().getMax();

	if (minDefs.size() != 1) {
	    // exactly one minimal deficiency expected. 
	    throw new VerifyException
		("Expected unique minimal element in class \"" + 
		 this.getPathName() + "\" but found " + minDefs + ". ");
	}
	if (maxDefs.size() != 1) {
	    // exactly one minimal deficiency expected. 
	    throw new VerifyException
		("Expected unique maximal element in class \"" + 
		 this.getPathName() + "\" but found " + maxDefs + ". ");
	}

	this.minDef = minDefs.toArray(new Deficiency[1])[0];
	this.maxDef = maxDefs.toArray(new Deficiency[1])[0];
    }

    // **** valid after verification only 
    public Deficiency getMinDefic() {
	return this.minDef;
    }

    // **** valid after verification only 
    public Deficiency getMaxDefic() {
	return this.maxDef;
    }

    Set<DeficiencyNode> getMinDeficN() {
	Set<DeficiencyNode> minDefs = new HashSet<DeficiencyNode>();
	for (DeficiencyNode node : this.deficiency2ordering.values()) {
	    if (node.getPredecessors().isEmpty()) {
		minDefs.add(node);
	    }
	}
	
	return minDefs;
    }

    public static SClass getSClass(String sName,
				   Package pkg,
				   SClass superClass,
				   Map<Deficiency, SClass> oldDef2innerClasses,
				   Map<Deficiency, DeficiencyNode> def2ord) {
	return new SClass(sName, pkg, superClass,
			  oldDef2innerClasses,
			  def2ord);	
    } // SClass constructor

 
    // checks whether the additional relations 
    // reflect relations in the superclass 
    // should thus not be invoked with BOOL_S_CLASS. 
    void verify() throws VerifyException {

	// inner classes need not be verified: 
	// they are either loaded or verified by the parser. 
	// the latter is the case if and only if they occur as inner classes 
	// with explicitly given Deficiency's and relations. 

	Deficiency def1, def2;
	Deficiency def1super, def2super;
	for (DeficiencyNode node1 : this.deficiency2ordering.values()) {
	    def1 = node1.getDeficiency();
	    for (DeficiencyNode node2 : node1.getSuccessors()) {
		def2 = node2.getDeficiency();
		def1super = getDeficiencyMap().map(def1);
		def2super = getDeficiencyMap().map(def2);
		if (def1super.equals(def2super)) {
		    throw new VerifyException
			("Relation \"" + def1 + "==>" + def2 + 
			 "\" should be specified in inner class because " + 
			 getSuperClass() + 
			 " maps both to " + def1super + ". ");
		}

		if (!getSuperClass().getType().implies(def1super, def2super)) {
		    throw new VerifyException
			("Relation \"" + def1 + "==>" + def2 + 
			 "\" not reflected by superclass " + getSuperClass() + 
			 ": " + def1super + "=/=>" + def2super + ". ");
		}
	    }
	}
    }

    /**
     * Throws an exception if the transitive hull of the relation 
     * contains a cycle. 
     *
     * @exception VerifyException 
     *    if the transitive hull of the relation contains a cycle. 
     */
    private void verifyNoShortcut() throws VerifyException {
	for (DeficiencyNode node : this.deficiency2ordering.values()) {
	    if (node.getPredecessors().contains(node.getDeficiency()) ||
		node.getSuccessors  ().contains(node.getDeficiency())) {
		throw new VerifyException
		("Deficiency " + node.getDeficiency() + 
		 " is cyclically related with itself. ");
	    }
	}
    }

    // **** invoked by the parser for inner classes only. 
    // checks absence of deficiencies, cycles 
    public void verifyInner() throws VerifyException {

	if (this.deficiency2ordering.keySet().isEmpty()) {
	    throw new VerifyException
		("No deficiencies found. ");
	}
	// Here, there is at least one deficiency. 

	verifyNoShortcut();

	Set<DeficiencyNode> minDeficN = getMinDeficN();
	if (minDeficN.isEmpty()) {
	    throw new VerifyException
		("Relation has no minimal elements and is hence cyclic. ");
	}

	Set<DeficiencyNode> occured = new HashSet<DeficiencyNode>(minDeficN);
	Set<DeficiencyNode> maximal = new HashSet<DeficiencyNode>(minDeficN);
	Set<DeficiencyNode> newMaximal = new HashSet<DeficiencyNode>();
	Set<DeficiencyNode> intersection;
	Iterator<DeficiencyNode> iter = maximal.iterator();
	DeficiencyNode node;
	do {
	    while (iter.hasNext()) {
		node = iter.next();
		newMaximal.addAll(node.getSuccessors());
	    }
	    intersection = new HashSet<DeficiencyNode>(occured);
	    intersection.retainAll(newMaximal);
	    if (!intersection.isEmpty()) {
		throw new VerifyException
		    ("Relation is cyclic: consider " + intersection + ". ");
	    }
	    occured.addAll(newMaximal);
	    maximal = newMaximal;
	    newMaximal = new HashSet<DeficiencyNode>();
	} while (!maximal.isEmpty());

	verifyMMDefics();
    }

    public String toString() {
	StringBuffer res = new StringBuffer();
	res.append("<SClass name=\"");
	res.append(getName());
	res.append("\" \npackage=\"");
	res.append(getPackage().getPathName());
	if (getSuperClass() != null) {
	    res.append("\" \nsuperClass=\"");
	    res.append(getSuperClass().getPathName());
	}
	res.append("\">\n<Deficiencies>\n");
	res.append(this.deficiency2ordering.keySet().toString());
	res.append("\n</Deficiencies>\n\n<InnerClasses>\n");
	for (Map.Entry<Deficiency, SClass> entry 
		 : this.oldDef2innerClasses.entrySet()) {
	    res.append("<InnerClass name=\"");
	    res.append(entry.getKey().getName());
	    res.append("\">");
	    res.append(entry.getValue());
	    res.append("</InnerClass>\n");
	}
	
	res.append("</InnerClasses>\n\n<Map>\n");
	res.append(this.map);
	res.append("</Map>\n\n<Relations>");
	Iterator<DeficiencyNode> iterN = 
	    this.deficiency2ordering.values().iterator();
	DeficiencyNode node;
	Deficiency def;
	while (iterN.hasNext()) {
	    node = iterN.next();
	    def = node.getDeficiency();
	    Iterator<DeficiencyNode> iterD = node.getSuccessors().iterator();
	    while (iterD.hasNext()) {
		res.append(def);
		res.append("\n==>");
		res.append(iterD.next().getDeficiency());
		res.append('\n');
	    }
	}
	
	res.append("</Relations>\n</SClass>\n");
	return res.toString();
    }

    public boolean equals(Object obj) {
	if (!(obj instanceof SClass)) {
	    return false;
	}
	return getPathName().equals(((SClass) obj).getPathName());
    }

    public int hashCode() {
	return getPathName().hashCode();
    }
} // SClass

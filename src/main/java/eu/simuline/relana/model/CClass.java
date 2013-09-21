package eu.simuline.relana.model;

import eu.simuline.relana.expressions.FormulaDecl;

import java.util.List;
//import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
//import java.util.TreeMap;
//import java.util.Iterator;

/**
 * Represents a component class. 
 *
 * @author <a href="mailto:ernst@local">Ernst Reissner</a>
 * @version 1.0
 */
public class CClass implements CClassLink {

    /* -------------------------------------------------------------------- *
     * inner classes.                                                       *
     * -------------------------------------------------------------------- */

    public static enum SClassModifier {

	/* ---------------------------------------------------------------- *
	 * constructor constants.                                           *
	 * ---------------------------------------------------------------- */

	INPUT(),
	OUTPUT();

	/* ---------------------------------------------------------------- *
	 * constants and initializer.                                       *
	 * ---------------------------------------------------------------- */

	/**
	 * Maps the names of the modifiers in the ccl-files 
	 * to the modifiers: 
	 * <ul>
	 * <li>
	 * <code>input</code>  maps to {@link #INPUT} and 
	 * <li>
	 * <code>output</code> maps to {@link #OUTPUT}. 
	 * </ul>
	 */
	final static Map<String,SClassModifier> NAME2MOD;

	static {
	    NAME2MOD = new HashMap<String,SClassModifier>();
	    NAME2MOD.put("input",   INPUT);
	    NAME2MOD.put("output", OUTPUT);
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	/**
	 * Gets the class modifier with the given name. 
	 *
	 * @param name
	 *    one of the names <code>input</code>, <code>output</code>. 
	 * @throws IllegalArgumentException
	 *    if the modifier is unknown. 
	 */
	public static SClassModifier get(String name) {
	    SClassModifier res = NAME2MOD.get(name);
	    if (res == null) {
		throw new IllegalArgumentException
		    ("Unknown access modifier: \"" + name + "\". ");
	    }
	    return res;
	}
    } // enum SClassModifier 

    /**
     * Represents the declaration of an {@link SClass} 
     * within this {@link CClass}. 
     * This comprises optional access modifiers, the class, the name 
     * and also optionally, a probability distribution {@link #distr} 
     * or a formula declaration {@link #form}. 
     */
    public final static class SClassDecl {

	/* ---------------------------------------------------------------- *
	 * attributes.                                                      *
	 * ---------------------------------------------------------------- */


	/**
	 * Whether this declaration is a redeclaration. 
	 */
	private final boolean isRedeclare;

	/**
	 * The set of modifiers of this effect. 
	 */
	private final Set<SClassModifier> modifiers;

	/**
	 * The class of this effect. 
	 */
	private final SClass sClass;

	/**
	 * The name of this effect which must be unique 
	 * within the keys of {@link #effects}. 
	 */
	private final String name;

	/**
	 * An optional probability distribution; otherwise <code>null</code>. 
	 */
	private final ProbDistr distr;

	/**
	 * An optional formula declaration; otherwise <code>null</code>. 
	 */
	private FormulaDecl form;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	/**
	 * Creates a new <code>SClassDecl</code> instance without formula.
	 *
	 * @param isRedeclare 
	 *    whether this is a redeclaration. 
	 * @param modifiers 
	 *    The set of mofifiers of this effect. 
	 * @param sClass 
	 *    The class of this effect. 
	 * @param name 
	 *    The name of this effect which must be unique 
	 *    within the keys of {@link #effects}. 
	 * @param distr 
	 *    An optional probability distribution; 
	 *    otherwise <code>null</code>. 
	 * @throws IllegalArgumentException
	 *    for inputs no probability distribution must be provided. 
	 * @see #setFormula
	 */
	public SClassDecl(boolean isRedeclare,
			  Set<SClassModifier> modifiers,
			  SClass sClass,
			  String name,
			  ProbDistr distr) {
	    this.isRedeclare = isRedeclare;
	    this.modifiers = modifiers;
	    this.sClass = sClass;
	    this.name = name;
	    // **** the latter maybe should be part of verification process. 
	    this.distr = distr;
	    if (isInput() && (this.distr != null)) {
		throw new IllegalArgumentException
		    ("SClass " + name + " is declared as input " + 
		     "and at the same time a probability distribution " + 
		     "is attached. ");
	    }	    
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	/**
	 * Sets the given formula <code>form</code> and checks consistency; 
	 * may throw various exceptions. 
	 *
	 * @param form 
	 *    a <code>FormulaDecl</code> which may also be <code>null</code>. 
	 * @throws IllegalArgumentException
	 *    if <code>form != null</code> and at the same time 
	 *    <ul>
	 *    <li>
	 *    this effect is declared as input or 
	 *    <li>
	 *    this effect has a probability distribution already. 
	 *    <li>
	 *    the formula is not assignment compatible 
	 *    which currently means the types do not coincide. 
	 *    </ul>
	 */
	public void setFormula(FormulaDecl form) {
	    this.form = form;
	    if (isInput() && (this.form != null)) {
		throw new IllegalArgumentException
		    ("SClass " + name + " is declared as input " + 
		     "and at the same time a formula is attached. ");
	    }

	    if ((this.distr != null) && (this.form != null)) {
		throw new IllegalArgumentException
		    ("Either a probability distribution may be given " + 
		     "or a formula but not both. ");
	    }

	    // here, one may consider 
	    // a weaker form of assignment compatibility ****
	    if (!this.form.retType().equals(this.sClass.getType())) {
		throw new IllegalArgumentException
		    ("Tried to assign formula \"" + this.form + 
		     "\" with return type " + this.form.retType() + 
		     " to variable \"" + this.name + 
		     "\" with type " + this.sClass.getType() + 
		     " which is not assignment compatible. ");
	    }
	    
	}

	public SClass getSClass() {
	    return this.sClass;
	}

	public String getName() {
	    return this.name;
	}

	/**
	 * Returns whether this effect is declared as input. 
	 *
	 * @return 
	 *    whether this effect is declared as input. 
	 */
	public boolean isInput() {
	    return this.modifiers.contains(SClassModifier.INPUT);
	}

	/**
	 * Returns whether this effect is declared as output. 
	 *
	 * @return 
	 *    whether this effect is declared as output. 
	 */
	public boolean isOutput() {
	    return this.modifiers.contains(SClassModifier.OUTPUT);
	}

	/**
	 * Returns whether this is a redeclaration. 
	 *
	 * @return 
	 *    whether this is a redeclaration. 
	 */
	public boolean isRedeclare() {
	    return this.isRedeclare;
	}

	/**
	 * Returns the probability distribution if any; 
	 * otherwise <code>null</code>. 
	 *
	 * @return 
	 *    {@link #distr}. 
	 */
	ProbDistr getProbDistr() {
	    return this.distr;
	}

	/**
	 * Returns the formula if any; otherwise <code>null</code>. 
	 *
	 * @return 
	 *    {@link #form}. 
	 */
	FormulaDecl getFormulaDecl() {
	    return this.form;
	}

	/**
	 * Returns a effect according to this declaration. 
	 *
	 * @return 
	 *    a effect according to this declaration. 
	 *    This depends on the class, the probability distribution 
	 *    and the name but not on the modifiers and the formula. 
	 */
	SInstance getSInstance() {
	    return new SInstance(getSClass().getType(),
				 getProbDistr(),
				 getName());
	}

	public String toString() {
	    StringBuffer res = new StringBuffer(32);
	    res.append('\n');
	    if (this.isRedeclare) {
		res.append("redeclare");
	    }	    
	    
	    res.append(this.modifiers);
	    res.append(getSClass().getName());
	    res.append(getName());
	    if (this.distr != null) {
		res.append(" distr: ");
		res.append(this.distr.toString());
	    }
	    
	    if (this.form != null) {
		res.append(" formula: ");
		res.append(this.form.toString());
	    }
	    return res.toString();
	}

	// for use in hash sets/maps 
	public boolean equals(Object obj) {
	    return super.equals(obj);
	}

	// for use in hash sets/maps 
	public int hashCode() {
	    return super.hashCode();
	}

    } // class SClassDecl 

    /* -------------------------------------------------------------------- *
     * constants.                                                           *
     * -------------------------------------------------------------------- */

    public final static CClass COMPONENT = 
    new CClass("Component",
	       Package.BUILD_IN,
	       null,
	       new HashMap<String,MapDecl>(),
	       new HashMap<String,CClassLink>(),
	       new HashMap<String,CClass.SClassDecl>()) {

	// maybe there should be an exception ****
	public MapDecl getMapDecl(String name) {
	    return null;
	}
	// maybe there should be an exception ****
	public CClassLink getComponentCls(String name) {
	    return null;
	}
	// maybe there should be an exception ****
	public SClassDecl getEffectDecl(String name) {
	    return null;
	}
	public CInstance getInstance() {
	    return new CInstance();
	}
	Set<String> getComponentNames() {
	    return new TreeSet<String>();
	}
	Set<String> getEffectNames() {
	    return new TreeSet<String>();
	}
	void verify() throws VerifyException {
	    // is empty. 
	}
    };

    /* -------------------------------------------------------------------- *
     * attributes.                                                          *
     * -------------------------------------------------------------------- */

    /**
     * The Name of this <code>CClass</code>. 
     */
    protected String cName;

    /**
     * The package of this class. 
     */
    private final Package pkg;

    /**
     * The unique superclass of this class. 
     * There is exactly one class without super class: 
     * {@link #COMPONENT} and the only one 
     * for which this field ist <code>null</code>. 
     */
    private final CClass superClass;

    /**
     * The maps declared in this class. 
     */
    private final Map<String,MapDecl> maps;

    /**
     * Maps the names of the subcomponents to their classes. 
     */
    private final Map<String,CClassLink> subComponents;

    /**
     * Maps the names of the effects to their classes. 
     */
    private final Map<String,SClassDecl> effects;

    /**
     * The location of the description of this class within the library. 
     */
    //private ClassLocator loc;

    /* -------------------------------------------------------------------- *
     * constructors.                                                        *
     * -------------------------------------------------------------------- */

    public static CClass getCClass(String cName,
				   Package pkg,
				   CClass superClass,
				   Map<String,MapDecl> maps,
				   Map<String,CClassLink> subComponents,
				   Map<String,SClassDecl> effects
//,		  ClassLocator loc
	) {
	return new CClass(cName,pkg,superClass,
			  maps,
			  subComponents,
			  effects);
    }

    public CClass(String cName,
		  Package pkg,
		  CClass superClass,
		  Map<String,MapDecl> maps,
		  Map<String,CClassLink> subComponents,
		  Map<String,SClassDecl> effects
//,		  ClassLocator loc
) {
	this.cName = cName;
	this.pkg = pkg;
	this.superClass = superClass;
	this.maps = maps;
	this.subComponents = subComponents;
	this.effects = effects;
	
	//this.loc = loc;
    } // CClass constructor

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    /**
     * Returns the short name of this class. 
     *
     * @return 
     *    {@link #cName}. 
     */
    public String getName() {
	return this.cName;
    }

    /**
     * Returns the package of this class. 
     *
     * @return 
     *    {@link #pkg}. 
     */
    public Package getPackage() {
	return this.pkg;
    }

    /**
     * Returns the superclass of this class. 
     * This is <code>null</code> if and only if 
     * this class is the unique overall base class {@link #COMPONENT}. 
     *
     * @return 
     *    {@link #superClass}. 
     */
    public CClass getSuperClass() {
	return this.superClass;
    }

    /**
     * Returns the map declaration with the given name. 
     * The lookup is done recursively down the inheritance hierarchy. 
     *
     * @param name
     *    the name of a declared map. 
     * @return 
     *    the map declaration with the given name 
     *    or <code>null</code> if no declaration is defined with this name.  
     */
    public MapDecl getMapDecl(String name) {
	MapDecl result = this.maps.get(name);
	if (result != null) {
	    return result;
	}
	return getSuperClass().getMapDecl(name);

    }

    /**
     * Returns the map between names and subcomponent classes. 
     *
     * @return 
     *    {@link #subComponents}. 
     */
    private Map<String,CClassLink> getName2ComponentClss() {
	return this.subComponents;
    }

    public CClassLink getComponentCls(String name) {
	CClassLink result = this.subComponents.get(name);
	if (result != null) {
	    return result;
	}
	return getSuperClass().getComponentCls(name);
    }

    public CClassLink getComponentCls(List<String> path) {
	CClass curr = this;
	for (String key : path) {
	    curr = (CClass)curr.getComponentCls(key);
	}
	
	return curr;
    }

    Set<String> getComponentNames() {
	Set<String> result = new TreeSet<String>
	    (getSuperClass().getComponentNames());
	result.addAll(getName2ComponentClss().keySet());
	return result;
    }

    // **** as required in FormulaParser: 
    // returns <code>null</code> if no effect with the given name is declared 
    // lookup is recursively down the inheritance hierarchy. 
    public SClassDecl getEffectDecl(String name) {
	SClassDecl result = this.effects.get(name);
	if (result != null) {
	    return result;
	}
	
	return getSuperClass().getEffectDecl(name);
    }

    // based on getEffectDecl(String)
    public SClassDecl getEffectDecl(List<String> path) {
	CClass curr = this;
	int ind = 0;
	for (; ind < path.size()-1; ind++) {
	    curr = (CClass)curr.getComponentCls(path.get(ind));
	    // **** also in superclass?
	}
	
	return curr.getEffectDecl(path.get(ind));
    }

    private Map<String,SClassDecl> getName2Effects() {
	return this.effects;
    }

    // recursively down the inheritance hierarchy. 
    Set<String> getEffectNames() {
	Set<String> result = 
	    new TreeSet<String>(getSuperClass().getEffectNames());
	result.addAll(getName2Effects().keySet());
	return result;
    }



    // **** should be recursive **** 
    // **** needed in Relana only: check that enclosing instance has no input 
    public Set<SClassDecl> getEffectsRec() {
	Set<SClassDecl> res = new HashSet<SClassDecl>();
	// add immediate effects declarations 
	for (SClassDecl decl : this.effects.values()) {
	    res.add(decl);
	}
	/*
	for (Map.Entry<String,CClassLink> entry 
		 : this.subComponents.entrySet()) {
	    Map<ClassLocator,SClassDecl> resInner = 
		entry.getValue().getEffectsRec();
	    String prefix = entry.getKey();
	    Map<ClassLocator,SClassDecl> resInnerPre = 
		new HashMap<ClassLocator,SClassDecl>();
	    for (Map.Entry<ClassLocator,SClassDecl> ld : resInner.entrySet()) {
		List<String> l = new ArrayList<String>(ld.getKey().getPath());
		l.add(0,prefix);

	    }
	    
	    res.put(ClassLocator.getLocator(entry.getKey()),
		    entry.getValue());
	}
	*/
	return res;
    }

    // replacement of CClassLink by CClass. 
    public CClassLink setComponent(String name, CClass cClass) {
//System.out.println(": "+this.subComponents.put(name,cClass).getClass());
	return this.subComponents.put(name,cClass);
    }


    void verify() throws VerifyException {
	// Here, getSuperClass() != null
	getSuperClass().verify();

	// verify that a effect is declared only 
	// if not already declared in superclass 
	SClassDecl overwritten, overwrite;
	for (Map.Entry<String,SClassDecl> entry 
		 : getName2Effects().entrySet()) {
	    overwrite   = entry.getValue();
	    overwritten = getSuperClass().getEffectDecl(entry.getKey());
	    if (overwrite.isRedeclare()) {
		// is redeclare 

		// check whether a declaration is overwritten 
		if (overwritten == null) {
		    // Here, a declaration is overwritten without redeclare 
		    throw new VerifyException
			("Found effect \"" + entry.getKey() + 
			 "\" redeclared in class \"" + getName() + 
			 "\" without being declared in any superclass. ");
		}

		// check whether access modifiers fit 
		if ((overwritten.isOutput() && !overwrite.isOutput()) ||
		    (overwritten.isInput () && !overwrite.isInput ())) {
		    throw new VerifyException
			("Weakened access priviligies of effect \"" + 
			 entry.getKey() + 
			 "\" by redeclaration in class " + getName() + ". ");
		}


		// check whether overwrite  .getSClass() is a subclass 
		// of            overwritten.getSClass() 
		// and determine subclass map. 

		SClass currentSCls = overwrite.getSClass();
		// compMap is a subclass map 
		//           currentSCls --> overwrite.getSClass()
		DeficiencyMap compMap = DeficiencyMap.identity(currentSCls);
		assert compMap.getTarget() == overwrite.getSClass();
		while (currentSCls != overwritten.getSClass()) {
		    assert compMap.getSource() == currentSCls;
		    if (currentSCls.getSuperClass() == null) {
			throw new VerifyException
			    ("Redeclared effect \"" + entry.getKey() + 
			     "\" of class " + 
			     overwritten.getSClass().getName() + 
			     " as " + overwrite.getSClass().getName() + 
			     " which is no subclass. ");
		    }
		    compMap = compMap.compose(currentSCls.getDeficiencyMap());
		    currentSCls = currentSCls.getSuperClass();		    
		}
		// Here, compMap is a subclass map 
		//    overwritten.getSClass()  --> overwrite.getSClass()



		// **** here the maps between the variables could be installed 
		// but this would be bad design. 
	    } else {
		// is first declaration: no redeclare 

		// nothing may be overwritten 
		if (overwritten != null) {
		    // Here, a declaration is overwritten without redeclare 
		    throw new VerifyException
			("Found effect \"" + entry.getKey() + 
			 "\" declared in class \"" + getName() + 
			 "\" and in a superclass: " + entry.getKey() + 
			 "; consider redeclare. ");
		}
	    }
	}

	// verify that a component is declared only 
	// if not already declared in superclass 
	Set<String> names = 
	    new TreeSet<String>(getSuperClass().getComponentNames());
	names.retainAll(getName2ComponentClss().keySet());
	if (!names.isEmpty()) {
	    throw new VerifyException
		("Found components declared in class \"" + getName() + 
		    "\" and in a superclass: " + names + ". ");
	}
	
    }

    /**
     * Returns an instance of this class. 
     * Also resolves formulae. 
     *
     * @return 
     *    a <code>CInstance</code> of this class. 
     */
    public CInstance getInstance() {

	// Here, superclass is not null 
	CInstance cInstance = getSuperClass().getInstance();

	// instantiate and add subcomponents 
	CClass cClass;
	for (Map.Entry<String,CClassLink> cEntry 
		 : this.subComponents.entrySet()) {
	    cClass = (CClass)cEntry.getValue();
	    //cInstance = cClass.getInstance();
	    cInstance.addComponent(cEntry.getKey(),
				   cClass.getInstance());
	    //name2cInstance.put(cEntry.getKey(),cInstance);
	}

	// instantiate and add effects 
	SInstance sInstance;
	SClassDecl decl;
	Map<SClassDecl,SInstance> declWithFormulae = 
	    new HashMap<SClassDecl,SInstance>();
	for (Map.Entry<String,SClassDecl> sEntry
		 : this.effects.entrySet()) {
	    decl = sEntry.getValue();
	    sInstance = decl.getSInstance();

	    //name2sInstance.put(sEntry.getKey(),sInstance);
	    cInstance.addEffect(sEntry.getKey(),sInstance);
	    if (decl.getFormulaDecl() != null) {
		declWithFormulae.put(decl,sInstance);
	    }
	}

	// resolve formulae 
	for (Map.Entry<SClassDecl,SInstance> entry 
		 : declWithFormulae.entrySet()) {
	    entry.getValue().setFormula(entry.getKey()
					.getFormulaDecl().resolve(cInstance));
	    //                           declaration      formula itself 
	}
	
	return cInstance;
    }

    public void addOccurence(CClassLoader.Occurence occ) {
	    // is empty. 
    }

    public boolean isResolved() {
	return true;
    }

    public String toString() {
	StringBuffer res = new StringBuffer(150);

	res.append("\n<CClass name=\"");
	res.append(this.cName);
	res.append("\" package=\"");
	res.append(this.pkg);
	res.append("\" superClass=\"");
	res.append(this.superClass.getName());
	res.append("\">\n");
	for (Map.Entry<String,MapDecl> entry 
		 : this.maps.entrySet()) {
	    res.append("<map name=\"");
	    res.append(entry.getKey());
	    res.append("\"/>\n");
	    res.append(entry.getValue());
	    res.append("</map>");
	}
	for (Map.Entry<String,CClassLink> entry 
		 : this.subComponents.entrySet()) {
	    res.append("<component name=\"");
	    res.append(entry.getKey());
	    res.append("\" class=\"");
	    res.append(entry.getValue().getName());
	    res.append("\"/>\n");
	}

	res.append("\n<effects>");
	for (SClassDecl decl : this.effects.values()) {
	    res.append(decl.toString());
	}
	res.append("\n</effects>\n</CClass>\n");
	return res.toString();
    }

} // CClass

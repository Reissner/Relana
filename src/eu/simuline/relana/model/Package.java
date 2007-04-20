package eu.simuline.relana.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a Package of a model class, 
 * no matter whether Effect or Component. 
 *
 *
 * Created: Tue Apr 19 13:47:17 2005
 *
 * @author <a href="mailto:ernst@local">Ernst Reissner</a>
 * @version 1.0
 */
public class Package {

    /**
     * The constant for the build-in-package, 
     * i.e. the package containing the built-in classes. 
     * Note that it is named <em>_BuiltInPackage</em> 
     * starting with an underscore 
     * which distinguishes it from all packages in a library. 
     */
    public final static Package BUILD_IN = new Package("_BuiltInPackage",null);

    final static Package ROOT = new Package("",null);

//     static {
// 	//PACKAGES = new HashMap();
//     } // static 

    /* -------------------------------------------------------------------- *
     * attributes.                                                          *
     * -------------------------------------------------------------------- */

    /**
     * The name of this package. 
     */
    private final String name;

    /**
     * The unique package containing this one. 
     * This is <code>null</code> 
     * exactly for {@link #BUILD_IN} and for {@link #ROOT}. 
     */
    private final Package superPackage;

    /**
     * Maps the names of the subPackages to the subPackages. 
     */
    private final Map<String,Package> subPackages;

    /* -------------------------------------------------------------------- *
     * constructors.                                                        *
     * -------------------------------------------------------------------- */

    private Package(String name, Package superPackage) {
	this.name = name;
	this.superPackage = superPackage;
	this.subPackages = new HashMap<String,Package>();
    } // Package constructor

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    /**
     * Returns the name of this package. 
     *
     * @return 
     *    the name of this package as a <code>String</code>. 
     */
    String getName() {
	return this.name;
    }

    public String getPathName() {
	return (getSuperPackage() == null) 
	    ? ""
	    : getSuperPackage().getPathName() + getName() + ".";
    }

    /**
     * Returns the path of this package. 
     * This is intended for use within a library hierarchy. 
     *
     * @return 
     *    the path of this package 
     *    starting with the name of the outermost package. 
     *    This excludes the names of {@link #BUILD_IN} and for {@link #ROOT}. 
     *    These are the only packages with an empty path. 
     *    For all other packages, the path includes the name of this package. 
     */
    public List<String> getPath() {
	List<String> path = new ArrayList<String>();
	addName(path);
	return path;
    }

    private void addName(List<String> path) {
	if (getSuperPackage() != null) {
	    getSuperPackage().addName(path);
	    path.add(getName());
	}
    }

    /**
     * Returns the enclosing package of this package. 
     *
     * @return 
     *    the enclosing package of this package. 
     *    This is <code>null</code> 
     *    exactly for {@link #BUILD_IN} and for {@link #ROOT}. 
     */
    Package getSuperPackage() {
	return this.superPackage;
    }

    Package addSubPackage(String name) {
	return (Package)this.subPackages.put(name,new Package(name,this));
    }

    Package subPackage(String name) {
	return (Package)this.subPackages.get(name);
    }

    // 
    //static Package getPackage(String path) {
    //}

    // only sub-packages of ROOT, no BuiltIn's 

    /**
     * Returns the <code>Package</code> with the given path. 
     * Note that this yields only sub-packages of {@link #ROOT} 
     * no BuiltIn's. 
     *
     * @return 
     *    the <code>Package</code> with the given path. 
     *    Note that the result is exactly the same 
     *    with respect to <code>==</code> for equal parameters. 
     */
    public static Package getPackage(List<String> path) {
	Package knot = ROOT;
	Package cand;
	for (int i = 0; i < path.size(); i++) {
	    cand = knot.subPackage(path.get(i));
	    if (cand != null) {
		knot = cand;
		continue;
	    }
	    cand = knot.addSubPackage(path.get(i));
	    assert cand == null;
	    knot = knot.subPackage(path.get(i));
	    assert knot != null;
	}
	
	return knot;
    }

    public String toString() {
	StringBuffer res = new StringBuffer();
	res.append("package ");
	res.append(getPath().toString());
/*
	res.append("<Package name=\"" + this.name);
	if (getSuperPackage() != null) {
	    res.append("\" superPkg=\"" + getSuperPackage().getName());
	}
	res.append("\">");
	
	// **** should be the full name 
	for (Package subPkg : this.subPackages.values()) {
	    res.append(subPkg.toString());
	}
	
	res.append("</Package>");
*/
	return  res.toString();
    }
    
} // Package

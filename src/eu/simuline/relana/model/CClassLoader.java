package eu.simuline.relana.model;

import eu.simuline.relana.parser.CClassParser;
import eu.simuline.relana.parser.ParseException;

import java.net.URL;
import java.net.URISyntaxException;

import java.io.File;
import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * The loader for all {@link CClass}es. 
 *
 *
 * Created: Thu Apr 14 23:35:25 2005
 *
 * @author <a href="mailto:ernst@local">Ernst Reissner</a>
 * @version 1.0
 */
public class CClassLoader {

    /* -------------------------------------------------------------------- *
     * inner classes.                                                       *
     * -------------------------------------------------------------------- */

    static class Occurence {
	private final ClassLocator loc;
	private final String component;
	Occurence(ClassLocator loc,String component) {
	    this.loc = loc;
	    this.component = component;
	}
	ClassLocator getLoc() {
	    return this.loc;
	}
	String compName() {
	    return this.component;
	}

	public String toString() {
	    StringBuffer res = new StringBuffer(25);
	    res.append("<Occurence>");
	    res.append(this.loc.toString());
	    res.append(this.component);

	    res.append("</Occurence>");
	    return res.toString();
	}
    } // class Occurence 

    static class ClassResolver implements CClassLink {
	private final ClassLocator loc;
	private final Set<Occurence> occurences;

	ClassResolver(ClassLocator loc) {
	    this.loc = loc;
	    this.occurences = new HashSet<Occurence>();
	}

	public String getName() {
	    return this.loc.getName();
	}

	public void addOccurence(Occurence occ) {
	    this.occurences.add(occ);
	}

	Set resolvationPoints() {
	    return this.occurences;
	}

	public boolean isResolved() {
	    return false;
	}

	public CClassLink setComponent(String name,CClass cClass) {
	    return null;
	}

	public String toString() {
	    StringBuffer res = new StringBuffer(40);
	    res.append("<ClassResolver>\n");
	    res.append(this.loc.toString());
	    res.append('\n');
	    res.append(this.occurences.toString());
	    res.append("\n</ClassResolver>");
	    return res.toString();
	}
    } // class ClassResolver 

    /* -------------------------------------------------------------------- *
     * class constants.                                                     *
     * -------------------------------------------------------------------- */


    /* -------------------------------------------------------------------- *
     * attributes.                                                          *
     * -------------------------------------------------------------------- */

    private final URL library;

    private final Map<ClassLocator,CClassLink> name2class;

    private final Stack<ClassLocator> unresolvedClasses;

    private final SClassLoader scLoader;

    /* -------------------------------------------------------------------- *
     * constructors.                                                        *
     * ---------------------------------------------------------------- --- */

    public CClassLoader(URL library) {
	this.library = library;

	this.name2class = new HashMap<ClassLocator,CClassLink>();
	this.name2class.put(new ClassLocator("Component",Package.BUILD_IN),
			    CClass.COMPONENT);
	this.unresolvedClasses = new Stack<ClassLocator>();

	this.scLoader = new SClassLoader(this.library);
    } // CClassLoader constructor
    
    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */


    public SClass loadSClass(ClassLocator loc,Package pkg) 
	throws IOException, ParseException {
	return this.scLoader.loadSClass(loc,pkg);
    }

    // **** copy from SClassParser **** used for superclass only. 
    public CClass loadCClass(ClassLocator loc,Package pkg) 
	throws IOException, ParseException {
	URL url = null;
	List<String> pkgPath = pkg.getPath();
	List<String> path = new ArrayList<String>(pkgPath);
	path.add("_");
	path.addAll(loc.getPath());

	for (int i = pkgPath.size(); i >= 0; i--) {
	    // try to resolve path and remove step by step entry i-1

	    path.remove(i);

	    // i points to the first entry of locPath within path 
	    ClassLocator currLoc  = ClassLocator.getLocator(path);

	    CClassLink cClass = this.name2class.get(currLoc);
	    if (cClass != null && cClass.isResolved()) {
		return (CClass)cClass;
	    }
	    // Here, class currLoc may not yet be resolved. 
	    try {
		url = new URL(this.library + 
			      currLoc.getPackage().getPathName()
			      .replace('.','/') + 
			      currLoc.getName() + ".ccl");
		File clsDoc = new File(url.toURI());
		if (clsDoc.exists()) {
		    return loadCClass(currLoc);
		}
	    } catch (URISyntaxException e) {
		throw new IOException// NOPMD
		    ("Could not locate file because no uri: " + 
		     url + ". ");
	    }
	} // for 

	throw new IOException("No appropriate class file found. ");
    }


    public CClass loadCClass(ClassLocator loc) 
	throws IOException, ParseException {
//System.out.println("loadCClass(");

	//CClassLink result = resolveLocInOcc(loc,null);!!!!!!!
	while (!this.unresolvedClasses.empty()) {
	    ClassLocator loc2 = this.unresolvedClasses.pop();
//System.out.println("-->loc2: "+loc2);
	    

	    InputStream str = new URL(this.library + 
				      loc2.getPackage().getPathName()
				      .replace('.','/')  + 
				      loc2.getName() + ".ccl")
		.openStream();
//System.out.println("file: "+path);


	    CClassParser ccParser = new CClassParser((Reader)null);
	    ccParser.setClassLoader(this);

	    ccParser.ReInit(new InputStreamReader(str));
	    CClass cClass = ccParser.cClass(loc2);
//System.out.println("XXthis.unresolvedClasses: "+ this.unresolvedClasses);

	    ClassResolver res = (ClassResolver)
		this.name2class.put(loc2,cClass);
	    Iterator iter = res.resolvationPoints().iterator();
	    while (iter.hasNext()) {
		Occurence occ = (Occurence)iter.next();
//System.out.println("occ: "+occ);
		((CClassLink)this.name2class.get(occ.getLoc()))
		    .setComponent(occ.compName(),cClass);

	    }
//System.out.println("XX2this.unresolvedClasses: "+ this.unresolvedClasses);
	}
	// Here, all links are resolved. 
//System.out.println("XX3this.unresolvedClasses: "+ this.unresolvedClasses);
	CClass cClass = (CClass)resolveLocInOcc(loc,null);
	cClass.verify();
	return cClass;
    }

    public CClassLink resolveLocInOcc(ClassLocator toBeResolved, 
				      ClassLocator loc,
				      String comp) {
	return  resolveLocInOcc(toBeResolved,new Occurence(loc,comp));
    }

    private CClassLink resolveLocInOcc(ClassLocator loc, Occurence occ) {
//System.out.println("resolveLocInOcc("+loc);
	
	CClassLink resolvation = (CClassLink)this.name2class.get(loc);
//System.out.println("resolvation: "+ resolvation);
	if (resolvation == null) {
	    // loc never occured before
	    resolvation = new ClassResolver(loc);
	    this.unresolvedClasses.push(loc);
//System.out.println("this.unresolvedClasses: "+ this.unresolvedClasses);
	    this.name2class.put(loc,resolvation);
	    // Here, it is as if resovation were not null. 
	}

	if (occ != null) {
	    ((CClassLink)resolvation).addOccurence(occ);
	}
	    

//System.out.println("...resolveLocInOcc("+loc);
	return resolvation;
    }

} // CClassLoader

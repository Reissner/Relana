package eu.simuline.relana.model;

import eu.simuline.relana.parser.SClassParser;
import org.antlr.v4.runtime.RecognitionException;

import java.net.URL;
import java.net.URISyntaxException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * SClassLoader for {@link SClass}es. 
 *
 *
 * Created: Wed Apr 20 21:56:45 2005
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
public class SClassLoader {

    /* -------------------------------------------------------------------- *
     * attributes.                                                          *
     * -------------------------------------------------------------------- */

    private final URL library;

    private final Map<ClassLocator, SClass> name2class;

    //private SClassParser scParser;

    /* -------------------------------------------------------------------- *
     * constructors.                                                        *
     * -------------------------------------------------------------------- */

    public SClassLoader(URL library) {
	this.library = library;
	this.name2class = new HashMap<ClassLocator, SClass>();
	this.name2class.put(new ClassLocator(SClass.BOOL_S_CLASS_NAME,
					     Package.BUILD_IN),
			    SClass.BOOLEAN);

    } // SClassLoader constructor
    
    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */


    public SClass loadSClass(ClassLocator loc,
			     Package pkg,
			     Map<ClassLocator, ClassLocator> subclassDep) 
	throws IOException, RecognitionException {
	
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

	    SClass sClass = this.name2class.get(currLoc);
	    if (sClass != null) {
		return sClass;
	    }
	    // Here, class currLoc may not yet be resolved. 
	    try {
		url = new URL(this.library + 
			      currLoc.getPackage().getPathName()
			      .replace('.', '/') + 
			      currLoc.getName() + ".scl");
		File clsDoc = new File(url.toURI());
		if (clsDoc.exists()) {
		    return resolveSClass(currLoc, subclassDep);
		}
	    } catch (URISyntaxException e) {
		throw new IOException// NOPMD
		    ("Could not locate file because no uri: " + 
		     url + ". ");
	    }

	} // for 

	throw new IOException("Found no class file for " + 
			      loc + " in " + pkg + ". ");
    }


    public SClass loadSClass(ClassLocator loc,
			     Package pkg) 
	throws IOException, RecognitionException {
	return loadSClass(loc, pkg, new HashMap<ClassLocator, ClassLocator>());
    }

    private SClass resolveSClass(ClassLocator loc,
				 Map<ClassLocator, ClassLocator> subclassDep) 
	throws IOException, RecognitionException {
	InputStream str = new URL(this.library + 
				  loc.getPackage().getPathName()
				  .replace('.', '/') + 
				  loc.getName() + ".scl")
	    .openStream();
	SClassParser scParser = new SClassParser(str);
	scParser.setClassLoader(this);
	SClass sClass = scParser.getSClass(loc, subclassDep);

	sClass.verify();
	this.name2class.put(loc, sClass);
	return sClass;
    }    
} // SClassLoader

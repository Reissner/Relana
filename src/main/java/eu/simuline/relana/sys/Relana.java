package eu.simuline.relana.sys;

import eu.simuline.relana.model.CClassLoader;
import eu.simuline.relana.model.CClass;
import eu.simuline.relana.model.FlatCInstance;
import eu.simuline.relana.model.SInstance;
import eu.simuline.relana.model.InstanceLocator;
//import eu.simuline.relana.model.Deficiency;

import eu.simuline.util.sgml.SGMLParser;

import org.xml.sax.SAXException;

import org.antlr.v4.runtime.RecognitionException;

import java.net.URL;
import java.net.MalformedURLException;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

//import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

/**
 * Relana's main class containing the main method only. 
 * The main method is intended to be invoked with a single argument 
 * which is interpreted as the url of a project file <code>*.rml</code> 
 * which is read by an instance of {@link Project}. 
 * A project describes the classes of a whole model and highlights 
 * a single component class as the "base class" 
 * which holds the overall model. 
 * <p>
 * The base class may not specify input 
 * as the whole model shall be closed in itself. 
 * **** all variables must be declared as output. **** 
 * Then an instance is created and flattened, eliminating all structures 
 * not required for computations. 
 * Finally the observables each of which are instances of a service classes, 
 * are extracted and their probability is computed. 
 * This is the output. 
 *
 * Created: Thu Apr 28 21:46:41 2005
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
public abstract class Relana { // NOPMD 
    public Relana() {
	// is empty. 
    } // Relana constructor

    public static void main(String[] args) 
	throws MalformedURLException, IOException, SAXException, 
	       RecognitionException {
	if (args.length != 1) {
	    throw new IllegalArgumentException
		("Expected a single argument: the project file " + 
		 "but found " + Arrays.asList(args) + ". ");
	}
	// Here, args[0] ist the only existing entry. 

	// read project file 
	URL proj = new URL(args[0]);
	InputStreamReader projectStr = 
	    new InputStreamReader(proj.openStream(), "UTF-8");
	SGMLParser projectParser = new SGMLParser();
	projectParser.parseXML(true);
	Project project = new Project();
	projectParser.setContentHandler(project);
	projectParser.setExceptionHandler(project);
	projectParser.parse(new BufferedReader(projectStr));
System.out.println("project: " + project);

	// load class and make sure that no input-effects occur. 
	CClassLoader loader = new CClassLoader(project.getLibrary());
	CClass cClass = loader.loadCClass(project.getBaseClass());
System.out.println("cClass: " + cClass);
	Set<CClass.SClassDecl> decls = cClass.getEffectsRec();
	//InstanceLocator loc;
	for (CClass.SClassDecl decl : decls) {
	    if (decl.isInput()) {
		throw new IllegalArgumentException
		    ("Found declaration of input variable " + decl + ". ");
	    }
	}

	// instantiate and get all output-variables under observation. 
	FlatCInstance flatCInstance = cClass.getInstance().flatten();
System.out.println("cInstance: " + flatCInstance);

	// verify whether all output effects are indeed declared as output 
	Set<InstanceLocator> outServ = project.getOutputEffects();
System.out.println("outServ: " + outServ);
	Map<List<String>, SInstance> observables = 
	    new HashMap<List<String>, SInstance>();
	for (InstanceLocator loc : outServ) {
	    SInstance serv = flatCInstance.getEffect(loc);
	    CClass.SClassDecl decl = cClass.getEffectDecl(loc.getPath());
	    if (!decl.isOutput()) {
		throw new IllegalArgumentException
		    ("Found non-output variable " + loc + ". ");
	    }
	    // this works with HashMap observables, 
	    // because loc.getPath() yields an unmodifyable list. 
	    observables.put(loc.getPath(), serv);
	}
	
	// Here, observables contains all stuff under consideration. 

System.out.println("observables: " + observables);
System.out.println("\nprobabilities: ");

	for (List<String> obs : observables.keySet()) {
	    System.out.println("\nobs : " + obs + 
			       " has prob " + flatCInstance.getProb(obs));

	}
    }
} // Relana

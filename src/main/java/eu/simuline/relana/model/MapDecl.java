package eu.simuline.relana.model;

import java.util.Map;
import java.util.Set;

/**
 * Represents the declaration of a map. 
 *
 *
 * Created: Mon May 23 18:54:29 2005
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
public final class MapDecl {

    /* -------------------------------------------------------------------- *
     * attributes.                                                          *
     * -------------------------------------------------------------------- */

    private boolean isRedeclare; // NOPMD ****
    private String mapName; // NOPMD ****
    private final Map<Set<Deficiency>, Deficiency> setOfNew2old;
    private final SClass source;

    private final SClass target;

    /*
     * The set of deficiencies mapped identically. 
     * As a consequence, this is a subset of both 
     * the set represented by {@link #source} 
     * and the set represented by {@link #target}. 
     */
    //private final Set<Deficiency> idDom;

    /**
     * The <code>map</code> declared here. 
     */
    private final DeficiencyMap map;

    /* -------------------------------------------------------------------- *
     * constructors.                                                        *
     * -------------------------------------------------------------------- */

    @edu.umd.cs.findbugs.annotations.SuppressWarnings
	(value = "URF_UNREAD_FIELD", 
	 justification = "to be used in later versions ")
    public MapDecl(boolean isRedeclare,
		   String mapName,
		   Map<Set<Deficiency>, Deficiency> setOfNew2old,
		   SClass source,
		   SClass target,
		   Set<Deficiency> idDom) {
	this.isRedeclare = isRedeclare;
	this.mapName = mapName;
	this.setOfNew2old = setOfNew2old;
	this.source = source;
	this.target = target;
//	this.idDom = idDom;
	this.map = new DeficiencyMap(this.setOfNew2old,
				     this.source,
				     this.target,
				     /* */idDom);
    } // MapDecl constructor

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    public DeficiencyMap getMap() {
	return this.map;
    }

    public String toString() {
	StringBuffer res = new StringBuffer();
	res.append("<MapDecl source=\"");
	res.append(source.getPathName().toString());
	res.append("\" target=\"");
	res.append(target.getPathName().toString());
	res.append("\">\n");
	res.append(this.setOfNew2old.toString());
	res.append("\n</MapDecl>");
	return res.toString();
    }

} // MapDecl

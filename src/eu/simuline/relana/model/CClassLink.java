package eu.simuline.relana.model;

/**
 * CClassLink.java
 *
 *
 * Created: Fri Apr 15 13:55:39 2005
 *
 * @author <a href="mailto:ernst@local">Ernst Reissner</a>
 * @version 1.0
 */
public interface CClassLink {

    String getName();
    boolean isResolved();
    CClassLink setComponent(String name,CClass cClass);
    void addOccurence(CClassLoader.Occurence occ);

}// CClassLink

package eu.simuline.relana.model;

/**
 * Represents a link to a {@link CClass} which also implements this interface. 
 * It is needed for resolution of {@link CClass}es. 
 *
 * Created: Fri Apr 15 13:55:39 2005
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
public interface CClassLink {

    String getName();
    boolean isResolved();
    CClassLink setComponent(String name, CClass cClass);
    void addOccurrence(CClassLoader.Occurrence occ);

} // CClassLink 

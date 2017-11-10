package eu.simuline.relana.model;

/**
 * Exception during verification of a class. 
 *
 * Thrown by {@link CClass} if 
 * <ul>
 * <li>
 * an effect is redeclared without being declared, 
 * <li>
 * redeclaration would weaken access privilegies 
 * <li>
 * trial to redeclare other but subclass 
 * <li>
 * Found effect with the same name: consider redeclare: more a warning 
 * <li>
 * declaration in class and in superclass 
 * </ul>
 *
 * Trown by {@link SClass} if 
 * <ul>
 * <li>
 * minimal/maximal element not unique 
 * <li>
 * relation should be spec in inner class because ... 
 * <li>
 * relation not reflected by superclass 
 * <li>
 * deficiency cyclically related with itself. 
 * <li>
 * no deficiencies found. 
 * <li>
 * relation has no minimal elements and is hence cyclic. 
 * <li>
 * <li>
 * relation is cyclic. 
 * </ul>
 * 
 * Created: Wed Apr 20 23:38:10 2005
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
public class VerifyException extends RuntimeException {

    private static final long serialVersionUID = -2479143000061671589L;

    public VerifyException(String message) {
	super(message);
    }
} // VerifyException

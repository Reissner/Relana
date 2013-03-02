package eu.simuline.relana.model;

/**
 * VerifyException.java
 *
 *
 * Created: Wed Apr 20 23:38:10 2005
 *
 * @author <a href="mailto:ernst@local">Ernst Reissner</a>
 * @version 1.0
 */
public class VerifyException extends RuntimeException {
    private static final long serialVersionUID = -2479143000061671589L;
    public VerifyException(String message) {
	super(message);
    } // VerifyException constructor
    
} // VerifyException

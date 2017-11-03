/**
 * Description for object oriented models in terms of java classes. 
 * Relana models are object oriented and the base class 
 * is a Component Class, represented by a {@link CClass}. 
 * A {@link CClass} is described by a <code>*.ccl</code> file 
 * and loaded by a {@link CClassLoader} using a {@link CClassParser} and 
 * an instance is represented by a {@link CInstance}. 
 * <p>
 * Components may be nested, i.e. contain subcomponents 
 * and those may be represented by a {@link CClassLink} 
 * which may be resolved to a {@link CClass} by need. 
 * Also, a component may provide services 
 * and services are also specified object oriented. 
 * Thus a service is represented by an {@link SClass}. 
 * An {@link SClass} is described by a <code>*.scl</code> file 
 * and loaded by an {@link SClassLoader} using an {@link SClassParser} and 
 * an instance is represented by a {@link CInstance}. 
 *
 * <code>x</code> ***** NOT YET COMPLETE. 
 */
package eu.simuline.relana.model;

import eu.simuline.relana.parser.CClassParser;
import eu.simuline.relana.parser.SClassParser;


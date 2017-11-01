
package eu.simuline.relana.model;

import eu.simuline.testhelpers.Actions;
// import eu.simuline.testhelpers.Accessor;
// import eu.simuline.testhelpers.Assert;

// import static org.junit.Assert.assertEquals;
// import static org.junit.Assert.assertTrue;
// import static org.junit.Assert.assertNull;
// import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.runner.JUnitCore;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


/**
 * DeficiencyMapTest.java
 *
 *
 * Created: Mon May 23 21:23:07 2005
 *
 * @author <a href="mailto:ernst@local">Ernst Reissner</a>
 * @version 1.0
 */ 
@RunWith(Suite.class)
@SuiteClasses({DeficiencyMapTest.TestAll.class})
public class DeficiencyMapTest {

    private final static String ROOT = 
    "file://" + 
	System.getProperty("environment") + 
	"eu/simuline/relana/";
 
    /* -------------------------------------------------------------------- *
     * framework.                                                           *
     * -------------------------------------------------------------------- */

    static DeficiencyMapTest TEST = new DeficiencyMapTest();

    public static class TestAll {
	@Test public void testConstructor() {
	    DeficiencyMapTest.TEST.testConstructor();
	} // testConstructor

	@Test public void testCompose() {
	    DeficiencyMapTest.TEST.testCompose();
	} // testCompose
    } // class TestAll


    /* -------------------------------------------------------------------- *
     * methods for tests.                                                   *
     * -------------------------------------------------------------------- */



    public void testConstructor() {

    } // testConstructor

    public void testCompose() {
    } // testCompose

    /* -------------------------------------------------------------------- *
     * framework.                                                           *
     * -------------------------------------------------------------------- */

    /**
     * Runs the test case.
     *
     * Uncomment either the textual UI, Swing UI, or AWT UI.
     */
    public static void main(String args[]) {
	Actions.runFromMain();
     }

} // DeficiencyMapTest


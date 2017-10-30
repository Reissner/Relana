
package eu.simuline.relana.sys;

import        eu.simuline.testhelpers.Actions;
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
 * RelanaTest.java
 *
 *
 * Created: Mon May 23 21:23:07 2005
 *
 * @author <a href="mailto:ernst@local">Ernst Reissner</a>
 * @version 1.0
 */ 
@RunWith(Suite.class)
@SuiteClasses({RelanaTest.TestAll.class})
public class RelanaTest {

    private final static String ROOT = 
    "file://" + 
	System.getProperty("environment") + 
	"src/test/resources/" + 
	"eu/simuline/relana/";

    /* -------------------------------------------------------------------- *
     * framework.                                                           *
     * -------------------------------------------------------------------- */


    static RelanaTest TEST = new RelanaTest();

    public static class TestAll {
	@Test public void testTinyPlane() throws Exception {
	    RelanaTest.TEST.testTinyPlane();
	}
	@Test public void testTinyPlaneAdd() throws Exception {
	    RelanaTest.TEST.testTinyPlaneAdd();
	}
	@Test public void testTinyPlaneB() throws Exception {
	    RelanaTest.TEST.testTinyPlaneB();
	}
	@Test public void testProj() throws Exception {
	    RelanaTest.TEST.testProj();
	}
	@Test public void testOrdMaps() throws Exception {
	    RelanaTest.TEST.testOrdMaps();
	}
    } // class TestAll

    /* -------------------------------------------------------------------- *
     * methods for tests.                                                   *
     * -------------------------------------------------------------------- */



	public void testTinyPlane() throws Exception {
	    Relana.main(new String[] {ROOT + "tinyPlane.rml"});
	    Relana.main(new String[] {ROOT + "tinyPlane2.rml"});
	    Relana.main(new String[] {ROOT + "tinyPlane3.rml"});
	}
	public void testTinyPlaneAdd() throws Exception {
	    Relana.main(new String[] {ROOT + "tinyPlaneAdd.rml"});
	}
	public void testTinyPlaneB() throws Exception {
	    Relana.main(new String[] {ROOT + "tinyPlaneB2.rml"});
	    //Relana.main(new String[] {ROOT + "tinyPlaneB3.rml"});
	}

	public void testProj() throws Exception {
	    Relana.main(new String[] {ROOT + "proj.rml"});
	}

	public void testOrdMaps() throws Exception {
	    Relana.main(new String[] {ROOT + "OrdMaps.rml"});
	}

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

} // RelanaTest

package comp2800_project;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Unit tests for Developer — mirrors testDeveloper.py exactly.
 */
public class TestDeveloper {

    private Developer dev;

    @Before
    public void setUp() {
        dev = new Developer();
    }

    @Test
    public void test_setPermission() {
        try {
            dev.setPermission(374666);
            assertEquals(2, dev.getPermission());
        } catch (IncorrectPasswordException e) {
            fail("Correct PIN should not throw");
        }

        try {
            dev.setPermission(123456);
            fail("Expected IncorrectPasswordException");
        } catch (IncorrectPasswordException e) {
            assertEquals(0, dev.getPermission());
        }
        System.out.println("setPermission passed");
    }

    @Test
    public void test_getPermission() {
        assertEquals(0, dev.getPermission());
        try {
            dev.setPermission(374666);
        } catch (IncorrectPasswordException e) {
            fail("Correct PIN should not throw");
        }
        assertEquals(2, dev.getPermission());
        System.out.println("getPermission passed");
    }
}

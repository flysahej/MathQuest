package comp2800_project;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Unit tests for Instructor — mirrors testInstructor.py exactly.
 */
public class TestInstructor {

    private Instructor instructor;

    @Before
    public void setUp() {
        instructor = new Instructor();
    }

    @Test
    public void test_setPermission() throws IncorrectPasswordException {
        instructor.setPermission(666473);
        assertEquals(1, instructor.getPermission());

        try {
            instructor.setPermission(123456);
            fail("Expected IncorrectPasswordException");
        } catch (IncorrectPasswordException e) {
            assertEquals(0, instructor.getPermission());
        }
        System.out.println("setPermission passed");
    }

    @Test
    public void test_getPermission() throws IncorrectPasswordException {
        assertEquals(0, instructor.getPermission());
        instructor.setPermission(666473);
        assertEquals(1, instructor.getPermission());
        System.out.println("getPermission passed");
    }
}

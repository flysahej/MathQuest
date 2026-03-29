package comp2800_project;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Unit tests for Player — mirrors testPlayer.py exactly.
 */
public class TestPlayer {

    private Player player;

    @Before
    public void setUp() {
        player = new Player();
    }

    @Test
    public void test_setPermission() {
        player.setPermission(123456);
        assertEquals(0, player.getPermission());
        System.out.println("setPermission passed");
    }

    @Test
    public void test_getPermission() {
        player.setPermission(2468);
        assertEquals(0, player.getPermission());
        System.out.println("getPermission passed");
    }

    @Test
    public void test_setName() {
        player.setName("John");
        assertEquals("John", player.getName());
        assertNotNull(player);
        System.out.println("setName passed");
    }

    @Test
    public void test_getName() {
        player.setName("Jane");
        assertEquals("Jane", player.getName());
        System.out.println("getName passed");
    }

    @Test
    public void test_setID() {
        player.setID();
        assertTrue(player.getID() >= 0 && player.getID() <= 10000);
        System.out.println("setID passed");
    }

    @Test
    public void test_getID() {
        player.setID();
        assertTrue(player.getID() >= 0);
        System.out.println("getID passed");
    }

    @Test
    public void test_updateScore() {
        player.updateScore(1000);
        assertEquals(1000.0, player.getScore(), 0.001);
        player.updateScore(2000);
        assertEquals(3000.0, player.getScore(), 0.001);
        System.out.println("updateScore passed");
    }

    @Test
    public void test_getScore() {
        player.updateScore(1000);
        assertEquals(1000.0, player.getScore(), 0.001);
        player.updateScore(2000);
        assertEquals(3000.0, player.getScore(), 0.001);
        System.out.println("getScore passed");
    }

    @Test
    public void test_updateDuckCount() {
        player.updateDuckCount(1);
        assertEquals(1, player.getDuckCount());
        player.updateDuckCount(2);
        assertEquals(3, player.getDuckCount());
        System.out.println("updateDuckCount passed");
    }

    @Test
    public void test_getDuckCount() {
        player.updateDuckCount(1);
        assertEquals(1, player.getDuckCount());
        player.updateDuckCount(2);
        assertEquals(3, player.getDuckCount());
        System.out.println("getDuckCount passed");
    }

    @Test
    public void test_updateStreakCount() {
        player.updateStreakCount();
        assertEquals(1, player.getStreakCount());
        player.updateStreakCount();
        assertEquals(2, player.getStreakCount());
        System.out.println("updateStreakCount passed");
    }

    @Test
    public void test_getStreakCount() {
        player.updateStreakCount();
        assertEquals(1, player.getStreakCount());
        player.updateStreakCount();
        assertEquals(2, player.getStreakCount());
        System.out.println("getStreakCount passed");
    }

    @Test
    public void test_updateCorrectCount() {
        player.updateCorrectCount();
        assertEquals(1, player.getCorrectCount());
        player.updateCorrectCount();
        assertEquals(2, player.getCorrectCount());
        System.out.println("updateCorrectCount passed");
    }

    @Test
    public void test_getCorrectCount() {
        player.updateCorrectCount();
        assertEquals(1, player.getCorrectCount());
        player.updateCorrectCount();
        assertEquals(2, player.getCorrectCount());
        System.out.println("getCorrectCount passed");
    }
}

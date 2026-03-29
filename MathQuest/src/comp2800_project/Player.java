package comp2800_project;

import java.util.Random;

/**
 * Represents a player in MathQuest.
 */
public class Player {

    private int    permissionValue = 0;
    private int    playerID;
    private String playerName;
    private double score        = 0;
    private int    duckCount    = 0;
    private int    streakCount  = 0;
    private int    correctCount = 0;

    public Player() {}

    public void setPermission(int pinInput) {
        this.permissionValue = 0;
    }

    public void setName(String name) {
        this.playerName = name;
    }

    public void setID() {
        this.playerID = new Random().nextInt(10001);
    }

    public void updateScore(double score) {
        this.score += score;
    }

    public void updateDuckCount(int count) {
        this.duckCount += count;
    }

    public void updateStreakCount() {
        this.streakCount++;
    }

    public void updateCorrectCount() {
        this.correctCount++;
    }

    public int    getPermission()   { return this.permissionValue; }
    public int    getID()           { return this.playerID; }
    public String getName()         { return this.playerName; }
    public double getScore()        { return this.score; }
    public int    getDuckCount()    { return this.duckCount; }
    public int    getStreakCount()  { return this.streakCount; }
    public int    getCorrectCount() { return this.correctCount; }
}

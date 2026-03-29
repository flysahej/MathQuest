package comp2800_project;

import java.awt.*;
import java.awt.image.*;

/**
 * Core game canvas — holds the buffer strategy and render loop.
 * The actual gameplay runs through MainMenu → GameBoard.
 */
public class Game extends Canvas implements Runnable {
    private Thread thread;
    private BufferStrategy buffer;

    public static GameManager gm = new GameManager();

    public Game() {}

    public void start() {
        this.createBufferStrategy(2);
        buffer = this.getBufferStrategy();
        thread = new Thread(this, "Graphics Thread");
        thread.start();
    }

    @Override
    public void run() {
        while (true) {
            render();
        }
    }

    private void render() {
        Graphics2D g2d = (Graphics2D) buffer.getDrawGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.dispose();
        buffer.show();
    }
}

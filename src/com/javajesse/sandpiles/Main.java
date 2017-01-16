import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Manages the animation of the growth of the piles board.
 * 
 * @author jesse
 *
 */
public class Main extends JComponent {

    private static final long serialVersionUID = 1L;

    static final int WIDTH = 800;
    static final int HEIGHT = 800;

    // initial setup
    static final int START_PILE = 1000000;
    static final int START_PILE_RADIUS = 0;
    static final int MAX = 3;

    // this delay can help keep your cpu fan from going full speed
    static final int TIMER_DELAY = 1;

    // drop just the top 4? Or split the pile into fourths and drop those sub-piles to neighbors?
    static final boolean DROP_ALL_PILE = false;

    // provides more smoothing vs specific control of each color value
    static final boolean INCLUDE_NEIGHBORS_AS_GRADIENTS = false;
    static int[] GRADIENT_MASK = { 0, 0, 1 };

    // the 5th element will be used for any pile > 4
    static Color[] COLORS = new Color[] {

            new Color(250, 250, 0),
            new Color(250, 200, 0),
            new Color(250, 150, 0),
            new Color(250, 100, 0),
            new Color(0, 0, 0) // new Color(100, 50, 0) is not so "harsh"

    };

    // used to differentiate between cells that once had a value and now have 0
    // from cells that never had a value (lie outside the current drawing)
    static boolean[][] overlay = new boolean[WIDTH][HEIGHT];

    // the state of the board
    public static boolean TOPPLE = true;
    static long[][] piles = null;

    /**
     * Draw the state of the board
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int x = 1; x < WIDTH - 1; x++) {
            for (int y = 1; y < HEIGHT - 1; y++) {
                if (overlay[x][y]) {

                    Color color = getColor(piles, x, y);
                    g.setColor(color);
                    g.drawLine(x, HEIGHT - y, x, HEIGHT - y);

                    // short-circuit if we get to the edge. Party is over
                    if (x >= WIDTH - 1 || x <= 1 || y >= HEIGHT - 1 || y <= 1) {
                        TOPPLE = false;
                    }
                }
            }
        }
    }

    /**
     * Kick off the Thread that will (1) generate the next board state and (2) call repaint to paint it
     */
    public Main() {
        new Timer(TIMER_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actEvt) {
                // continue until no piles can be toppled
                if (TOPPLE) {
                    piles = Piles.getNextPiles(piles);
                    repaint();
                }
                else {
                    // done!
                    ((Timer) actEvt.getSource()).stop();
                }
            }
        }).start();

    }

    public static void main(String[] args) {
        piles = initPiles(START_PILE, START_PILE_RADIUS);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGui();
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    private static void createAndShowGui() {
        Main mainPanel = new Main();

        JFrame frame = new JFrame("Piles");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.getContentPane().setBackground(Color.white);

        frame.setVisible(true);
    }

    public static long[][] initPiles(int center, int radius) {

        piles = new long[WIDTH][HEIGHT];

        int centerx = WIDTH / 2;
        int centery = HEIGHT / 2;

        piles[centerx][centery] = START_PILE;

        for (int x = 0; x < radius; x++) {
            for (int y = 0; y < radius; y++) {
                piles[centerx + x][centery + y] = START_PILE;
            }
        }

        return piles;
    }

    /**
     * If INCLUDE_NEIGHBORS_AS_GRADIENTS, we call getColorGradientBasedOnNeighbors() Otherwise, just take the color from the COLORS array
     * 
     * @param piles
     * @param x
     * @param y
     * @return
     */
    static Color getColor(long[][] piles, int x, int y) {

        if (INCLUDE_NEIGHBORS_AS_GRADIENTS) {
            return getColorGradientBasedOnNeighbors(piles, x, y);
        }
        else {
            long val = piles[x][y];

            if (val < 0) {
                // how could val ever be negative? dunno... code happens...
                return Color.white;
            }
            else if (val > MAX) {
                return COLORS[4];
            }
            else {
                return COLORS[(int) val];
            }
        }
    }

    /**
     * Rather than using a piles value to determine the color, include the values of the neighbors as well. Should provide more "smoothing"
     * 
     * @param piles
     * @param x
     * @param y
     * @return
     */
    static Color getColorGradientBasedOnNeighbors(long[][] piles, int x, int y) {

        long result = piles[x][y];

        if (result == 0) {
            return Color.white;
        }

        result += piles[x - 1][y];
        result += piles[x + 1][y];
        result += piles[x][y - 1];
        result += piles[x][y + 1];
        result += piles[x - 1][y + 1];
        result += piles[x + 1][y + 1];
        result += piles[x - 1][y - 1];
        result += piles[x + 1][y - 1];

        double ratio = (result * 1.0) / 27;

        if (ratio > 1.0) {
            ratio = 1.0;
        }

        int colorVal = (int) (200 * ratio);
        colorVal = 255 - colorVal;

        return new Color(GRADIENT_MASK[0], GRADIENT_MASK[1], GRADIENT_MASK[2]);

    }

}
package com.javajesse.sandpiles;

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

    // a delay between each iteration will keep your cpu fan from waking up your cat
    static final int TIMER_DELAY = 1;

    static final int WIDTH = 800;
    static final int HEIGHT = 800;
    static final int START_X = (WIDTH/2);
    static final int START_Y = (HEIGHT/2);

    // initial setup
    static final int START_PILE = 1000000;
    static final int START_PILE_RADIUS = 0;

    static final int MAX = 3;

    // distribute only the top 4? Or split the pile into quarters and drop those sub-piles to neighbors?
    static final boolean DROP_ALL_PILE = false;

    // should I move items to a pile if it's larger than I was to begin with?
    static final boolean CAN_DISTRIBUTE_TO_LARGER_PILE = false;

    static String getTitle() {
        return "Piles  [drop_all="+DROP_ALL_PILE+", can_dist_to_larger="+CAN_DISTRIBUTE_TO_LARGER_PILE+", start="+START_PILE+", radius="+START_PILE_RADIUS+"]";
    }

    // provides more smoothing vs specific control of each color value
    static final boolean INCLUDE_NEIGHBORS_AS_GRADIENTS = true;
    static double[] GRADIENT_MASK = { -0.5, 0.0, -1.0 };

    // the 5th element will be used for any pile > 3
    static Color[] COLORS = new Color[] {

            new Color(255, 255, 255),
            new Color(250, 226, 125),
            new Color(250, 150, 75),
            new Color(250, 50, 0),
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

        JFrame frame = new JFrame(getTitle());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.getContentPane().setBackground(Color.white);

        frame.setVisible(true);
    }

    public static long[][] initPiles(int center, int radius) {

        piles = new long[WIDTH][HEIGHT];

        int centerx = START_X;
        int centery = START_Y;

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

        int colorValRed = (int) (250 * ratio * GRADIENT_MASK[0]);
        if (colorValRed <0) colorValRed = 255 + colorValRed;
        int colorValGreen = (int) (250 * ratio * GRADIENT_MASK[1]);
        if (colorValGreen <0) colorValGreen = 255 + colorValGreen;
        int colorValBlue = (int) (250 * ratio * GRADIENT_MASK[2]);
        if (colorValBlue <0) colorValBlue = 255 + colorValBlue;

        return new Color(colorValRed, colorValGreen, colorValBlue);

    }

}
package com.javajesse.sandpiles;

public class Piles {

    public Piles() {
    }

    public static long[][] getNextPiles(long[][] piles) {

        int width = Main.WIDTH;
        int height = Main.HEIGHT;

        Main.TOPPLE = false;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {

                if (piles[x][y] > 0) {
                    Main.overlay[x][y] = true;
                }

                if (piles[x][y] > Main.MAX) {

                    Main.TOPPLE = true;
                    long amount = piles[x][y];

                    long amountRemoved = 0;

                    long amountToMove = 1;
                    if (Main.DROP_ALL_PILE) {
                        amountToMove = (long) Math.floor(amount / 4);
                    }

                    if (piles[x][y - 1] <= amount || Main.CAN_DISTRIBUTE_TO_LARGER_PILE) {
                        piles[x][y - 1] += amountToMove;
                        amountRemoved += amountToMove;
                    }

                    if (piles[x][y + 1] <= amount || Main.CAN_DISTRIBUTE_TO_LARGER_PILE) {
                        piles[x][y + 1] += amountToMove;
                        amountRemoved += amountToMove;
                    }

                    if (piles[x - 1][y] <= amount || Main.CAN_DISTRIBUTE_TO_LARGER_PILE) {
                        piles[x - 1][y] += amountToMove;
                        amountRemoved += amountToMove;
                    }

                    if (piles[x + 1][y] <= amount || Main.CAN_DISTRIBUTE_TO_LARGER_PILE) {
                        piles[x + 1][y] += amountToMove;
                        amountRemoved += amountToMove;
                    }

                    if (amountRemoved > piles[x][y]) {
                        piles[x][y] = 0;
                    } else {
                        piles[x][y] -= amountRemoved;
                    }
                }
            }

        }

        // at the very end, show some stats
        if (!Main.TOPPLE) {
            showSum(piles);
        }

        return piles;
    }

    public static void showSum(long[][] piles) {

        int minx = Main.WIDTH;
        int maxx = 0;
        int miny = Main.HEIGHT;
        int maxy = 0;
        int bigx = 0;
        int bigy = 0;
        long bigval = 0;

        long result = 0;
        for (int y = 0; y < Main.WIDTH; y++) {
            for (int x = 0; x < Main.HEIGHT; x++) {
                long val = piles[x][y];
                if (val > 0) {
                    result += val;
                    if (x < minx)
                        minx = x;
                    if (y < miny)
                        miny = y;
                    if (x > maxx)
                        maxx = x;
                    if (y > maxy)
                        maxy = y;
                }
                if (val > Main.MAX && val > bigval) {
                    bigval = val;
                    bigx = x;
                    bigy = y;
                }
            }
        }
        int area = (maxx - minx) * (maxy - miny);
        System.out.println(
            result + "  (" + minx + "," + miny + "),(" + maxx + "," + maxy + ") AERA = " + area + "   BIG = " + bigval + " @ (" + bigx + ","
                + bigy + ")");

    }
}

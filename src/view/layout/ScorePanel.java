package view.layout;

import model.Board;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;

import static model.Board.*;


/**
 * Panel to display player's score.
 * Implements PCL to update score.
 *
 * @author James
 * @author Tyler
 * @author Josh
 * @author Cam
 * @version 3.0
 */
public final class ScorePanel extends JPanel implements PropertyChangeListener {

    /**
     * x coord of score text.
     */
    private static final int TEXT_X = 5;

    /**
     * y coord of score text.
     */
    private static final int TEXT_Y = 25;

    /**
     * Size of score text.
     */
    private static final int TEXT_SIZE = 20;

    /**
     * Score gained for each row cleared.
     */
    private static final int SCORE_GAIN = 10;

    /**
     * Score gained for one row cleared.
     */
    private static final int ONE_LINE_CLEARED = 40;
    /**
     * Score gained for two rows cleared.
     */
    private static final int TWO_LINES_CLEARED = 100;
    /**
     * Score gained for three rows cleared.
     */
    private static final int THREE_LINES_CLEARED = 300;
    /**
     * Score gained for four rows cleared.
     */
    private static final int FOUR_LINES_CLEARED = 1200;

    /**
     * Level up every 5 rows cleared.
     */
    private static final int LEVEL_UP = 5;

    private static final int LEVEL_UP_TIMER_CHANGE = 2;

    /**
     * Ensures only one panel is instantiated.
     */
    private static int count;

    /**
     * Score of the player. Updates when rows are cleared.
     */
    private int myScore;

    /**
     * Number of lines cleared by the player.
     */
    private int myLinesCleared;

    /**
     * The level of the game.
     */
    private int myLevel;

    /**
     * Timer to manage game updates at regular intervals.
     */
    private final Timer myGameTimer;

    /**
     *  The game board associated with this menu.
     */
    private Board myBoard;

    /**
     * The original delay of the timer.
     */
    private final int myOriginalDelay;

    /**
     * Panel used to display the player's score.
     * Sets background color.
     *
     * @throws IllegalArgumentException if more than one ScorePanel is instantiated.
     */
    public ScorePanel(final Board theBoard, final Timer theGameTimer) {
        super();
        myBoard = theBoard;
        myScore = 0;
        myLinesCleared = 0;
        myLevel = 1;
        myOriginalDelay = theGameTimer.getDelay();
        this.myGameTimer = theGameTimer;
//        this.myBoard.addPropertyChangeListener(this);

        if (count > 0) {
            throw new IllegalArgumentException("Only one ScorePanel allowed");
        }
        count++;

        setLayout(new BorderLayout());
        final JPanel scorePanel = new JPanel(new FlowLayout());
        scorePanel.setOpaque(false);
    }

    @Override
    public void paintComponent(final Graphics theGraphics) {
        super.paintComponent(theGraphics);
        final Graphics2D g2d = (Graphics2D) theGraphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        final int w = getWidth();
        final int h = getHeight();
        final Color color1 = Color.YELLOW;
        final Color color2 = Color.CYAN;
        final GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        g2d.setPaint(Color.BLACK);
        showScores(theGraphics);
    }

    /**
     * Displays player's score in score panel.
     *
     * @param theGraphics Graphics object used to display text.
     */
    private void showScores(final Graphics theGraphics) {
        theGraphics.setFont(new Font("" + theGraphics.getFont(), Font.PLAIN, TEXT_SIZE));
        theGraphics.drawString("Score: " + myScore, TEXT_X, TEXT_Y + 5);
        theGraphics.drawString("Level:  " + myLevel, TEXT_X, TEXT_Y + TEXT_SIZE + 10);
        theGraphics.drawString("Lines:  " + myLinesCleared, TEXT_X, TEXT_Y + TEXT_SIZE * 2 + 15);

        theGraphics.setFont(new Font("" + theGraphics.getFont(), Font.PLAIN, TEXT_SIZE - 7));
        theGraphics.drawString("Next level in " + (LEVEL_UP - myLinesCleared % LEVEL_UP) + " lines",
                TEXT_X, TEXT_Y + TEXT_SIZE * 3 + 15);
    }

    /**
     * Updates the score and timer when a 5 rows are cleared.
     */
    private void calculateLevel() {
        if (myLinesCleared % LEVEL_UP == 0) {
            myLevel++;
            myGameTimer.setDelay((myGameTimer.getDelay() / LEVEL_UP_TIMER_CHANGE) + myGameTimer.getDelay() / 4);
        }
    }

    /**
     * Updates the score when a row is cleared.
     */
    private void calculateScore(final int theNumberOfRowsCleared) {
        int score = 0;
        switch (theNumberOfRowsCleared) {
            case 1:
                score = ONE_LINE_CLEARED;
                break;
            case 2:
                score = TWO_LINES_CLEARED;
                break;
            case 3:
                score = THREE_LINES_CLEARED;
                break;
            case 4:
                score = FOUR_LINES_CLEARED;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + theNumberOfRowsCleared);
        }
        myScore += score * myLevel;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent theEvent) {
        if (PROPERTY_ROW_CLEARED.equals(theEvent.getPropertyName())) {
            if ((int) theEvent.getNewValue() == 1 || (int) theEvent.getNewValue() == 2 ||
                    (int) theEvent.getNewValue() == 3 || (int) theEvent.getNewValue() == 4) {

                System.out.println("lines cleared: " + theEvent.getNewValue());

                myLinesCleared += (int) theEvent.getNewValue();
                calculateScore((int) theEvent.getNewValue());
                calculateLevel();
                repaint();
            }
        }

        if (PROPERTY_NEW_GAME.equals(theEvent.getPropertyName())) {
            myScore = 0;
            myLinesCleared = 0;
            myLevel = 1;
            myGameTimer.setDelay(myOriginalDelay);
            repaint();
        }

        if (PROPERTY_NEXT_PIECE_CHANGES.equals(theEvent.getPropertyName())) {
            System.out.println("Score + 4");
            myScore += 4;
        }
    }
}
package view.layout;

import static javax.sound.sampled.AudioSystem.getAudioInputStream;
import static model.Board.PROPERTY_GAME_OVER;
import static model.Board.PROPERTY_NEW_GAME;
import static model.Board.PROPERTY_NEXT_PIECE_CHANGES;
import static model.Board.PROPERTY_ROW_CLEARED;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import model.Board;
import model.TetrisPiece;
import view.controller.TetrisGUI;



/**
 * A class representing the main panel for a Tetris game.
 *
 * @author Group 7
 * @version Autumn 2023
 */
@SuppressWarnings("ClassWithTooManyFields")
/*
 * warning is suppressed because there are too many fields to
 * make this class smaller.
 */
public class MainPanel extends JPanel {

    /**
     * Constant width of panel.
     */
    private static final int WIDTH1 = 200;

    /**
     * Constant height of panel.
     */
    private static final int HEIGHT1 = 400;

    /**
     * Constant width of panel.
     */
    private static final int WIDTH2 = 160;

    /**
     * Constant height of panel.
     */
    private static final int HEIGHT2 = 110;

    /**
     * Constant used for gap.
     */
    private static final int GAP = 10;

    /**
     * Ensures only one panel is instantiated.
     */
    private static int count;

    /**
     * The Panel which contains the NextPiecePanel, ControlPanel, and ScorePanel.
     */
    private JPanel mySecondaryPanel;
    /**
     * The Panel which contains the game board.
     */
    private final JPanel myGamePanel;
    /**
     * The Panel which contains the next piece.
     */
    private final JPanel myNextPiecePanel;
    /**
     * The Panel which contains the controls.
     */
    private JPanel myControlPanel;
    /**
     * The Panel which contains the score.
     */
    private final JPanel myScorePanel;
    /**
     * The game board associated with this menu.
     * It represents the current state of the Tetris game, including the arrangement
     * of Tetris blocks and handling game logic.
     */
    private final Board myBoard;
    /**
     * Timer to manage game updates at regular intervals.
     */
    private final Timer myGameTimer;

    /**
     * Clip of music to be played.
     * Music-
     * Pixel Story by Roa Music | <a href="https://soundcloud.com/roa_music1031">...</a>
     * Music promoted by <a href="https://www.free-stock-music.com">...</a>
     * Creative Commons / Attribution 3.0 Unported License (CC BY 3.0)
     * <a href="https://creativecommons.org/licenses/by/3.0/deed.en_US">...</a>
     */
    private Clip myMusicClip;

    /**
     * Clip of sound effect to be played.
     */
    private Clip mySoundClip;

    /**
     * Current difficulty level of the game.
     */
    private int myCurrentDifficulty;

    /**
     * GUI to display game.
     */
    private final TetrisGUI myTetrisGUI;


    /**
     * Constructs a new MainPanel object.
     * Initializes the game board, sets up GUI components, and adds necessary listeners.
     * @param theBoard The game board associated with this menu.
     * @param theGameTimer Timer to manage game updates at regular intervals.
     * @param thePanelArray Array of panels to be added to the MainPanel.
     * @param theSoundList List of sound files to be used in the game.
     * @param theTetrisGUI GUI to display game.
     */
    @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
    /*
     * warning is suppressed because count is used to ensure only one
     * MainPanel is instantiated.
     */
    public MainPanel(final Board theBoard, final Timer theGameTimer,
                     final JPanel[] thePanelArray, final List<File> theSoundList,
                     final TetrisGUI theTetrisGUI) {
        super();

        if (count > 0) {
            throw new IllegalArgumentException("Only one MainPanel allowed");
        }
        count++;

        myBoard = theBoard;
        myGameTimer = theGameTimer;
        myGamePanel = thePanelArray[0];
        myNextPiecePanel = thePanelArray[1];
        myScorePanel = thePanelArray[2];
        myTetrisGUI = theTetrisGUI;
        constructorHelper(theSoundList);
    }

    /**
     * Helper method used for constructor.
     *
     * @param theSoundList List of sounds used in game.
     */
    private void constructorHelper(final List<File> theSoundList) {
        buildComponents();
        layoutComponents();
        addListeners();
        createMusic(theSoundList);
    }

    /**
     * Initializes the components of the MainPanel.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    /*
    Too many constants to add another Height when matching Width exists.
     */
    private void buildComponents() {
        mySecondaryPanel = new JPanel();
        myControlPanel = new ControlPanel();

        myGamePanel.setPreferredSize(new Dimension(WIDTH1, HEIGHT1));
        myNextPiecePanel.setPreferredSize(new Dimension(WIDTH2, WIDTH2));
        myControlPanel.setPreferredSize(new Dimension(WIDTH2, HEIGHT2));
        myScorePanel.setPreferredSize(new Dimension(WIDTH2, HEIGHT2));
    }

    /**
     * Lays out the components of the MainPanel.
     */
    private void layoutComponents() {
        setLayout(new BorderLayout(GAP / 2, GAP));
        add(myGamePanel, BorderLayout.WEST);
        add(mySecondaryPanel, BorderLayout.EAST);

        mySecondaryPanel.setLayout(new BorderLayout(GAP, GAP));
        mySecondaryPanel.add(myNextPiecePanel, BorderLayout.NORTH);
        mySecondaryPanel.add(myControlPanel, BorderLayout.CENTER);
        mySecondaryPanel.add(myScorePanel, BorderLayout.SOUTH);
    }

    /**
     * Adds listeners to the MainPanel.
     */
    private void addListeners() {
        myBoard.addPropertyChangeListener(this::propertyChange);
        addKeyListener(new ControlKeyListener());
        setFocusable(true);
        requestFocus();
    }

    /**
     * Creates audio clip from .wav audio file.
     * @param theSoundFile File containting .wav audio files to turn into clips.
     */
    private void createMusic(final List<File> theSoundFile) {
        final File musicFile = theSoundFile.get(0);
        final File soundFile = theSoundFile.get(1);

        final AudioInputStream audioInput;
        try {
            audioInput = getAudioInputStream(musicFile);
        } catch (final UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException(e);
        }
        try {
            myMusicClip = AudioSystem.getClip();
        } catch (final LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        try {
            myMusicClip.open(audioInput);
        } catch (final LineUnavailableException | IOException e) {
            throw new RuntimeException(e);
        }
        createSound(soundFile);
    }

    private void createSound(final File theSoundFile) {
        final AudioInputStream audioInput;
        try {
            audioInput = getAudioInputStream(theSoundFile);
        } catch (final UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException(e);
        }
        try {
            mySoundClip = AudioSystem.getClip();
        } catch (final LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        try {
            mySoundClip.open(audioInput);
        } catch (final LineUnavailableException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Starts music.
     */
    private void playMusic() {
        myMusicClip.start();
    }

    /**
     * Pauses music playing
     */
    private void pauseMusic() {
        myMusicClip.stop();
    }

    private void propertyChange(final PropertyChangeEvent theEvent) {
        if (PROPERTY_GAME_OVER.equals(theEvent.getPropertyName())
                && (Boolean) theEvent.getNewValue()) {
            myGameTimer.stop();
            pauseMusic();

            JOptionPane.showMessageDialog(null,
                    "              Game Over!");
        } else if (PROPERTY_NEXT_PIECE_CHANGES.equals(theEvent.getPropertyName())) {
            final TetrisPiece nextPiece = (TetrisPiece) theEvent.getNewValue();
        }
        if (PROPERTY_NEW_GAME.equals(theEvent.getPropertyName())) {
            myMusicClip.setMicrosecondPosition(0);
            myMusicClip.start();
        }
        if (PROPERTY_ROW_CLEARED.equals(theEvent.getPropertyName())) {
            if ((int) theEvent.getNewValue() > 0) {
                mySoundClip.setMicrosecondPosition(0);
                mySoundClip.start();
            }
        }
    }

    class ControlKeyListener extends KeyAdapter {

        ControlKeyListener() {
            super();
        }

        @SuppressWarnings({"OverlyLongMethod", "checkstyle:CyclomaticComplexity", "CheckStyle"})
        /*
         * OverlyLongMethod warning is suppressed because the method is necessary to
         * use the keyListeners.
         * CyclomaticComplexity warning is suppressed with "Checksyle" because the method is necessary to
         * use the keyListeners.
         */
        @Override
        public void keyPressed(final KeyEvent theEvent) {
            if (myGameTimer.isRunning()) {
                switch (theEvent.getKeyCode()) {
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        myBoard.right();
                        break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        myBoard.left();
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        myBoard.down();
                        myGamePanel.repaint();

                        if ("Hard".equals(myTetrisGUI.getCurrentDifficulty())) {
                            myBoard.step();
                        } else {
                            myGameTimer.restart();
                        }
                        break;
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        myBoard.rotateCW();
                        break;
                    case KeyEvent.VK_SPACE:
                        myBoard.drop();
                        myGamePanel.repaint();
                        break;
                    default:
                        break;
                }
            }
            if (theEvent.getKeyCode() == KeyEvent.VK_P) {
                if (myGameTimer.isRunning()) {
                    myGameTimer.stop();
                    pauseMusic();
                } else {
                    myGameTimer.start();
                    playMusic();
                }
            }
        }
    }
}
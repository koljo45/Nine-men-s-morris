package hr.fer.mv.nmm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 * Class made to support user interaction by displaying the current state of the
 * board and additional data that guides the player. It's responsible for
 * parsing player mouse events. These events are then used to interact with
 * {@link GameLogic} which allows the game to advance.
 * <p>
 * If background or token images are null the board and tokens are drawn using
 * {@link Graphics}. Default font is used for fonts that are null.
 * <p>
 * Display setting can be found in {@link Dependencies} class.
 * 
 * @author Matija Videkoviæ
 */
public class GameBoardDisplay extends JComponent implements MouseListener, MouseMotionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Image backgroundImage;
	private Image[] tokenImages;
	private MPoint lastHoveredPoint;
	private TokenSkin[][] board;
	private BlockingQueue<TokenTranslate> tokenTranslateQueue;
	private MPoint focusedPoint;
	private String playerDirectionText = "";
	private String toolTipText = "";
	private Point lastMouseReleaseLocation;
	private Point toolTipTextLocation;
	private Rectangle2D toolTipTextDimension;
	private AnimationTranslate playerDirectionTextAnimation;
	private AnimationCounter toolTipTextTimer;
	private Font playerDirectionTextFont;
	private Font toolTipTextFont;
	private JLabel[] tokenCountLabels;
	private GameLogic gameLogic;

	/**
	 * Creates a new instance of this class. tokenImages and tokenCountLabels must
	 * have the same number of elements that isn't 0.
	 * 
	 * @param backgroundImage         image to be drawn as the background of the
	 *                                board
	 * @param tokenImages             images used to distinct each players token
	 * @param tokenCountLabels        labels for displaying current token situation
	 * @param playerDirectionTextFont font used for the player direction texts
	 * @param toolTipTextFont         font used for the tool tip texts
	 * @param gameLogic               {@link GameLogic} instance used for advancing
	 *                                the game.
	 */
	public GameBoardDisplay(Image backgroundImage, Image[] tokenImages, JLabel[] tokenCountLabels,
			Font playerDirectionTextFont, Font toolTipTextFont, GameLogic gameLogic) {
		super();
		addMouseListener(this);
		addMouseMotionListener(this);
		if (tokenImages.length < 1 || tokenCountLabels.length < 1 || tokenImages.length != tokenCountLabels.length)
			throw new IllegalArgumentException(
					"tokenImages and tokenCountLabels have to contain same number of elements that is greater than 0");
		this.tokenImages = new Image[tokenImages.length];
		System.arraycopy(tokenImages, 0, this.tokenImages, 0, tokenImages.length);
		this.tokenCountLabels = new JLabel[tokenCountLabels.length];
		System.arraycopy(tokenCountLabels, 0, this.tokenCountLabels, 0, tokenCountLabels.length);
		this.backgroundImage = backgroundImage;
		this.playerDirectionTextFont = playerDirectionTextFont;
		this.toolTipTextFont = toolTipTextFont;
		this.gameLogic = gameLogic;

		if (playerDirectionTextFont == null)
			playerDirectionTextFont = new Font(null, Font.PLAIN, (int) Dependencies.PLAYER_DIRECTION_TEXT_SIZE);
		if (toolTipTextFont == null)
			toolTipTextFont = new Font(null, Font.PLAIN, (int) Dependencies.TOOL_TIP_TEXT_SIZE);

		tokenTranslateQueue = new ArrayBlockingQueue<>(10);
		playerDirectionTextAnimation = new AnimationTranslate(
				Dependencies.PLAYER_DIRECTION_TEXT_TRANSLATE_TIME_MILISECONDS / Dependencies.ANIMATION_STEP_MILISECONDS,
				new Point(Dependencies.FRAME_SIZE.width / 2, Dependencies.FRAME_SIZE.height / 2),
				Dependencies.PLAYER_DIRECTION_TEXT_LOCATION);
		toolTipTextTimer = new AnimationCounter(
				Dependencies.TOOL_TIP_TEXT_TIME_MILISECONDS / Dependencies.ANIMATION_STEP_MILISECONDS);

		board = new TokenSkin[Dependencies.NUMBER_OF_SQARES][Dependencies.NUMBER_OF_POINTS_PER_SQARE];
		for (int i = 0; i < Dependencies.NUMBER_OF_SQARES; i++)
			for (int j = 0; j < Dependencies.NUMBER_OF_POINTS_PER_SQARE; j++)
				board[i][j] = TokenSkin.NO_TOKEN;

		toolTipTextLocation = new Point(0, 0);

		// Swing timer events get executed on the event dispatch thread...
		Timer timer = new Timer(Dependencies.ANIMATION_STEP_MILISECONDS, e -> {
			try {
				int oldSize = tokenTranslateQueue.size();
				for (int i = 0; i < oldSize; i++) {
					TokenTranslate tT = tokenTranslateQueue.take();
					if (!tT.stepsCompleted()) {
						tT.advanceStep();
						tokenTranslateQueue.put(tT);
					}
				}
				if (!playerDirectionTextAnimation.stepsCompleted() || !toolTipTextTimer.stepsCompleted())
					oldSize = 1;

				playerDirectionTextAnimation.advanceStep();
				toolTipTextTimer.advanceStep();
				if (oldSize > 0)
					repaint();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		});

		timer.setRepeats(true);
		timer.setCoalesce(false);
		timer.start();
	}

	public GameBoardDisplay(Image backgroundImage, Image[] tokenImages, JLabel[] tokenCountLabels,
			Font playerDirectionTextFont, Font toolTipTextFont) {
		this(backgroundImage, tokenImages, tokenCountLabels, playerDirectionTextFont, toolTipTextFont, null);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawBackground(g);
		drawBoard(g);
		drawTranslatedTokens(g);
		drawPlayerDirectionText(g);
		if (!toolTipTextTimer.stepsCompleted())
			drawToolTipText(g);
	}

	private void drawBackground(Graphics g) {
		if (backgroundImage != null)
			g.drawImage(backgroundImage, 0, 0, null);
		else {
			for (int i = 0; i < Dependencies.NUMBER_OF_SQARES; i++)
				for (int j = 0; j < Dependencies.NUMBER_OF_POINTS_PER_SQARE; j++) {
					for (Mill m : new MPoint(i, j).getMills()) {
						Point p1 = Dependencies.getPointCoordinate(m.p1);
						Point p3 = Dependencies.getPointCoordinate(m.p3);
						g.drawLine(p1.x, p1.y, p3.x, p3.y);
					}
				}
		}
	}

	private void drawBoard(Graphics g) {
		for (int i = 0; i < Dependencies.NUMBER_OF_SQARES; i++)
			for (int j = 0; j < Dependencies.NUMBER_OF_POINTS_PER_SQARE; j++) {
				double scaleFactor = (focusedPoint != null && i == focusedPoint.square && j == focusedPoint.place)
						? Dependencies.FOCUS_SCALE_FACTOR
						: 1;
				TokenSkin ts = board[i][j];
				if (ts == TokenSkin.BLACK_TOKEN)
					drawTokenOnPoint(Dependencies.getPointCoordinate(i, j), scaleFactor, g, ts);
				else if (ts == TokenSkin.WHITE_TOKEN)
					drawTokenOnPoint(Dependencies.getPointCoordinate(i, j), scaleFactor, g, ts);
			}
	}

	private void drawTranslatedTokens(Graphics g) {
		try {
			for (int i = 0; i < tokenTranslateQueue.size(); i++) {
				TokenTranslate tT = tokenTranslateQueue.remove();
				drawTokenOnPoint(tT.getCurrentPoint(), Dependencies.FOCUS_SCALE_FACTOR, g, tT.getTokenSkin());
				tokenTranslateQueue.add(tT);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void drawTokenOnPoint(Point p, double scaleFactor, Graphics g, TokenSkin ts) {
		if (ts == TokenSkin.BLACK_TOKEN)
			if (tokenImages[0] != null)
				drawImageOnPoint(p, scaleFactor, g, tokenImages[0]);
			else
				drawOvalOnPoint(p, scaleFactor, g, Color.black);
		else if (ts == TokenSkin.WHITE_TOKEN)
			if (tokenImages[1] != null)
				drawImageOnPoint(p, scaleFactor, g, tokenImages[1]);
			else
				drawOvalOnPoint(p, scaleFactor, g, Color.white);
	}

	private void drawImageOnPoint(Point p, double scaleFactor, Graphics g, Image img) {
		int width = (int) (img.getWidth(null) * scaleFactor);
		int height = (int) (img.getHeight(null) * scaleFactor);
		int x = p.x - width / 2;
		int y = p.y - height / 2;
		g.drawImage(img, x, y, width, height, null);
	}

	private void drawOvalOnPoint(Point p, double scaleFactor, Graphics g, Color c) {
		int width = (int) (Dependencies.POINT_SIZE.getWidth() * scaleFactor);
		int height = (int) (Dependencies.POINT_SIZE.getHeight() * scaleFactor);
		int x = p.x - width / 2;
		int y = p.y - height / 2;
		Color defaultColor = g.getColor();
		g.setColor(c);
		g.fillOval(x, y, width, height);
		g.setColor(defaultColor);
	}

	private void drawPlayerDirectionText(Graphics g) {
		Point p = playerDirectionTextAnimation.getCurrentPoint();
		Font defaultFont = g.getFont();
		g.setFont(playerDirectionTextFont);
		g.drawString(playerDirectionText, p.x, p.y);
		g.setFont(defaultFont);
	}

	private void drawToolTipText(Graphics g) {
		Font defaultFont = g.getFont();
		Color defaultColor = g.getColor();
		g.setFont(toolTipTextFont);
		if (toolTipTextDimension == null)
			toolTipTextDimension = g.getFontMetrics().getStringBounds(toolTipText, g);
		int width = (int) toolTipTextDimension.getWidth();
		int height = (int) toolTipTextDimension.getHeight();
		int x = toolTipTextLocation.x;
		int y = toolTipTextLocation.y;
		// magical number 20, had to add it because last two letters get cut off
		if (x + width > Dependencies.FRAME_SIZE.getWidth())
			x -= (x + width) - Dependencies.FRAME_SIZE.getWidth() + 20;
		g.setColor(Color.white);
		g.fillRect(x, y - height, width, height);
		g.setColor(defaultColor);
		g.drawString(toolTipText, x, y);
		g.setFont(defaultFont);
	}
	
	/**
	 * Sets the {@link GameLogic} instance used for advancing the game.
	 * 
	 * @param gameLogic {@link GameLogic} instance
	 */
	public void setGameLogic(GameLogic gameLogic) {
		this.gameLogic = gameLogic;
	}

	/**
	 * Set a token on the given point on the board. tokenSkin is used for the tokens
	 * appearance.
	 * 
	 * @param point     to set the token on
	 * @param tokenSkin to be used for the appearance of the token
	 */
	public void setPointToken(MPoint point, TokenSkin tokenSkin) {
		board[point.square][point.place] = tokenSkin;
	}

	//

	/**
	 * Sets a focus point which focuses any token on it.
	 * 
	 * @param point for focusing tokens
	 */
	public void setFocusPoint(MPoint point) {
		focusedPoint = point;
	}

	/**
	 * Disables focus point for tokens.
	 */
	public void clearFocusPoint() {
		focusedPoint = null;
	}

	/**
	 * Starts an animation which translates a token with the given skin. Start of
	 * the animation is at point1 and end at point2. After the transition has ended
	 * there is no visible token as it disappears with the animation.
	 * 
	 * @param point1    start point for the animation
	 * @param point2    end point for the animation
	 * @param tokenSkin used for the translated token
	 */
	public void translateToken(MPoint point1, MPoint point2, TokenSkin tokenSkin) {
		try {
			tokenTranslateQueue.put(new TokenTranslate(
					(int) Dependencies.TOKEN_TRANSLATE_TIME_MILISECONDS / Dependencies.ANIMATION_STEP_MILISECONDS,
					Dependencies.getPointCoordinate(point1), Dependencies.getPointCoordinate(point2), tokenSkin));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Starts an animation which translates a token with the given skin. Start of
	 * the animation is at point1 and end at point2. After the transition has ended
	 * there is no visible token as it disappears with the animation. When the
	 * animation ends onEndAction gets executed.
	 * 
	 * @param point1      start point for the animation
	 * @param point2      end point for the animation
	 * @param tokenSkin   token skin used for the translated token
	 * @param onEndAction {@link ActionListener} that gets executed on animation end
	 */
	public void translateToken(MPoint point1, MPoint point2, TokenSkin tokenSkin, ActionListener onEndAction) {
		try {
			TokenTranslate tT = new TokenTranslate(
					(int) Dependencies.TOKEN_TRANSLATE_TIME_MILISECONDS / Dependencies.ANIMATION_STEP_MILISECONDS,
					Dependencies.getPointCoordinate(point1), Dependencies.getPointCoordinate(point2), tokenSkin);
			tT.addActionListener(onEndAction);
			tokenTranslateQueue.put(tT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	// Info panel

	/**
	 * Updates the token counter for the tokens with the given skin with the given
	 * value.
	 * 
	 * @param tokensLeft number that the token counter is set to
	 * @param tokenSkin  skin for which the counter is set
	 */
	public void updateTokenCounter(int tokensLeft, TokenSkin tokenSkin) {
		if (tokenSkin == TokenSkin.BLACK_TOKEN)
			tokenCountLabels[0].setText("" + tokensLeft);
		else if (tokenSkin == TokenSkin.WHITE_TOKEN)
			tokenCountLabels[1].setText("" + tokensLeft);
	}

	/**
	 * Shows a dialog with the given endGameText text. It offers the player YES and
	 * NO option. Upon selecting the YES option this function returns. Upon
	 * selecting the NO option this program terminates with
	 * <code>System.exit(0)</code>
	 * 
	 * @param dialogText  text to display in the dialog.
	 * @param dialogTitle title for the dialog
	 */
	public void showConfirmDialog(String dialogText, String dialogTitle) {
		int option = JOptionPane.showConfirmDialog(null, dialogText, dialogTitle, JOptionPane.YES_NO_OPTION);
		if (option == JOptionPane.NO_OPTION || option == JOptionPane.CLOSED_OPTION)
			System.exit(0);
	}

	/**
	 * Update the text which is used to indicate the players next step in the game.
	 * 
	 * @param text text containing information about the players next move.
	 */
	public void updatePlayerDirectionText(String text) {
		playerDirectionText = text;
		if (Dependencies.PLAYER_DIRECTION_TEXT_FLYING)
			playerDirectionTextAnimation.reset();
	}

	/**
	 * Shows a tool tip text which helps the player complete his current move. Used
	 * when the player is trying to do illegal moves.
	 * 
	 * @param text text to be displayed as a tool tip
	 */
	public void showToolTipText(String text) {
		toolTipText = text;
		toolTipTextDimension = null;
		toolTipTextLocation = lastMouseReleaseLocation.getLocation();
		toolTipTextTimer.reset();
	}

	// Event handling

	@Override
	public void mouseDragged(MouseEvent arg0) {

	}

	@Override
	public void mouseMoved(MouseEvent mouseEvent) {
		MPoint p = eventToPoint(mouseEvent.getPoint());
		if (p != null && !p.equals(lastHoveredPoint) && gameLogic != null)
			gameLogic.pointEntered(p);
		lastHoveredPoint = p;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	private MPoint mousePressPoint;

	@Override
	public void mousePressed(MouseEvent mouseEvent) {
		mousePressPoint = eventToPoint(mouseEvent.getPoint());
	}

	@Override
	public void mouseReleased(MouseEvent mouseEvent) {
		lastMouseReleaseLocation = mouseEvent.getPoint();
		if (mousePressPoint != null && mousePressPoint.equals(eventToPoint(mouseEvent.getPoint())) && gameLogic != null)
			gameLogic.pointClicked(mousePressPoint);
	}

	private MPoint eventToPoint(Point event) {
		for (int i = 0; i < Dependencies.NUMBER_OF_SQARES; i++)
			for (int j = 0; j < Dependencies.NUMBER_OF_POINTS_PER_SQARE; j++) {
				Point point = Dependencies.getPointCoordinate(i, j);
				Dimension size = Dependencies.POINT_SIZE;
				if ((event.x > point.x - size.width / 2 && event.x < point.x + size.width / 2)
						&& (event.y > point.y - size.height / 2 && event.y < point.y + size.height / 2)) {
					return new MPoint(i, j);
				}
			}
		return null;
	}
}

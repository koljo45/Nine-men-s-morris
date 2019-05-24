package hr.fer.mv.nmm;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class MainFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private BufferedImage blackTokenImg, whiteTokenImg, backgroundImage;
	private GameLogic gameLogic;
	private JLabel blackTokenCountLable;
	private JLabel whiteTokenCountLable;
	private Font playerDirectionTextFont;
	private Font toolTipTextFont;

	public MainFrame() {
		// Frame initialization
		setSize(Dependencies.FRAME_SIZE);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle(Dependencies.GAME_NAME);
		this.setFocusable(true);
		setVisible(true);

		JButton confirmBtn = new JButton("Confirm");
		confirmBtn.addActionListener(e -> gameLogic.confirmAction());
		add(confirmBtn, BorderLayout.SOUTH);

		JPanel infoPanel = new JPanel();
		JLabel blackTCL = new JLabel("Black token count: ");
		blackTokenCountLable = new JLabel("");
		JLabel gap = new JLabel(new String(new char[50]).replace('\0', ' '));
		JLabel whiteTCL = new JLabel("White token count: ");
		whiteTokenCountLable = new JLabel("");
		infoPanel.add(blackTCL);
		infoPanel.add(blackTokenCountLable);
		infoPanel.add(gap);
		infoPanel.add(whiteTCL);
		infoPanel.add(whiteTokenCountLable);
		add(infoPanel, BorderLayout.NORTH);

		// Load dependencies
		try (InputStream backgroundImageIS = getClass()
				.getResourceAsStream(Dependencies.DEPENDENCIES_ROOT_PATH + "/" + Dependencies.GAME_BOARD_IMG);
				InputStream blackTokenImgIS = getClass()
						.getResourceAsStream(Dependencies.DEPENDENCIES_ROOT_PATH + "/" + Dependencies.BLACK_TOKEN_IMG);
				InputStream whiteTokenImgIS = getClass()
						.getResourceAsStream(Dependencies.DEPENDENCIES_ROOT_PATH + "/" + Dependencies.WHITE_TOKEN_IMG);
				InputStream fontIS = getClass().getResourceAsStream(
						Dependencies.DEPENDENCIES_ROOT_PATH + "/" + Dependencies.PLAYER_DIRECTION_TEXT_FONT)) {
			backgroundImage = ImageIO.read(backgroundImageIS);
			blackTokenImg = ImageIO.read(blackTokenImgIS);
			whiteTokenImg = ImageIO.read(whiteTokenImgIS);
			playerDirectionTextFont = Font.createFont(Font.TRUETYPE_FONT, fontIS)
					.deriveFont(Dependencies.PLAYER_DIRECTION_TEXT_SIZE);
			toolTipTextFont = playerDirectionTextFont.deriveFont(Dependencies.TOOL_TIP_TEXT_SIZE);
		} catch (IOException | FontFormatException | SecurityException | NullPointerException e) {
			e.printStackTrace();
		}
		if (backgroundImage != null)
			setIconImage(backgroundImage);
		if (playerDirectionTextFont != null) {
			blackTCL.setFont(playerDirectionTextFont);
			blackTokenCountLable.setFont(playerDirectionTextFont);
			whiteTCL.setFont(playerDirectionTextFont);
			whiteTokenCountLable.setFont(playerDirectionTextFont);
		}

		GameBoardDisplay gameDisplay = new GameBoardDisplay(backgroundImage,
				new Image[] { blackTokenImg, whiteTokenImg },
				new JLabel[] { blackTokenCountLable, whiteTokenCountLable }, playerDirectionTextFont, toolTipTextFont);

		gameLogic = new GameLogic(gameDisplay);
		gameDisplay.setGameLogic(gameLogic);

		this.add(gameDisplay, BorderLayout.CENTER);

		this.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent keyEvent) {
			}

			@Override
			public void keyReleased(KeyEvent keyEvent) {
				if (keyEvent.getKeyCode() == KeyEvent.VK_R)
					gameLogic.specialAction();
				else if (keyEvent.getKeyCode() == KeyEvent.VK_SPACE)
					gameLogic.confirmAction();
			}

			@Override
			public void keyPressed(KeyEvent keyEvent) {
			}
		});

		gameDisplay.showConfirmDialog(
				"Greetings, traveler!\n\n" + "Welcome to the game of Merels.\n"
						+ "Your goal is to place three tokens in a horizontal line or a vertical line.\n"
						+ "After you have done this you are allowed to take one of your opponents tokens.\n"
						+ "Beware! If you are left with 2 tokens you lose.\n" + "If you are unable to move, you lose.\n"
						+ "So grab a friend and sit down for a game of Merels!\n\n" + "Controls:\n" + "R - surrender\n"
						+ "Space (Confirm button) - confirm your selection\n\n" + "Do you wish to play the game?",
				"Welcome to Merels!");
	}

	public static void main(String[] args) {
		try {
			SwingUtilities.invokeAndWait(() -> {
				MainFrame mainFrame = new MainFrame();
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}

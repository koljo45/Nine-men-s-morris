package hr.fer.mv.nmm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class that simulates a game of Nine men's morris. The game consists of two
 * main phases:
 * <ul>
 * <li>1. Placing men on vacant points
 * <li>2. Moving men to adjacent points
 * <li>3. (optional phase) Moving men to any vacant point when the player has
 * been reduced to three men
 * </ul>
 * Players try to form 'mills'—three of their own tokens lined horizontally or
 * vertically—allowing a player to remove an opponent's token from the game. A
 * player wins by reducing the opponent to two pieces (where they could no
 * longer form mills and thus be unable to win), or by leaving them without a
 * legal move (checkmate).
 * <p>
 * Supports multiple point events. In addition we have confirm and special
 * events. Confirm event needs to be sent when a player forms a mill and has to
 * confirm his choice of enemy token for removal. Player can chose not to remove
 * any token and send the confirm event to skip removal. Special event is sent
 * when the current player wants to surrender.
 * <p>
 * This class works with {@link GameBoardDisplay} to display current game state.
 * <p>
 * Game settings such as number of tokens per player or flying can be found in
 * {@link Dependencies}.
 * 
 * @author Matija Videkoviæ
 *
 */
public class GameLogic {

	public enum PointOwner {
		Empty, Player1, Player2
	}

	public enum GamePhase {
		Placing, Moving
	}

	// We use Confirm to let player confirm their choice
	// Timeout could be sent if we want to time players turn
	private enum EventType {
		PointClick, PointEnter, PointExit, Confirm, Timeout
	}

	private class GameEvent {
		private MPoint point;
		private EventType eventType;

		public GameEvent(MPoint p, EventType et) {
			point = p;
			eventType = et;
		}

		public MPoint getPoint() {
			return point;
		}

		public EventType getEventType() {
			return eventType;
		}
	}

	private PointOwner[][] board = new PointOwner[Dependencies.NUMBER_OF_SQARES][Dependencies.NUMBER_OF_POINTS_PER_SQARE];

	private GamePhase currentGamePhase;
	private PointOwner currentPlayer;
	private int[] playerTokenNum = new int[2];
	private int[] playerTokensToPlace = new int[2];

	private MPoint selectedPoint;
	private boolean checkForMills = false;
	private boolean millMade = false;

	private GameBoardDisplay boardDisplay;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param boardDisplay {@link GameBoardDisplay} instance
	 */
	public GameLogic(GameBoardDisplay boardDisplay) {
		this.boardDisplay = boardDisplay;
		setupGame();
	}

	/**
	 * Creates a new instance of this class.
	 */
	public GameLogic() {
		this(null);
		setupGame();
	}

	/**
	 * Sets the {@link GameBoardDisplay} used for displaying current game state.
	 * 
	 * @param boardDisplay {@link GameBoardDisplay} instance
	 */
	public void setGameBoardDisplay(GameBoardDisplay boardDisplay) {
		this.boardDisplay = boardDisplay;
	}

	private void setupGame() {
		currentGamePhase = GamePhase.Placing;
		currentPlayer = PointOwner.Player1;
		playerTokenNum[0] = 0;
		playerTokenNum[1] = 0;
		playerTokensToPlace[0] = Dependencies.NUMBER_OF_TOKENS_PER_PLAYER;
		playerTokensToPlace[1] = Dependencies.NUMBER_OF_TOKENS_PER_PLAYER;
		updateTokenCounter(playerTokensToPlace[0], TokenSkin.BLACK_TOKEN);
		updateTokenCounter(playerTokensToPlace[1], TokenSkin.WHITE_TOKEN);
		for (int i = 0; i < Dependencies.NUMBER_OF_SQARES; i++)
			for (int j = 0; j < Dependencies.NUMBER_OF_POINTS_PER_SQARE; j++) {
				MPoint p = new MPoint(i, j);
				setPointOwner(p, PointOwner.Empty);
				drawTokenOnPoint(p, PointOwner.Empty);
			}
		selectedPoint = null;
		checkForMills = false;
		millMade = false;
		updatePlayerDirectionText(currentPlayer.toString() + " place your token");
	}

	/**
	 * Creates a click event on the given point of the board. This event is then
	 * processed based on the current state of the game.
	 * 
	 * @param point point at which the click happened
	 */
	public void pointClicked(MPoint point) {
		gameEventHandler(new GameEvent(point, EventType.PointClick));
	}

	/**
	 * Creates a point entered event on the given point of the board. This event is
	 * then processed based on the current state of the game.
	 * 
	 * @param point point which the mouse entered
	 */
	public void pointEntered(MPoint point) {
	}

	/**
	 * Creates a point exited event on the given point of the board. This event is
	 * then processed based on the current state of the game.
	 * 
	 * @param point point which the mouse exited
	 */
	public void pointExited(MPoint point) {

	}

	/**
	 * Generate a confirm action which is processed based on the current state of
	 * the game. Primarily called when the current player has to confirm his choice
	 * of opponent token for removal.
	 */
	public void confirmAction() {
		gameEventHandler(new GameEvent(MPoint.zero, EventType.Confirm));
	}

	/**
	 * Generate a special action which is processed based on the current state of
	 * the game. In this case it ends the game by surrender.
	 */
	public void specialAction() {
		currentPlayer = currentPlayer == PointOwner.Player1 ? PointOwner.Player2 : PointOwner.Player1;
		endGame();
	}

//	  Gets called every time there is an interaction between the player and one of
//	  the points on the board Here we do most of the work concerning the rules and
//	  the natural flow of the game
	private void gameEventHandler(GameEvent ev) {
		MPoint eventPoint = ev.getPoint();
		PointOwner eventPointOwner = getPointOwner(eventPoint);
		PointOwner opponent = currentPlayer == PointOwner.Player1 ? PointOwner.Player2 : PointOwner.Player1;
		int currentPlayerIndex = currentPlayer == PointOwner.Player1 ? 0 : 1;
		// Special selection mode we enter when a player forms a mill
		if (millMade) {
			// Select one of the enemy tokens
			if (ev.getEventType() == EventType.PointClick) {
				if (eventPointOwner != PointOwner.Empty && eventPointOwner != currentPlayer) {
					// Reselecting already selected point removes selection
					if (eventPoint.equals(selectedPoint)) {
						selectedPoint = null;
						clearFocusPointToken();
						updatePlayerDirectionText(currentPlayer.toString() + " take enemy token if you wish");
					} else {
						if (!formsMill(eventPoint, eventPointOwner) || !hasFreeToken(eventPointOwner)) {
							selectedPoint = eventPoint;
							setFocusPointToken(selectedPoint);
							updatePlayerDirectionText("Press confirm button to remove selected token");
						} else {
							System.out.println("You must remove a token that doesn't form a mill");
							updateToolTipText("You must remove a token that doesn't form a mill");
						}
					}
				} else if (eventPointOwner == PointOwner.Empty && selectedPoint != null) {
					// Deselect can be performed by selecting an empty point
					selectedPoint = null;
					clearFocusPointToken();
					updatePlayerDirectionText(currentPlayer.toString() + " take enemy token if you wish");
				} else {
					// Player isn't selecting enemy tokens, clear old selection
					if (selectedPoint != null) {
						selectedPoint = null;
						clearFocusPointToken();
						updatePlayerDirectionText(currentPlayer.toString() + " take enemy token if you wish");
					}
					System.out.println("Please select an enemy token for removal");
					updateToolTipText("Please select an enemy token for removal");
				}
				// Player has to confirm this important choice of removing enemy tokens
			} else if (ev.getEventType() == EventType.Confirm) {
				// Check if any token was selected
				if (selectedPoint != null) {
					int opponentIndex = getPointOwner(selectedPoint) == PointOwner.Player1 ? 0 : 1;
					playerTokenNum[opponentIndex]--;
					setPointOwner(selectedPoint, PointOwner.Empty);
					drawTokenOnPoint(selectedPoint, PointOwner.Empty);
					clearFocusPointToken();
					// Opponent ran out of tokens
					if ((playerTokenNum[opponentIndex] + playerTokensToPlace[opponentIndex]) <= 2
							|| !hasMovableToken(opponent)) {
						endGame();
						return;
					}
				}
				// End your turn
				endTurn();
			}
		} else if (currentGamePhase == GamePhase.Placing) {
			if (ev.getEventType() == EventType.PointClick) {
				if (eventPointOwner == PointOwner.Empty) {
					// Update board state
					playerTokensToPlace[currentPlayerIndex]--;
					playerTokenNum[currentPlayerIndex]++;
					setPointOwner(eventPoint, currentPlayer);
					// Update board appearance and counter
					updateTokenCounter(playerTokensToPlace[currentPlayerIndex],
							currentPlayer == PointOwner.Player1 ? TokenSkin.BLACK_TOKEN : TokenSkin.WHITE_TOKEN);
					drawTokenOnPoint(eventPoint, currentPlayer);

					// When the second player places all of his pieces the second phase begins
					if (currentPlayer == PointOwner.Player2 && playerTokensToPlace[currentPlayerIndex] <= 0)
						currentGamePhase = GamePhase.Moving;
					// Check for mills every time a new token is placed
					checkForMills = true;
				} else if (eventPointOwner == currentPlayer) {
					System.out.println("You have already taken this point");
					updateToolTipText("You have already taken this point");
				} else {
					System.out.println("This point is taken by your opponent");
					updateToolTipText("This point is taken by your opponent");
				}
			}
		} else if (currentGamePhase == GamePhase.Moving) {
			if (ev.getEventType() == EventType.PointClick) {
				// Player picks his tokens for moving for as long as he wants
				if (eventPointOwner == currentPlayer) {
					selectedPoint = eventPoint;
					setFocusPointToken(selectedPoint);
				} else if (selectedPoint != null) {
					if (eventPointOwner == PointOwner.Empty) {
						if (selectedPoint.isNeighbour(eventPoint)
								|| (playerTokenNum[currentPlayerIndex] == 3 && Dependencies.ALLOW_FLYING)) {
							// Move the token to it's new place
							setPointOwner(selectedPoint, PointOwner.Empty);
							setPointOwner(eventPoint, currentPlayer);
							drawTokenOnPoint(selectedPoint, PointOwner.Empty);
							// Animate the translation, when it ends draw translated token on end position
							translateToken(selectedPoint, eventPoint, currentPlayer, new ActionListener() {
								MPoint actionPoint = new MPoint(eventPoint);
								PointOwner actionOwner = Enum.valueOf(PointOwner.class, currentPlayer.name());

								@Override
								public void actionPerformed(ActionEvent arg0) {
									drawTokenOnPoint(actionPoint, actionOwner);
								}
							});
							clearFocusPointToken();
							selectedPoint = null;
							// After we move a token check for checkmate
							if (Dependencies.CHECK_CHECKMATE && !hasMovableToken(opponent)) {
								endGame();
								return;
							}
							// Every time we move a token check if a mill was formed
							checkForMills = true;
						} else {
							System.out.println("You must choose a place neibghouring the selected token");
							updateToolTipText("You must choose a place neighbouring the selected token");
						}
					} else {
						System.out.println("You must choose a free place");
						updateToolTipText("You must choose a free place");
					}
				} else {
					System.out.println("It's moving phase, choose one of your tokens");
					updateToolTipText("It's moving phase, choose one of your tokens");
				}
			}
		}
		// Check for mills if a new token was added or an already placed one was moved
		// Gets called every time we set checkForMills = true
		// Did we make a bridge and need to continue our turn or do we end our turn?
		// If checkForMills != true this means the player is still making his choices
		if (checkForMills) {
			if (formsMill(eventPoint, currentPlayer)) {
				updatePlayerDirectionText(currentPlayer.toString() + " take enemy token if you wish");
				millMade = true;
			} else
				endTurn();
			checkForMills = false;
		}
	}

	// Ends the current players turn and gives a turn to the opponent. Also displays
	// a direction text for the new active player.
	private void endTurn() {
		currentPlayer = currentPlayer == PointOwner.Player1 ? PointOwner.Player2 : PointOwner.Player1;
		selectedPoint = null;
		millMade = false;
		if (currentGamePhase == GamePhase.Placing)
			updatePlayerDirectionText(currentPlayer.toString() + " place a new token");
		else if (currentGamePhase == GamePhase.Moving)
			updatePlayerDirectionText(currentPlayer.toString() + " move one of your tokens");
	}

	// Ends the game by showing the game end dialog with the current player as the
	// winner, the game is reset if the player chooses to play again.
	private void endGame() {
		showGameEndDialog(currentPlayer.toString() + " Won!\n Do you want to play again?");
		System.out.println("Winner: " + (currentPlayer == PointOwner.Player1 ? "Player1" : "Player2"));
		setupGame();
	}

	private PointOwner getPointOwner(MPoint p) {
		return board[p.square][p.place];
	}

	private void setPointOwner(MPoint p, PointOwner po) {
		board[p.square][p.place] = po;
	}

	// Check if a point forms a mill for the given point owner
	private boolean formsMill(MPoint p, PointOwner po) {
		for (Mill m : p.getMills())
			if (getPointOwner(m.p1) == po && getPointOwner(m.p2) == po && getPointOwner(m.p3) == po)
				return true;
		return false;
	}

	// Check if the provided owner has tokens that aren't part of a mill
	private boolean hasFreeToken(PointOwner po) {
		for (int i = 0; i < Dependencies.NUMBER_OF_SQARES; i++)
			for (int j = 0; j < Dependencies.NUMBER_OF_POINTS_PER_SQARE; j++) {
				MPoint p = new MPoint(i, j);
				if (getPointOwner(p) == po) {
					if (formsMill(p, po))
						continue;
					return true;
				}
			}
		return false;
	}

	// Check if a PointOwner has any movable tokens
	private boolean hasMovableToken(PointOwner po) {
		for (int i = 0; i < Dependencies.NUMBER_OF_SQARES; i++)
			for (int j = 0; j < Dependencies.NUMBER_OF_POINTS_PER_SQARE; j++) {
				MPoint p = new MPoint(i, j);
				if (getPointOwner(p) == po) {
					for (MPoint neighbour : p.getNeighbours())
						if (getPointOwner(neighbour) == PointOwner.Empty)
							return true;
				}
			}
		return false;
	}

	// Draw a token associated with the given PointOwner on the given point.
	private void drawTokenOnPoint(MPoint p, PointOwner po) {
		if (boardDisplay == null)
			return;
		TokenSkin ts = TokenSkin.NO_TOKEN;
		if (po == PointOwner.Player1)
			ts = TokenSkin.BLACK_TOKEN;
		else if (po == PointOwner.Player2)
			ts = TokenSkin.WHITE_TOKEN;
		boardDisplay.setPointToken(p, ts);
		boardDisplay.repaint();
	}

	private void translateToken(MPoint p1, MPoint p2, PointOwner po, ActionListener onEnd) {
		if (boardDisplay == null)
			return;
		boardDisplay.translateToken(p1, p2, po == PointOwner.Player1 ? TokenSkin.BLACK_TOKEN : TokenSkin.WHITE_TOKEN,
				onEnd);
		boardDisplay.repaint();
	}

	private void setFocusPointToken(MPoint p) {
		if (boardDisplay == null)
			return;
		boardDisplay.setFocusPoint(p);
		boardDisplay.repaint();
	}

	private void clearFocusPointToken() {
		if (boardDisplay == null)
			return;
		boardDisplay.clearFocusPoint();
		boardDisplay.repaint();
	}

	private void updateTokenCounter(int cnt, TokenSkin ts) {
		if (boardDisplay == null)
			return;
		boardDisplay.updateTokenCounter(cnt, ts);
		boardDisplay.repaint();
	}

	private void updatePlayerDirectionText(String text) {
		if (boardDisplay == null)
			return;
		boardDisplay.updatePlayerDirectionText(text);
		boardDisplay.repaint();
	}

	private void updateToolTipText(String text) {
		if (boardDisplay == null)
			return;
		boardDisplay.showToolTipText(text);
		boardDisplay.repaint();
	}

	private void showGameEndDialog(String text) {
		if (boardDisplay == null)
			return;
		boardDisplay.showConfirmDialog(text, "GAMEOVER");
		boardDisplay.repaint();
	}

}

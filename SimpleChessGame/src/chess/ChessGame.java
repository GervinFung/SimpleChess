package chess;

import chess.pieces.*;
import chess.pieces.Piece.COLOR;
import chess.boardUtils.Board;
import chess.pieces.piece_location.Location;
import chess.pieces.piece_move.Move;
import chess.players.*;

import static chess.pieces.piece_move.Move.*;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JMenuBar;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.FlowLayout;

import java.io.File;

public final class ChessGame extends JPanel {

    // default version of serial UID
    private static final long serialVersionUID = 1L;
    private final Board board;
    private final int OFFSET = 64 * 3;

    public static void main(String[] args) {
        final ChessGame chess = new ChessGame();
        chess.startGame();
    }

    public ChessGame() {
        this.board = new Board();
        startPlayer(this.board.getPlayers());
    }

    private void startPlayer(final Player[] players) {
        players[0] = new Player();
        players[0].setMyTurn(true);
        players[1] = new Player();
        players[1].setMyTurn(false);
        board.swapPlayers();
    }

    private void setPlayerPiece(final Board board) {
        board.getPlayers()[0].setAllMyPieces(board.getWhitePieces());
        board.getPlayers()[1].setAllMyPieces(board.getBlackPieces());
    }

    public void startGame() {
        this.board.startNewGame(0, 1, 4, 3);
        final JFrame jf = new JFrame("Simple Chess Game");
        setPlayerPiece(this.board);

        final JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);
        jf.setJMenuBar(menuBar);

        final JButton friend = new JButton("Friend"), exit = new JButton("Exit");
        friend.setFocusPainted(false);
        friend.setBorderPainted(false);
        friend.setBackground(menuBar.getBackground());
        exit.setBorderPainted(false);
        exit.setBackground(menuBar.getBackground());

        menuBar.add(friend);
        menuBar.add(exit);

        jf.setSize(896, 576);
        jf.add(this);
        jf.setLocationRelativeTo(null);
        jf.setBackground(Color.lightGray);
        jf.setResizable(false);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
        final FlowLayout LEFT = new FlowLayout(FlowLayout.LEFT), RIGHT = new FlowLayout(FlowLayout.RIGHT);
        this.displayPieceCaptured(new JLabel("White Piece"), LEFT);
        this.displayPieceCaptured(new JLabel("                                                                   "),
                LEFT);
        this.displayPieceCaptured(new JLabel("Black Piece  "), RIGHT);
        this.displayPieceCaptured(new JLabel("Graveyard"), LEFT);
        this.displayPieceCaptured(new JLabel("                                                                    "),
                LEFT);
        this.displayPieceCaptured(new JLabel("Graveyard   "), RIGHT);

        final class Friend implements ActionListener {
            private final JFrame jf;
            private final Board board;

            public Friend(final JFrame jf, final Board board) {
                this.jf = jf;
                this.board = board;
            }

            @Override
            public void actionPerformed(final ActionEvent event) {
                final int option = JOptionPane.showConfirmDialog(null, "Do you want to play as black?",
                        "Choose your side", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                startPlayer(this.board.getPlayers());
                playAsBlack(this.jf, this.board, option);
            }
        }
        final Friend fr = new Friend(jf, this.board);
        friend.addActionListener(fr);

        final class QuitGame implements ActionListener {
            private final JFrame jf;

            public QuitGame(final JFrame jf) {
                this.jf = jf;
            }

            @Override
            public void actionPerformed(final ActionEvent event) {
                final int option = JOptionPane.showConfirmDialog(this.jf,
                        "Are you sure to start quit game?\nGame progress would not be saved", "Quit Game",
                        JOptionPane.WARNING_MESSAGE);
                if (option == 0) {
                    System.exit(0);
                }
            }
        }

        final QuitGame quit = new QuitGame(jf);
        exit.addActionListener(quit);

        this.activateMouseHandler(jf);
    }

    private void playAsBlack(final JFrame jf, final Board board, final int option) {
        if (option == 0) {
            board.startNewGame(7, -1, 3, 4);
        } else if (option == 1) {
            board.startNewGame(0, 1, 4, 3);
        }
        setPlayerPiece(board);
        validate();
        repaint();
        activateMouseHandler(jf);
    }

    private void activateMouseHandler(final JFrame jf) {
        final MouseHandler mouse = new MouseHandler(this.board);
        jf.addMouseListener(mouse);
        jf.addMouseMotionListener(mouse);
    }

    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        this.drawBoard(g);
        this.drawHint(g);
        this.drawPiece(g);
    }

    private void displayPieceCaptured(final JLabel label, final FlowLayout layout) {
        final Font font = new Font("Comic Sans MS", Font.BOLD, 24);
        label.setFont(font);
        label.setLocation(0, 200);
        this.setLayout(layout);
        this.add(label);
    }

    private void drawBoard(final Graphics g) {
        for (int i = 0; i < this.board.getBoard().length; i++) {
            for (int j = 0; j < this.board.getBoard().length; j++) {
                if ((j % 2 == 0 && i % 2 == 0) || (j % 2 != 0 && i % 2 != 0)) {
                    // Mint Color
                    // g.setColor(new Color(0xADEFD1FF));
                    g.setColor(Color.WHITE);
                } else if ((j % 2 != 0 && i % 2 == 0) || (j % 2 == 0 && i % 2 != 0)) {
                    // Living Coral Color
                    // g.setColor(new Color(0xFC766AFF));
                    g.setColor(Color.decode("#1D3D63"));
                }
                g.fillRect(64 * i + OFFSET, 64 * j, 64, 64);
            }
        }
    }

    private void drawHint(final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g;
        AlphaComposite ac = null;
        for (final Move move : this.board.getLocationHints()) {
            final int LOCATION_X = move.getLocation().getX();
            final int LOCATION_Y = move.getLocation().getY();
            if (move instanceof AttackMove) {
                ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
                g2.setColor(new Color(204, 0, 0));
            } else {
                ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
                g2.setColor(new Color(100 ,100 ,93));
            }
            g2.setComposite(ac);
            g2.fillRect(64 * LOCATION_X + OFFSET, 64 * LOCATION_Y, 64, 64);
        }
    }

    private void drawPiece(final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g;
        final AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
        final String CONST_IMAGE_PATH = "image/chessPieceImages/";
        String imagePath;
        for (final Piece blackPiece : this.board.getBlackPieces()) {
            if (blackPiece instanceof King) {
                imagePath = CONST_IMAGE_PATH + "King.png";
            } else if (blackPiece instanceof Queen) {
                imagePath = CONST_IMAGE_PATH + "Queen.png";
            } else if (blackPiece instanceof Rook) {
                imagePath = CONST_IMAGE_PATH + "Rook.png";
            } else if (blackPiece instanceof Bishop) {
                imagePath = CONST_IMAGE_PATH + "Bishop.png";
            } else if (blackPiece instanceof Knight) {
                imagePath = CONST_IMAGE_PATH + "Knight.png";
            } else if (blackPiece instanceof Pawn) {
                imagePath = CONST_IMAGE_PATH + "Pawn.png";
            } else
                imagePath = null;
            displayPiece(g2, ac, imagePath, blackPiece.getY_ForDisplay(), blackPiece.getX_ForDisplay());
        }
        for (final Piece whitePiece : this.board.getWhitePieces()) {
            if (whitePiece instanceof King) {
                imagePath = CONST_IMAGE_PATH + "King_w.png";
            } else if (whitePiece instanceof Queen) {
                imagePath = CONST_IMAGE_PATH + "Queen_w.png";
            } else if (whitePiece instanceof Rook) {
                imagePath = CONST_IMAGE_PATH + "Rook_w.png";
            } else if (whitePiece instanceof Bishop) {
                imagePath = CONST_IMAGE_PATH + "Bishop_w.png";
            } else if (whitePiece instanceof Knight) {
                imagePath = CONST_IMAGE_PATH + "Knight_w.png";
            } else if (whitePiece instanceof Pawn) {
                imagePath = CONST_IMAGE_PATH + "Pawn_w.png";
            } else
                imagePath = null;
            displayPiece(g2, ac, imagePath, whitePiece.getY_ForDisplay(), whitePiece.getX_ForDisplay());
        }
    }

    private void displayPiece(final Graphics2D g2, final AlphaComposite ac, final String imagePath, final int y,
            final int x) {
        if (imagePath != null) {
            final File file = new File(imagePath);
            g2.setComposite(ac);
            displayImage(g2, file.getAbsolutePath(), y, x);
        }
    }

    private void displayImage(final Graphics g, final String imagePath, final int y, final int x) {
        final int size = 64;
        g.drawImage(new ImageIcon(imagePath).getImage(), (x * size) + OFFSET, (y * size), this);
    }

    private final class MouseHandler implements MouseListener, MouseMotionListener {

        private boolean gameEnds;
        private final Player[] players;
        private final Board board;

        public MouseHandler(final Board board) {
            this.gameEnds = false;
            this.players = board.getPlayers();
            this.board = board;
        }


        @Deprecated
        public void mouseClicked(final MouseEvent event) {}
        @Deprecated
        public void mouseEntered(final MouseEvent event) {}
        @Deprecated
        public void mouseExited(final MouseEvent event) {}
        @Deprecated
        public void mouseMoved(final MouseEvent event) {}

        @Override
        public void mousePressed(final MouseEvent event) {
            // if mouse is left pressed only
            if (event.getButton() == MouseEvent.BUTTON1 && !this.gameEnds) {
                final int fromX = (int) Math.floor(event.getX() / 64.0) - 3;
                final int fromY = (int) Math.floor(event.getY() / 64.0) - 1;
                try {
                    players[0].findMyPiece(board, new Location(fromX, fromY));
                    final Piece myPiece = players[0].getMyPiece();
                    final Board board = this.board;
                    final King myKing = players[0].getMyKing();
                    board.setLocationHints(myPiece.calculateAllLegalMoves(board, myKing));
                    validate();
                    repaint();
                } catch (final ArrayIndexOutOfBoundsException | NullPointerException e) {
                    // Handle player pressing outside of board or empty tiles
                }
            }
        }

        @Override
        public void mouseReleased(final MouseEvent event) {
            // if mouse is left released only
            if (event.getButton() == MouseEvent.BUTTON1 && !this.gameEnds) {
                final Piece myPiece = players[0].getMyPiece();
                try {
                    board.getLocationHints().clear();
                    final int toX = (int) Math.floor(event.getX() / 64.0) - 3;
                    final int toY = (int) Math.floor(event.getY() / 64.0) - 1;
                    final Board board = this.board;
                    final Location oriLocation = myPiece.getLocation();
                    final Location destination = new Location(toX, toY);
                    checkMoveLegal(board, oriLocation, destination);
                } catch (final ArrayIndexOutOfBoundsException | NullPointerException e) {
                    // Handle player releasing piece outside of board
                    repositionPiece(this.players);
                }
                validate();
                repaint();
            }
        }

        @Override
        public void mouseDragged(final MouseEvent event) {
            final Piece myPiece = players[0].getMyPiece();
            if (myPiece != null) {
                final int dragX = (int) Math.floor(event.getX() / 64.0) - 3;
                final int dragY = (int) Math.floor(event.getY() / 64.0 - 1);
                myPiece.setCoordinateForDisplay(dragX, dragY);
                validate();
                repaint();
            }
        }

        private void repositionPiece(final Player[] players) {
            final Player player = this.players[0];
            final Player enemy = this.players[1];
            try {
                player.getMyPiece().resetCoordinateForDisplay();
                player.setMyPieceToNull();
                enemy.setMyPieceToNull();
            } catch (final NullPointerException e) {
                // Handle if user doesnt click any pieces
            }
        }

        private Piece promotePawn(final Piece piece) {
            final InterfacePromotion promotion = new InterfacePromotion(piece);
            return promotion.startPromotion();
        }
        private void checkMoveLegal(Board board, final Location oriLocation, final Location destination) {
            final Player player = players[0];
            final Player enemy = players[1];
            final Piece myPiece = player.getMyPiece();
            final boolean playerOne = player.getMyTurn();
            final Board tempBoard = Move.executeMove(myPiece.calculateAllLegalMoves(board, player.getMyKing()), new Move(destination));
            if (tempBoard != null) {
                //board = tempBoard;
                if (board.getCanDisableEnPassant() != playerOne && board.getEnPassantPawn() != null) {
                    board.disableEnPassantPawn();
                }
                if (Pawn.isPawn(myPiece)) {
                    final Pawn pawn = (Pawn) myPiece;
                    board.reduceStaticPawn();
                    if (pawn.getCanPromote()) {
                        player.promoteMyPawn(promotePawn(myPiece));
                    } else if (pawn.getCanBeEnPassant()) {
                        board.storeEnPassantPawn(pawn, playerOne);
                    }
                }
                checkKingCondition(oriLocation);
                if (board.fiftyMoveDraw()) {
                    JOptionPane.showMessageDialog(null, "Game Drawn due to 50 moves rule");
                    JOptionPane.showMessageDialog(null, "Select Friend if you wish to start a new one or Exit if you wish to quit");
                    gameEnds = true;
                }
                player.setMyPieceToNull();
                enemy.setMyPieceToNull();
                board.swapPlayers();
            }
            else {
                repositionPiece(players);
            }
        }

        private void checkKingCondition(final Location oriLocation) {
            final Player player = players[0];
            final Player enemy = players[1];
            final Piece myPiece = player.getMyPiece();
            final Piece[] myPieces = player.getAllMyPieces();
            final King enemyKing = enemy.getMyKing();
            enemyKing.kingInCheck(board, myPiece, oriLocation);

            final String myName = (enemyKing.getColor() == COLOR.WHITE) ? "Black" : "White" ;
            final String enemyName = (enemyKing.getColor() == COLOR.BLACK) ? "Black" : "White" ;

            final boolean directCheck = enemyKing.getKingDirectCheckStatus();
            final boolean discoverCheck = enemyKing.getKingDiscoverCheckStatus();
            final boolean doubleCheck = enemyKing.getKingDoubleCheckStatus();

            if (directCheck) {
                enemyKing.setDirectCheckPiece(myPiece);
            }
            if (discoverCheck) {
                enemyKing.setDiscoverCheckPiece(board, myPieces);
            }

            if (directCheck || discoverCheck || doubleCheck) {
                final boolean checkMate = enemy.checkMate(board);
                if (checkMate) {
                    JOptionPane.showMessageDialog(null, enemyName + " has been checkmated. " + myName + " has won");
                    JOptionPane.showMessageDialog(null, "Select Friend if you wish to start a new one or Exit if you wish to quit");
                    gameEnds = true;
                }
                else {
                    if (doubleCheck) {
                        JOptionPane.showMessageDialog(null, enemyName + " King is in double check");
                    } else if (directCheck || discoverCheck) {
                        JOptionPane.showMessageDialog(null, enemyName + " King is in check");
                    }
                }

            } else {
                final boolean gameDraw = enemy.staleMate(board);
                if (gameDraw) {
                    JOptionPane.showMessageDialog(null, "Game drawn as " + enemyName + " has no legal moves avaliable");
                    JOptionPane.showMessageDialog(null, "Select Friend if you wish to start a new one or Exit if you wish to quit");
                    gameEnds = true;
                }
            }

            final boolean onlyHasKing = board.onlyHasKing();
            if (onlyHasKing) {
                JOptionPane.showMessageDialog(null, "Game drawn as both side only has a King");
                JOptionPane.showMessageDialog(null, "Select Friend if you wish to start a new one or Exit if you wish to quit");
                gameEnds = true;
            }
        }
    }

    private final class InterfacePromotion{
    
        private final ImageIcon[] icon = new ImageIcon[4];
        private final Location destination;
        private final Piece piece;
        public InterfacePromotion (final Piece piece) {
            this.piece = piece;
            this.destination = piece.getLocation();
        }

        protected Piece startPromotion() {
            final int destinationX = this.destination.getX();
            final int destinationY = this.destination.getY();
            final COLOR color = this.piece.getColor();
            setPromotionPiece();
            JOptionPane.showMessageDialog(null, "You only have 1 chance to promote your pawn\nChoose wisely");
            while (true) {
                final int promoteOption = JOptionPane.showOptionDialog(null, null, "Pawn Promotion", JOptionPane.PLAIN_MESSAGE, -1, null, this.icon, null);
                if (promoteOption == 0) {
                    return new Queen(this.piece.getIndex(), destinationX, destinationY, color);
                } else if (promoteOption == 1) {
                    return new Rook(this.piece.getIndex(), destinationX, destinationY, color);
                } else if (promoteOption == 2) {
                    return new Bishop(this.piece.getIndex(), destinationX, destinationY, color);
                } else if (promoteOption == 3) {
                    return new Knight(this.piece.getIndex(), destinationX, destinationY, color);
                }
                JOptionPane.showMessageDialog(null, "You must promote your pawn");
            }
        }

        private String imageAbsolutePath(final String imagePath) {
            final File file = new File(imagePath);
            return file.getAbsolutePath();
        }

        private void setPromotionPiece() {
            if (this.piece.getColor().equals(COLOR.WHITE)) {
                this.icon[0] = new ImageIcon(imageAbsolutePath("image/chessPieceImages/Queen_w.png"));
                this.icon[1] = new ImageIcon(imageAbsolutePath("image/chessPieceImages/Rook_w.png"));
                this.icon[2] = new ImageIcon(imageAbsolutePath("image/chessPieceImages/Bishop_w.png"));
                this.icon[3] = new ImageIcon(imageAbsolutePath("image/chessPieceImages/Knight_w.png"));
            } else {
                this.icon[0] = new ImageIcon(imageAbsolutePath("image/chessPieceImages/Queen.png"));
                this.icon[1] = new ImageIcon(imageAbsolutePath("image/chessPieceImages/Rook.png"));
                this.icon[2] = new ImageIcon(imageAbsolutePath("image/chessPieceImages/Bishop.png"));
                this.icon[3] = new ImageIcon(imageAbsolutePath("image/chessPieceImages/Knight.png"));
            }
        }
    }
}
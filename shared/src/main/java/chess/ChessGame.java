package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor teamTurn;

    public ChessGame() {

        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {

        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {

        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {

        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null){
            return null;
        }
        //calculate all the moves
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);

        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : possibleMoves){

            ChessBoard copyBoard = new ChessBoard(board);
            ChessPiece movingPiece = copyBoard.getPiece(startPosition);
            //get all moves in piece
            copyBoard.addPiece(startPosition, null);
            copyBoard.addPiece(move.getEndPosition(), movingPiece);

            ChessBoard originalBoard = board;
            board = copyBoard;

            boolean kingCheck = isInCheck(piece.getTeamColor());
            //swap back board
            board = originalBoard;

            //check if the move will check the king
            if (!kingCheck){
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {


        ChessPiece piece = board.getPiece(move.getStartPosition());

        if (piece == null) {
            throw new InvalidMoveException();
        }

        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException();
        }

        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (!validMoves.contains(move)){
            throw new InvalidMoveException();
        }

        board.addPiece(move.getStartPosition(), null);

        ChessPiece placePiece = piece;

        if (move.getPromotionPiece() != null){
            placePiece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }

        placePiece.setHasMoved(true);
        board.addPiece(move.getEndPosition(), placePiece);

        castling(piece, move);

        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {

        ChessPosition kingPosition = null;
        //find king
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition kingPos = new ChessPosition(row, col);
                ChessPiece kingPiece = board.getPiece(kingPos);

                if (kingPiece != null && kingPiece.getTeamColor() == teamColor
                        && kingPiece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPosition = kingPos;
                }
            }
        }

        //check enemy moves
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition enemyPos = new ChessPosition(row, col);
                ChessPiece enemyPiece = board.getPiece(enemyPos);

                if (enemyPiece != null && enemyPiece.getTeamColor() != teamColor) {
                    Collection<ChessMove> moves =
                            enemyPiece.pieceMoves(board, enemyPos);

                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        //check if can check
        if (!isInCheck(teamColor)){
            return false;
        }
        //check if any piece can still move
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(pos);
                    //if one can move
                    if (!moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {

        if (isInCheck(teamColor)) {
            return false;
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row,col);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(pos);
                    //if one can move
                    if (!moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {

        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {

        return board;
    }

    private void castling(ChessPiece piece, ChessMove move) {

        if (piece.getPieceType() != ChessPiece.PieceType.KING) {
            return;
        }

        int startCol = move.getStartPosition().getColumn();
        int endCol = move.getEndPosition().getColumn();
        int row = move.getStartPosition().getRow();

        if (Math.abs(startCol - endCol) != 2) {
            return;
        }

        // right rook move
        if (endCol == 7) {
            ChessPosition rookStart = new ChessPosition(row, 8);
            ChessPosition rookEnd = new ChessPosition(row, 6);

            ChessPiece rook = board.getPiece(rookStart);
            board.addPiece(rookStart, null);
            rook.setHasMoved(true);
            board.addPiece(rookEnd, rook);
        }

        // left rook move
        if (endCol == 3) {
            ChessPosition rookStart = new ChessPosition(row, 1);
            ChessPosition rookEnd = new ChessPosition(row, 4);

            ChessPiece rook = board.getPiece(rookStart);
            board.addPiece(rookStart, null);
            rook.setHasMoved(true);
            board.addPiece(rookEnd, rook);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(getBoard(), chessGame.getBoard()) && getTeamTurn() == chessGame.getTeamTurn();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBoard(), getTeamTurn());
    }

    @Override
    public String toString() {
        return String.format("%s,%s", getTeamTurn(),getBoard());
    }
}

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
    private ChessPosition enPassantCheck;

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

        //en passant moves
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && enPassantCheck != null) {

            int row = startPosition.getRow();
            int col = startPosition.getColumn();
            int dir = (piece.getTeamColor() == TeamColor.WHITE) ? 1 : -1;

            // en passant row
            if ((piece.getTeamColor() == TeamColor.WHITE && row == 5)
                    || (piece.getTeamColor() == TeamColor.BLACK && row == 4)) {

                // left
                if (enPassantCheck.getRow() == row && enPassantCheck.getColumn() == col - 1) {

                    ChessPosition end = new ChessPosition(row + dir, col - 1);

                    if (board.getPiece(end) == null) {
                        possibleMoves.add(new ChessMove(startPosition, end, null));
                    }
                }

                // right
                if (enPassantCheck.getRow() == row && enPassantCheck.getColumn() == col + 1) {

                    ChessPosition end = new ChessPosition(row + dir, col + 1);

                    if (board.getPiece(end) == null) {
                        possibleMoves.add(new ChessMove(startPosition, end, null));
                    }
                }
            }
        }

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

            // check castling rules
            if (piece.getPieceType() == ChessPiece.PieceType.KING
                    && Math.abs(move.getStartPosition().getColumn() - move.getEndPosition().getColumn()) == 2) {

                //if currently in check
                if (isInCheck(piece.getTeamColor())) {
                    continue;
                }

                int row = move.getStartPosition().getRow();
                int startCol = move.getStartPosition().getColumn();
                int endCol = move.getEndPosition().getColumn();

                int midCol = (startCol + endCol) / 2;
                ChessPosition midSquare = new ChessPosition(row, midCol);

                if (canEnemyAttack(midSquare, piece.getTeamColor())) {
                    continue;
                }
            }
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


        if (move.getPromotionPiece() != null){
            piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }

        piece.setHasMoved(true);
        board.addPiece(move.getEndPosition(), piece);

        enPassant(piece, move.getStartPosition(), move.getEndPosition());

        //check if pawn move double
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int startRow = move.getStartPosition().getRow();
            int endRow = move.getEndPosition().getRow();

            if (Math.abs(startRow - endRow) == 2) {
                enPassantCheck = move.getEndPosition();
            }
            else {
                enPassantCheck = null;
            }
        }
        else {
            enPassantCheck = null;
        }



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

        ChessPosition kingPosition = getKingPos(teamColor);

        if (kingPosition == null) {
            return false;
        }

        return canEnemyAttack(kingPosition, teamColor);
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

        return !hasValidMoves(teamColor);
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

        return !hasValidMoves(teamColor);
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

    private void enPassant(ChessPiece piece, ChessPosition start, ChessPosition end) {

        //check if pawn
        if (piece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return;
        }

        //cannot move straight
        if (start.getColumn() == end.getColumn()) {
            return;
        }

        int capturedRow;

        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            capturedRow = end.getRow() - 1;
        } else {
            capturedRow = end.getRow() + 1;
        }


        ChessPosition capturedPos = new ChessPosition(capturedRow, end.getColumn());

        ChessPiece capturedPawn = board.getPiece(capturedPos);

        if (capturedPawn != null
                && capturedPawn.getPieceType() == ChessPiece.PieceType.PAWN
                && capturedPawn.getTeamColor() != piece.getTeamColor()) {

            board.addPiece(capturedPos, null);
        }
    }

    private boolean hasValidMoves(TeamColor teamColor){
        //check if any piece have moves left
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(pos);
                    //if one can move
                    if (!moves.isEmpty()) {
                        return true;  // Found at least one legal move
                    }
                }
            }
        }
        return false;
    }

    private ChessPosition getKingPos(TeamColor teamColor){
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition kingPos = new ChessPosition(row, col);
                ChessPiece kingPiece = board.getPiece(kingPos);

                if (kingPiece != null
                        &&kingPiece.getTeamColor() == teamColor
                        && kingPiece.getPieceType() == ChessPiece.PieceType.KING) {
                    return kingPos;
                }
            }
        }
        return null;
    }

    private boolean canEnemyAttack(ChessPosition square, TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {

                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece enemy = board.getPiece(pos);

                if (enemy == null) {
                    continue;
                }

                if (enemy.getTeamColor() == teamColor) {
                    continue;
                }

                Collection<ChessMove> moves = enemy.pieceMoves(board, pos);

                for (ChessMove move : moves) {
                    if (move.getEndPosition().equals(square)) {
                        return true;
                    }
                }

            }
        }
        return false;
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

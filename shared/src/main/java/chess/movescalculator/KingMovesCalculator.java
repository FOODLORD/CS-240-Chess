package chess.movescalculator;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KingMovesCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();
        //king moves

        ChessPiece piece = board.getPiece(position);
        ChessGame.TeamColor color = piece.getTeamColor();

        int[][] moveDirection = {
                {1, 0}, // up
                {1, 1}, // top right
                {0, 1}, // right
                {-1, 1}, // bottom right
                {-1, 0}, // down
                {-1, -1}, // bottom left
                {0, -1}, // left
                {1, -1} // top left
        };

        for (int[] dir : moveDirection) {
            int nextRow = position.getRow() + dir[0];
            int nextCol = position.getColumn() + dir[1];

            if (nextRow < 1 || nextRow > 8 || nextCol < 1 || nextCol > 8) {
                continue;
            }

            ChessPosition target = new ChessPosition(nextRow, nextCol);
            ChessPiece targetPiece = board.getPiece(target);

            if (targetPiece == null || targetPiece.getTeamColor() != color) {
                // move to empty square
                moves.add(new ChessMove(position, target, null));
            }
        }

        //start castling

        if (!piece.hasMoved()) {
            int rowStart = (color == ChessGame.TeamColor.WHITE) ? 1 : 8;

            if (position.getRow() == rowStart && position.getColumn() == 5) {

                //king move right
                ChessPosition rookPos = new ChessPosition(rowStart, 8);
                ChessPiece rook = board.getPiece(rookPos);

                if (rook != null && rook.getPieceType() == ChessPiece.PieceType.ROOK && !rook.hasMoved()) {
                    ChessPosition fPos = new ChessPosition(rowStart, 6);
                    ChessPosition gPos = new ChessPosition(rowStart, 7);

                    if (board.getPiece(fPos) == null && board.getPiece(gPos) == null) {
                        moves.add(new ChessMove(position, gPos, null));
                    }
                }

                //king move left

                rookPos = new ChessPosition(rowStart, 1);
                rook = board.getPiece(rookPos);

                if (rook != null && rook.getPieceType() == ChessPiece.PieceType.ROOK && !rook.hasMoved()) {
                    ChessPosition dPos = new ChessPosition(rowStart, 4);
                    ChessPosition cPos = new ChessPosition(rowStart, 3);

                    if (board.getPiece(new ChessPosition(rowStart, 2)) == null
                            && board.getPiece(dPos) == null && board.getPiece(cPos) == null) {
                        moves.add(new ChessMove(position, cPos, null));
                    }
                }
            }
        }


        return moves;
    }



}

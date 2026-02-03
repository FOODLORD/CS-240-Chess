package chess.movescalculator;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BishopMovesCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();
        //Bishop moves

        ChessPiece piece = board.getPiece(position);
        ChessGame.TeamColor color = piece.getTeamColor();

        int[][] moveDirection = {
                {1, 1}, // top right
                {1, -1}, // top left
                {-1, 1}, // bottom right
                {-1, -1}, // bottom left
        };


        for (int[] dir : moveDirection) {
            int nextRow = position.getRow() + dir[0];
            int nextCol = position.getColumn() + dir[1];

            while (nextRow >= 1 && nextRow <= 8 && nextCol >= 1 && nextCol <= 8) {

                ChessPosition target = new ChessPosition(nextRow, nextCol);
                ChessPiece targetPiece = board.getPiece(target);

                if (targetPiece == null) {
                    // move to empty square
                    moves.add(new ChessMove(position, target, null));
                }

                else {
                    // square have another piece
                    if (targetPiece.getTeamColor() != color) {
                        // enemy piece
                        moves.add(new ChessMove(position, target, null));
                    }
                    break;
                }

                nextRow += dir[0];
                nextCol += dir[1];
            }
        }

        return moves;
    }
}

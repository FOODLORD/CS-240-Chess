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

            if (targetPiece == null) {
                // move to empty square
                moves.add(new ChessMove(position, target, null));
            }

            else if (targetPiece.getTeamColor() != color) {
                // enemy piece
                moves.add(new ChessMove(position, target, null));
            }
        }

        return moves;
    }
}

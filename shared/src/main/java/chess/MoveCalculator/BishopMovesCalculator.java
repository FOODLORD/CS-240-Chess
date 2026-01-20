package chess.MoveCalculator;

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

        int[][] move_direction = {
                {1, 1}, // top right
                {1, -1}, // top left
                {-1, 1}, // bottom right
                {-1, -1}, // bottom left
        };


        for (int[] dir : move_direction) {
            int next_row = position.getRow() + dir[0];
            int next_col = position.getColumn() + dir[1];

            while (next_row >= 1 && next_row <= 8 && next_col >= 1 && next_col <= 8) {

                ChessPosition target = new ChessPosition(next_row, next_col);
                ChessPiece targetPiece = board.getPiece(target);

                if (targetPiece == null) {
                    // move to empty square
                    moves.add(new ChessMove(position, target, null));
                } else {
                    // square have another piece
                    if (targetPiece.getTeamColor() != color) {
                        // enemy piece
                        moves.add(new ChessMove(position, target, null));
                    }
                    break;
                }

                next_row += dir[0];
                next_col += dir[1];
            }
        }

        return moves;
    }
}

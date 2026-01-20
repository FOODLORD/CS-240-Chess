package chess.MoveCalculator;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KnightMovesCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        ChessPiece piece = board.getPiece(position);
        ChessGame.TeamColor color = piece.getTeamColor();
        //Knight moves

        int[][] move_direction = {
                {2,  1}, // up2-right1
                {1,  2}, // up1-right1
                {-1,  2}, // down1-right2
                {-2,  1}, // down2-right1
                {-2, -1}, // down2-left1
                {-1, -2}, // down1-left2
                { 1, -2}, // up1-left2
                { 2, -1} // up2-left1
        };

        for (int[] dir : move_direction) {
            int next_row = position.getRow() + dir[0];
            int next_col = position.getColumn() + dir[1];

            if (next_row < 1 || next_row > 8 || next_col < 1 || next_col > 8) {
                continue;
            } //in the board

            ChessPosition target = new ChessPosition(next_row, next_col);
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

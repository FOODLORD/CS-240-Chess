package chess.movescalculator;

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

        int[][] moveDirection = {
                {2,  1}, // up2-right1
                {1,  2}, // up1-right1
                {-1,  2}, // down1-right2
                {-2,  1}, // down2-right1
                {-2, -1}, // down2-left1
                {-1, -2}, // down1-left2
                { 1, -2}, // up1-left2
                { 2, -1} // up2-left1
        };

        for (int[] dir : moveDirection) {
            int nextRow = position.getRow() + dir[0];
            int nextCol = position.getColumn() + dir[1];

            if (nextRow < 1 || nextRow > 8 || nextCol < 1 || nextCol > 8) {
                continue;
            } //in the board

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

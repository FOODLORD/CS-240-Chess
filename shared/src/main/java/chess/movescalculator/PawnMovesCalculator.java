package chess.movescalculator;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PawnMovesCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        ChessPiece piece = board.getPiece(position);
        ChessGame.TeamColor color = piece.getTeamColor();

        //Pawn moves

        int moveForward;
        int startRow;
        int promotionRow;

        if (color == ChessGame.TeamColor.WHITE) {
            moveForward = 1;
            startRow = 2;
            promotionRow = 8;
        }

        else { //black
            moveForward = -1;
            startRow = 7;
            promotionRow = 1;
        }

        int rowPosition = position.getRow();
        int colPosition = position.getColumn();

        //moving forward
        int nextRow = rowPosition + moveForward;
        if (nextRow >= 1 && nextRow <= 8) {
            ChessPosition forwardPosition = new ChessPosition(nextRow, colPosition);
            if (board.getPiece(forwardPosition) == null) {

                //promotion
                if (nextRow == promotionRow) {
                    promotionMoves(moves, position, forwardPosition);
                }

                else {
                    moves.add(new ChessMove(position, forwardPosition, null));
                }

                //move to squares from the start
                if (rowPosition == startRow) {
                    int moveDoubleRow = rowPosition + 2 * moveForward;
                    ChessPosition doublePos = new ChessPosition(moveDoubleRow, colPosition);
                    if (board.getPiece(doublePos) == null) {
                        moves.add(new ChessMove(position, doublePos, null));
                    }
                }
            }
        }

        //capturing diagonally

        int[][] captureDirection = {
                {moveForward, -1}, //up1-left1
                {moveForward, 1} //up1-right1
        };

        for (int[] dir : captureDirection) {
            int targetRow = rowPosition + dir[0];
            int targetCol = colPosition + dir[1];

            if (targetRow < 1 || targetRow > 8 || targetCol < 1 || targetCol > 8) {
                continue;
            }

            ChessPosition target = new ChessPosition(targetRow, targetCol);
            ChessPiece targetPiece = board.getPiece(target);

            if (targetPiece != null && targetPiece.getTeamColor() != color) {
                if (targetRow == promotionRow) {
                    promotionMoves(moves, position, target);
                } else {
                    moves.add(new ChessMove(position, target, null));
                }
            }
        }


        return moves;
    }

    private void promotionMoves(List<ChessMove> moves, ChessPosition fromPosition, ChessPosition toTarget
    ) {
        moves.add(new ChessMove(fromPosition, toTarget, ChessPiece.PieceType.QUEEN));
        moves.add(new ChessMove(fromPosition, toTarget, ChessPiece.PieceType.ROOK));
        moves.add(new ChessMove(fromPosition, toTarget, ChessPiece.PieceType.BISHOP));
        moves.add(new ChessMove(fromPosition, toTarget, ChessPiece.PieceType.KNIGHT));
    }
}

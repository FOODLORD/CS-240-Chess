package chess.MoveCalculator;

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

        int move_forward;
        int start_row;
        int promotion_row;

        if (color == ChessGame.TeamColor.WHITE) {
            move_forward = 1;
            start_row = 2;
            promotion_row = 8;
        }

        else { //black
            move_forward = -1;
            start_row = 7;
            promotion_row = 1;
        }

        int row_position = position.getRow();
        int col_position = position.getColumn();

        //moving forward
        int next_row = row_position + move_forward;
        if (next_row >= 1 && next_row <= 8) {
            ChessPosition forward_position = new ChessPosition(next_row, col_position);
            if (board.getPiece(forward_position) == null) {

                //promotion
                if (next_row == promotion_row) {
                    PromotionMoves(moves, position, forward_position);
                }

                else {
                    moves.add(new ChessMove(position, forward_position, null));
                }

                //move to squares from the start
                if (row_position == start_row) {
                    int move_double_row = row_position + 2 * move_forward;
                    ChessPosition doublePos = new ChessPosition(move_double_row, col_position);
                    if (board.getPiece(doublePos) == null) {
                        moves.add(new ChessMove(position, doublePos, null));
                    }
                }
            }
        }

        //capturing diagonally

        int[][] capture_direction = {
                {move_forward, -1}, //up1-left1
                {move_forward, 1} //up1-right1
        };

        for (int[] dir : capture_direction) {
            int target_row = row_position + dir[0];
            int target_col = col_position + dir[1];

            if (target_row < 1 || target_row > 8 || target_col < 1 || target_col > 8) {
                continue;
            }

            ChessPosition target = new ChessPosition(target_row, target_col);
            ChessPiece targetPiece = board.getPiece(target);

            if (targetPiece != null && targetPiece.getTeamColor() != color) {
                if (target_row == promotion_row) {
                    PromotionMoves(moves, position, target);
                } else {
                    moves.add(new ChessMove(position, target, null));
                }
            }
        }


        return moves;
    }

    private void PromotionMoves(List<ChessMove> moves, ChessPosition from_position, ChessPosition to_target
    ) {
        moves.add(new ChessMove(from_position, to_target, ChessPiece.PieceType.QUEEN));
        moves.add(new ChessMove(from_position, to_target, ChessPiece.PieceType.ROOK));
        moves.add(new ChessMove(from_position, to_target, ChessPiece.PieceType.BISHOP));
        moves.add(new ChessMove(from_position, to_target, ChessPiece.PieceType.KNIGHT));
    }
}

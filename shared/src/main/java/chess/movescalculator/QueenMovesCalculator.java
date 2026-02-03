package chess.movescalculator;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueenMovesCalculator implements PieceMovesCalculator{

    private final BishopMovesCalculator bishopCalculator = new BishopMovesCalculator();
    private final RookMovesCalculator rookCalculator = new RookMovesCalculator();
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        moves.addAll(bishopCalculator.pieceMoves(board, position));
        moves.addAll(rookCalculator.pieceMoves(board, position));

        return moves;
    }
}

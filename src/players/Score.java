package players;

import game.AbstractPlayer;

public class Score {
    private static final int CORNER_SCORE = 4;
    private static final int SIDE_SCORE = 1;
    private static final int SIDE_ADJACENT_CORNER = 2;
    private static final int INNER_CORNER = 3;
    private AbstractPlayer pleyer;

    public  Score (AbstractPlayer player){
        this.pleyer = player;
    }

    public int score(int [][] board,AbstractPlayer player)
    {
//        int board[][] = state.getBoard();
        int totalScore = 0;

        totalScore += considerCorner(board, 0, 0);
        totalScore += considerCorner(board, 0, board.length - 1);
        totalScore += considerCorner(board, board.length - 1, 0);
        totalScore += considerCorner(board, board.length - 1, board.length - 1);

        totalScore += considerRow(board, 0);
        totalScore += considerRow(board, board.length - 1);
        totalScore += considerCol(board, 0);
        totalScore += considerCol(board, board.length- 1);

        totalScore += considerInnerCorner(board, 0, 0, 1, 1);
        totalScore += considerInnerCorner(board, 0, board.length - 1, 1, board.length - 2);
        totalScore += considerInnerCorner(board, board.length - 1, 0, board.length - 2, 1);
        totalScore += considerInnerCorner(board, board.length - 1, board.length - 1, board.length - 2, board.length - 2);

        return totalScore;
    }

    private int considerCorner(int board[][], int row, int col)
    {
        //add extra points for holding a corner, because these pieces are stable and can never be
        //captured
        if (board[row][col] == pleyer.getMyBoardMark())
        {
            return CORNER_SCORE;
        }

        if (board[row][col] == pleyer.getOpponentBoardMark())
        {
            return -CORNER_SCORE;
        }

        return 0;
    }

    private int considerRow(int board[][], int row)
    {
        int localScore = 0;
        int start = 1, end = board.length - 1;

        //if there is nothing in the corner yet, it is very bad to have a piece in one of the
        //adjacent spots. Therefore, subtract points if this is the case
        if (board[row][board.length -1] != pleyer.getOpponentBoardMark() && board[row][board.length-1] != pleyer.getMyBoardMark() )
        {
            start++;
            if (board[row][1] == pleyer.getMyBoardMark())
            {
                localScore += -SIDE_ADJACENT_CORNER;
            }
            else if (board[row][1] == pleyer.getOpponentBoardMark())
            {
                localScore += SIDE_ADJACENT_CORNER;
            }
        }

        if (board[row][0] != pleyer.getOpponentBoardMark() && board[row][0] != pleyer.getMyBoardMark())
        {
            end--;
            if (board[row][board.length - 2] == pleyer.getMyBoardMark())
            {
                localScore += -SIDE_ADJACENT_CORNER;
            }
            else if (board[row][board.length - 2] == pleyer.getOpponentBoardMark())
            {
                localScore += SIDE_ADJACENT_CORNER;
            }
        }

        //finally, after handling above cases, grant additional points for holding the sides,
        //because these positions are relatively stable
        for (int i = start; i < end; i++)
        {
            if (board[row][i] == pleyer.getMyBoardMark())
            {
                localScore += SIDE_SCORE;
            }
            else if (board[row][i] == pleyer.getOpponentBoardMark())
            {
                localScore += -SIDE_SCORE;
            }
        }

        return localScore;
    }

    private int considerCol(int board[][], int col)
    {
        int localScore = 0;
        int start = 1, end = board.length - 1;
        //if there is nothing in the corner yet, it is very bad to have a piece in one of the
        //adjacent spots. Therefore, subtract points if this is the case
        if (board[0][col] != pleyer.getMyBoardMark() && board[0][col] != pleyer.getOpponentBoardMark() )
        {
            start++;
            if (board[1][col] == pleyer.getMyBoardMark())
            {
                localScore += -SIDE_ADJACENT_CORNER;
            }
            else if (board[1][col] == pleyer.getOpponentBoardMark())
            {
                localScore += SIDE_ADJACENT_CORNER;
            }
        }

        if (board[board.length - 1][col] != pleyer.getMyBoardMark() && board[board.length - 1][col] != pleyer.getOpponentBoardMark())
        {
            end--;
            if (board[board.length - 2][col] == pleyer.getMyBoardMark())
            {
                localScore += -SIDE_ADJACENT_CORNER;
            }
            else if (board[board.length - 2][col] == pleyer.getOpponentBoardMark())
            {
                localScore += SIDE_ADJACENT_CORNER;
            }
        }

        //finally, after handling above cases, grant additional points for holding the sides,
        //because these positions are relatively stable
        for (int i = start; i < end; i++)
        {
            if (board[i][col] == pleyer.getMyBoardMark())
            {
                localScore += SIDE_SCORE;
            }
            else if (board[i][col] == pleyer.getOpponentBoardMark())
            {
                localScore += -SIDE_SCORE;
            }
        }

        return localScore;
    }

    private int considerInnerCorner(int board[][], int corner_row, int corner_col, int row, int col)
    {
        if (board[corner_row][corner_col] != pleyer.getOpponentBoardMark()  && board[corner_row][corner_col] != pleyer.getMyBoardMark())
        {
            if (board[row][col] == pleyer.getMyBoardMark())
            {
                return -INNER_CORNER;
            }

            if (board[row][col] == pleyer.getOpponentBoardMark())
            {
                return INNER_CORNER;
            }
        }

        return 0;
    }
}

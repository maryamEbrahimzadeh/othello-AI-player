package players;

import game.AbstractPlayer;
import game.BoardSquare;
import game.Move;
import game.OthelloGame;

import java.util.List;
import java.lang.Math;
import java.util.Random;
import java.util.Vector;

public class AI extends AbstractPlayer {
    private static final int CORNER_SCORE = 4;
    private static final int SIDE_SCORE = 1;
    private static final int SIDE_ADJACENT_CORNER = 2;
    private static final int INNER_CORNER = 3;

    public AI(int depth) {
        super(depth);
    }

    @Override
    public BoardSquare play(int[][] board) {
        //System.out.println("hello");
        // generate children of s0
        //State s0 = new State(board, 0, 0, null,null);

        State currentState = new State(board, 0, 0, null,null);
        List<Move> moves = getGame().getValidMoves(board, getMyBoardMark());
        State ns,ns2 = null;
        //System.out.println("size " + moves.size());

        for (Move move : moves) {
            //System.out.println(move.getBardPlace().getCol() +" "+move.getBardPlace().getRow());
            int[][] boardCopy = copy(board);
            int [][] nextBoard = getGame().do_move(boardCopy, move.getBardPlace(), this);
            //int [][] nextBoard2 = getGame().do_move(boardCopy, move.getBardPlace(), this);
            ns = new State(nextBoard, 0,0, currentState,move.getBardPlace());
            //ns2 = new State(nextBoard2, 0,0, s0, move.getBardPlace());
            //s0.addChild(ns);
            currentState.addChild(ns);
        }

        int maxIteration =  1000;
        int iteration = 1;
        //State currentState = new State(s0.getBoard(), s0.getT(), s0.getN(),null);

        while (iteration <= maxIteration ){
            Monte_carllo_func(currentState);
            iteration++;
        }


        if (currentState.getChildren().size()>0) {
            int tmax = currentState.getChildren().firstElement().getT();
            State  tmaxnode = currentState.getChildren().firstElement();
            for (State ch : currentState.getChildren()) {
                if (ch.getT() >= tmax) {
                    tmax = ch.getT();
                    tmaxnode = ch;
                }
            }
            return tmaxnode.action;
        }else{
            return  new BoardSquare(-1,-1);
        }

    }

    void Monte_carllo_func(State currentState){
        List<Move> moves ;
        State ns = null;

        if (currentState.getChildren().size() == 0){
            //System.out.println("i am leaf");
            //if it is leaf
            if (currentState.getN() == 0){
                currentState.setT(Rollout(currentState));
                backPropogate(currentState,currentState.getT());
            }else{
                //System.out.println("leaf not n not 0");
                moves = getGame().getValidMoves(currentState.getBoard(), getMyBoardMark());
                ns = null;
                for (Move move : moves) {
                    int[][] boardCopy = copy(currentState.getBoard());
                    int [][] nextBoard = getGame().do_move(boardCopy, move.getBardPlace(), this);
                    ns = new State(nextBoard, 0,0, currentState,move.getBardPlace());
                    currentState.addChild(ns);
                }
                if (moves.size() > 0) {
                    currentState = currentState.getChildren().firstElement();
                    currentState.setT(Rollout(currentState));
                    backPropogate(currentState, currentState.getT());
                }
            }
        }else{
            //if it is not leaf
            //System.out.println("i am not leaf");
            double maxUCB = currentState.getChildren().firstElement().UCBFunction() + currentState.getChildren().firstElement().rate ;
            State maxNode = currentState.getChildren().firstElement();

            for ( State child : currentState.getChildren()){
                if (child.UCBFunction() + child.rate > maxUCB){
                    maxUCB = child.UCBFunction() + child.rate;
                    maxNode = child;
                }
            }
            //System.out.println(maxUCB +" "+maxNode.rate);
            //System.out.println("here");
            Monte_carllo_func(maxNode);

        }
    }


    private int[][] copy(int[][] board) {
        board = board.clone();
        for (int i = 0; i < board.length; i++) {
            board[i] = board[i].clone();
        }
        return board;
    }

    private int Rollout(State cs){
        //System.out.println("rolleout");
        OthelloGame game = new OthelloGame();
        AbstractPlayer  player1 = new MGreedy(2);
        player1.setBoardMark(this.getOpponentBoardMark());
        player1.setOpponentBoardMark(this.getMyBoardMark());
        player1.setGame(game);

        AbstractPlayer  player2 = new MRandomPlayer(2);
        player2.setBoardMark(this.getMyBoardMark());
        player2.setOpponentBoardMark(this.getOpponentBoardMark());
        player2.setGame(game);

        int [][] board = (cs.getBoard()).clone();
        for (int i = 0; i < board.length; i++) {
            board[i] = cs.getBoard()[i].clone();
        }

        while (  !(game.getValidMoves(board, player1.getMyBoardMark()).isEmpty() &&
                game.getValidMoves(board,player2.getMyBoardMark()).isEmpty())   ){
            //System.out.println("rollewhile");
            BoardSquare move = player1.play(board);
            board = game.do_move(board, move, player1);
            BoardSquare move2 = player2.play(board);
            board = game.do_move(board, move2, player2);
        }


        int value = score(board,this);


        return  value;
    }

    private  void  backPropogate(State s, int value){
        State parent = s ;
        while (parent != null){
            parent.setT(parent.getT()+ value);
            parent.setN(parent.getN()+1);
            parent = parent.getParent();
        }
    }

    public int score(int [][] board,AbstractPlayer player)
    {
//        int board[][] = state.getBoard();
        int totalScore = 0;

        totalScore += considerCorner(board, 0, 0,player);
        totalScore += considerCorner(board, 0, board.length - 1,player);
        totalScore += considerCorner(board, board.length - 1, 0,player);
        totalScore += considerCorner(board, board.length - 1, board.length - 1,player);

        totalScore += considerRow(board, 0,player);
        totalScore += considerRow(board, board.length - 1,player);
        totalScore += considerCol(board, 0,player);
        totalScore += considerCol(board, board.length- 1,player);

        totalScore += considerInnerCorner(board, 0, 0, 1, 1,player);
        totalScore += considerInnerCorner(board, 0, board.length - 1, 1, board.length - 2,player);
        totalScore += considerInnerCorner(board, board.length - 1, 0, board.length - 2, 1,player);
        totalScore += considerInnerCorner(board, board.length - 1, board.length - 1, board.length - 2, board.length - 2,player);

        return totalScore;
    }

    private int considerCorner(int board[][], int row, int col,AbstractPlayer player)
    {
        //add extra points for holding a corner, because these pieces are stable and can never be
        //captured
        if (board[row][col] == player.getMyBoardMark())
        {
            return CORNER_SCORE;
        }

        if (board[row][col] == player.getOpponentBoardMark())
        {
            return -CORNER_SCORE;
        }

        return 0;
    }

    private int considerRow(int board[][], int row,AbstractPlayer player)
    {
        int localScore = 0;
        int start = 1, end = board.length - 1;

        //if there is nothing in the corner yet, it is very bad to have a piece in one of the
        //adjacent spots. Therefore, subtract points if this is the case
        if (board[row][board.length -1] != player.getOpponentBoardMark() && board[row][board.length-1] != player.getMyBoardMark() )
        {
            start++;
            if (board[row][1] == player.getMyBoardMark())
            {
                localScore += -SIDE_ADJACENT_CORNER;
            }
            else if (board[row][1] == player.getOpponentBoardMark())
            {
                localScore += SIDE_ADJACENT_CORNER;
            }
        }

        if (board[row][0] != player.getOpponentBoardMark() && board[row][0] != player.getMyBoardMark())
        {
            end--;
            if (board[row][board.length - 2] == player.getMyBoardMark())
            {
                localScore += -SIDE_ADJACENT_CORNER;
            }
            else if (board[row][board.length - 2] == player.getOpponentBoardMark())
            {
                localScore += SIDE_ADJACENT_CORNER;
            }
        }

        //finally, after handling above cases, grant additional points for holding the sides,
        //because these positions are relatively stable
        for (int i = start; i < end; i++)
        {
            if (board[row][i] == player.getMyBoardMark())
            {
                localScore += SIDE_SCORE;
            }
            else if (board[row][i] == player.getOpponentBoardMark())
            {
                localScore += -SIDE_SCORE;
            }
        }

        return localScore;
    }

    private int considerCol(int board[][], int col,AbstractPlayer player)
    {
        int localScore = 0;
        int start = 1, end = board.length - 1;
        //if there is nothing in the corner yet, it is very bad to have a piece in one of the
        //adjacent spots. Therefore, subtract points if this is the case
        if (board[0][col] != player.getMyBoardMark() && board[0][col] != player.getOpponentBoardMark() )
        {
            start++;
            if (board[1][col] == player.getMyBoardMark())
            {
                localScore += -SIDE_ADJACENT_CORNER;
            }
            else if (board[1][col] == player.getOpponentBoardMark())
            {
                localScore += SIDE_ADJACENT_CORNER;
            }
        }

        if (board[board.length - 1][col] != player.getMyBoardMark() && board[board.length - 1][col] != player.getOpponentBoardMark())
        {
            end--;
            if (board[board.length - 2][col] == player.getMyBoardMark())
            {
                localScore += -SIDE_ADJACENT_CORNER;
            }
            else if (board[board.length - 2][col] == player.getOpponentBoardMark())
            {
                localScore += SIDE_ADJACENT_CORNER;
            }
        }

        //finally, after handling above cases, grant additional points for holding the sides,
        //because these positions are relatively stable
        for (int i = start; i < end; i++)
        {
            if (board[i][col] == player.getMyBoardMark())
            {
                localScore += SIDE_SCORE;
            }
            else if (board[i][col] == player.getOpponentBoardMark())
            {
                localScore += -SIDE_SCORE;
            }
        }

        return localScore;
    }

    private int considerInnerCorner(int board[][], int corner_row, int corner_col, int row, int col,AbstractPlayer player)
    {
        if (board[corner_row][corner_col] != player.getOpponentBoardMark()  && board[corner_row][corner_col] != player.getMyBoardMark())
        {
            if (board[row][col] == player.getMyBoardMark())
            {
                return -INNER_CORNER;
            }

            if (board[row][col] == player.getOpponentBoardMark())
            {
                return INNER_CORNER;
            }
        }

        return 0;
    }
}

    class State {
        private int [][] board;
        private int t;
        private int n;
        private State  parent;
        public  BoardSquare action;
        public  int rate ;

        public Vector<State> getChildren() {
            return children;
        }

        public void setChildren(Vector<State> children) {
            this.children = children;
        }

        private Vector<State> children ;

        public int getN() {
            return n;
        }

        public State getParent() {
            return parent;
        }

        public void setParent(State parent) {
            this.parent = parent;
        }

        public void setN(int n) {
            this.n = n;
        }

        public int getT() {
            return t;
        }

        public void setT(int t) {
            this.t = t;
        }

        public int[][] getBoard() {
            return board;
        }

        public void setBoard(int[][] board) {
            this.board = board;
        }

        public  State(int [][] board, int t, int n, State parent, BoardSquare action){
            this.board = board;
            this.n = n;
            this.t = n;
            this.parent = parent;
            this.action = action;
            this.rate = rate_compute();
            children =  new Vector<State>();
        }
        int rate_compute(){
            int score = 0;
            if (action==null){
                return 0;
            }
            int myMark = board[action.getRow()][action.getCol()];
            for(int i=0;i<board.length;i++){
                for (int j=0;j<board.length;j++){
                 if(board[i][j] == myMark && parent.board[i][j] != myMark){
                     score++;
                 }
                }
            }
            return score/2;
        }

        void  addChild(State s){
            this.children.add(s);
        }

        double UCBFunction(){
            if (this.n == 0)
                return Double.POSITIVE_INFINITY;
            return this.t / this.n + 2 * Math.sqrt(Math.log(parent.n) / this.n);
        }
    }

class MGreedy extends AbstractPlayer {
    public MGreedy(int depth) {
        super(depth);
    }

    @Override
    public BoardSquare play(int[][] board) {
        List<Move> moves = getGame().getValidMoves(board, getMyBoardMark());

        BoardSquare bestMove = new BoardSquare(-1, -1); // No Move
        int bestMoveMarks = -1;

        for (Move move : moves) {
            int[][] boardCopy = copy(board);
            getGame().do_move(boardCopy, move.getBardPlace(), this);
            if (countMyMarks(boardCopy) > bestMoveMarks) {
                bestMove = move.getBardPlace();
            }
        }

        return bestMove;
    }

    private int[][] copy(int[][] board) {
        board = board.clone();
        for (int i = 0; i < board.length; i++) {
            board[i] = board[i].clone();
        }
        return board;
    }

    private int countMyMarks(int[][] board) {
        int nMarks = 0;
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[0].length; c++) {
                if (board[r][c] == getMyBoardMark())
                    nMarks++;
            }
        }
        return nMarks;
    }
}

class MRandomPlayer extends AbstractPlayer {

    public MRandomPlayer(int depth) {
        super(depth);
    }

    @Override
    public BoardSquare play(int[][] tab) {
        OthelloGame jogo = new OthelloGame();
        Random r = new Random();
        List<Move> jogadas = jogo.getValidMoves(tab, getMyBoardMark());
        if (jogadas.size() > 0) {
            return jogadas.get(r.nextInt(jogadas.size())).getBardPlace();
        } else {
            return new BoardSquare(-1, -1);
        }
    }

}

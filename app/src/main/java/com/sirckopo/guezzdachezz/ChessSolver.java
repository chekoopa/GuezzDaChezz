package com.sirckopo.guezzdachezz;

import android.os.AsyncTask;
import android.util.Log;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ChessSolver {

    static final String errorSignal = "*";
    static final String errorBadFEN = "*FEN";
    static final String errorBadDepth = "*Depth";
    static final String errorGeneric = "*Error";

    static class SolverTask extends AsyncTask<String, String, String> {

        int minDepth = 0;
        int firstDone = 0;
        int firstNeed = 0;
        LinkedList<ChessMove> solutions = new LinkedList<>();

        int countPossible(ChessLayout l) {
            // parsed_layouts++
            int output = 0;

            for (int x = 1; x <= ChessLayout.size; x++) for (int y = 1; y <= ChessLayout.size; y++)
                if (l.getBoard(x, y) != ChessLayout.fEmpty &&
                        l.getBoard(x, y) / ChessLayout.fBlack == (l.getMove() ? 1 : 0)) {
                    LinkedList<ChessMove> mbuf = l.moveBuffer(x, y);
                    for (ChessMove cm : mbuf) if (l.tryMove(cm)) {
                        if (l.getBoardFigure(x, y) == ChessLayout.fPawn &&
                                cm.getCode(3) == (l.getMove() ? 1 : 8))
                            output += 4;
                        else
                            output += 1;
                    }
            }

            return output;
        }

        boolean solveInOne(ChessLayout inlay) {
            // parsed_layouts++
            ChessLayout l = inlay;

            for (int x = 1; x <= ChessLayout.size; x++) for (int y = 1; y <= ChessLayout.size; y++)
                if (l.getBoard(x, y) != ChessLayout.fEmpty &&
                        l.getBoard(x, y) / ChessLayout.fBlack == (l.getMove() ? 1 : 0)) {
                    LinkedList<ChessMove> mbuf = l.moveBuffer(x, y);
                    for (ChessMove cm : mbuf) if (l.doMove(cm)) {
                        if (l.getBoardFigure(x, y) == ChessLayout.fPawn &&
                                cm.getCode(3) == (!l.getMove() ? 1 : 8)) {
                            for (int i = ChessLayout.fKnight; i <= ChessLayout.fQueen; i++) {
                                l.setBoard(cm.getCode(2), cm.getCode(3), ChessLayout.fPawn +
                                        (l.getMove() ? 0 : ChessLayout.fBlack));
                                if (l.isCheckmate(l.getMove()))
                                    return true;
                            }
                        } else {
                            if (l.isCheckmate(l.getMove()))
                                return true;
                        }
                        l = inlay;
                    }
            }
            return false;
        }

        private void solutionsInOne(ChessLayout inlay) {
            // parsed_layouts++
            ChessLayout l = inlay;

            for (int x = 1; x <= ChessLayout.size; x++) for (int y = 1; y <= ChessLayout.size; y++)
                if (l.getBoard(x, y) != ChessLayout.fEmpty &&
                        l.getBoard(x, y) / ChessLayout.fBlack == (l.getMove() ? 1 : 0)) {
                    LinkedList<ChessMove> mbuf = l.moveBuffer(x, y);
                    for (ChessMove cm : mbuf)
                        if (l.doMove(cm)) {
                            if (l.getBoardFigure(x, y) == ChessLayout.fPawn &&
                                    cm.getCode(3) == (!l.getMove() ? 1 : 8)) {
                                for (int i = ChessLayout.fKnight; i <= ChessLayout.fQueen; i++) {
                                    l.setBoard(cm.getCode(2), cm.getCode(3), ChessLayout.fPawn +
                                            (l.getMove() ? 0 : ChessLayout.fBlack));
                                    if (l.isCheckmate(l.getMove()))
                                        solutions.add(cm);
                                }
                            } else {
                                if (l.isCheckmate(l.getMove()))
                                    solutions.add(cm);
                            }
                            l = inlay;
                        }
                }
        }

        boolean solveFirst(ChessLayout inlay, int depth) {
            // parsed_layouts++

            boolean inOne = solveInOne(inlay);
            if (inOne) {
                if (depth == minDepth)
                    solutionsInOne(inlay);
                return true;
            }
            if (depth == 1)
                return false;

            ChessLayout l = inlay;
            for (int x = 1; x <= ChessLayout.size; x++) for (int y = 1; y <= ChessLayout.size; y++)
                if (l.getBoard(x, y) != ChessLayout.fEmpty &&
                        l.getBoard(x, y) / ChessLayout.fBlack == (l.getMove() ? 1 : 0)) {
                    LinkedList<ChessMove> mbuf = l.moveBuffer(x, y);
                    for (ChessMove cm : mbuf) if (l.doMove(cm)) {
                        if (l.getBoardFigure(x, y) == ChessLayout.fPawn &&
                                cm.getCode(3) == (!l.getMove() ? 1 : 8)) {
                            for (int i = ChessLayout.fKnight; i <= ChessLayout.fQueen; i++) {
                                l.setBoard(cm.getCode(2), cm.getCode(3), ChessLayout.fPawn +
                                        (l.getMove() ? 0 : ChessLayout.fBlack));
                                cm.setString(cm.getString() + ChessLayout.tFigures.charAt(i));
                                if (solveSecond(l, depth - 1) && depth == minDepth)
                                    solutions.add(cm);
                            }
                        } else {
                            if (solveSecond(l, depth - 1) && depth == minDepth)
                                solutions.add(cm);
                        }
                        l = inlay;
                    }
                    if (depth == minDepth) {
                        firstDone++;
                        this.onProgressUpdate(String.valueOf(firstDone) + " / " +
                                String.valueOf(firstNeed) + " (" +
                                String.valueOf(firstNeed / firstDone) + "%)");
                    }
                }

            return false;
        }

        boolean solveSecond(ChessLayout inlay, int depth) {
            // parsed_layouts++

            if (depth == 1 || solveInOne(inlay))
                return false;

            ChessLayout l = inlay;
            for (int x = 1; x <= ChessLayout.size; x++) for (int y = 1; y <= ChessLayout.size; y++)
                if (l.getBoard(x, y) != ChessLayout.fEmpty &&
                        l.getBoard(x, y) / ChessLayout.fBlack == (l.getMove() ? 1 : 0)) {
                    LinkedList<ChessMove> mbuf = l.moveBuffer(x, y);
                    for (ChessMove cm : mbuf) if (l.doMove(cm)) {
                        if (l.getBoardFigure(x, y) == ChessLayout.fPawn &&
                                cm.getCode(3) == (!l.getMove() ? 1 : 8)) {
                            for (int i = ChessLayout.fKnight; i <= ChessLayout.fQueen; i++) {
                                l.setBoard(cm.getCode(2), cm.getCode(3), ChessLayout.fPawn +
                                        (l.getMove() ? 0 : ChessLayout.fBlack));
                                if (solveFirst(l, depth - 1))
                                    return false;
                            }
                        } else {
                            if (solveFirst(l, depth - 1))
                                return false;
                        }
                        l = inlay;
                    }
                }
            return false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String fen = params[0];
            ChessLayout chessLayout = new ChessLayout();
            if (!chessLayout.loadFEN(fen))
                return errorBadFEN;
            if (chessLayout.getDepth() == 0)
                return errorBadDepth;
            firstNeed = countPossible(chessLayout);
            firstDone = 0;
            minDepth = chessLayout.getDepth() * 2 - 1;
            if (solveFirst(chessLayout, minDepth)) {
                String output = "";
                for (ChessMove cm : solutions) {
                    output += (output.length() == 0 ? "" : " ") + cm.getString();
                }
                return output;
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Log.i("GDC", "Solver sez " + values[0]);
            //TODO: prettier progress notifier?
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (onFinish != null) {
                onFinish.run();
            }
        }
    }

    private static SolverTask task;
    private static Runnable onFinish;

    public static void cancel() {
        if (isTaskFree()) return;
        task.cancel(true);
    }

    public static void solve(String fen) {
        task = new SolverTask();
        task.execute(fen);
    }

    public static void solve(String fen, Runnable run) {
        task = new SolverTask();
        onFinish = run;
        task.execute(fen);
    }

    public static boolean isTaskFree() {
        if (task == null) return true;
        String status = task.getStatus().toString();
        return !status.equalsIgnoreCase("running");
    }

    public static String get() {
        if (task == null) return errorGeneric;
        String answer;
        try {
            answer = task.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            e.printStackTrace();
            answer = errorGeneric;
        }
        return answer;
    }


}

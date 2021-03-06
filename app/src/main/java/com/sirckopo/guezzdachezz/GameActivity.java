package com.sirckopo.guezzdachezz;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.LinkedList;


public class GameActivity extends ActionBarActivity {

    RelativeLayout llBase;
    TableLayout tlChessboard;
    LinkedList<Button> butSquares = new LinkedList<>();
    
    MenuItem miMoveIndicator;
    MenuItem miHint;
    MenuItem miWriteLayout;
    MenuItem miLevelIndicator;
    MenuItem miReset;
    MenuItem miFreeplay;

    boolean canWriteLayouts = false;

    private int screenWidth;
    private int screenHeight;
    private boolean isLandscape;

    private void updateScreenMetrics() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        isLandscape = (screenWidth > screenHeight);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // all the dynamic layout magic is right here
        makeLayout();

        try {
            layoutStorage.createDataBase();
            layoutStorage.openDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        // Chess things

        Intent intent = getIntent();
        // a little workaround for keeping a problem on rotation
        if (savedInstanceState != null) {
            problemSet = savedInstanceState.getString("set");
            currentId = savedInstanceState.getInt("id");
            canWriteLayouts = savedInstanceState.getBoolean("writer");
        } else {
            problemSet = intent.getStringExtra("set");
            currentId = intent.getIntExtra("id", 0);
            canWriteLayouts = intent.getBooleanExtra("writer", false);
        }

        if (problemSet == null) {
            baseFEN = (intent.getStringExtra("fen") == null ? ChessLayout.startLayout :
                    intent.getStringExtra("fen"));
        } else if (problemSet.equalsIgnoreCase("server")) {
            baseFEN = intent.getStringExtra("fen");
            String[] sol = intent.getStringExtra("solutions").split(" ");
            solutions = new ChessMove[sol.length][2];
            for (int i = 0; i < sol.length; i++) {
                solutions[i][0] = new ChessMove(sol[i]);
            }
        } else {
            getProblem();
        }

        if (savedInstanceState == null) {
            lMain.loadFEN(baseFEN);
        } else {
            lMain.loadFEN(savedInstanceState.getString("board"));
        }
        llBase.post(new Runnable() {
            @Override
            public void run() {
                updateSquares();
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("board", lMain.getFEN());
        outState.putString("set", problemSet);
        outState.putInt("id", currentId);
        outState.putBoolean("writer", canWriteLayouts);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        layoutStorage.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_game, menu);
        miMoveIndicator = menu.findItem(R.id.action_king);
        miHint = menu.findItem(R.id.action_hint);
        miWriteLayout = menu.findItem(R.id.action_write_layout);
        miLevelIndicator = menu.findItem(R.id.action_level);
        miReset = menu.findItem(R.id.action_reset);
        miFreeplay = menu.findItem(R.id.action_freeplay);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_hint:
                makeHint();
                return true;
            case R.id.action_reset:
                resetLayout();
                return true;
            case R.id.action_freeplay:
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra("fen", baseFEN);
                startActivity(intent);
                return true;
            case R.id.action_write_layout:
                writeLayout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void makeLayout() {
        updateScreenMetrics();

        llBase = new RelativeLayout(this);
        setContentView(llBase, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        tlChessboard = new TableLayout(this);
        tlChessboard.setOrientation(TableLayout.VERTICAL);
        tlChessboard.setBackgroundColor(Color.argb(255, 0, 255, 0));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                isLandscape ? LayoutParams.WRAP_CONTENT : LayoutParams.MATCH_PARENT,
                isLandscape ? LayoutParams.MATCH_PARENT : LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        llBase.addView(tlChessboard, params);

        for (int j = 8; j > 0; j--) {
            TableRow trRank = new TableRow(this);
            trRank.setOrientation(TableRow.HORIZONTAL);
            tlChessboard.addView(trRank);

            for (int i = 1; i <= 8; i++) {
                Button butSquare = new Button(this);
                butSquare.setTag(i * 10 + j);
                butSquare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        squarePress(v);
                    }
                });
                butSquare.setMinimumWidth(1);
                butSquare.setMinimumHeight(1);
                butSquare.setPadding(0, 0, 0, 0);
                butSquare.setTextColor(Color.BLACK);
                butSquares.add(butSquare);
                trRank.addView(butSquare);
            }
        }

        llBase.post(new Runnable() {
            @Override
            public void run() {
                int butSize = (isLandscape ? tlChessboard.getMeasuredHeight() :
                        tlChessboard.getMeasuredWidth()) / 8 + 1;
                for (Button b : butSquares) {
                    b.setWidth(butSize);
                    b.setHeight(butSize);
                    b.setTextSize(TypedValue.COMPLEX_UNIT_PX, butSize * 4 / 5);
                    int code = (Integer)b.getTag();
                    b.setBackgroundColor(((code / 10 + code % 10) % 2 == 0) ?
                            Color.argb(128, 0, 0, 0) :
                            Color.argb(128, 255, 255, 255));
                }
            }
        });

        //llBase.addView(tvTester, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
        //        LayoutParams.WRAP_CONTENT, 1));
    }

    LayoutStorage layoutStorage = new LayoutStorage(this);
    ProgressStorage progressStorage = new ProgressStorage(this);

    ChessLayout lMain = new ChessLayout();

    String baseFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 #1";
    String problemSet = "";
    int currentId = 0;

    ChessMove[][] solutions = {};

    final int STATE_WAIT = 0;
    final int STATE_SQUARE = 1;
    final int STATE_PROMOTION = 2;

    int state = 0;
    int sqx = 0;
    int sqy = 0;
    LinkedList<ChessMove> moveBuffer;

    private void getProblem() {
        // just to transform a current problem into a plain layout
        solutions = null;

        if (problemSet.equalsIgnoreCase("server")) {
            // we don't need any more problems, eh
            finish();
            return;
        }

        String[] problem = layoutStorage.getProblem(problemSet, currentId);
        if (problem == null) {
            greetAndFinish();
            return;
        }
        baseFEN = problem[0];
        String[] sol = problem[1].split(" ");
        solutions = new ChessMove[sol.length][2];
        for (int i = 0; i < sol.length; i++) {
            solutions[i][0] = new ChessMove(sol[i]);
        }

        resetLayout();
    }

    private void resetLayout() {
        lMain.loadFEN(baseFEN);
        moveReset();
        updateSquares();
    }

    private void makeHint() {
        if (solutions == null || solutions.length == 0)
            return;
        int i = (int) Math.floor(Math.random() * solutions.length);
        int cell = solutions[i][0].getCode(0) * 10 + solutions[i][0].getCode(1);
        highlightCell(cell);
    }

    private void updateSquares() {
        //TODO: implement fancier graphical representation (using SVG or PNG) ?
        for (Button b: butSquares) {
            int sq = (Integer) b.getTag();
            int fig = lMain.getBoard(sq / 10, sq % 10);
            if (fig == 0) {
                b.setText("");
                continue;
            }
            b.setText(ChessLayout.getUnicodeCharString(fig));
        }
        if (miMoveIndicator != null)
            miMoveIndicator.setTitle(ChessLayout.getUnicodeCharString(ChessLayout.fKing +
                           (lMain.getMove() ? ChessLayout.fBlack : 0)) +
                           (lMain.getMove() ? getString(R.string.chess_black) :
                                              getString(R.string.chess_white)));
        if (miHint != null)
            miHint.setVisible(solutions != null && solutions.length != 0);
        if (miLevelIndicator != null)
            miLevelIndicator.setTitle(currentId != 0 ? String.valueOf(currentId) :
                    getString(R.string.status_freeplay));
        if (miReset != null)
            miReset.setVisible(solutions == null || solutions.length == 0);
        if (miFreeplay != null)
            miFreeplay.setVisible(solutions != null && solutions.length != 0);
        if (miWriteLayout != null)
            miWriteLayout.setVisible(canWriteLayouts);
    }

    private void moveReset() {
        state = 0;
        sqx = 0;
        sqy = 0;
        moveBuffer = null;
        for (Button b: butSquares) {
            int code = (Integer) b.getTag();
            b.setBackgroundColor(((code / 10 + code % 10) % 2 == 0) ?
                    Color.argb(128, 0, 0, 0) :
                    Color.argb(128, 255, 255, 255));
        }
    }

    private void highlightMoveBuffer() {
        for (ChessMove cm : moveBuffer) {
            tlChessboard.findViewWithTag(cm.getCode(2) * 10 +
                    cm.getCode(3)).setBackgroundColor((
                    (cm.getCode(2) + cm.getCode(3)) % 2 == 0) ?
                    Color.argb(192, 0, 0, 0) :
                    Color.argb(192, 255, 255, 255));
        }
    }

    private void squarePress(View v) {
        if (lMain.isCheckmate(lMain.getMove())) {
            mistakeNudge();
            Toast.makeText(getApplicationContext(), getString(R.string.tip_game_over),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        int sq = (Integer) v.getTag();
        switch (state) {
            case STATE_WAIT:
                int fig = lMain.getBoard(sq / 10, sq % 10);
                if ( fig == 0 || fig / ChessLayout.fBlack !=
                        (lMain.getMove()? 1 : 0)) {
                    return;
                }
                moveBuffer = lMain.moveBuffer(sq / 10, sq % 10);
                if (moveBuffer.isEmpty()) {
                    moveBuffer = null;
                    return;
                }
                sqx = sq;
                highlightMoveBuffer();
                state = STATE_SQUARE;
                break;
            case STATE_SQUARE:
                //TODO: unite promotion and plain move codes
                if (sq % 10 == (lMain.getMove() ? 1 : 8) &&
                     lMain.getBoardFigure(sqx / 10, sqx % 10) == ChessLayout.fPawn) {
                    ChessMove desiredMove = new ChessMove(sqx / 10, sqx % 10, sq / 10, sq % 10);
                    if (!isInMoveBuffer(desiredMove)) {
                        moveReset();
                        updateSquares();
                        if (lMain.getBoard(sq / 10, sq % 10) != 0) {
                            state = STATE_WAIT;
                            squarePress(v);
                        }
                        return;
                    }
                    state = STATE_PROMOTION;
                    showPromotionScreen();
                    sqy = sq;
                    return;
                }
                ChessMove desiredMove = new ChessMove(sqx / 10, sqx % 10, sq / 10, sq % 10);
                if (!isInMoveBuffer(desiredMove)) {
                    moveReset();
                    updateSquares();
                    if (lMain.getBoard(sq / 10, sq % 10) != 0) {
                        state = STATE_WAIT;
                        squarePress(v);
                    }
                    return;
                }
                if (lMain.doMove(desiredMove)) {
                    warnMoveResult(desiredMove);
                } else warnKingIsStillAttacked(desiredMove);
                updateSquares();
                moveReset();
                break;
            case STATE_PROMOTION:
                int newfig = 0;
                switch (sq) {
                    case 35: newfig = ChessLayout.fKnight; break;
                    case 45: newfig = ChessLayout.fBishop; break;
                    case 55: newfig = ChessLayout.fRook; break;
                    case 65: newfig = ChessLayout.fQueen; break;
                }
                if (newfig != 0) {
                    desiredMove = new ChessMove(sqx / 10, sqx % 10, sqy / 10, sqy % 10);
                    if (lMain.doMove(desiredMove)) {
                        lMain.setBoard(sqy / 10, sqy % 10, newfig +
                                        (lMain.getMove() ? 0 : ChessLayout.fBlack));
                        ChessMove prettyMove = new ChessMove(sqx / 10, sqx % 10, sqy / 10, sqy % 10,
                                                             newfig);
                        warnMoveResult(prettyMove);
                    } else warnKingIsStillAttacked(desiredMove);
                }
                updateSquares();
                moveReset();
                break;
        }

    }

    private void warnKingIsStillAttacked(ChessMove cm) {
        Toast.makeText(getApplicationContext(), getString(R.string.tip_king_attacked),
                Toast.LENGTH_SHORT).show();
        highlightOwnKing();
    }

    private void warnMoveResult(ChessMove cm) {
        if (solutions == null || solutions.length == 0) { // just check for a check/mate
            if (lMain.isCheck(lMain.getMove())) {
                if (lMain.isCheckmate(lMain.getMove())) {
                    Toast.makeText(getApplicationContext(), getString(R.string.tip_checkmate),
                            Toast.LENGTH_SHORT).show();
                    explodeOwnKing();
                } else
                    Toast.makeText(getApplicationContext(), getString(R.string.tip_check),
                            Toast.LENGTH_SHORT).show();
            }
            return;
        }

        for (ChessMove[] solution : solutions) {
            if (solution[0].isEqual(cm)) {
                makeLevelUp();
                return;
            }
        }
        makeMistake();
    }

    private void showPromotionScreen() {
        ((TextView) tlChessboard.findViewWithTag(35)).setText(
                ChessLayout.getUnicodeCharString(ChessLayout.fKnight +
                        (lMain.getMove() ? ChessLayout.fBlack : 0)));
        ((TextView) tlChessboard.findViewWithTag(45)).setText(
                ChessLayout.getUnicodeCharString(ChessLayout.fBishop +
                        (lMain.getMove() ? ChessLayout.fBlack : 0)));
        ((TextView) tlChessboard.findViewWithTag(55)).setText(
                ChessLayout.getUnicodeCharString(ChessLayout.fRook +
                        (lMain.getMove() ? ChessLayout.fBlack : 0)));
        ((TextView) tlChessboard.findViewWithTag(65)).setText(
                ChessLayout.getUnicodeCharString(ChessLayout.fQueen +
                        (lMain.getMove() ? ChessLayout.fBlack : 0)));
        tlChessboard.findViewWithTag(35).setBackgroundColor(
                Color.argb(128, 128, 128, 128));
        tlChessboard.findViewWithTag(45).setBackgroundColor(
                Color.argb(128, 128, 128, 128));
        tlChessboard.findViewWithTag(55).setBackgroundColor(
                Color.argb(128, 128, 128, 128));
        tlChessboard.findViewWithTag(65).setBackgroundColor(
                Color.argb(128, 128, 128, 128));
    }

    private boolean isInMoveBuffer(ChessMove desm) {
        for (ChessMove cm: moveBuffer)
            if (desm.isEqual(cm))
                return true;
        return false;
    }

    private void highlightCell(int tag) {
        for (Button btn : butSquares) {
            int code = (Integer) btn.getTag();
            if (code == tag) {
                highlightView(btn);
                break;
            }
        }
    }

    private void highlightOwnKing() {
        for (Button btn : butSquares) {
            int code = (Integer) btn.getTag();
            int ownColor = ((code / 10 + code % 10) % 2 == 0) ? Color.argb(128, 0, 0, 0) :
                    Color.argb(128, 255, 255, 255);
            if (lMain.getBoard(code / 10, code % 10) == ChessLayout.fKing +
                    (lMain.getMove() ? ChessLayout.fBlack : 0)) {
                highlightView(btn);
                break;
            }
        }
    }

    private void highlightView(View v) {
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(
                ObjectAnimator.ofFloat(v, "alpha", 1f, 0.1f, 1f, 0.1f, 1f));
        animSet.setDuration(1000);
        animSet.start();
    }

    private void explodeOwnKing() {
        for (Button btn : butSquares) {
            int code = (Integer) btn.getTag();
            int ownColor = ((ColorDrawable)btn.getBackground()).getColor();
            if (lMain.getBoard(code / 10, code % 10) == ChessLayout.fKing +
                    (lMain.getMove() ? ChessLayout.fBlack : 0)) {
                AnimatorSet animSet = new AnimatorSet();
                animSet.playTogether(
                    ObjectAnimator.ofInt(btn, "backgroundColor", ownColor, Color.RED, Color.YELLOW),
                    ObjectAnimator.ofInt(btn, "textColor", Color.BLACK, Color.WHITE, Color.BLACK));
                animSet.setDuration(500);
                animSet.start();
                break;
            }
        }
    }

    private void makeLevelUp() {
        LinkedList<Animator> animList = new LinkedList<>();
        for (Button btn : butSquares)
            animList.add(ObjectAnimator.ofFloat(btn, "alpha", 1f, 0.1f, 1f));
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animList);
        animSet.setDuration(500);
        animSet.start();
        delayLevelUp();
    }

    private void delayLevelUp() {
        llBase.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentId > progressStorage.getLastCompleted(problemSet))
                    progressStorage.writeLastCompleted(problemSet, currentId);
                currentId += 1;
                getProblem();
            }
        }, 700);
    }


    private void mistakeNudge() {
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(
                ObjectAnimator.ofFloat(tlChessboard, "alpha", 1, 0.5f, 1));
        animSet.setDuration(1000);
        animSet.start();
    }

    private void makeMistake() {
        mistakeNudge();
        delayReset();
    }

    private void delayReset() {
        llBase.postDelayed(new Runnable() {
            @Override
            public void run() {
                resetLayout();
            }
        }, 600);
    }

    private void greetAndFinish() {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(getString(R.string.dialog_greeting));
        dlgAlert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                CloseActivity();
            }
        });
        dlgAlert.show();
    }

    private void CloseActivity() {
        this.finish();
    }

    CustomLayoutStorage customLayoutStorage = new CustomLayoutStorage(this);

    void writeLayout() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.dialog_name_entry));
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        alert.setView(input);

        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString().trim();
                if (value.length() == 0) {
                    Toast.makeText(GameActivity.this, getString(R.string.error_bad_name),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!customLayoutStorage.write(value, lMain.getFEN())) {
                    Toast.makeText(GameActivity.this,
                            getString(R.string.error_name_exists).replace("%s", value),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(GameActivity.this, getString(R.string.tip_saved)
                        .replace("%s", value), Toast.LENGTH_SHORT).show();
            }
        });
        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        Dialog dialog = alert.show();
        // dirty hack for styling the divider
        dialog.findViewById(dialog.getContext().getResources().
                getIdentifier("android:id/titleDivider", null, null)).
                setBackgroundColor(getResources().getColor(R.color.divider_green));
    }
}

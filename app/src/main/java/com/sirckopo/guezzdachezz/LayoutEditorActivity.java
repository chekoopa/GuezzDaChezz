package com.sirckopo.guezzdachezz;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.LinkedList;

public class LayoutEditorActivity extends ActionBarActivity {

    ChessLayout lMain = new ChessLayout();
    String layoutName = "";
    int currentFigure = ChessLayout.fEmpty;

    boolean waitingForSelect = false;

    CustomLayoutStorage customLayoutStorage = new CustomLayoutStorage(this);

    RelativeLayout llBase;
    TableLayout tlChessboard;
    LinkedList<Button> butSquares = new LinkedList<>();

    MenuItem miFigureIndicator;

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

        Intent intent = getIntent();
        if (savedInstanceState != null) {
            layoutName = savedInstanceState.getString("name");
            currentFigure = savedInstanceState.getInt("figure");
            lMain.loadFEN(savedInstanceState.getString("fen"));
        } else {
            layoutName = intent.getStringExtra("name");
            lMain.loadFEN(customLayoutStorage.read(layoutName));
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
        outState.putString("fen", lMain.getFEN());
        outState.putString("name", layoutName);
        outState.putInt("figure", currentFigure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout_editor, menu);
        miFigureIndicator = menu.findItem(R.id.action_select_figure);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_figure:
                showFigureSelector();
                break;
            case R.id.action_board_parameters:
                showParameters();
                break;
            case R.id.action_board_data:
                showDataOperations();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
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

    }

    private void updateSquares() {
        for (Button b: butSquares) {
            int sq = (Integer) b.getTag();
            b.setBackgroundColor(((sq / 10 + sq % 10) % 2 == 0) ?
                    Color.argb(128, 0, 0, 0) :
                    Color.argb(128, 255, 255, 255));
            int fig = lMain.getBoard(sq / 10, sq % 10);
            if (fig == 0) {
                b.setText("");
                continue;
            }
            b.setText(ChessLayout.getUnicodeCharString(fig));

            miFigureIndicator.setTitle((currentFigure == 0 ? "X" :
                    (currentFigure / ChessLayout.fBlack == 1 ?
                            ChessLayout.tFiguresUnicodeB : ChessLayout.tFiguresUnicodeW)
                            .charAt(currentFigure % ChessLayout.fBlack)).toString());
        }
    }

    private void showFigureSelector() {
        // TODO: display figures
        waitingForSelect = true;
    }

    private void squarePress(View v) {
        if (waitingForSelect) {
            // TODO: selection code
            updateSquares();
            waitingForSelect = false;
        } else {
            // TODO: editor code
        }
    }

    private static final int MENU_MOVE = 1;
    private static final int MENU_DEPTH = 2;
    private static final int MENU_COOP = 3;

    public void showParameters() {
        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.action_board_parameters));
        Menu menu = popupMenu.getMenu();
        menu.add(Menu.NONE, MENU_MOVE, Menu.NONE, "White move");
        menu.add(Menu.NONE, MENU_DEPTH, Menu.NONE, "No mate");
        menu.add(Menu.NONE, MENU_COOP, Menu.NONE, "Simple mate");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return false;
            }
        });
        popupMenu.show();
    }

    private static final int MENU_SAVE = 1;
    private static final int MENU_RELOAD = 2;
    private static final int MENU_IMPORT = 3;
    private static final int MENU_EXPORT = 4;

    public void showDataOperations() {
        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.action_board_parameters));
        Menu menu = popupMenu.getMenu();
        menu.add(Menu.NONE, MENU_SAVE, Menu.NONE, "Save");
        menu.add(Menu.NONE, MENU_RELOAD, Menu.NONE, "Reload");
        menu.add(Menu.NONE, MENU_IMPORT, Menu.NONE, "Import FEN");
        menu.add(Menu.NONE, MENU_EXPORT, Menu.NONE, "Export FEN");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return false;
            }
        });
        popupMenu.show();
    }
}

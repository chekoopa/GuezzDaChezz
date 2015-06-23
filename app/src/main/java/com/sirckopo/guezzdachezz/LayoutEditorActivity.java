package com.sirckopo.guezzdachezz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.util.LinkedList;

public class LayoutEditorActivity extends ActionBarActivity {

    ChessLayout lMain = new ChessLayout();
    String layoutName = "";
    int currentFigure = ChessLayout.fEmpty;

    boolean waitingForSelect = false;
    boolean somethingChanged = false;

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
    public void onBackPressed() {
        if (somethingChanged) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage(getString(R.string.dialog_save_exit).replace("%s", layoutName));
            alert.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (saveLayout())
                        finish();
                }
            });
            alert.setNeutralButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    finish();
                }
            });
            alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            alert.show();
        } else {
            finish();
        }
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
        llBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideFigureSelector();
            }
        });
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
                    int code = (Integer) b.getTag();
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
        }
        miFigureIndicator.setTitle(currentFigure == 0 ? "X" :
                (currentFigure / ChessLayout.fBlack == 1 ? "B" : "W") + "-"
                        + ChessLayout.tFigures.charAt(currentFigure % ChessLayout.fBlack));
    }

    private void showFigureSelector() {
        if (waitingForSelect) {
            waitingForSelect = false;
            updateSquares();
            return;
        }
        for (int tag = 15; tag <= 85; tag += 10) {
            Button btn = (Button) tlChessboard.findViewWithTag(tag);
            btn.setBackgroundColor(Color.argb(128, 128, 128, 128));
            if (tag == 15 || tag == 85) {
                btn.setText("X");
            } else {
                btn.setText(String.valueOf(ChessLayout.tFiguresUnicodeW.charAt(tag / 10 - 1)));
            }
        }
        for (int tag = 14; tag <= 84; tag += 10) {
            Button btn = (Button) tlChessboard.findViewWithTag(tag);
            btn.setBackgroundColor(Color.argb(128, 128, 128, 128));
            if (tag == 14 || tag == 84) {
                btn.setText("X");
            } else {
                btn.setText(String.valueOf(ChessLayout.tFiguresUnicodeB.charAt(tag / 10 - 1)));
            }
        }
        waitingForSelect = true;
    }

    private void hideFigureSelector() {
        if (waitingForSelect) {
            waitingForSelect = false;
            updateSquares();
        }
    }

    private void squarePress(View v) {
        int tag = (Integer) v.getTag();
        if (waitingForSelect) {
            if (tag % 10 == 4 || tag % 10 == 5) {
                if (tag / 10 == 1 || tag / 10 == 8) {
                    currentFigure = ChessLayout.fEmpty;
                } else {
                    // don't you mind on this little hack?
                    currentFigure = (tag / 10 - 1) + (tag % 10 == 4 ? ChessLayout.fBlack : 0);
                }
            }
            waitingForSelect = false;
        } else {
            if (currentFigure % ChessLayout.fBlack == ChessLayout.fKing) {
                boolean found = false;
                for (int x = 1; x <= ChessLayout.size && !found; x++)
                    for (int y = 1; y <= ChessLayout.size && !found; y++)
                        if (lMain.getBoard(x, y) == currentFigure) {
                            lMain.setBoard(x, y, ChessLayout.fEmpty);
                            found = true;
                        }
            } else if (currentFigure % ChessLayout.fBlack == ChessLayout.fPawn) {
                if (tag % 10 == 1 || tag % 10 == 8) {
                    return;
                }
            }
            lMain.setBoard(tag / 10, tag % 10, currentFigure);
            somethingChanged = true;
        }
        updateSquares();
    }

    private static final int MENU_MOVE = 1;
    private static final int MENU_DEPTH = 2;
    private static final int MENU_COOP = 3;

    public void showParameters() {
        hideFigureSelector();
        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.action_board_parameters));
        Menu menu = popupMenu.getMenu();
        menu.add(Menu.NONE, MENU_MOVE, Menu.NONE, lMain.getMove() ?
                getString(R.string.chess_move_black) : getString(R.string.chess_move_white));
        menu.add(Menu.NONE, MENU_DEPTH, Menu.NONE, lMain.getDepth() == 0 ?
                getString(R.string.chess_no_mate) : getString(R.string.chess_depth_pre) + " " +
                String.valueOf(lMain.getDepth()));
        if (lMain.getDepth() > 0)
            menu.add(Menu.NONE, MENU_COOP, Menu.NONE, lMain.getCoop() ?
                    getString(R.string.chess_mate_help) : getString(R.string.chess_mate_direct));
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case MENU_MOVE:
                        lMain.setMove(!lMain.getMove());
                        somethingChanged = true;
                        break;
                    case MENU_DEPTH:
                        changeDepth();
                        break;
                    case MENU_COOP:
                        lMain.setCoop(!lMain.getCoop());
                        somethingChanged = true;
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private static final int MENU_RESET = 1;
    private static final int MENU_SAVE = 2;
    private static final int MENU_RELOAD = 3;
    private static final int MENU_IMPORT = 4;
    private static final int MENU_EXPORT = 5;

    public void showDataOperations() {
        hideFigureSelector();
        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.action_board_parameters));
        Menu menu = popupMenu.getMenu();
        menu.add(Menu.NONE, MENU_RESET, Menu.NONE, getString(R.string.action_clear));
        menu.add(Menu.NONE, MENU_SAVE, Menu.NONE, getString(R.string.action_save));
        menu.add(Menu.NONE, MENU_RELOAD, Menu.NONE, getString(R.string.action_revert));
        menu.add(Menu.NONE, MENU_IMPORT, Menu.NONE, getString(R.string.action_import));
        menu.add(Menu.NONE, MENU_EXPORT, Menu.NONE, getString(R.string.action_export));
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case MENU_RESET:
                        lMain.reset();
                        somethingChanged = true;
                        updateSquares();
                        break;
                    case MENU_SAVE:
                        saveLayout();
                        break;
                    case MENU_RELOAD:
                        lMain.loadFEN(customLayoutStorage.read(layoutName));
                        somethingChanged = false;
                        updateSquares();
                        break;
                    case MENU_IMPORT:
                        importFEN();
                        break;
                    case MENU_EXPORT:
                        exportFEN();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void changeDepth() {
        String[] itemsArray = {getString(R.string.chess_no_mate),
                getString(R.string.chess_depth_pre) + " 1",
                getString(R.string.chess_depth_pre) + " 2",
                getString(R.string.chess_depth_pre) + " 3"};

        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setItems(itemsArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                lMain.setDepth(which);
                somethingChanged = true;
                dialog.dismiss();
            }
        });
        dlgAlert.show();
    }

    private void importFEN() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.dialog_import_fen));
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        alert.setView(input);

        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String fen = input.getText().toString().trim();
                ChessLayout cl = new ChessLayout();
                if (!cl.loadFEN(fen)) {
                    Toast.makeText(LayoutEditorActivity.this, getString(R.string.error_bad_fen),
                            Toast.LENGTH_SHORT).show();
                    return;
                } else if (!cl.isLegit()) {
                    Toast.makeText(LayoutEditorActivity.this, getString(R.string.error_bad_layout),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                lMain.loadFEN(fen);
                updateSquares();
                somethingChanged = true;
                Toast.makeText(LayoutEditorActivity.this, getString(R.string.tip_import),
                        Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        Dialog dialog = alert.show();
        // dirty hack for styling the divider
        dialog.findViewById(dialog.getContext().getResources().
                getIdentifier("android:id/titleDivider", null, null)).
                setBackgroundColor(getResources().getColor(R.color.divider_green));
    }

    private void exportFEN() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.dialog_export_fen));
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(lMain.getFEN());
        input.selectAll();
        alert.setView(input);

        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
        Dialog dialog = alert.show();
        // dirty hack for styling the divider
        dialog.findViewById(dialog.getContext().getResources().
                getIdentifier("android:id/titleDivider", null, null)).
                setBackgroundColor(getResources().getColor(R.color.divider_green));
    }

    private boolean saveLayout() {
        if (!lMain.isLegit()) {
            Toast.makeText(this, getString(R.string.error_bad_layout), Toast.LENGTH_SHORT).show();
            return false;
        }
        customLayoutStorage.rewrite(layoutName, lMain.getFEN());
        somethingChanged = false;
        Toast.makeText(LayoutEditorActivity.this, getString(R.string.tip_saved)
                .replace("%s", layoutName), Toast.LENGTH_SHORT).show();
        return true;
    }

}

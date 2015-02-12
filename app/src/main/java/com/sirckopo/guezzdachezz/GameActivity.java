package com.sirckopo.guezzdachezz;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.util.LinkedList;


public class GameActivity extends ActionBarActivity {

    LinearLayout llBase;
    LinearLayout llBar;
    TableLayout tlChessboard;
    LinkedList<Button> butSquares = new LinkedList<>();
    Button butReset;
    Button butHint;

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
        //setContentView(R.layout.activity_game);

        updateScreenMetrics();

        //Toast.makeText(GameActivity.this, screenWidth + " " + screenHeight + " " + isLandscape,
        //        Toast.LENGTH_LONG).show();

        llBase = new LinearLayout(this);
        llBase.setOrientation(isLandscape ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        setContentView(llBase, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        LinearLayout llTest = new LinearLayout(this);
        llTest.setOrientation(!isLandscape ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        //llTest.setBackgroundColor(Color.argb(255, 255, 0, 0));
        llBase.addView(llTest);

        tlChessboard = new TableLayout(this);
        tlChessboard.setOrientation(TableLayout.VERTICAL);
        tlChessboard.setBackgroundColor(Color.argb(255, 0, 255, 0));
        llBase.addView(tlChessboard, new LayoutParams(
                isLandscape ? LayoutParams.WRAP_CONTENT : LayoutParams.MATCH_PARENT,
                isLandscape ? LayoutParams.MATCH_PARENT : LayoutParams.WRAP_CONTENT));

        for (int j = 8; j > 0; j--) {
            TableRow trRank = new TableRow(this);
            trRank.setOrientation(TableRow.HORIZONTAL);
            tlChessboard.addView(trRank);
            //tlChessboard.addView(trRank, new TableLayout.LayoutParams(
            //        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));

            for (int i = 1; i <= 8; i++) {
                Button butSquare = new Button(this);
                butSquare.setText("");
                butSquare.setTag(i * 10 + j);
                butSquare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Button b = (Button) v;
                        b.setText(v.getTag().toString());
                    }
                });
                butSquare.setMinimumWidth(0);
                butSquare.setMinimumHeight(0);
                butSquare.setPadding(0, 0, 0, 0);
                butSquares.add(butSquare);
                trRank.addView(butSquare);
                //trRank.addView(butSquare, new TableRow.LayoutParams(
                //        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));

            }
        }


        llBar = new LinearLayout(this);
        llBar.setOrientation(!isLandscape ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        //llBar.setBackgroundColor(Color.argb(255, 0, 0, 255));
        llBase.addView(llBar);

        butReset = new Button(this);
        butReset.setText("Reset");
        butReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Button b : butSquares) {
                    b.setText("");
                }
            }
        });
        llBar.addView(butReset, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, 1));

        butHint = new Button(this);
        butHint.setText("Hint");
        butHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(GameActivity.this, screenWidth + " " + screenHeight + "\n" +
                                llBase.getMeasuredWidth() + " " + llBase.getMeasuredHeight() + "\n" +
                                tlChessboard.getMeasuredWidth() + " " + tlChessboard.getMeasuredHeight(),
                        Toast.LENGTH_LONG).show();
            }
        });
        llBar.addView(butHint, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, 1));

        llBase.post(new Runnable() {
            @Override
            public void run() {
                int butSize = (isLandscape ? tlChessboard.getMeasuredHeight() :
                        tlChessboard.getMeasuredWidth()) / 8;
                for (Button b : butSquares) {
                    b.setWidth(butSize);
                    b.setHeight(butSize);
                    int code = (Integer)b.getTag();
                    b.setBackgroundColor(((code / 10 + code % 10) % 2 == 0) ?
                            Color.argb(128, 0, 0, 0) :
                            Color.argb(128, 255, 255, 255));
                }
            }
        });
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
}

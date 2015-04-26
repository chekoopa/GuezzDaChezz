package com.sirckopo.guezzdachezz;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    LayoutStorage layoutStorage = new LayoutStorage(this);

    LinearLayout lBase;
    LinearLayout lButtons;

    Button butFreeplay;
    Button butServerPlay;
    Button butOneMove1;
    Button butTwoMove1;
    Button butAbout;

    ImageView iwLogo;

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

        try {
            layoutStorage.createDataBase();
            layoutStorage.overrideDataBase();
            layoutStorage.openDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        layoutStorage.close();
    }

    private void makeLayout() {
        updateScreenMetrics();

        lBase = new LinearLayout(this);
        lBase.setOrientation(isLandscape ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        setContentView(lBase);

        iwLogo = new ImageView(this);
        iwLogo.setImageResource(R.drawable.figure_wn);
        iwLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        lBase.addView(iwLogo, new LinearLayout.LayoutParams(
                isLandscape ? screenWidth / 2 : screenWidth,
                isLandscape ? screenHeight    : screenHeight / 2));

        lButtons = new LinearLayout(this);
        lButtons.setOrientation(LinearLayout.VERTICAL);
        lBase.addView(lButtons, new LinearLayout.LayoutParams(
                isLandscape ? LinearLayout.LayoutParams.WRAP_CONTENT :
                        LinearLayout.LayoutParams.MATCH_PARENT,
                isLandscape ? LinearLayout.LayoutParams.MATCH_PARENT :
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        butFreeplay = new Button(this);
        butFreeplay.setBackgroundResource(R.drawable.greenhitechbutton);
        butFreeplay.setText(getString(R.string.action_freeplay));
        butFreeplay.setId(R.id.butGame);
        butFreeplay.setOnClickListener(sendToGame);
        lButtons.addView(butFreeplay, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1));

        butOneMove1 = new Button(this);
        butOneMove1.setBackgroundResource(R.drawable.greenhitechbutton);
        butOneMove1.setText(getString(R.string.action_onemove));
        butOneMove1.setId(R.id.butOneMove);
        butOneMove1.setOnClickListener(sendToGame);
        lButtons.addView(butOneMove1, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));

        butTwoMove1 = new Button(this);
        butTwoMove1.setBackgroundResource(R.drawable.greenhitechbutton);
        butTwoMove1.setText(getString(R.string.action_twomove));
        butTwoMove1.setId(R.id.butTwoMove);
        butTwoMove1.setOnClickListener(sendToGame);
        lButtons.addView(butTwoMove1, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));

        butServerPlay = new Button(this);
        butServerPlay.setBackgroundResource(R.drawable.greenhitechbutton);
        butServerPlay.setText(getString(R.string.action_server));
        butServerPlay.setId(R.id.butServerPlay);
        butServerPlay.setOnClickListener(sendToGame);
        lButtons.addView(butServerPlay, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));

        butAbout = new Button(this);
        butAbout.setBackgroundResource(R.drawable.greenhitechbutton);
        butAbout.setText(getString(R.string.action_about));
        butAbout.setId(R.id.butAbout);
        butAbout.setOnClickListener(sendToGame);
        lButtons.addView(butAbout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));

    }

    View.OnClickListener sendToGame = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendToGame(v);
        }
    };

    public void sendToGame(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.butGame:
                intent = new Intent(this, GameActivity.class);
                intent.putExtra("id", 0);
                break;
            case R.id.butOneMove:
                showLevelSelect("onemove_1");
                return;
            case R.id.butTwoMove:
                showLevelSelect("twomove_1");
                return;
            case R.id.butServerPlay:
                intent = new Intent(this, ServerConnectionActivity.class);
                break;
            case R.id.butAbout:
                intent = new Intent(this, AboutActivity.class);
                break;
        }
        startActivity(intent);
    }

    public void showLevelSelect(String set) {
        FragmentManager fragmentManager = getFragmentManager();
        LevelSelectFragment newFragment = new LevelSelectFragment();
        newFragment.problemSet = set;
        newFragment.show(fragmentManager, "dialog");
    }
}

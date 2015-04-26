package com.sirckopo.guezzdachezz;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;


public class AboutActivity extends ActionBarActivity {

    ProgressStorage progressStorage = new ProgressStorage(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    int presses = 0;

    public void explode(View v) {
        Button btn = (Button) findViewById(R.id.btnReset);
        switch(presses) {
            case 0:
                btn.setText(getString(R.string.action_reset_progress_confirm));
                presses++;
                break;
            case 1:
                progressStorage.clearData();
                btn.setText(getString(R.string.action_reset_progress_done));
                AnimatorSet animSet = new AnimatorSet();
                animSet.playTogether(
                        ObjectAnimator.ofInt(btn, "backgroundColor", Color.YELLOW, Color.WHITE),
                        ObjectAnimator.ofFloat(btn, "alpha", 1, 0.1f),
                        ObjectAnimator.ofInt(btn, "textColor", Color.RED, Color.WHITE));
                animSet.setDuration(1000);
                animSet.start();
                presses++;
                break;
        }
    }
}

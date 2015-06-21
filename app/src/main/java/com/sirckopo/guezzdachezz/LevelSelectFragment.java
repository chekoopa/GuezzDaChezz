package com.sirckopo.guezzdachezz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

public class LevelSelectFragment extends DialogFragment {

    LayoutStorage layoutStorage;
    ProgressStorage progressStorage;

    SeekBar levelSelector;
    TextView statusText;

    public String problemSet = "";
    int problemId = 0;
    int lastCompleted = 0;
    int maxProblems = 0;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_levelselect, null);

        builder.setView(layout)
                .setTitle(getString(R.string.dialog_select_board))
                .setPositiveButton(R.string.go, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(LevelSelectFragment.this.getDialog().getContext(),
                                GameActivity.class);
                        intent.putExtra("set", problemSet);
                        intent.putExtra("id", problemId);
                        startActivity(intent);
                        LevelSelectFragment.this.getDialog().dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LevelSelectFragment.this.getDialog().cancel();
                    }
                });

        progressStorage = new ProgressStorage(layout.getContext());
        layoutStorage = new LayoutStorage(layout.getContext());
        try {
            layoutStorage.createDataBase();
            layoutStorage.openDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        if (savedInstanceState != null)
            problemSet = savedInstanceState.getString("set");

        maxProblems = layoutStorage.getSize(problemSet);
        if (maxProblems == 0)
            LevelSelectFragment.this.getDialog().dismiss();

        lastCompleted = progressStorage.getLastCompleted(problemSet);
        if (lastCompleted == 0) {
            layout.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(LevelSelectFragment.this.getDialog().getContext(),
                            GameActivity.class);
                    intent.putExtra("set", problemSet);
                    intent.putExtra("id", 1);
                    startActivity(intent);
                    LevelSelectFragment.this.getDialog().dismiss();
                }
            });
        }

        statusText = (TextView) layout.findViewById(R.id.statusText);
        levelSelector = (SeekBar) layout.findViewById(R.id.levelSelector);
        levelSelector.setOnSeekBarChangeListener(seekBarListener);
        levelSelector.setMax(Math.min(lastCompleted + 1, maxProblems) - 1);
        levelSelector.setProgress(levelSelector.getMax());

        Dialog dialog = builder.show();

        // dirty hack for styling the divider
        dialog.findViewById(dialog.getContext().getResources().
                getIdentifier("android:id/titleDivider", null, null)).
                setBackgroundColor(Color.rgb(0, 196, 0));

        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("set", problemSet);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            problemId = levelSelector.getProgress() + 1;
            statusText.setText(String.valueOf(problemId) + " / " +
                    String.valueOf(Math.min(lastCompleted + 1, maxProblems)) + " " +
                    getString(R.string.divider_of) + " " + String.valueOf(maxProblems));
        }
    };

}

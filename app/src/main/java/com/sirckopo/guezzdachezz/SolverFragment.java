package com.sirckopo.guezzdachezz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.widget.TextView;

public class SolverFragment extends DialogFragment {

    public String fen = "";

    private TextView tvStatus;
    boolean done = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        waitText = getString(R.string.tip_solve_wait);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        tvStatus = new TextView(this.getActivity());
        tvStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tvStatus.setPadding(20, 20, 20, 20);

        builder.setView(tvStatus)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        if (savedInstanceState != null) {
            String status = savedInstanceState.getString("status");
            if (!status.equals("waiting")) {
                tvStatus.setText(status);
                done = true;
            } else {
                ChessSolver.updateRunnables(onFinish, onUpdate);
                onUpdate.run();
            }
        } else {
            ChessSolver.solve(fen, onFinish, onUpdate);
            tvStatus.setText(getText(R.string.tip_solve_wait));
        }

        return builder.create();
    }

    Runnable onFinish = new Runnable() {
        @Override
        public void run() {
            onFinish();
        }
    };

    void onFinish() {
        String answer = ChessSolver.get();
        if (answer.startsWith(ChessSolver.errorSignal)) {
            // TODO: more specific information
            answer = getString(R.string.error_solve)  + " (" + answer + ")";
        } else if (answer.length() == 0) {
            answer = getString(R.string.tip_no_solutions);
        }
        tvStatus.setText(answer);
        done = true;
    }

    Runnable onUpdate = new Runnable() {
        @Override
        public void run() {
            onUpdate();
        }
    };

    private String waitText = ""; // getString(R.string.tip_solve_wait)

    void onUpdate() {
        tvStatus.setText(waitText + " " +
                ChessSolver.getSolveStatus());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("status", (done ? tvStatus.getText().toString() : "waiting"));
        outState.putString("fen", fen);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        ChessSolver.cancel();
    }
}

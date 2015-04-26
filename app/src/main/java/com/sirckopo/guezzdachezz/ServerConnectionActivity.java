package com.sirckopo.guezzdachezz;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class ServerConnectionActivity extends ActionBarActivity {

    Button butPing;
    Button butProblem;
    TextView lblStatus;
    TextView txtIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_connection);

        butPing = (Button) findViewById(R.id.btnPing);
        butProblem = (Button) findViewById(R.id.btnGo);
        lblStatus = (TextView) findViewById(R.id.lblTip);
        txtIP = (TextView) findViewById(R.id.txtIP);
    }

    public void processProblem() {
        String answer = JSONOperator.get();
        if (answer.length() == 0) {
            answer = getString(R.string.error_connection);
        } else {
            try {
                JSONObject json = new JSONObject(answer);
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra("set", "server");
                intent.putExtra("fen", json.getString("fen"));
                intent.putExtra("solutions", json.getString("solutions"));
                intent.putExtra("answers", json.getString("answers"));
                answer = getString(R.string.tip_problem_success);
                startActivity(intent);
            } catch (JSONException e) {
                answer = getString(R.string.error_connection);
                e.printStackTrace();
            }
        }
        lblStatus.setText(answer);
        butPing.setEnabled(true);
        butProblem.setEnabled(true);
    }

    public void processPing() {
        String answer = JSONOperator.get();
        try {
            JSONObject json = new JSONObject(answer);
            if (json.getString("answer").equalsIgnoreCase("pong")) {
                answer = getString(R.string.tip_ping_success);
            } else {
                answer = getString(R.string.error_connection);
            }
        } catch (JSONException e) {
            answer = getString(R.string.error_connection);
            e.printStackTrace();
        }
        lblStatus.setText(answer);
        butPing.setEnabled(true);
        butProblem.setEnabled(true);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void getAProblem(View v) {
        sendJSON("{\"type\": \"random\"}", new Runnable() {
            @Override
            public void run() {
                processProblem();
            }
        });
    }

    public void makeAPing(View v) {
        sendJSON("{\"type\": \"ping\"}", new Runnable() {
            @Override
            public void run() {
                processPing();
            }
        });
    }

    private void sendJSON(String line, Runnable finish) {
        if (!JSONOperator.isTaskFree()) return;
        if (!isNetworkAvailable())
            lblStatus.setText(getString(R.string.error_connectivity));
        butPing.setEnabled(false);
        butProblem.setEnabled(false);
        lblStatus.setText(getString(R.string.tip_connection_wait));
        JSONOperator.send(line, txtIP.getText().toString(), 13373, finish);
    }

}

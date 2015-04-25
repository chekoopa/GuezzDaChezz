package com.sirckopo.guezzdachezz;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class ServerConnectionActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_connection);

        //TODO: hide a JSON exchange into an AsyncTask
        // OMG, A HERESY!
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void getAProblem(View v) {
        TextView tw = (TextView) findViewById(R.id.txtIP);
        String answer;
        if (isNetworkAvailable()) {
            answer = interchangeJSON("random", tw.getText().toString());
            if (answer == "") {
                answer = "Something is wrong with the connection.";
            } else {
                try {
                    JSONObject json = new JSONObject(answer);
                    Intent intent = new Intent(this, GameActivity.class);
                    intent.putExtra("set", "server");
                    intent.putExtra("fen", json.getString("fen"));
                    intent.putExtra("solutions", json.getString("solutions"));
                    intent.putExtra("answers", json.getString("answers"));
                    answer = "Got a problem, main screen turn on";
                    startActivity(intent);

                } catch (JSONException e) {
                    answer = "Something is wrong with the connection.";
                    e.printStackTrace();
                }
            }
        } else {
            answer = "Uh-uh-uh, you'd better check your network";
        }
        TextView lbl = (TextView) findViewById(R.id.lblTip);
        lbl.setText(answer);
    }

    public void makeAPing(View v) {
        TextView tw = (TextView) findViewById(R.id.txtIP);
        String answer;
        if (isNetworkAvailable()) {
            answer = interchangeJSON("ping", tw.getText().toString());
            try {
                JSONObject json = new JSONObject(answer);
                if (json.getString("answer").equalsIgnoreCase("pong")) {
                    answer = "Pong! You can proceed with receiving problems.";
                } else {
                    answer = "Something is wrong with the connection.";
                }
            } catch (JSONException e) {
                answer = "Something is wrong with the connection.";
                e.printStackTrace();
            }
        } else {
            answer = "Uh-uh-uh, you'd better check your network";
        }
        TextView lbl = (TextView) findViewById(R.id.lblTip);
        lbl.setText(answer);
    }


    private String interchangeJSON(String type, String ipaddr) {
        String string = "{\"type\":\"" + type + "\"}";
        DataInputStream is;
        DataOutputStream os;
        String result = "";

        try {
            Socket socket = new Socket(InetAddress.getByName(ipaddr), 13373);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            PrintWriter pw = new PrintWriter(os);
            pw.println(string);
            pw.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            result = in.readLine();
            JSONObject json = new JSONObject(result);
            if(!json.has("answer")) {
                result = "";
            }
            is.close();
            os.close();

        } catch (IOException e) {
            result = "";
            e.printStackTrace();
        } catch (JSONException e) {
            result = "";
            e.printStackTrace();
        }
        return result;
    }
}

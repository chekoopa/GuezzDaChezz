package com.sirckopo.guezzdachezz;

import android.os.AsyncTask;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JSONOperator {

    static class JSONTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String string = params[0];
            String ipaddr = params[1];
            DataInputStream is;
            DataOutputStream os;
            String result;
            try {
                Socket socket = new Socket(InetAddress.getByName(ipaddr), port);
                socket.setSoTimeout(7000);
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

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (onFinish != null) {
                onFinish.run();
            }
        }
    }

    private static JSONTask task;
    private static int port = 13373;
    private static Runnable onFinish;

    public static void send(String line, String ipaddr, int newPort) {
        task = new JSONTask();
        port = newPort;
        onFinish = null;
        task.execute(line, ipaddr);
    }

    public static void cancel() {
        if (isTaskFree()) return;
        task.cancel(true);
    }

    public static void send(String line, String ipaddr, int newPort, Runnable finish) {
        task = new JSONTask();
        port = newPort;
        onFinish = finish;
        task.execute(line, ipaddr);
    }

    public static boolean isTaskFree() {
        if (task == null) return true;
        String status = task.getStatus().toString();
        return !status.equalsIgnoreCase("running");
    }

    public static String get() {
        if (task == null) return "";
        String answer;
        try {
            answer = task.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            answer = "";
        } catch (ExecutionException e) {
            e.printStackTrace();
            answer = "";
        } catch (TimeoutException e) {
            e.printStackTrace();
            answer = "";
        }
        return answer;
    }

}

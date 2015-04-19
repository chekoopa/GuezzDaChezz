package com.sirckopo.guezzdachezz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    LayoutStorage layoutStorage = new LayoutStorage(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            layoutStorage.createDataBase();
            layoutStorage.overrideDataBase();
            layoutStorage.openDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void sendToGame(View v) {
        Intent intent = new Intent(this, GameActivity.class);
        switch (v.getId()) {
            case R.id.butGame:
                intent.putExtra("id", 0);
                break;
            case R.id.butOneMove:
                if (findViewById(R.id.texLevelSelect) == null) return;
                TextView tw = (TextView) findViewById(R.id.texLevelSelect);
                int id = Integer.decode(tw.getText().toString());
                int setSize = layoutStorage.getSize("onemove_1");
                if (id < 1) id = 1;
                if (id > setSize) id = setSize;
                intent.putExtra("set", "onemove_1");
                intent.putExtra("id", id);
                break;
            case R.id.butTwoMove:
                intent.putExtra("fen", ChessLayout.startLayout);
                break;
            case R.id.butTestLay:
                intent.putExtra("fen", "8/5P2/8/k7/8/2K5/8/8 w - - 0 1");
                break;
        }
        startActivity(intent);
    }
}

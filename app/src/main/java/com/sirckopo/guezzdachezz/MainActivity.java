package com.sirckopo.guezzdachezz;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                intent.putExtra("set", "onemove_1");
                intent.putExtra("id", 1);
                break;
            case R.id.butTwoMove:
                intent.putExtra("fen", ChessLayout.startLayout);
                break;
            case R.id.butTestLay:
                intent.putExtra("fen", "8/5P2/8/k7/8/2K5/8/8 w - - 0 1");
                break;
        }
        // ChessMove[][] sol = {{new ChessMove("e2e4"), null}};
        // intent.putExtra("solutions", );
        startActivity(intent);
    }
}

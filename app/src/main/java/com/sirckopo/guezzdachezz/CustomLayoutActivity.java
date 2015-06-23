package com.sirckopo.guezzdachezz;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


public class CustomLayoutActivity extends ActionBarActivity {

    CustomLayoutStorage customLayoutStorage = new CustomLayoutStorage(this);

    String[] layouts = null;
    ArrayAdapter<String> layoutsAdapter = null;

    AdapterView.OnItemClickListener layoutClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            openDialog(layouts[position]);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_layout);

        updateList();
        ((ListView) findViewById(R.id.layoutList)).setOnItemClickListener(layoutClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_custom_layout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_new) {
            addNewLayout();
        }
        return super.onOptionsItemSelected(item);
    }

    void updateList() {
        layouts = customLayoutStorage.getList();
        layoutsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                layouts);
        ((ListView) findViewById(R.id.layoutList)).setAdapter(layoutsAdapter);
    }

    private void openDialog(final String name) {
        String[] itemsArray = {getString(R.string.action_play), getString(R.string.action_edit),
                getString(R.string.action_solve), getString(R.string.action_copy),
                getString(R.string.action_rename), getString(R.string.action_delete)};

        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

        dlgAlert.setTitle(name);
        dlgAlert.setItems(itemsArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        sendToGame(name);
                        break;
                    case 1:
                        sendToEditor(name);
                        break;
                    case 2:
                        solveLayout(name);
                        break;
                    case 3:
                        copyLayout(name);
                        break;
                    case 4:
                        renameLayout(name);
                        break;
                    case 5:
                        deleteLayout(name);
                        break;
                }
                dialog.dismiss();
            }
        });
        dlgAlert.show();
    }

    void addNewLayout() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.dialog_name_entry));
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        alert.setView(input);

        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString().trim();
                if (value.length() == 0) {
                    Toast.makeText(CustomLayoutActivity.this, getString(R.string.error_bad_name),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!customLayoutStorage.write(value, ChessLayout.startLayout)) {
                    overwriteLayoutWarn(value);
                    return;
                }
                updateList();
                sendToEditor(value);
            }
        });
        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    void deleteLayout(final String name) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.dialog_delete) + " '" + name + "'?");
        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                customLayoutStorage.delete(name);
                updateList();
            }
        });
        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    void overwriteLayoutWarn(final String name) {
        Toast.makeText(CustomLayoutActivity.this,
                getString(R.string.error_name_exists).replace("%s", name),
                Toast.LENGTH_SHORT).show();
    }

    void renameLayout(final String name) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.dialog_name_entry));
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(name);
        alert.setView(input);

        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString().trim();
                if (value.length() == 0) {
                    Toast.makeText(CustomLayoutActivity.this, getString(R.string.error_bad_name),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (name.equals(value)) {
                    return;
                }
                if (!customLayoutStorage.rename(name, value)) {
                    overwriteLayoutWarn(value);
                    return;
                }
                customLayoutStorage.delete(name);
                updateList();

            }
        });
        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    void copyLayout(final String name) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.dialog_name_entry));
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(name);
        alert.setView(input);

        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString().trim();
                if (value.length() == 0) {
                    Toast.makeText(CustomLayoutActivity.this, getString(R.string.error_bad_name),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (name.equals(value)) {
                    return;
                }
                if (!customLayoutStorage.write(value, customLayoutStorage.read(name))) {
                    overwriteLayoutWarn(value);
                    return;
                }
                updateList();
            }
        });
        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    void sendToGame(final String name) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("fen", customLayoutStorage.read(name));
        intent.putExtra("writer", true);
        startActivity(intent);
    }

    void sendToEditor(final String name) {
        Intent intent = new Intent(this, LayoutEditorActivity.class);
        intent.putExtra("name", name);
        startActivity(intent);
    }

    void solveLayout(final String name) {
        FragmentManager fragmentManager = getFragmentManager();
        SolverFragment newFragment = new SolverFragment();
        newFragment.fen = customLayoutStorage.read(name);
        newFragment.show(fragmentManager, "dialog");
    }
}
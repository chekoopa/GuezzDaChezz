package com.sirckopo.guezzdachezz;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class CustomLayoutStorage extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "custom.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_TABLE = "custom_layouts";

    public static final String NAME_COLUMN = "name";
    public static final String FEN_COLUMN = "fen";

    private static final String DATABASE_CREATE_SCRIPT = "create table `"
            + DATABASE_TABLE + "` (`" + BaseColumns._ID
            + "` integer primary key autoincrement, `" + NAME_COLUMN
            + "` text not null, `" + FEN_COLUMN + "` text not null);";

    public CustomLayoutStorage(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public synchronized void close() {
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    // TODO: make layout write-rewrites more clear?
    // rewrite-aware
    public boolean write(String name, String fen) {
        if (read(name) != null)
            return false;
        SQLiteDatabase sqdb = this.getWritableDatabase();
        String insertQuery = "INSERT INTO " + DATABASE_TABLE + " (`" + NAME_COLUMN +
                "`, `" + FEN_COLUMN + "`) VALUES ('" + name + "', '" + fen + "')";
        sqdb.execSQL(insertQuery);
        sqdb.close();
        return true;
    }

    public boolean rewrite(String name, String fen) {
        if (read(name) == null)
            return false;
        SQLiteDatabase sqdb = this.getWritableDatabase();
        String updateQuery = "UPDATE " + DATABASE_TABLE + " SET "+ FEN_COLUMN + "='" + fen +
                "' WHERE `" + NAME_COLUMN + "`='" + name + "'";
        sqdb.execSQL(updateQuery);
        sqdb.close();
        return true;
    }

    public String read(String name) {
        SQLiteDatabase sqdb = this.getReadableDatabase();

        String selectQuery = "SELECT " + FEN_COLUMN + " FROM " + DATABASE_TABLE +
                " WHERE `" + NAME_COLUMN + "`='" + name + "'";
        Cursor cursor = sqdb.rawQuery(selectQuery, null);

        if (cursor.getCount() == 0) {
            cursor.close();
            sqdb.close();
            return null;
        }
        cursor.moveToFirst();
        String output = cursor.getString(cursor.getColumnIndex(FEN_COLUMN));
        cursor.close();
        sqdb.close();

        return output;
    }

    public String[] getList() {
        SQLiteDatabase sqdb = this.getReadableDatabase();

        String selectQuery = "SELECT " + NAME_COLUMN + " FROM " + DATABASE_TABLE;
        Cursor cursor = sqdb.rawQuery(selectQuery, null);

        String[] output = new String[cursor.getCount()];

        cursor.moveToFirst();
        for (int i = 0; i < output.length; i++) {
            output[i] = cursor.getString(cursor.getColumnIndex(NAME_COLUMN));
            cursor.moveToNext();
        }
        cursor.close();
        sqdb.close();

        return output;
    }

    // no error on bad name, eh.
    public void delete(String name) {
        SQLiteDatabase sqdb = this.getWritableDatabase();
        String nukeQuery = "delete from " + DATABASE_TABLE + " where " +
                NAME_COLUMN + "='" + name + "'";
        sqdb.execSQL(nukeQuery);
        sqdb.close();
    }

    public boolean rename(String oldName, String newName) {
        if (read(newName) != null || read(oldName) == null)
            return false;
        SQLiteDatabase sqdb = this.getWritableDatabase();
        String updateQuery = "UPDATE " + DATABASE_TABLE + " SET "+ NAME_COLUMN + "='" + newName +
                "' WHERE `" + NAME_COLUMN + "`='" + oldName + "'";
        sqdb.execSQL(updateQuery);
        sqdb.close();
        return true;
    }
}

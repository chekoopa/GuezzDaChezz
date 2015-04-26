package com.sirckopo.guezzdachezz;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class ProgressStorage extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "progress.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_TABLE = "last_completed";

    public static final String SET_COLUMN = "set";
    public static final String LEVEL_COLUMN = "level";

    private static final String DATABASE_CREATE_SCRIPT = "create table `"
            + DATABASE_TABLE + "` (`" + BaseColumns._ID
            + "` integer primary key autoincrement, `" + SET_COLUMN
            + "` text not null, `" + LEVEL_COLUMN + "` integer);";

    public ProgressStorage(Context context) {
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

    public int getLastCompleted(String set) {
        SQLiteDatabase sqdb = this.getReadableDatabase();

        String selectQuery = "SELECT " + LEVEL_COLUMN + " FROM " + DATABASE_TABLE +
                             " WHERE `" + SET_COLUMN + "`='" + set + "'";
        Cursor cursor = sqdb.rawQuery(selectQuery, null);

        if (cursor.getCount() == 0) {
            cursor.close();
            sqdb.close();
            addSet(set);
            return 0;
        }
        cursor.moveToFirst();
        int level = cursor.getInt(cursor.getColumnIndex(LEVEL_COLUMN));
        cursor.close();
        sqdb.close();

        return level;
    }

    private void addSet(String set) {
        SQLiteDatabase sqdb = this.getWritableDatabase();
        String insertQuery = "INSERT INTO " + DATABASE_TABLE + " (`" + SET_COLUMN +
                "`, `" + LEVEL_COLUMN + "`) VALUES ('" + set + "', '0')";
        sqdb.execSQL(insertQuery);
        sqdb.close();
    }

    public void writeLastCompleted(String set, int level) {
        getLastCompleted(set); // just to make sure that we'll have a line in this table
        SQLiteDatabase sqdb = this.getWritableDatabase();
        String updateQuery = "UPDATE " + DATABASE_TABLE + " SET level='" + level +
                             "' WHERE `" + SET_COLUMN + "`='" + set + "'";
        sqdb.execSQL(updateQuery);
        sqdb.close();
    }

    public void clearData() {
        SQLiteDatabase sqdb = this.getWritableDatabase();
        String nukeQuery = "delete from " + DATABASE_TABLE;
        sqdb.execSQL(nukeQuery);
        sqdb.close();
    }
}
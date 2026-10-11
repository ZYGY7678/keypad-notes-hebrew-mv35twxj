package com.generated.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class NoteDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NOTES = "notes";
    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_CONTENT = "content";
    public static final String COL_TIMESTAMP = "timestamp";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_TITLE + " TEXT, " +
                    COL_CONTENT + " TEXT, " +
                    COL_TIMESTAMP + " INTEGER" +
                    ");";

    public NoteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    public long insertNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, note.getTitle());
        cv.put(COL_CONTENT, note.getContent());
        cv.put(COL_TIMESTAMP, note.getTimestamp());
        return db.insert(TABLE_NOTES, null, cv);
    }

    public int updateNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, note.getTitle());
        cv.put(COL_CONTENT, note.getContent());
        cv.put(COL_TIMESTAMP, note.getTimestamp());
        return db.update(TABLE_NOTES, cv, COL_ID + "=?", new String[]{String.valueOf(note.getId())});
    }

    public int deleteNote(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NOTES, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Note getNote(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NOTES,
                new String[]{COL_ID, COL_TITLE, COL_CONTENT, COL_TIMESTAMP},
                COL_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        Note note = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                note = new Note(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP))
                );
            }
            cursor.close();
        }
        return note;
    }

    public List<Note> searchNotes(String query) {
        List<Note> list = new ArrayList<Note>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor;

        if (query == null || query.trim().length() == 0) {
            cursor = db.query(
                    TABLE_NOTES,
                    new String[]{COL_ID, COL_TITLE, COL_CONTENT, COL_TIMESTAMP},
                    null, null, null, null,
                    COL_TIMESTAMP + " DESC"
            );
        } else {
            String q = "%" + query.trim() + "%";
            cursor = db.query(
                    TABLE_NOTES,
                    new String[]{COL_ID, COL_TITLE, COL_CONTENT, COL_TIMESTAMP},
                    COL_TITLE + " LIKE ? OR " + COL_CONTENT + " LIKE ?",
                    new String[]{q, q},
                    null, null,
                    COL_TIMESTAMP + " DESC"
            );
        }

        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(new Note(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP))
                ));
            }
            cursor.close();
        }
        return list;
    }
}

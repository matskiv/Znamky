package com.matskiv.znamky;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME 	= "znamky.db";
	public static final String TABLE_MARKS 		= "marks";
	public static final String COLUMN_ID 		= "_id";
	public static final String COLUMN_ORDER 	= "ordercol";
	public static final String COLUMN_VALUE 	= "value";
	public static final String COLUMN_WEIGHT 	= "weight";
	public static final String COLUMN_WEIGHTED 	= "weighted";
	public static final String COLUMN_SUBJECT 	= "subject";
	public static final String COLUMN_DATE 		= "date";
	public static final String COLUMN_EXAMTYPE 	= "examtype"; // used for HODNOTA from ver.code 3
	public static final String COLUMN_NOTE 		= "note";
	public static final String COLUMN_TEACHER 	= "teacher";
	
	public static final String[] markColumns = { 
		SQLiteHelper.COLUMN_ID, 
		SQLiteHelper.COLUMN_VALUE, 
		SQLiteHelper.COLUMN_WEIGHT, 
		SQLiteHelper.COLUMN_WEIGHTED,
		SQLiteHelper.COLUMN_SUBJECT, 
		SQLiteHelper.COLUMN_DATE, 
		SQLiteHelper.COLUMN_EXAMTYPE, 
		SQLiteHelper.COLUMN_NOTE, 
		SQLiteHelper.COLUMN_TEACHER,
		SQLiteHelper.COLUMN_ORDER
	};
	
	private SQLiteDatabase database;
	
	public SQLiteHelper(Context cxt) {
		super(cxt, DATABASE_NAME, null, 2);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		String tableCreate = "";
		
		tableCreate = "create table " + TABLE_MARKS + "( " 
			+ COLUMN_ID + " integer primary key, "
			+ COLUMN_VALUE + " text, "
			+ COLUMN_WEIGHT + " integer, "
			+ COLUMN_WEIGHTED + " real, "
			+ COLUMN_SUBJECT + " text, "
			+ COLUMN_DATE + " text, "
			+ COLUMN_EXAMTYPE + " text, "
			+ COLUMN_NOTE + " text, "
			+ COLUMN_TEACHER + " text, "
			+ COLUMN_ORDER + " integer "
			+ ");";
		database.execSQL(tableCreate);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {		
	}
	
	public void open() {
		database = getWritableDatabase();
	}
	
	public void close() {
		database.close();
	}
	
	public void addMark(Mark m) {
		database.replace(SQLiteHelper.TABLE_MARKS, null, m.getValues());
	}
	
	public void addMarks(List<Mark> marks) {
		database.beginTransaction();
		try {
			for (Mark m: marks) {
				database.replace(SQLiteHelper.TABLE_MARKS, null, m.getValues());
			}
			database.setTransactionSuccessful();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			database.endTransaction();
		}
	}
	
	public void truncateMarks() {
		try {
			database.execSQL("DELETE FROM " + SQLiteHelper.TABLE_MARKS + ";");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<Mark> getAllMarks() {
		List<Mark> marks = new ArrayList<Mark>();
		Cursor cursor = null;
		
		try {
			cursor = database.query(SQLiteHelper.TABLE_MARKS, markColumns, null, null, null, null, SQLiteHelper.COLUMN_ORDER);
		} catch (Exception e) {
			e.printStackTrace();
			return marks;
		}
		
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Mark m = cursorToMark(cursor);
			marks.add(m);
			cursor.moveToNext();
		}
		
		cursor.close();
		return marks;
	}
	
	public Hashtable<Integer, Mark> getAllMarksHashTable() {
		Hashtable<Integer, Mark> marks = new Hashtable<Integer, Mark>();
		Cursor cursor = null;
		
		try {
			cursor = database.query(SQLiteHelper.TABLE_MARKS, markColumns, null, null, null, null, SQLiteHelper.COLUMN_ORDER);
		} catch (Exception e) {
			e.printStackTrace();
			return marks;
		}
		
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Mark m = cursorToMark(cursor);
			marks.put(cursor.getInt(0) ,m);
			cursor.moveToNext();
		}
		
		cursor.close();
		return marks;
	}
	
	private Mark cursorToMark(Cursor c) {
		return new Mark(c.getInt(0), c.getString(1), c.getInt(2), c.getFloat(3), c.getString(4), c.getString(5), c.getString(6), c.getString(7), c.getString(8), c.getInt(9));
	}
	
}
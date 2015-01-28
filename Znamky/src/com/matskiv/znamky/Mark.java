package com.matskiv.znamky;

import java.io.Serializable;
import java.util.HashMap;

import android.content.ContentValues;

public class Mark extends HashMap<String, Object> implements Serializable, Comparable<HashMap<String, Object>>{
	
	private static final long serialVersionUID = -6469738123846039940L;
	
	public final static String ValueWeight = "valwei";
	public final static String SubjectFixed = "subfix";
	public final static String DateFixed = "datfix";
	
	public Mark(int id, String value) {
		super();
		put(SQLiteHelper.COLUMN_ID, id);
		put(SQLiteHelper.COLUMN_VALUE, value);
	}
	
	public Mark(int id, String value, int weight, float weighted, String subject,
			String date, String examType, String note, String teacher, int order) {
		super();
		put(SQLiteHelper.COLUMN_ID, id);
		put(SQLiteHelper.COLUMN_VALUE, value);
		put(SQLiteHelper.COLUMN_WEIGHT, weight);
		put(SQLiteHelper.COLUMN_WEIGHTED, weighted);
		put(SQLiteHelper.COLUMN_SUBJECT, subject);
		put(SQLiteHelper.COLUMN_DATE, date);
		put(SQLiteHelper.COLUMN_EXAMTYPE, examType);
		put(SQLiteHelper.COLUMN_NOTE, note);
		put(SQLiteHelper.COLUMN_TEACHER, teacher);
		put(SQLiteHelper.COLUMN_ORDER, order);
		
		put(Mark.ValueWeight, this.getValueWeigth());
		put(Mark.SubjectFixed, this.getSubjectFixed());
		put(Mark.DateFixed, this.getDateFixed());
	}
	
	public Mark(HashMap<String, Object> hm) {
		super(hm);
	}
	
	public ContentValues getValues() {
		ContentValues values = new ContentValues();
		for (String k : SQLiteHelper.markColumns) {
			values.put(k, get(k).toString());
		}
		return values;
	}
	
	public static float valueFlo(String v) {
		if (v.length() > 2 || v.length() < 1) return -2;
		if (v.equals("N")) return -1;
		if (v.equals("-")) return 0;
		float out = 0;
		try {
			out = Float.parseFloat(v.substring(0, 1));
		} catch (Exception e) {
			out = -2;
		}
		if (out != -2 && v.length() > 1 && v.substring(1, 2).equals("-")) out += 0.5;
		return out;
	}
	
	public String getValueWeigth() {
		return ( Mark.fixLength(get(SQLiteHelper.COLUMN_VALUE).toString(), 2)+"("+get(SQLiteHelper.COLUMN_WEIGHT)+")");
	}
	
	public String getSubjectFixed() {
		return Mark.fixLength(get(SQLiteHelper.COLUMN_SUBJECT).toString(), 5);
	}
	
	public String getDateFixed() {
		return Mark.fixLength(get(SQLiteHelper.COLUMN_DATE).toString(), 5);
	}
	
	public static String fixLength(String s, int l) {
		if(s.length()==l) return s;
		if(s.length()>l)return s.substring(0,l);
		for (int i = l-s.length(); i>0; i--) {
			s += " ";
		}
		return s;
	}

	@Override
	public int compareTo(HashMap<String, Object> m) {
		if (m == null) {
			return 1;
		}
		if ((get(SQLiteHelper.COLUMN_ID).toString()).compareTo(m.get(SQLiteHelper.COLUMN_ID).toString()) != 0 ||
			(get(SQLiteHelper.COLUMN_VALUE).toString()).compareTo(m.get(SQLiteHelper.COLUMN_VALUE).toString()) != 0 ||	
			(get(SQLiteHelper.COLUMN_SUBJECT).toString()).compareTo(m.get(SQLiteHelper.COLUMN_SUBJECT).toString()) != 0 ||	
			(get(SQLiteHelper.COLUMN_DATE).toString()).compareTo(m.get(SQLiteHelper.COLUMN_DATE).toString()) != 0 ||	
			(get(SQLiteHelper.COLUMN_EXAMTYPE).toString()).compareTo(m.get(SQLiteHelper.COLUMN_EXAMTYPE).toString()) != 0 ||	
			(get(SQLiteHelper.COLUMN_NOTE).toString()).compareTo(m.get(SQLiteHelper.COLUMN_NOTE).toString()) != 0 ||	
			(get(SQLiteHelper.COLUMN_TEACHER).toString()).compareTo(m.get(SQLiteHelper.COLUMN_TEACHER).toString()) != 0
		) {
			return 1;
		}
		return 0;
	}

}
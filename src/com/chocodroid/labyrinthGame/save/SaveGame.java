package com.chocodroid.labyrinthGame.save;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SaveGame
{
	private static final String[] sValue = { "value" };
	private static final String[] sScore = { "level","score","tiempo","online"};
	private SaveGameHelper mHelper;
	private SQLiteDatabase mDB;

	public SaveGame(Context context)
	{
		if(mHelper!=null){
			//mHelper.close();
			mHelper=null;
		}
		if(mDB!=null){
			//mDB.close();
			mDB=null;
		}
		
		mHelper = new SaveGameHelper(context);
		mDB = mHelper.getWritableDatabase();
	}

	//SCORES--------------------------------------------------------------------------------------------------------------------------------------
	public void deleteScore(String key)
	{
		mDB.execSQL("DELETE FROM scores WHERE level='" + key + "';");
	}

	public void writeScore(String level, int score, String tiempo,String online)
	{
		ArrayList<Score> listaScores = readAllScore();
		if(listaScores.size()<=Integer.valueOf(level) || listaScores.get(Integer.valueOf(level)-1).getScore()>score){
			deleteScore(level);
			mDB.execSQL("INSERT INTO scores (level, score, tiempo, online) VALUES ('" + level + "', " + score + ", '" +  tiempo + "', 'N');");
		}
	}
	
	
	public ArrayList<Score> readAllScore()
	{
		ArrayList<Score> listaScores = new ArrayList<Score>();
		String level=null;
		int score = 0;
		String tiempo=null;
		String online=null;
		
		Cursor c = mDB.rawQuery("SELECT * FROM scores ORDER BY score DESC", null);
		if (c != null)
		{
			c.moveToFirst();
			while(c.getPosition() < c.getCount()){
				level = c.getString(0);
				score = c.getInt(1);
				tiempo = c.getString(2);
				online=c.getString(3);
				
				listaScores.add(new Score(level,score,tiempo,online));
				c.moveToNext();
			}
			c.close();
			c = null;
		}
		return listaScores;
	}

	
	public ArrayList<Score> readAllScoreNoOnline()
	{
		ArrayList<Score> listaScores = new ArrayList<Score>();
		String level=null;
		int score = 0;
		String tiempo=null;
		String online=null;
		
		Cursor c = mDB.rawQuery("SELECT * FROM scores WHERE online=='N' ORDER BY score DESC", null);
		if (c != null)
		{
			c.moveToFirst();
			while(c.getPosition() < c.getCount()){
				level = c.getString(0);
				score = c.getInt(1);
				tiempo = c.getString(2);
				online=c.getString(3);
				
				listaScores.add(new Score(level,score,tiempo,online));
				c.moveToNext();
			}
			c.close();
			c = null;
		}
		return listaScores;
	}
	
	public boolean updateScoreONLINE(Score s){
		mDB.execSQL("UPDATE scores SET online='S' WHERE level='" + s.getLevel() + "';");
		return true;
	}

	//STRING--------------------------------------------------------------------------------------------------------------------------------------
	public void deleteString(String key)
	{
		mDB.execSQL("DELETE FROM strings WHERE key='" + key + "';");
	}

	public void writeString(String key, String value)
	{
		deleteString(key);
		mDB.execSQL("INSERT INTO strings (key, value) VALUES ('" + key + "', '" + value + "');");
	}

	public String readString(String key)
	{
		String ret = null;
		Cursor c = mDB.query("strings", sValue, "key='" + key + "'", null, null, null, null, "1");
		if (c != null)
		{
			if (c.moveToFirst())
				ret = c.getString(0);
			c.close();
			c = null;
		}
		return ret;
	}

	//INT--------------------------------------------------------------------------------------------------------------------------------------
	public void deleteInt(String key)
	{
		mDB.execSQL("DELETE FROM ints WHERE key='" + key + "';");
	}

	public void writeInt(String key, int value)
	{
		deleteInt(key);
		mDB.execSQL("INSERT INTO ints (key, value) VALUES ('" + key + "', " + value + ");");
	}

	public int readInt(String key)
	{
		int ret = 0;
		Cursor c = mDB.query("ints", sValue, "key='" + key + "'", null, null, null, null, "1");
		if (c != null)
		{
			if (c.moveToFirst())
				ret = c.getInt(0);
			c.close();
			c = null;
		}
		return ret;
	}

	//FLOAT--------------------------------------------------------------------------------------------------------------------------------------
	public void deleteFloat(String key)
	{
		mDB.execSQL("DELETE FROM floats WHERE key='" + key + "';");
	}

	public void writeFloat(String key, float value)
	{
		deleteFloat(key);
		mDB.execSQL("INSERT INTO floats (key, value) VALUES ('" + key + "', " + value + ");");
	}

	public float readFloat(String key)
	{
		float ret = 0;
		Cursor c = mDB.query("floats", sValue, "key='" + key + "'", null, null, null, null, "1");
		if (c != null)
		{
			if (c.moveToFirst())
				ret = c.getFloat(0);
			c.close();
			c = null;
		}
		return ret;
	}

	private static char toHexDigit(int b)
	{
		return (char) ((b < 10) ? ('0' + b) : ('A' + b - 10));
	}

	private static String toBlobString(byte[] bytes)
	{
		final int length = bytes.length;
		char chars[] = new char[length * 2 + 3];
		chars[0] = 'x';
		chars[1] = '\'';
		int index = 2;
		for (int n = 0; n < length; n++)
		{
			int value = bytes[n] & 0xff;
			chars[index++] = toHexDigit(value / 16);
			chars[index++] = toHexDigit(value % 16);
		}

		chars[index] = '\'';
		return new String(chars);
	}

	public void deleteBlob(String key)
	{
		mDB.execSQL("DELETE FROM blobs WHERE key='" + key + "';");
	}

	public void writeBlob(String key, byte[] value)
	{
		deleteBlob(key);
		mDB.execSQL("INSERT INTO blobs (key, value) VALUES ('" + key + "', " + toBlobString(value) + ");");
	}

	public byte[] readBlob(String key)
	{
		byte[] ret = null;
		Cursor c = mDB.query("blobs", sValue, "key='" + key + "'", null, null, null, null, "1");
		if (c != null)
		{
			if (c.moveToFirst())
				ret = c.getBlob(0);
			c.close();
			c = null;
		}
		return ret;
	}

	public class SaveGameHelper extends SQLiteOpenHelper
	{
		SaveGameHelper(Context context)
		{
			super(context, "labyrinthGameChocodroid.db", null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL("CREATE TABLE strings (key CHAR(16), value VARCHAR(255), PRIMARY KEY(key), UNIQUE(key));");
			db.execSQL("CREATE TABLE ints (key CHAR(16), value INTEGER, PRIMARY KEY(key), UNIQUE(key));");
			db.execSQL("CREATE TABLE floats (key CHAR(16), value FLOAT, PRIMARY KEY(key), UNIQUE(key));");
			db.execSQL("CREATE TABLE blobs (key CHAR(16), value BLOB, PRIMARY KEY(key), UNIQUE(key));");
			db.execSQL("CREATE TABLE scores (level CHAR(16), score INTEGER, tiempo VARCHAR(255), online CHAR(1),PRIMARY KEY(level), UNIQUE(level));");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			db.execSQL("DROP TABLE IF EXISTS strings");
			db.execSQL("DROP TABLE IF EXISTS ints");
			db.execSQL("DROP TABLE IF EXISTS floats");
			db.execSQL("DROP TABLE IF EXISTS blobs");
			db.execSQL("DROP TABLE IF EXISTS scores");
			onCreate(db);
		}
	}
	
	//CLOSE
	public void cerrarBD(){
		 if (mDB!=null){
				mDB.close();
		 }
	}
	
	public boolean estaCerrada(){
		 if (mDB!=null){
				return false;
		 }
		 return true;
	}
}
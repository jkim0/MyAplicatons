package com.loyid.grammarbook;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper 
{
	private static final String TAG = "DatabaseHelper";
	/**
	 * The database that the provider uses as its underlying data store
	 */
    private static final String DATABASE_NAME = "grammars.db";
	
    /**
     * The database version
     */
    private static final int DATABASE_VERSION = 1;
    
	DatabaseHelper(Context context) {
		// calls the super constructor, requesting the default cursor factory.
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE " + GrammarProviderContract.Grammars.TABLE_NAME + " ("
				+ GrammarProviderContract.Grammars._ID + " INTEGER PRIMARY KEY,"
				+ GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR + " TEXT NOT NULL,"
				+ GrammarProviderContract.Grammars.COLUMN_NAME_MEANING + " TEXT,"
				+ GrammarProviderContract.Grammars.COLUMN_NAME_CREATED_DATE + " INTEGER,"
				+ GrammarProviderContract.Grammars.COLUMN_NAME_MODIFIED_DATE + " INTEGER"
				+ ");");
		
		db.execSQL("CREATE TABLE " + GrammarProviderContract.Meanings.TABLE_NAME + " ("
				+ GrammarProviderContract.Meanings._ID + " INTEGER PRIMARY KEY,"
				+ GrammarProviderContract.Meanings.COLUMN_NAME_WORD + " TEXT NOT NULL,"
				+ GrammarProviderContract.Meanings.COLUMN_NAME_TYPE + " TEXT,"
				+ GrammarProviderContract.Meanings.COLUMN_NAME_CREATED_DATE + " INTEGER,"
				+ GrammarProviderContract.Meanings.COLUMN_NAME_MODIFIED_DATE + " INTEGER,"
				+ GrammarProviderContract.Meanings.COLUMN_NAME_REFER_COUNT + " INTEGER DEFAULT 0 NOT NULL"
				+ ");");
		
		db.execSQL("CREATE TABLE " + GrammarProviderContract.Mappings.TABLE_NAME + " ("
				+ GrammarProviderContract.Mappings._ID + " INTEGER PRIMARY KEY,"
				+ GrammarProviderContract.Mappings.COLUMN_NAME_GRAMMAR_ID + " INTEGER,"
				+ GrammarProviderContract.Mappings.COLUMN_NAME_MEANING_ID + " INTEGER"
				+ ");");
		
		db.execSQL("CREATE TRIGGER IF NOT EXISTS decrease_meaning_refer_mount AFTER DELETE on "
				+ GrammarProviderContract.Mappings.TABLE_NAME + " "
				+ "BEGIN "
					+ "UPDATE " + GrammarProviderContract.Meanings.TABLE_NAME + " "
					+ "SET refer_count = refer_count - 1 "
					+ "WHERE _id = old." + GrammarProviderContract.Mappings.COLUMN_NAME_MEANING_ID + ";"
					+ "DELETE from " + GrammarProviderContract.Meanings.TABLE_NAME + " WHERE refer_count <= 0;"
				+ "END");
		
		db.execSQL("CREATE TRIGGER IF NOT EXISTS increase_meaning_refer_mount AFTER INSERT on "
				+ GrammarProviderContract.Mappings.TABLE_NAME + " "
				+ "BEGIN "
					+ "UPDATE " + GrammarProviderContract.Meanings.TABLE_NAME + " "
					+ "SET refer_count = refer_count + 1 "
					+ "WHERE _id = new." + GrammarProviderContract.Mappings.COLUMN_NAME_MEANING_ID + ";"
				+ "END");
		
		db.execSQL("CREATE TRIGGER IF NOT EXISTS update_mapping_after_delete AFTER DELETE on "
				+ GrammarProviderContract.Grammars.TABLE_NAME + " "
				+ "BEGIN "
					+ "DELETE from " + GrammarProviderContract.Mappings.TABLE_NAME
					+ " WHERE grammar_id = old." + GrammarProviderContract.Grammars._ID + ";"
				+ "END");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		// Logs that the database is being upgraded
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		
		// Kills the table and existing data
		db.execSQL("DROP TABLE IF EXISTS " + GrammarProviderContract.Grammars.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + GrammarProviderContract.Meanings.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + GrammarProviderContract.Mappings.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS decrease_meaning_refer_mount");
		db.execSQL("DROP TABLE IF EXISTS increase_meaning_refer_mount");
		db.execSQL("DROP TABLE IF EXISTS update_mapping_after_delete");
		
		// Recreates the database with a new version
		onCreate(db);
	}
}

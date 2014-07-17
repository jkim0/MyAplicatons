package com.loyid.grammarbook;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class GrammarProvider extends ContentProvider {
	private static final String TAG = "GrammarProvider";
	
	/**
	 * The database that the provider uses as its underlying data store
	 */
    private static final String DATABASE_NAME = "grammars.db";
	
    /**
     * The database version
     */
    private static final int DATABASE_VERSION = 1;

    private DatabaseHelper mDatabaseHelper = null;
    
    /**
     * A projection map used to select columns from the database
     */
    private static HashMap<String, String> sGrammarsProjectionMap;
    
    private static final int GRAMMARS = 0;
    private static final int GRAMMAR_ID = 1;
    private static final int MEANING = 2;
    private static final int MEANING_ID = 3;
    private static final int MAPPING = 4;
    private static final int MAPPING_ID = 5;
    
    /**
     * A UriMatcher instance
     */
    private static final UriMatcher sUriMatcher;
    
    static {
    	
    	sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    	sUriMatcher.addURI(GrammarProviderContract.AUTHORITY,
    			GrammarProviderContract.Grammars.TABLE_NAME, GRAMMARS);
    	sUriMatcher.addURI(GrammarProviderContract.AUTHORITY,
    			GrammarProviderContract.Grammars.TABLE_NAME + "/#", GRAMMARS);
    	sUriMatcher.addURI(GrammarProviderContract.AUTHORITY,
    			GrammarProviderContract.Meanings.TABLE_NAME, MEANING);
    	sUriMatcher.addURI(GrammarProviderContract.AUTHORITY,
    			GrammarProviderContract.Meanings.TABLE_NAME + "/#", MEANING);
    	sUriMatcher.addURI(GrammarProviderContract.AUTHORITY,
    			GrammarProviderContract.Mappings.TABLE_NAME, MAPPING);
    	sUriMatcher.addURI(GrammarProviderContract.AUTHORITY,
    			GrammarProviderContract.Mappings.TABLE_NAME + "/#", MAPPING);
    	
    	sGrammarsProjectionMap = new HashMap<String, String>();
    	sGrammarsProjectionMap.put(GrammarProviderContract.Grammars._ID,
    			GrammarProviderContract.Grammars._ID);
    	sGrammarsProjectionMap.put(GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR,
    			GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR);
    	sGrammarsProjectionMap.put(GrammarProviderContract.Grammars.COLUMN_NAME_MEANING,
    			GrammarProviderContract.Grammars.COLUMN_NAME_MEANING);
    	sGrammarsProjectionMap.put(GrammarProviderContract.Grammars.COLUMN_NAME_CREATED_DATE,
    			GrammarProviderContract.Grammars.COLUMN_NAME_CREATED_DATE);
    	sGrammarsProjectionMap.put(GrammarProviderContract.Grammars.COLUMN_NAME_MODIFIED_DATE,
    			GrammarProviderContract.Grammars.COLUMN_NAME_MODIFIED_DATE);
    }
    
	public GrammarProvider() {
	}

	static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			// calls the super constructor, requesting the default cursor factory.
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL("CREATE TABLE " + GrammarProviderContract.Grammars.TABLE_NAME + " ("
					+ GrammarProviderContract.Grammars._ID + " INTEGER PRIMARY KEY,"
					+ GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR + " TEXT,"
					+ GrammarProviderContract.Grammars.COLUMN_NAME_MEANING + " TEXT,"
					+ GrammarProviderContract.Grammars.COLUMN_NAME_CREATED_DATE + " INTEGER,"
					+ GrammarProviderContract.Grammars.COLUMN_NAME_MODIFIED_DATE + " INTEGER"
					+ ");");
			
			db.execSQL("CREATE TABLE " + GrammarProviderContract.Meanings.TABLE_NAME + " ("
					+ GrammarProviderContract.Meanings._ID + " INTEGER PRIMARY KEY,"
					+ GrammarProviderContract.Meanings.COLUMN_NAME_WORD + " TEXT,"
					+ GrammarProviderContract.Meanings.COLUMN_NAME_TYPE + " TEXT,"
					+ GrammarProviderContract.Meanings.COLUMN_NAME_CREATED_DATE + " INTEGER,"
					+ GrammarProviderContract.Meanings.COLUMN_NAME_MODIFIED_DATE + " INTEGER"
					+ ");");
			
			db.execSQL("CREATE TABLE " + GrammarProviderContract.Meanings.TABLE_NAME + " ("
					+ GrammarProviderContract.Mappings._ID + " INTEGER PRIMARY KEY,"
					+ GrammarProviderContract.Mappings.COLUMN_NAME_GRAMMAR_ID + " INTEGER,"
					+ GrammarProviderContract.Mappings.COLUMN_NAME_MEANING_ID + " INTEGER"
					+ ");");
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
			
			// Recreates the database with a new version
			onCreate(db);
		}
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// Implement this to handle requests to delete one or more rows.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public String getType(Uri uri) {
		// TODO: Implement this to handle requests for the MIME type of the data
		// at the given URI.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO: Implement this to handle requests to insert a new row.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public boolean onCreate() {
		// TODO: Implement this to initialize your content provider on startup.
		mDatabaseHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO: Implement this to handle query requests from clients.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO: Implement this to handle requests to update one or more rows.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}

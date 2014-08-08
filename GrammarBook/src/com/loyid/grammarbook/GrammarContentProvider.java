package com.loyid.grammarbook;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class GrammarContentProvider extends ContentProvider {
	private static final String TAG = "GrammarContentProvider";
	
	private DatabaseHelper mDBHelper = null;
	
	public static final int GRAMMAR = 0;
	public static final int GRAMMAR_ID = 1;
	public static final int MEANING = 2;
	public static final int MEANING_ID = 3;
	public static final int MAPPING = 4;
	public static final int MAPPING_ID = 5;
	
	/**
	 * A UriMatcher instance
	 */
	private static final UriMatcher sUriMatcher;
    
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(GrammarProviderContract.AUTHORITY, GrammarProviderContract.Grammars.TABLE_NAME, GRAMMAR);
		sUriMatcher.addURI(GrammarProviderContract.AUTHORITY, GrammarProviderContract.Grammars.TABLE_NAME + "/#", GRAMMAR_ID);
		sUriMatcher.addURI(GrammarProviderContract.AUTHORITY, GrammarProviderContract.Meanings.TABLE_NAME, MEANING);
		sUriMatcher.addURI(GrammarProviderContract.AUTHORITY, GrammarProviderContract.Meanings.TABLE_NAME + "/#", MEANING_ID);
		sUriMatcher.addURI(GrammarProviderContract.AUTHORITY, GrammarProviderContract.Mappings.TABLE_NAME, MAPPING);
		sUriMatcher.addURI(GrammarProviderContract.AUTHORITY, GrammarProviderContract.Mappings.TABLE_NAME + "/#", MAPPING_ID);
	}
	
	public GrammarContentProvider() {
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.d(TAG, "delete uri = " + uri + " selection = " + selection + " selectionArgs = " + selectionArgs);
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		int count;
		
		switch (sUriMatcher.match(uri)) {
		case GRAMMAR:
			count = db.delete(GrammarProviderContract.Grammars.TABLE_NAME, selection, selectionArgs);
			break;
		case GRAMMAR_ID:
			String grammarId = uri.getPathSegments().get(GrammarProviderContract.Grammars.GRAMMAR_ID_PATH_POSITION);
			if (selection != null) {
				selection += " AND " + GrammarProviderContract.Grammars._ID + " = " + grammarId;
			}
			count = db.delete(GrammarProviderContract.Grammars.TABLE_NAME, selection, selectionArgs);
			break;
		default:
			throw new UnsupportedOperationException("Invalid URI = " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case GRAMMAR:
			return GrammarProviderContract.Grammars.CONTENT_TYPE;
		case GRAMMAR_ID:
			return GrammarProviderContract.Grammars.CONTENT_ITEM_TYPE;
		case MEANING:
			return GrammarProviderContract.Meanings.CONTENT_TYPE;
		case MEANING_ID:
			return GrammarProviderContract.Meanings.CONTENT_ITEM_TYPE;
		case MAPPING:
			return GrammarProviderContract.Mappings.CONTENT_TYPE;
		case MAPPING_ID:
			return GrammarProviderContract.Mappings.CONTENT_ITEM_TYPE;
		default:
				throw new IllegalArgumentException("Unknown URI = " + uri);
		}
	}
	
	private Uri insertGrammar(Uri uri, ContentValues initialValues) {
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			throw new IllegalArgumentException("ContentValues is null");
		}
		
		if (values.containsKey(GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR) == false) {
			throw new IllegalArgumentException(GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR + "is set null in ContentValues");
		}
		
		String meanings = null;
		if (values.containsKey(GrammarProviderContract.Grammars.COLUMN_NAME_MEANING) == true) {
			meanings = values.getAsString(GrammarProviderContract.Grammars.COLUMN_NAME_MEANING);			
		}
		
		Long now = Long.valueOf(System.currentTimeMillis());
		
		if (values.containsKey(GrammarProviderContract.Grammars.COLUMN_NAME_CREATED_DATE) == false) {
			values.put(GrammarProviderContract.Grammars.COLUMN_NAME_CREATED_DATE, now);
		}
		
		if (values.containsKey(GrammarProviderContract.Grammars.COLUMN_NAME_MODIFIED_DATE) == false) {
			values.put(GrammarProviderContract.Grammars.COLUMN_NAME_MODIFIED_DATE, now);
		}
		
		long rowId;
		db.beginTransaction();
		
		try {
			rowId = db.insert(GrammarProviderContract.Grammars.TABLE_NAME, null, values);
			
			updateMeaningInfo(db, rowId, meanings, now);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		if (rowId > 0) {
			Uri grammarUri = ContentUris.withAppendedId(GrammarProviderContract.Grammars.CONTENT_GRAMMAR_ID_URI_BASE, rowId);
			return grammarUri;
		}
		
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(TAG, "insert uri = " + uri + " values = " + values.toString());
		Uri returnUri;
		switch(sUriMatcher.match(uri)) {
		case GRAMMAR:			
			returnUri = insertGrammar(uri, values);
			break;
		default:
			throw new UnsupportedOperationException("Invalid URI = " + uri);	
		}
		
		if (returnUri != null) {
			getContext().getContentResolver().notifyChange(returnUri, null);
			return returnUri;
		}
		
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		// TODO: Implement this to initialize your content provider on startup.
		mDBHelper = new DatabaseHelper(getContext());
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Log.d(TAG, "query uri = " + uri + " projection = " + projection + " selection = "
			+ selection + " selectionArgs = " + selectionArgs + " sortOrder = " + sortOrder);
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		List<String> prependArgs = new ArrayList<String>();
		String groupBy = null;
		String limit = uri.getQueryParameter("limit");
		String filter = uri.getQueryParameter("filter");
		
		String [] keywords = null;
		if (filter != null) {
			filter = Uri.decode(filter).trim();
			
			if (!TextUtils.isEmpty(filter)) {
				String [] searchWords = filter.split(" ");
				keywords = new String[searchWords.length];
				Collator col = Collator.getInstance();
				col.setStrength(Collator.PRIMARY);
				for (int i = 0; i < searchWords.length; i++) {
					String key = GrammarProviderContract.keyFor(searchWords[i]);
					key = key.replace("\\", "\\\\");
					key = key.replace("%", "\\%");
					key = key.replace("_", "\\_");
					keywords[i] = key;
				}
			}
		}
		
		if (uri.getQueryParameter("distinct") != null) {
			qb.setDistinct(true);
		}
		
		switch(sUriMatcher.match(uri)) {
		case GRAMMAR:
			qb.setTables(GrammarProviderContract.Grammars.TABLE_NAME);
			break;
		case GRAMMAR_ID:
			qb.setTables(GrammarProviderContract.Grammars.TABLE_NAME);
			qb.appendWhere("_id=?");
			prependArgs.add(uri.getPathSegments().get(GrammarProviderContract.Grammars.GRAMMAR_ID_PATH_POSITION));
			break;
		case MEANING:
			qb.setTables(GrammarProviderContract.Meanings.TABLE_NAME);
			break;
		case MEANING_ID:
			qb.setTables(GrammarProviderContract.Meanings.TABLE_NAME);
			qb.appendWhere("_id=?");
			prependArgs.add(uri.getPathSegments().get(GrammarProviderContract.Meanings.MEANING_ID_PATH_POSITION));
			break;
		case MAPPING:
			qb.setTables(GrammarProviderContract.Mappings.TABLE_NAME);
			break;
		case MAPPING_ID:
			qb.setTables(GrammarProviderContract.Mappings.TABLE_NAME);
			qb.appendWhere("_id=?");
			prependArgs.add(uri.getPathSegments().get(GrammarProviderContract.Mappings.MAPPING_ID_PATH_POSITION));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI = " + uri);	
		}
		
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		
		Cursor c = qb.query(db, projection, selection,
                combine(prependArgs, selectionArgs), groupBy, null, sortOrder, limit);
		
		if (c != null) {
			c.setNotificationUri(getContext().getContentResolver(), uri);
		}
		
		return c;
	}
	
	private String[] combine(List<String> prepend, String[] userArgs) {
		int presize = prepend.size();
		if (presize == 0) {
			return userArgs;
		}
		
		int usersize = (userArgs != null) ? userArgs.length : 0;
		String [] combined = new String[presize + usersize];
		
		for (int i = 0; i < presize; i++) {
			combined[i] = prepend.get(i);
		}
		
		for (int i = 0; i < usersize; i++) {
			combined[presize + i] = userArgs[i];
		}
		
		return combined;
    }

	private int updateGrammar(long rowId, ContentValues initialValues, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		int count;
		
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			throw new IllegalArgumentException("ContentValues is null");
		}
		
		if (values.containsKey(GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR) == false) {
			throw new IllegalArgumentException(GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR + "is set null in ContentValues");
		}
				
		String meanings = null;
		if (values.containsKey(GrammarProviderContract.Grammars.COLUMN_NAME_MEANING) == true) {
			meanings = values.getAsString(GrammarProviderContract.Grammars.COLUMN_NAME_MEANING);			
		}
		
		Long now = Long.valueOf(System.currentTimeMillis());
		
		if (values.containsKey(GrammarProviderContract.Grammars.COLUMN_NAME_MODIFIED_DATE) == false) {
			values.put(GrammarProviderContract.Grammars.COLUMN_NAME_MODIFIED_DATE, now);
		}
		
		db.beginTransaction();
		
		try {
			count = db.update(GrammarProviderContract.Grammars.TABLE_NAME, initialValues, selection, selectionArgs);
			updateMeaningInfo(db, rowId, meanings, now);
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return count;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		Log.d(TAG, "update uri = " + uri + " value = " + values.toString()
				+ " selection = " + selection + " selectionArgs = " + selectionArgs);
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case GRAMMAR_ID:
			String grammarId = uri.getPathSegments().get(GrammarProviderContract.Grammars.GRAMMAR_ID_PATH_POSITION);
			String prependSelection = GrammarProviderContract.Grammars._ID + " = " + grammarId;
			if (selection != null) {
				selection += " AND " + GrammarProviderContract.Grammars._ID + " = " + grammarId;
			} else {
				selection = prependSelection;
			}
			count = updateGrammar(Integer.valueOf(grammarId), values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI = " + uri);
		}
		
		if (count > 0 && !db.inTransaction()) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		
		return count;
	}
	
	private void updateMeaningInfo(SQLiteDatabase db, long grammarId, String meanings, long timeStamp) {
		if (meanings != null) {
			String[] meaningGroup = meanings.split(GrammarUtils.IDENTIFIER_MEANING_GROUP);
			Log.d(TAG, "meanings = " + meanings);
			for (int i = 0; i < meaningGroup.length; i++) {
				Log.d(TAG, "###### meaing = " + meaningGroup[i]);
				String[] splits = meaningGroup[i].split(GrammarUtils.IDENTIFIER_MEANING);
				int type = Integer.valueOf(splits[0]);
				String meaning = splits[1];
				
				ContentValues meanValues = new ContentValues();
				meanValues.put(GrammarProviderContract.Meanings.COLUMN_NAME_TYPE, type);
				meanValues.put(GrammarProviderContract.Meanings.COLUMN_NAME_WORD, meaning);
				
				String whereClause = GrammarProviderContract.Mappings.COLUMN_NAME_GRAMMAR_ID + " = ?";
				String[] whereArgs = { String.valueOf(grammarId) };
				int result = db.delete(GrammarProviderContract.Mappings.TABLE_NAME, whereClause, whereArgs);
				Log.d(TAG, "remove " + result + " items from Mappings table");
				
				String mselection = GrammarProviderContract.Meanings.COLUMN_NAME_WORD + " = ?"
						+ " AND " + GrammarProviderContract.Meanings.COLUMN_NAME_TYPE + " = ?";

				String[] mselectionArgs = new String[] { meaning, splits[0] };
				
				Cursor cursor = db.query(GrammarProviderContract.Meanings.TABLE_NAME,
						null,
						mselection,
						mselectionArgs,
						null,
						null,
						GrammarProviderContract.Meanings.DEFAULT_SORT_ORDER);

				long meaningId = -1;
				if (cursor != null) {
					if (cursor.getCount() > 0 && cursor.moveToFirst()) {
						int index = cursor.getColumnIndex(GrammarProviderContract.Meanings._ID);
						meaningId = cursor.getLong(index);
					}
					
					cursor.close();
				}

				if (meaningId < 0) {
					Log.d(TAG, "insert meaning");
					meanValues.put(GrammarProviderContract.Meanings.COLUMN_NAME_CREATED_DATE, timeStamp);
					meanValues.put(GrammarProviderContract.Meanings.COLUMN_NAME_MODIFIED_DATE, timeStamp);
					meaningId = db.insert(GrammarProviderContract.Meanings.TABLE_NAME, null, meanValues);
				} else {
					Log.d(TAG, "update meaning index = " + meaningId);
					whereClause = GrammarProviderContract.Meanings._ID + " = ?";
					whereArgs = new String[] { String.valueOf(meaningId) };
					meanValues.put(GrammarProviderContract.Meanings.COLUMN_NAME_MODIFIED_DATE, timeStamp);
					db.update(GrammarProviderContract.Meanings.TABLE_NAME, meanValues, whereClause, whereArgs);
				}
				
				ContentValues mappingValues = new ContentValues();
				mappingValues.put(GrammarProviderContract.Mappings.COLUMN_NAME_GRAMMAR_ID, grammarId);
				mappingValues.put(GrammarProviderContract.Mappings.COLUMN_NAME_MEANING_ID, meaningId);
				db.insert(GrammarProviderContract.Mappings.TABLE_NAME, null, mappingValues);
			}
		}
	}
}

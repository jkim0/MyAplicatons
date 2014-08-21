package com.loyid.grammarbook;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
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
	public static final int TEST_RESULT = 6;
	public static final int TEST_RESULT_ID = 7;
	
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
		sUriMatcher.addURI(GrammarProviderContract.AUTHORITY, GrammarProviderContract.TestResult.TABLE_NAME, TEST_RESULT);
		sUriMatcher.addURI(GrammarProviderContract.AUTHORITY, GrammarProviderContract.TestResult.TABLE_NAME + "/#", TEST_RESULT_ID);
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
		case TEST_RESULT:
			count = db.delete(GrammarProviderContract.TestResult.TABLE_NAME, selection, selectionArgs);
			break;
		case TEST_RESULT_ID:
			String resultId = uri.getPathSegments().get(GrammarProviderContract.TestResult.TEST_RESULT_ID_PATH_POSITION);
			if (selection != null) {
				selection += " AND " + GrammarProviderContract.TestResult._ID + " = " + resultId;
			}
			count = db.delete(GrammarProviderContract.TestResult.TABLE_NAME, selection, selectionArgs);
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
		case TEST_RESULT:
			return GrammarProviderContract.TestResult.CONTENT_TYPE;
		case TEST_RESULT_ID:
			return GrammarProviderContract.TestResult.CONTENT_ITEM_TYPE;
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
		Uri returnUri = null;
		switch(sUriMatcher.match(uri)) {
		case GRAMMAR:			
			returnUri = insertGrammar(uri, values);
			break;
		case TEST_RESULT:
			SQLiteDatabase db = mDBHelper.getWritableDatabase();
			long rowId = db.insert(GrammarProviderContract.TestResult.TABLE_NAME, null, values);
			if (rowId > 0) {
				returnUri = ContentUris.withAppendedId(GrammarProviderContract.TestResult.CONTENT_TEST_RESULT_ID_URI_BASE, rowId);
			}
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
			if (readBooleanQueryParameter(uri, GrammarProviderContract.GRAMMAR_BOOK_INDEX_EXTRAS, false)) {
				Log.d(TAG, "wow");
				Bundle bundle = getIndexExtras(db, qb, selection, selectionArgs, sortOrder);
				//((AbstractCursor)c).setExtras(bundle);
	        }
			c.setNotificationUri(getContext().getContentResolver(), uri);
		}
		
		return c;
	}
	
	static boolean readBooleanQueryParameter(Uri uri, String parameter,
			boolean defaultValue) {
		// Manually parse the query, which is much faster than calling uri.getQueryParameter
		String query = uri.getEncodedQuery();
		if (query == null) {
			return defaultValue;
		}
		
		int index = query.indexOf(parameter);
		if (index == -1) {
			return defaultValue;
		}
		
		index += parameter.length();
		
		return !matchQueryParameter(query, index, "=0", false)
				&& !matchQueryParameter(query, index, "=false", true);
	}
	
	private static boolean matchQueryParameter(String query, int index, String value,
			boolean ignoreCase) {
		int length = value.length();
		return query.regionMatches(ignoreCase, index, value, 0, length)
				&& (query.length() == index + length || query.charAt(index + length) == '&');
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
	
	private static final class GrammarIndexQuery {
		public static final String LETTER = "letter";
		public static final String TITLE = "title";
		public static final String COUNT = "count";
		
		public static final String[] COLUMNS = new String[] {
			LETTER, TITLE, COUNT
		};
		
		public static final int COLUMN_LETTER = 0;
		public static final int COLUMN_TITLE = 1;
		public static final int COLUMN_COUNT = 2;
		
		// The first letter of the sort key column is what is used for the index headings.
		public static final String SECTION_HEADING = "SUBSTR(%1$s,1,1)";
		
		public static final String ORDER_BY = LETTER + " COLLATE " + "PHONEBOOK";//PHONEBOOK_COLLATOR_NAME;
	}
	
	private static Bundle getIndexExtras(final SQLiteDatabase db, final SQLiteQueryBuilder qb, final String selection, final String[] selectionArgs,
			final String sortOrder) {
		String sortKey;
		
		// The sort order suffix could be something like "DESC".
		// We want to preserve it in the query even though we will change
		// the sort column itself.
		String sortOrderSuffix = "";
		if (sortOrder != null) {
			int spaceIndex = sortOrder.indexOf(' ');
			if (spaceIndex != -1) {
				sortKey = sortOrder.substring(0, spaceIndex);
				sortOrderSuffix = sortOrder.substring(spaceIndex);
			} else {
				sortKey = sortOrder;
			}
		} else {
			throw new IllegalArgumentException(
					"The sortOrder is null.");
		}
		
		HashMap<String, String> projectionMap = new HashMap<String, String>();//Maps.newHashMap();
		String sectionHeading = String.format(Locale.US, GrammarIndexQuery.SECTION_HEADING, sortKey);
		projectionMap.put(GrammarIndexQuery.LETTER,
				sectionHeading + " AS " + GrammarIndexQuery.LETTER);
		projectionMap.put(GrammarIndexQuery.TITLE,
				"GET_PHONEBOOK_INDEX(" + sectionHeading + ",'" + Locale.getDefault().toString() + "')"
						+ " AS " + GrammarIndexQuery.TITLE);
		projectionMap.put(GrammarIndexQuery.COUNT,
				"COUNT(*) AS " + GrammarIndexQuery.COUNT);
		qb.setProjectionMap(projectionMap);
		
		Cursor indexCursor = qb.query(db, GrammarIndexQuery.COLUMNS, selection, selectionArgs,
                GrammarIndexQuery.ORDER_BY, null /* having */,
                GrammarIndexQuery.ORDER_BY + sortOrderSuffix);
		
		try {
			int groupCount = indexCursor.getCount();
			String titles[] = new String[groupCount];
			int counts[] = new int[groupCount];
			int indexCount = 0;
			String currentTitle = null;
			
			// Since GET_PHONEBOOK_INDEX is a many-to-1 function, we may end up
			// with multiple entries for the same title.  The following code
			// collapses those duplicates.
			for (int i = 0; i < groupCount; i++) {
				indexCursor.moveToNext();
				String title = indexCursor.getString(GrammarIndexQuery.COLUMN_TITLE);
				if (title == null) {
					title = "";
				}
				
				int count = indexCursor.getInt(GrammarIndexQuery.COLUMN_COUNT);
				if (indexCount == 0 || !TextUtils.equals(title, currentTitle)) {
					titles[indexCount] = currentTitle = title;
					counts[indexCount] = count;
					indexCount++;
				} else {
					counts[indexCount - 1] += count;
				}
			}
			
			if (indexCount < groupCount) {
				String[] newTitles = new String[indexCount];
				System.arraycopy(titles, 0, newTitles, 0, indexCount);
				titles = newTitles;
				
				int[] newCounts = new int[indexCount];
				System.arraycopy(counts, 0, newCounts, 0, indexCount);
				counts = newCounts;
			}
			
			for (int j = 0; j < titles.length; j++) {
				Log.d(TAG, "title = " + titles[j] + " counts = " + counts[j]);
			}
			
			Bundle bundle = new Bundle();
			bundle.putStringArray(GrammarProviderContract.EXTRA_GRAMMAR_BOOK_INDEX_TITLES, titles);
			bundle.putIntArray(GrammarProviderContract.EXTRA_GRAMMAR_BOOK_INDEX_COUNTS, counts);
			return bundle;
		} finally {
			indexCursor.close();
		}
	}
}

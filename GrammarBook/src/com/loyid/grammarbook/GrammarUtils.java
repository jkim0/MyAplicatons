package com.loyid.grammarbook;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;

public class GrammarUtils {
	private static final String TAG = "GrammarUtils";
	
	private static DatabaseHelper sDatabaseHelper = null;
	
	private static final int GRAMMAR_TYPE_NOUN = 0;
	private static final int GRAMMAR_TYPE_VERB = 1;
	private static final int GRAMMAR_TYPE_ADJECTIVE = 2;
	private static final int GRAMMAR_TYPE_ADVERB = 3;
	private static final int GRAMMAR_TYPE_PREPOSITION = 4;
	private static final int GRAMMAR_TYPE_IDIOM = 5;
	
	private static final String STR_GRAMMAR_TYPE_NOUN = "noun";
	private static final String STR_GRAMMAR_TYPE_VERB = "verb";
	private static final String STR_GRAMMAR_TYPE_ADJECTIVE = "adjective";
	private static final String STR_GRAMMAR_TYPE_ADVERB = "adverb";
	private static final String STR_GRAMMAR_TYPE_PREPOSITION = "preposition";
	private static final String STR_GRAMMAR_TYPE_IDIOM = "idom";
	
	private static final String PREFIX_GRAMMAR_TYPE_NOUN = "#";
	private static final String PREFIX_GRAMMAR_TYPE_VERB = "@";
	private static final String PREFIX_GRAMMAR_TYPE_ADJECTIVE = "$";
	private static final String PREFIX_GRAMMAR_TYPE_ADVERB = "%";
	private static final String PREFIX_GRAMMAR_TYPE_PREPOSITION = "!";
	private static final String PREFIX_GRAMMAR_TYPE_IDIOM = "*";
	
	public static final int DEFAULT_MEANING_COUNT = 10;
	
	private static final HashMap<Integer, String> MAP_GRAMMAR_TYPE_TO_STRING = new HashMap<Integer, String> () { 
		{
			put(GRAMMAR_TYPE_NOUN, STR_GRAMMAR_TYPE_VERB);
			put(GRAMMAR_TYPE_VERB, STR_GRAMMAR_TYPE_NOUN);
			put(GRAMMAR_TYPE_ADJECTIVE, STR_GRAMMAR_TYPE_ADJECTIVE);
			put(GRAMMAR_TYPE_ADVERB, STR_GRAMMAR_TYPE_ADVERB);
			put(GRAMMAR_TYPE_PREPOSITION, STR_GRAMMAR_TYPE_PREPOSITION);
			put(GRAMMAR_TYPE_IDIOM, STR_GRAMMAR_TYPE_IDIOM);
		}
	};
	
	private static final HashMap<String, Integer> MAP_GRAMMAR_PREFIX_TO_TYPE = new HashMap<String, Integer> () { 
		{
			put(PREFIX_GRAMMAR_TYPE_NOUN, GRAMMAR_TYPE_NOUN);
			put(PREFIX_GRAMMAR_TYPE_VERB, GRAMMAR_TYPE_VERB);
			put(PREFIX_GRAMMAR_TYPE_ADJECTIVE, GRAMMAR_TYPE_ADJECTIVE);
			put(PREFIX_GRAMMAR_TYPE_ADVERB, GRAMMAR_TYPE_ADVERB);
			put(PREFIX_GRAMMAR_TYPE_PREPOSITION, GRAMMAR_TYPE_PREPOSITION);
			put(PREFIX_GRAMMAR_TYPE_IDIOM, GRAMMAR_TYPE_IDIOM);
		}
	};
	
	private static final HashMap<String, String> MAP_GRAMMAR_PREFIX_TO_STRING = new HashMap<String, String> () { 
		{
			put(PREFIX_GRAMMAR_TYPE_NOUN, STR_GRAMMAR_TYPE_VERB);
			put(PREFIX_GRAMMAR_TYPE_VERB, STR_GRAMMAR_TYPE_NOUN);
			put(PREFIX_GRAMMAR_TYPE_ADJECTIVE, STR_GRAMMAR_TYPE_ADJECTIVE);
			put(PREFIX_GRAMMAR_TYPE_ADVERB, STR_GRAMMAR_TYPE_ADVERB);
			put(PREFIX_GRAMMAR_TYPE_PREPOSITION, STR_GRAMMAR_TYPE_PREPOSITION);
			put(PREFIX_GRAMMAR_TYPE_IDIOM, STR_GRAMMAR_TYPE_IDIOM);
		}
	};
		
	public static class Meaning {
		public int mType;
		public String mTypeStr;
		public String mMeaning;
			
		public Meaning() {			
		}
		
		public Meaning(int type, String typeStr, String meaning) {
			mType = type;
			mTypeStr = typeStr;
			mMeaning = meaning;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Meanins {");
			sb.append("type = " + mType);
			sb.append(", typeStr = " + mTypeStr);
			sb.append(", meaning = " + mMeaning);
			sb.append("}");
			return sb.toString();
		}
		
		public boolean equals(Meaning target) {
			if (( mType == target.mType || mTypeStr.equals(target.mTypeStr))
					&& mMeaning.equals(target.mMeaning)) {
				return true;
			}
			return false;
		}
	}
	
	public static class Grammar {
		public String mGrammar;
		public ArrayList<Meaning> mMeanings = new ArrayList<Meaning>();
		
		public void addMeaing(Meaning newMean) {
			Log.d(TAG, "addMeaning new = " + newMean);
			for (int i = 0; i < mMeanings.size(); i++) {
				final Meaning oldMean = mMeanings.get(i);
				if (newMean.equals(oldMean)) {
					Log.d(TAG, newMean.toString() + "is Already exists for grammar = " + mGrammar);
					return;
				}
			}
			
			mMeanings.add(newMean);
		}
		
		public void addMeaing(int type, String typeStr, String meaning) {
			Meaning mean = new Meaning(type, typeStr, meaning);
			addMeaing(mean);
		}
		
		public void setGrammar(String grammar) {
			mGrammar = grammar;
		}
	}
	
	public static DatabaseHelper getDatabaseHelper(Context context) {
		if (sDatabaseHelper == null) 
			sDatabaseHelper = new DatabaseHelper(context);
		return sDatabaseHelper;
	}
	
	public static long getGrammarId(Context context, String strGrammar) {
		long grammarId = -1;
		
		DatabaseHelper dbHelper = getDatabaseHelper(context);
		String selection = GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR + " = ?";		
		String[] selectionArgs = { strGrammar };
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(GrammarProviderContract.Grammars.TABLE_NAME, 
				null,
				selection,
				selectionArgs,
				null,
				null,
				GrammarProviderContract.Grammars.DEFAULT_SORT_ORDER);
		
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int index = cursor.getColumnIndex(GrammarProviderContract.Grammars._ID);
				grammarId = cursor.getLong(index);
			}
			
			cursor.close();
		}
		
		return grammarId;
	}
	
	public static boolean addGrammar(Context context, Grammar grammar, Runnable doneCallback) {
		final String strGrammar = grammar.mGrammar.trim();
		if (strGrammar == null || strGrammar.length() == 0) {
			Log.e(TAG, "insert failed because grammar is not inputted.");
			return false;
		}
		
		DatabaseHelper dbHelper = getDatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		long grammarId = getGrammarId(context, strGrammar);
		
		Long now = Long.valueOf(System.currentTimeMillis());
		
		StringBuilder sb = null;
		ArrayList<Meaning> meanings = grammar.mMeanings;
		int count = meanings.size();
		
		if (count <= 0) {
			Log.e(TAG, "insert failed because there is no meaing fields.");
			return false;
		}
		
		if (grammarId < 0 && count > 0) {
			ContentValues values = new ContentValues();
			values.put(GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR, strGrammar);
			values.put(GrammarProviderContract.Grammars.COLUMN_NAME_CREATED_DATE, now);
			values.put(GrammarProviderContract.Grammars.COLUMN_NAME_MODIFIED_DATE, now);
			
			grammarId = db.insert(GrammarProviderContract.Grammars.TABLE_NAME, null, values);
		}
		
		String whereClause = GrammarProviderContract.Mappings.COLUMN_NAME_GRAMMAR_ID + " = ?";
		String[] whereArgs = { String.valueOf(grammarId) };
		int result = db.delete(GrammarProviderContract.Mappings.TABLE_NAME, whereClause, whereArgs);
		Log.d(TAG, "remove " + result + " items from Mappings table");
		
		for (int i = 0; i < count; i++) {
			Meaning meaning = meanings.get(i);
			int type = meaning.mType;
			String typeStr = meaning.mTypeStr.trim();
			String strMeaning = meaning.mMeaning.trim();
			
			if (strMeaning != null && strMeaning.length() > 0) {
				ContentValues values = new ContentValues();
				values.put(GrammarProviderContract.Meanings.COLUMN_NAME_WORD, strMeaning);
				values.put(GrammarProviderContract.Meanings.COLUMN_NAME_TYPE, type);

				String selection = GrammarProviderContract.Meanings.COLUMN_NAME_WORD + " = ?"
						+ " AND " + GrammarProviderContract.Meanings.COLUMN_NAME_TYPE + " = ?";

				String[] selectionArgs = new String[] { strMeaning, String.valueOf(type) };
				
				Cursor cursor = db.query(GrammarProviderContract.Meanings.TABLE_NAME, 
						null,
						selection,
						selectionArgs,
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
					values.put(GrammarProviderContract.Meanings.COLUMN_NAME_CREATED_DATE, now);
					values.put(GrammarProviderContract.Meanings.COLUMN_NAME_MODIFIED_DATE, now);
					meaningId = db.insert(GrammarProviderContract.Meanings.TABLE_NAME, null, values);
				} else {
					Log.d(TAG, "update meaning index = " + meaningId);
					whereClause = GrammarProviderContract.Meanings._ID + " = ?";
					whereArgs = new String[] { String.valueOf(meaningId) };
					values.put(GrammarProviderContract.Meanings.COLUMN_NAME_MODIFIED_DATE, now);
					db.update(GrammarProviderContract.Meanings.TABLE_NAME, values, whereClause, whereArgs);
				}
				
				values = new ContentValues();
				values.put(GrammarProviderContract.Mappings.COLUMN_NAME_GRAMMAR_ID, grammarId);
				values.put(GrammarProviderContract.Mappings.COLUMN_NAME_MEANING_ID, meaningId);
				db.insert(GrammarProviderContract.Mappings.TABLE_NAME, null, values);
				
				if (sb == null)
					sb = new StringBuilder();
				sb.append(type);
				sb.append("%" + typeStr);
				sb.append("%" + strMeaning);
				
				if (i != count - 1) {
					sb.append("#");
				}
			}
		
		}
		
		if (sb != null) {
			String strMeaning = sb.toString();
			Log.d(TAG, "############# saveGrammar Grammar = " + strGrammar + " meaning = " + strMeaning);
			
			ContentValues values = new ContentValues();
			values.put(GrammarProviderContract.Grammars.COLUMN_NAME_MEANING, strMeaning);
			values.put(GrammarProviderContract.Grammars.COLUMN_NAME_MODIFIED_DATE, now);
			
			Log.d(TAG, "update grammar index = " + grammarId);
			whereClause = GrammarProviderContract.Grammars._ID + " = ?";
			whereArgs = new String[] { String.valueOf(grammarId) };
				
			db.update(GrammarProviderContract.Grammars.TABLE_NAME, values, whereClause, whereArgs);			
		} else {
			Log.d(TAG, "failed to save Grammar data because there is no meanings");
			return false;
		}
		
		if (doneCallback != null)
			doneCallback.run();
		
		return true;
	}
	
	public static final boolean loadDataFromFile(Context context, Uri fileUri, Runnable callback) {
		Log.d(TAG, "loadDataFromFile uri = " + fileUri);
		
		try {
			InputStream is = context.getContentResolver().openInputStream(fileUri);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			boolean grammarStarted = false;
			boolean headerStarted = false;
			while(true) {
				String line = reader.readLine();
				if (line == null) break;
				
				if (line.startsWith("#")) {
					if (headerStarted) {
						grammarStarted = true;
					} else {
						headerStarted = true;
					}
					continue;
				}
				
				if (grammarStarted) {
					Grammar grammarData = new Grammar();
					String[] grammarGroup = line.split(":");
					String grammar = grammarGroup[0];
					grammarData.mGrammar = grammar;
					String[] meanings = grammarGroup[1].split(",");
					Log.d(TAG, "######### grammar = " + grammar);
					for (int i = 0; i < meanings.length; i++) {
						String tempMeaning = meanings[i].trim();
						String typeChar = tempMeaning.substring(0, 1);
						String meaning = tempMeaning.substring(1).trim();
						Log.d(TAG, "######## type = " + typeChar + " meaning = " + meaning);
						if (meaning == null || meaning.length() == 0)
							continue;
						grammarData.addMeaing(getGrammarTypeByPrefix(typeChar), getGrammarTypeStringByPrefix(typeChar), meaning);
					}
					
					if (grammarData.mMeanings.size() > 0) {
						addGrammar(context, grammarData, null);
					}
				} else if (headerStarted) {
					String[] prefixGroup = line.split(":");
					String prefix = prefixGroup[0].trim();
					String kind = prefixGroup[1].trim();
					Log.d(TAG, "######## prefix = " + prefix + " kind = " + kind);
				}
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Can not read from file");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		if (callback != null) {
			callback.run();
		}
		
		return true;
	}
	
	private static int getGrammarTypeByPrefix(String prefix) {
		return MAP_GRAMMAR_PREFIX_TO_TYPE.get(prefix);
	}
	
	private static String getGrammarTypeStringByPrefix(String prefix) {
		return MAP_GRAMMAR_PREFIX_TO_STRING.get(prefix);
	}
	
	private static String getGrammarTypeStringByType(int type) {
		return MAP_GRAMMAR_TYPE_TO_STRING.get(type);
	}
}

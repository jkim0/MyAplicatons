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

public class GrammarUtils {
	private static final String TAG = "GrammarUtils";
	
	private static DatabaseHelper sDatabaseHelper = null;
	
	public static final int GRAMMAR_TYPE_NOUN = 0;
	public static final int GRAMMAR_TYPE_VERB = 1;
	public static final int GRAMMAR_TYPE_ADJECTIVE = 2;
	public static final int GRAMMAR_TYPE_ADVERB = 3;
	public static final int GRAMMAR_TYPE_PREPOSITION = 4;
	public static final int GRAMMAR_TYPE_IDIOM = 5;
	
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
	
	public static final String IDENTIFIER_MEANING_GROUP = "#";
	public static final String IDENTIFIER_MEANING = "%";
	
	public static final int TYPE_TEST_OBJECTIVE = 0;
	public static final int TYPE_TEST_SUBJECTIVE = 1;
	
	public static final int TYPE_QUESTION_MEANING = 0;
	public static final int TYPE_QUESTION_GRAMMAR = 1;
	
	public static final int DEFAULT_MEANING_COUNT = 10;
	public static final int DEFAULT_TEST_COUNT = 20;
	public static final int DEFAULT_EXAMPLE_COUNT = 4;
	public static final int DEFAULT_ANSWER_COUNT = 1;
	
	private static final HashMap<Integer, String> MAP_GRAMMAR_TYPE_TO_STRING = new HashMap<Integer, String> () { 
		{
			put(GRAMMAR_TYPE_NOUN, STR_GRAMMAR_TYPE_NOUN);
			put(GRAMMAR_TYPE_VERB, STR_GRAMMAR_TYPE_VERB);
			put(GRAMMAR_TYPE_ADJECTIVE, STR_GRAMMAR_TYPE_ADJECTIVE);
			put(GRAMMAR_TYPE_ADVERB, STR_GRAMMAR_TYPE_ADVERB);
			put(GRAMMAR_TYPE_PREPOSITION, STR_GRAMMAR_TYPE_PREPOSITION);
			put(GRAMMAR_TYPE_IDIOM, STR_GRAMMAR_TYPE_IDIOM);
		}
	};
	
	private static final HashMap<String, Integer> MAP_GRAMMAR_STRING_TO_TYPE = new HashMap<String, Integer> () { 
		{
			put(STR_GRAMMAR_TYPE_NOUN, GRAMMAR_TYPE_NOUN);
			put(STR_GRAMMAR_TYPE_VERB, GRAMMAR_TYPE_VERB);
			put(STR_GRAMMAR_TYPE_ADJECTIVE, GRAMMAR_TYPE_ADJECTIVE);
			put(STR_GRAMMAR_TYPE_ADVERB, GRAMMAR_TYPE_ADVERB);
			put(STR_GRAMMAR_TYPE_PREPOSITION, GRAMMAR_TYPE_PREPOSITION);
			put(STR_GRAMMAR_TYPE_IDIOM, GRAMMAR_TYPE_IDIOM);
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
			put(PREFIX_GRAMMAR_TYPE_NOUN, STR_GRAMMAR_TYPE_NOUN);
			put(PREFIX_GRAMMAR_TYPE_VERB, STR_GRAMMAR_TYPE_VERB);
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
			sb.append("Meanins : {");
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
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Grammar : " + mGrammar + " : {");
			int size = mMeanings.size();
			for (int i = 0; i < size; i++) {
				sb.append(mMeanings.get(i).toString());
				if (i < size - 1) {
					sb.append(", ");
				}
			}
			sb.append("}");
			return sb.toString();
		}
	}

	public static class Question {
		public int mType = -1; // use only meaning question.
		public String mSubject = null;
		public int mExampleCount = 0; // use only objective question;
		public ArrayList<String> mExamples = null; // use only objective question.
		public ArrayList<String> mCorrectAnswerStr = null; // use only subjective question.
		public ArrayList<Integer> mCorrectAnswer = null; // use only objective question.
		public boolean mIsRight = false;
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Question : {");
			sb.append("type = " + mType);
			sb.append(", Subject = " + mSubject);
			sb.append(", ExampleCount = " + mExampleCount);
			if (mExamples != null) {
				sb.append(", mExamples { ");
				for (int i = 0; i < mExamples.size(); i++) {
					sb.append(mExamples.get(i));
					if (i < mExamples.size() - 1)
						sb.append(", ");
				}
				sb.append("}");
			}
			if (mCorrectAnswerStr != null) {
				sb.append(", mCorrectAnswerStr { ");
				for (int i = 0; i < mCorrectAnswerStr.size(); i++) {
					sb.append(mCorrectAnswerStr.get(i));
					if (i < mCorrectAnswerStr.size() - 1)
						sb.append(", ");
				}
				sb.append("}");
			}
			
			if (mCorrectAnswer != null) {
				sb.append(", mCorrectAnswer { ");
				for (int i = 0; i < mCorrectAnswer.size(); i++) {
					sb.append(mCorrectAnswer.get(i));
					if (i < mCorrectAnswer.size() - 1)
						sb.append(", ");
				}
				sb.append("}");
			}
			
			sb.append(", isRight = " + mIsRight);
			sb.append("}");
			
			return sb.toString();
		}
	}
	
	public static class Questions {
		public int mTestType = -1;
		public int mQuestionType = -1;
		public int mScore = 0;
		public int mCount = 0;
		public int mSolvedCount = 0;
		public ArrayList<Question> mQuestions = null;
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Questions : {");
			sb.append("TestType = " + mTestType);
			sb.append(", QuestionType = " + mQuestionType);
			sb.append(", Score = " + mScore);
			if (mQuestions != null) {
				sb.append(", Questons = {");
				for (int i = 0; i < mQuestions.size(); i++) {
					sb.append(mQuestions.get(i).toString());
					if (i < mQuestions.size() - 1)
						sb.append(", ");
				}
				sb.append("}");
			}
			sb.append("}");
			return sb.toString();
		}
	}
	
	public static DatabaseHelper getDatabaseHelper(Context context) {
		if (sDatabaseHelper == null) 
			sDatabaseHelper = new DatabaseHelper(context);
		return sDatabaseHelper;
	}
	
	public static long getGrammarId(Context context, String strGrammar) {
		long grammarId = -1;
		
		String[] projection = { GrammarProviderContract.Grammars._ID };
		String selection = GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR + " = ?";		
		String[] selectionArgs = { strGrammar };
		
		Cursor cursor = context.getContentResolver().query(GrammarProviderContract.Grammars.CONTENT_URI,
				projection, selection, selectionArgs, GrammarProviderContract.Grammars.DEFAULT_SORT_ORDER);
		
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
		
		ArrayList<Meaning> meanings = grammar.mMeanings;
		int count = meanings.size();
		
		if (count <= 0) {
			Log.e(TAG, "insert failed because there is no meaing fields.");
			return false;
		}
		
		ContentValues values = new ContentValues();
		values.put(GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR, strGrammar);
		String meaningStr = "";
		for (int i = 0; i < count; i++) {
			Meaning m = meanings.get(i);
			meaningStr += m.mType + "%" + m.mMeaning;
			if (i < count - 1) {
				meaningStr += "#";
			}			
		}
		values.put(GrammarProviderContract.Grammars.COLUMN_NAME_MEANING, meaningStr);
		if (grammarId < 0 && count > 0) {			
			context.getContentResolver().insert(GrammarProviderContract.Grammars.CONTENT_URI, values);
		} else {
			context.getContentResolver().update(
					Uri.withAppendedPath(GrammarProviderContract.Grammars.CONTENT_GRAMMAR_ID_URI_BASE, "" + grammarId),
					values, null, null);
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
	
	public static int getGrammarTypeByPrefix(String prefix) {
		return MAP_GRAMMAR_PREFIX_TO_TYPE.get(prefix.toLowerCase());
	}
	
	public static String getGrammarTypeStringByPrefix(String prefix) {
		return MAP_GRAMMAR_PREFIX_TO_STRING.get(prefix.toLowerCase());
	}
	
	public static String getGrammarTypeStringByType(int type) {
		return MAP_GRAMMAR_TYPE_TO_STRING.get(type);
	}
	
	public static int getGrammarTypeByString(String type) {
		return MAP_GRAMMAR_STRING_TO_TYPE.get(type.toLowerCase());
	}
	
	public static Grammar getGrammarInfo(Context context, long grammarId) {
		if (grammarId < 0) {
			Log.e(TAG, "failed to getGrammarInfo grammarId = " + grammarId);
			return null;
		}
		
		Grammar grammarInfo = new Grammar();
		DatabaseHelper dbHelper = getDatabaseHelper(context);
		
		String selection = GrammarProviderContract.Grammars._ID + " = ?";		
		String[] selectionArgs = { String.valueOf(grammarId) };
		
		String[] projection = {
				GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR,
				GrammarProviderContract.Grammars.COLUMN_NAME_MEANING
		};
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = context.getContentResolver().query(GrammarProviderContract.Grammars.CONTENT_URI,
				projection, selection, selectionArgs, GrammarProviderContract.Grammars.DEFAULT_SORT_ORDER);
		
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int grammarColumnIndex = cursor.getColumnIndex(GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR);
				int meaningColumnIndex = cursor.getColumnIndex(GrammarProviderContract.Grammars.COLUMN_NAME_MEANING);
				grammarInfo.mGrammar = cursor.getString(grammarColumnIndex);
				String meanings = cursor.getString(meaningColumnIndex);
				String[] meaningGroup = meanings.split(GrammarUtils.IDENTIFIER_MEANING_GROUP);
				for (int i = 0; i < meaningGroup.length; i++) {
					String[] splits = meaningGroup[i].split(GrammarUtils.IDENTIFIER_MEANING);
					int type;
					String typeStr;
					String mean;
					if (splits.length > 2) {
						type = Integer.valueOf(splits[0]);
						typeStr = splits[1];
						mean = splits[2];
					} else {
						type = Integer.valueOf(splits[0]);
						typeStr = GrammarUtils.getGrammarTypeStringByType(type);
						mean = splits[1];
					}
					grammarInfo.addMeaing(type, typeStr, mean);
				}
			}
			
			cursor.close();
		}
				
		return grammarInfo;
	}
	
	public static Questions generateTestSource(Context context, int testType, int questionType,
			int questionCount, int exampleCount, int answerCount) {
		Log.e(TAG, "generateTestSource testType = " + testType + " questionType = " + questionType + " questionCount = " + questionCount
				+ " exampleCount = " + exampleCount + " answerCount = " + answerCount);
		DatabaseHelper dbHelper = getDatabaseHelper(context);
		
		if (testType < 0 || testType == 0 && questionType < 0) {
			Log.e(TAG, "failed to generateTestSource testType = " + testType + " questionType = " + questionType);
			return null;
		}
		
		String tableName = null;
		String[] projections = null;
		String subjectColumnName = null;
		String indexColumnName = null;
		String typeColumnName = GrammarProviderContract.Meanings.COLUMN_NAME_TYPE;
		String exampleTableName = null;
		String[] exampleProjections = null;
		boolean needExample = true;
				
		if (testType == TYPE_TEST_SUBJECTIVE) {
			questionType = TYPE_QUESTION_GRAMMAR;
			needExample = false;
		}
		
		if (questionType == TYPE_QUESTION_MEANING) {
			tableName = GrammarProviderContract.Grammars.TABLE_NAME;
			subjectColumnName = GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR;
			indexColumnName = GrammarProviderContract.Grammars._ID;
			projections = new String[] {
					GrammarProviderContract.Grammars._ID,
					GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR
			};
		} else if (questionType == TYPE_QUESTION_GRAMMAR) {
			tableName = GrammarProviderContract.Meanings.TABLE_NAME;
			subjectColumnName = GrammarProviderContract.Meanings.COLUMN_NAME_WORD;
			indexColumnName = GrammarProviderContract.Meanings._ID;
			projections = new String[] {
					GrammarProviderContract.Meanings._ID,
					GrammarProviderContract.Meanings.COLUMN_NAME_WORD,
					GrammarProviderContract.Meanings.COLUMN_NAME_TYPE
			};
		} else {
			Log.e(TAG, "failed to generateTestSource testType = " + testType + " questionType = " + questionType);
			return null;
		}
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		Cursor cursor = db.query(tableName, 
				projections,
				null,
				null,
				null,
				null,
				"RANDOM()",
				String.valueOf(questionCount));
		
		boolean generateSuccess = true;
		Questions questions = new Questions();
		questions.mQuestionType = questionType;
		questions.mTestType = testType;
		questions.mCount = questionCount;
		
		questions.mQuestions = new ArrayList<Question>();
		
		if (cursor != null) {
			int cursorCount = cursor.getCount();
			if (cursor.moveToFirst()) {		
				int IDColumnIndex = cursor.getColumnIndex(indexColumnName);
				int subjectColumnIndex = cursor.getColumnIndex(subjectColumnName);
				int typeColumnIndex = cursor.getColumnIndex(typeColumnName);
				
				for (int i = 0 ; i < questionCount; i++) {
					long subjectId = cursor.getLong(IDColumnIndex);
					String subject = cursor.getString(subjectColumnIndex);
					Question question = new Question();
					question.mSubject = subject;
					
					if (questionType == TYPE_QUESTION_GRAMMAR) {							
						question.mType = cursor.getInt(typeColumnIndex);
					}
						
					if (testType == TYPE_TEST_OBJECTIVE) {
						ArrayList<String> exams = new ArrayList<String>();
						ArrayList<Integer> correct = getExamples(context, questionType, subjectId, exams, exampleCount, answerCount);
						
						if (correct == null || correct.size() <= 0 || exams.size() <= 0) {
							Log.e(TAG, "failed to generateTestSource while generate examples.");								
							generateSuccess = false;
							break;
						}
						question.mCorrectAnswer = correct;
						question.mExamples = exams;
						question.mExampleCount = exampleCount;
					} else if (testType == TYPE_TEST_SUBJECTIVE){
						ArrayList<String> correct = getGrammarsByMeaningId(context, subjectId);
						if (correct == null || correct.size() <= 0) {
							Log.e(TAG, "failed to generateTestSource while generate examples.");
							generateSuccess = false;
							break;
						}
						question.mCorrectAnswerStr = correct;
					}
					questions.mQuestions.add(question);
					if (i < cursorCount) {
						cursor.moveToNext();
					} else {
						int next = (int)(Math.random() * cursorCount);
						cursor.move(next);
					}
				}
			} else {
				Log.e(TAG, "failed to generateTestSource because cursor exception.");
				generateSuccess = false;
			}
			
			cursor.close();
		}
		
		if (!generateSuccess) return null;
		
		return questions;
	}
	
	private static ArrayList<Integer> getExamples(Context context, int questionType, long id,
			ArrayList<String> exams, int exampleCount, int answerCount) {
		Log.e(TAG, "getExamples questionType = " + questionType + " id = " + id + " exampleCount = " + exampleCount + " answerCount = " + answerCount);
		DatabaseHelper dbHelper = getDatabaseHelper(context);
		if (id < 0) {
			Log.e(TAG, "failed to getExamples id = " + id);
			return null;
		}
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String answerSelection = null;
		String exampleSelection = null;
		String tableName = null;
		String[] selectionArgs = { String.valueOf(id) };
		String exampleColumnName = null;
		
		if (questionType == TYPE_QUESTION_GRAMMAR) {
			exampleColumnName = GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR;
			tableName = GrammarProviderContract.Grammars.TABLE_NAME;
			
			String mappingSelection = "SELECT " + GrammarProviderContract.Mappings.COLUMN_NAME_GRAMMAR_ID
					+ " FROM " + GrammarProviderContract.Mappings.TABLE_NAME
					+ " WHERE " + GrammarProviderContract.Mappings.COLUMN_NAME_MEANING_ID + " = ?";
			
			answerSelection = GrammarProviderContract.Grammars._ID + " IN (" + mappingSelection + ")";
			exampleSelection = GrammarProviderContract.Grammars._ID + " NOT IN (" + mappingSelection + ")"; 
		} else if (questionType == TYPE_QUESTION_MEANING) {
			tableName = GrammarProviderContract.Meanings.TABLE_NAME;
			exampleColumnName = GrammarProviderContract.Meanings.COLUMN_NAME_WORD;
			String mappingSelection = "SELECT " + GrammarProviderContract.Mappings.COLUMN_NAME_MEANING_ID
					+ " FROM " + GrammarProviderContract.Mappings.TABLE_NAME
					+ " WHERE " + GrammarProviderContract.Mappings.COLUMN_NAME_GRAMMAR_ID + " = ?";
			answerSelection = GrammarProviderContract.Meanings._ID + " IN (" + mappingSelection + ")";
			exampleSelection = GrammarProviderContract.Meanings._ID + " NOT IN (" + mappingSelection + ")";
		} else {
			Log.e(TAG, "failed to getExamples questionType = " + questionType);
			return null;
		}
		
		String[] projections = { exampleColumnName };
		
		Cursor cursor = db.query(tableName, 
				projections,
				exampleSelection,
				selectionArgs,
				null,
				null,
				"RANDOM()",
				String.valueOf(exampleCount));
		
		if (cursor != null) {
			int cursorCount = cursor.getCount();
			int exampleColumnIndex = cursor.getColumnIndex(exampleColumnName); 
			if (cursor.moveToFirst()) {
				for (int i = 0; i < exampleCount; i++) {
					String answer = cursor.getString(exampleColumnIndex);
					exams.add(answer);
					if (i < cursorCount) {
						cursor.moveToNext();
					} else {
						int next = (int)(Math.random() * cursorCount);
						cursor.move(next);
					}
				}
			}
			cursor.close();
		} else {
			Log.e(TAG, "failed to query for examples in getExamples().");
			return null;
		}
		
		cursor = db.query(tableName, 
				projections,
				answerSelection,
				selectionArgs,
				null,
				null,
				"RANDOM()",
				String.valueOf(answerCount));
		
		ArrayList<Integer> correct = new ArrayList<Integer>();
		if (cursor != null) {
			int cursorCount = cursor.getCount();
			int exampleColumnIndex = cursor.getColumnIndex(exampleColumnName); 
			if (cursor.moveToFirst()) {
				for (int i = 0; i < cursorCount; i++) {
					String answer = cursor.getString(exampleColumnIndex);
					int index = -1;
					do {
						index = (int)(Math.random() * exampleCount);
					} while(index >= exampleCount || correct.contains(index));
					
					correct.add(index);
					exams.set(index, answer);
				}
			}
			cursor.close();
		}
		
		if (correct == null || correct.size() <= 0) {
			Log.e(TAG, "failed to query for correct in getExamples().");
			return null;
		}
		
		return correct;
	}
	
	public static ArrayList<String> getGrammarsByMeaningId(Context context, long id) {
		DatabaseHelper dbHelper = getDatabaseHelper(context);
		
		if (id < 0) {
			Log.e(TAG, "failed to getGrammarsByMeaningId id = " + id);
			return null;
		}
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		String [] projections = {
			GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR
		};
		
		String selection = GrammarProviderContract.Grammars._ID + " IN ("
				+ "SELECT " + GrammarProviderContract.Mappings.COLUMN_NAME_GRAMMAR_ID
				+ " FROM " + GrammarProviderContract.Mappings.TABLE_NAME
				+ " WHERE " + GrammarProviderContract.Mappings.COLUMN_NAME_MEANING_ID + " = ?)";
		String[] selectionArgs = { String.valueOf(id) };
		
		Cursor cursor = db.query(GrammarProviderContract.Grammars.TABLE_NAME, 
				projections,
				selection,
				selectionArgs,
				null,
				null,
				GrammarProviderContract.Grammars.DEFAULT_SORT_ORDER);
		
		ArrayList<String> result = null;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int cursorCount = cursor.getCount();
				int columnIndex = cursor.getColumnIndex(GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR);
				result = new ArrayList<String>();
				for (int i = 0; i < cursorCount; i++) {
					result.add(cursor.getString(columnIndex));
				}
			}
			cursor.close();
		}		
		
		return result;
	}
	
	public static String getTypeString(Context context, int type) {
		String typeStr = context.getString(R.string.type_unknown);
		switch(type) {
		case GrammarUtils.GRAMMAR_TYPE_NOUN:
			typeStr = context.getString(R.string.label_grammar_type_noun);
			break;
		case GrammarUtils.GRAMMAR_TYPE_VERB:
			typeStr = context.getString(R.string.label_grammar_type_verb);
			break;
		case GrammarUtils.GRAMMAR_TYPE_ADJECTIVE:
			typeStr = context.getString(R.string.label_grammar_type_adjective);
			break;
		case GrammarUtils.GRAMMAR_TYPE_ADVERB:
			typeStr = context.getString(R.string.label_grammar_type_adverb);
			break;
		case GrammarUtils.GRAMMAR_TYPE_PREPOSITION:
			typeStr = context.getString(R.string.label_grammar_type_preposition);
			break;
		case GrammarUtils.GRAMMAR_TYPE_IDIOM:
			typeStr = context.getString(R.string.label_grammar_type_idiom);
			break;
		}
		return typeStr;
	}
	
	public static boolean deleteGrammar(Context context, long grammarId) {
		Log.d(TAG, "deleteGrammar id = " + grammarId);
		DatabaseHelper dbHelper = getDatabaseHelper(context);
		
		if (grammarId < 0) {
			Log.e(TAG, "failed to deleteGrammar id = " + grammarId);
			return false;
		}
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		String whereClause = GrammarProviderContract.Grammars._ID + " = ?";
		String[] whereArgs = { String.valueOf(grammarId) };
		
		int count = db.delete(GrammarProviderContract.Grammars.TABLE_NAME, whereClause, whereArgs);
		
		return count > 0 ? true : false;
	}
}

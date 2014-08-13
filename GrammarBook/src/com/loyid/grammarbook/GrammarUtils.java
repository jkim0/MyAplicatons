package com.loyid.grammarbook;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

public class GrammarUtils {
	private static final String TAG = "GrammarUtils";
	
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
	private static final String PREFIX_MEANING_SEPARATOR = "separator";
	
	private static final String DEFAULT_MEANING_SEPARATOR = "|";
	
	public static final String IDENTIFIER_MEANING_GROUP = "¤";
	public static final String IDENTIFIER_MEANING = "¡";
	
	public static final int TYPE_TEST_OBJECTIVE = 0;
	public static final int TYPE_TEST_SUBJECTIVE = 1;
	
	public static final int TYPE_QUESTION_MEANING = 0;
	public static final int TYPE_QUESTION_GRAMMAR = 1;
	
	public static final int DEFAULT_MEANING_COUNT = 10;
	public static final int DEFAULT_TEST_COUNT = 20;
	public static final int DEFAULT_EXAMPLE_COUNT = 4;
	public static final int DEFAULT_ANSWER_COUNT = 1;
	
	public static final int MAX_TRY_COUNT = 2;
	
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
	
	private static final HashMap<Integer, String> MAP_GRAMMAR_TYPE_TO_PREFIX = new HashMap<Integer, String> () { 
		{
			put(GRAMMAR_TYPE_NOUN, PREFIX_GRAMMAR_TYPE_NOUN);
			put(GRAMMAR_TYPE_VERB, PREFIX_GRAMMAR_TYPE_VERB);
			put(GRAMMAR_TYPE_ADJECTIVE, PREFIX_GRAMMAR_TYPE_ADJECTIVE);
			put(GRAMMAR_TYPE_ADVERB, PREFIX_GRAMMAR_TYPE_ADVERB);
			put(GRAMMAR_TYPE_PREPOSITION, PREFIX_GRAMMAR_TYPE_PREPOSITION);
			put(GRAMMAR_TYPE_IDIOM, PREFIX_GRAMMAR_TYPE_IDIOM);
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
		
		public void addMeaning(Meaning newMean) {
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
		
		public void addMeaning(int type, String typeStr, String meaning) {
			Meaning mean = new Meaning(type, typeStr, meaning);
			addMeaning(mean);
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
		public int mTryCount = 0;
		public ArrayList<Integer> mObjAnswer = null;
		public ArrayList<String> mSubjAnswer = null;
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Question : {");
			sb.append("type = " + mType);
			sb.append(", Subject = " + mSubject);
			sb.append(", ExampleCount = " + mExampleCount);
			sb.append(", mExamples { ");
			if (mExamples != null) {
				for (int i = 0; i < mExamples.size(); i++) {
					sb.append(mExamples.get(i));
					if (i < mExamples.size() - 1)
						sb.append(", ");
				}
			} else {
				sb.append("no examples");
			}

			sb.append(" }");
			
			sb.append(", mCorrectAnswerStr { ");
			if (mCorrectAnswerStr != null) {
				for (int i = 0; i < mCorrectAnswerStr.size(); i++) {
					sb.append(mCorrectAnswerStr.get(i));
					if (i < mCorrectAnswerStr.size() - 1)
						sb.append(", ");
				}
			} else {
				sb.append("no correct answer");
			}

			sb.append(" }");
			
			sb.append(", mCorrectAnswer { ");
			if (mCorrectAnswer != null) {
				for (int i = 0; i < mCorrectAnswer.size(); i++) {
					sb.append(mCorrectAnswer.get(i));
					if (i < mCorrectAnswer.size() - 1)
						sb.append(", ");
				}
			} else {
				sb.append("no answer");
			}
			sb.append(" }");
			
			sb.append(", isRight = " + mIsRight);
			sb.append(", tryCount = " + mTryCount);
			sb.append("}");
			
			return sb.toString();
		}
	}
	
	public static class Questions {
		public int mTestType = -1;
		public int mQuestionType = -1;
		public int mCount = 0;
		public int mSolvedCount = 0;
		public ArrayList<Question> mQuestions = null;
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Questions : {");
			sb.append("TestType = " + mTestType);
			sb.append(", QuestionType = " + mQuestionType);
			sb.append(", Count = " + mCount);
			sb.append(", SolvedCount = " + mSolvedCount);
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
		
		public boolean deleteSavedFile(Context context) {
			String fileName = getTestFilePath(context, mTestType, mQuestionType);
			
			if (fileName == null) {
				Log.e(TAG, "failed to get dir for data file");
				return false;
			}
			
			File dataFile = new File(fileName);
			if (dataFile.exists() && dataFile.delete()) {
				Log.d(TAG, "data file deleted.");
			}
			return true;
		}
		
		public boolean saveToFile(Context context) {
			String fileName = getTestFilePath(context, mTestType, mQuestionType);
			
			if (fileName == null) {
				Log.e(TAG, "failed to get dir for data file");
				return false;
			}
			
			File dataFile = new File(fileName);
			
			try {
				dataFile.createNewFile();
			} catch (IOException ex) {
				Log.e(TAG, "failed to create data file", ex);
				return false;
			}
			
			FileOutputStream fos = null;
			try{
				fos = new FileOutputStream(dataFile);
			}catch(FileNotFoundException e){
				Log.e(TAG, "failed to create file output stream");
				return false;
			}
			
			XmlSerializer serializer = Xml.newSerializer();
			try {
				serializer.setOutput(fos, "UTF-8");
				serializer.startDocument("UTF-8", true);
				serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
				serializer.startTag(null, "Questions");
				serializer.attribute(null, "test_type", String.valueOf(mTestType));
				serializer.attribute(null, "question_type", String.valueOf(mQuestionType));
				serializer.attribute(null, "count", String.valueOf(mCount));
				serializer.attribute(null, "solved_count", String.valueOf(mSolvedCount));
				
				for (int i = 0; i < mQuestions.size(); i++) {
					Question question = mQuestions.get(i);
					serializer.startTag(null, "Question");
					serializer.attribute(null, "type", String.valueOf(question.mType));
					serializer.attribute(null, "subject", question.mSubject);
					serializer.attribute(null, "is_right", String.valueOf(question.mIsRight));
					serializer.attribute(null, "try_count", String.valueOf(question.mTryCount));
					
					int id = 0;
					if (mTestType == TYPE_TEST_OBJECTIVE) {
						serializer.startTag(null, "examples");
						serializer.attribute(null, "example_count", String.valueOf(question.mExampleCount));
						for (id = 0; id < question.mExampleCount; id++) {
							serializer.startTag(null, "example");
							serializer.attribute(null, "data", question.mExamples.get(id));
							serializer.endTag(null, "example");
						}
						serializer.endTag(null, "examples");
						
						serializer.startTag(null, "correct_answers");
						serializer.attribute(null, "count", String.valueOf(question.mCorrectAnswer.size()));
						for (id = 0; id < question.mCorrectAnswer.size(); id++) {
							serializer.startTag(null, "answer");
							serializer.attribute(null, "data", String.valueOf(question.mCorrectAnswer.get(id)));
							serializer.endTag(null, "answer");
						}
						serializer.endTag(null, "correct_answers");
						
						serializer.startTag(null, "answers");
						int answeredCount = question.mObjAnswer == null ? 0 : question.mObjAnswer.size();
						serializer.attribute(null, "count", String.valueOf(answeredCount));
						for (id = 0; id < answeredCount; id++) {
							serializer.startTag(null, "answer");
							serializer.attribute(null, "data", String.valueOf(question.mObjAnswer.get(id)));
							serializer.endTag(null, "answer");
						}
						serializer.endTag(null, "answers");
						
					} else {
						serializer.startTag(null, "correct_answers");
						serializer.attribute(null, "count", String.valueOf(question.mCorrectAnswerStr.size()));
						for (id = 0; id < question.mCorrectAnswerStr.size(); id++) {
							serializer.startTag(null, "answer");
							serializer.attribute(null, "data", question.mCorrectAnswerStr.get(id));
							serializer.endTag(null, "answer");
						}
						serializer.endTag(null, "correct_answers");
						
						serializer.startTag(null, "answers");
						int answeredCount = question.mSubjAnswer == null ? 0 : question.mSubjAnswer.size();
						serializer.attribute(null, "count", String.valueOf(answeredCount));
						for (id = 0; id < answeredCount; id++) {
							serializer.startTag(null, "answer");
							serializer.attribute(null, "data", question.mSubjAnswer.get(id));
							serializer.endTag(null, "answer");
						}
						serializer.endTag(null, "answers");
					}
					
					serializer.endTag(null, "Question");
				}
				serializer.endTag(null, "Questions");
				serializer.endDocument();
				serializer.flush();				
			} catch (Exception ex) {
				Log.e(TAG, "failed to data serialize", ex);
				return false;
			}
			
			try {
				if (fos != null)
					fos.close();
			} catch (IOException ex) {
				Log.d(TAG, "failed to close file output stream");
			}
			
			return true;
		}
	}
	
	public static String getTestDirPath(Context context) {
		String dirPath = context.getFilesDir().getAbsolutePath() + "/tests";
		File dir = new File(dirPath);
		if( !dir.exists() && !dir.mkdirs()) {
			Log.e(TAG, "failed to make dir for data file");
			return null;
		}
		
		return dirPath;
	}
	
	public static String getTestFilePath(Context context, int testType, int questionType) {
		String dirPath = getTestDirPath(context);
		if( dirPath == null) {
			Log.e(TAG, "failed to get dir for data file");
			return null;
		}
		
		String filename = "test";
		if (testType == TYPE_TEST_OBJECTIVE) {
			filename += "_objective";
			if (questionType == TYPE_QUESTION_MEANING) {
				filename += "_meaning";
			} else {
				filename += "_grammar";
			}
		} else {
			filename += "_subjetive";
		}
		
		String fileName = dirPath + "/" + filename + ".xml";
		
		return fileName;
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
		
		long grammarId = getGrammarId(context, strGrammar);
		
		ArrayList<Meaning> meanings = grammar.mMeanings;
		int count = meanings.size();
		
		if (count <= 0) {
			Log.e(TAG, "insert failed because there is no meaning fields.");
			return false;
		}
		
		ContentValues values = new ContentValues();
		values.put(GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR, strGrammar);
		String meaningStr = "";
		for (int i = 0; i < count; i++) {
			Meaning m = meanings.get(i);
			meaningStr += m.mType + IDENTIFIER_MEANING + m.mMeaning;
			if (i < count - 1) {
				meaningStr += IDENTIFIER_MEANING_GROUP;
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
			String separator = DEFAULT_MEANING_SEPARATOR;
			while(true) {
				String line = reader.readLine();
				if (line == null) break;
				
				if (line.contains("######")) {
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
					String[] meanings = grammarGroup[1].split("[" + separator + "]");
					Log.d(TAG, "######### grammar = " + grammar);
					for (int i = 0; i < meanings.length; i++) {
						String tempMeaning = meanings[i].trim();
						String typeChar = tempMeaning.substring(0, 1);
						String meaning = tempMeaning.substring(1).trim();
						Log.d(TAG, "######## type = " + typeChar + " meaning = " + meaning);
						if (meaning == null || meaning.length() == 0)
							continue;
						grammarData.addMeaning(getGrammarTypeByPrefix(typeChar), getGrammarTypeStringByPrefix(typeChar), meaning);
					}
					
					if (grammarData.mMeanings.size() > 0) {
						addGrammar(context, grammarData, null);
					}
				} else if (headerStarted) {
					String[] prefixGroup = line.split(":");
					String prefix = prefixGroup[0].trim();
					String kind = prefixGroup[1].trim();
					if (prefix.equals(PREFIX_MEANING_SEPARATOR)) {
						separator = kind;
					}
					Log.d(TAG, "######## prefix = " + prefix + " kind = " + kind);
				}
			}
			reader.close();
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
	
	public static String getGrammarPrefixByType(int type) {
		return MAP_GRAMMAR_TYPE_TO_PREFIX.get(type);
	}
	
	public static Grammar getGrammarInfo(Context context, long grammarId) {
		if (grammarId < 0) {
			Log.e(TAG, "failed to getGrammarInfo grammarId = " + grammarId);
			return null;
		}
		
		Grammar grammarInfo = new Grammar();
		
		String selection = GrammarProviderContract.Grammars._ID + " = ?";		
		String[] selectionArgs = { String.valueOf(grammarId) };
		
		String[] projection = {
				GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR,
				GrammarProviderContract.Grammars.COLUMN_NAME_MEANING
		};
		
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
					grammarInfo.addMeaning(type, typeStr, mean);
				}
			}
			
			cursor.close();
		}
				
		return grammarInfo;
	}
	
	private static ArrayList<String> readExample(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, null, "examples");
		ArrayList<String> examples = new ArrayList<String>();
		
		int exampleCount = Integer.valueOf(parser.getAttributeValue(null, "example_count"));
		
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }			
			
			parser.require(XmlPullParser.START_TAG, null, "example");
			String e = parser.getAttributeValue(null, "data");
			examples.add(parser.getAttributeValue(null, "data"));
			parser.nextTag();
			parser.require(XmlPullParser.END_TAG, null, "example");
		}
		parser.require(XmlPullParser.END_TAG, null, "examples");
		
		return examples;
	}
	
	private static ArrayList<Integer> readObjectAnswerList(XmlPullParser parser, String name) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, null, name);
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		int count = Integer.valueOf(parser.getAttributeValue(null, "count"));
		
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }			
			
			parser.require(XmlPullParser.START_TAG, null, "answer");
			list.add(Integer.valueOf(parser.getAttributeValue(null, "data")));
			parser.nextTag();
			parser.require(XmlPullParser.END_TAG, null, "answer");
		}
		parser.require(XmlPullParser.END_TAG, null, name);
		
		return list;
	}
	
	private static ArrayList<String> readSubjectAnswerList(XmlPullParser parser, String name) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, null, name);
		ArrayList<String> list = new ArrayList<String>();
		
		int count = Integer.valueOf(parser.getAttributeValue(null, "count"));
		
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }			
			
			parser.require(XmlPullParser.START_TAG, null, "answer");
			list.add(parser.getAttributeValue(null, "data"));
			parser.nextTag();
			parser.require(XmlPullParser.END_TAG, null, "answer");
		}
		parser.require(XmlPullParser.END_TAG, null, name);
		
		return list;
	}
	
	private static Question readQuestion(XmlPullParser parser, int testType) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, null, "Question");
		Question question = new Question();
		
		question.mType = Integer.valueOf(parser.getAttributeValue(null, "type"));
		question.mSubject = parser.getAttributeValue(null, "subject");
		question.mIsRight = Boolean.valueOf(parser.getAttributeValue(null, "is_right"));
		question.mTryCount = Integer.valueOf(parser.getAttributeValue(null, "try_count"));
		Log.d(TAG, "name = " + parser.getName() + " type = " + question.mType + " subject = " + question.mSubject
				+ " isRight = " + question.mIsRight + " tryCount = " + question.mTryCount);
		
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
			
			String name = parser.getName();
			
			if (name.equals("examples")) {
				question.mExamples = readExample(parser);
				question.mExampleCount = question.mExamples.size();
			} else if (name.equals("correct_answers")) {
				if (testType == TYPE_TEST_OBJECTIVE) {
					question.mCorrectAnswer = readObjectAnswerList(parser, name);
				} else {
					question.mCorrectAnswerStr = readSubjectAnswerList(parser, name);
				}
			} else if (name.equals("answers")) {
				if (testType == TYPE_TEST_OBJECTIVE) {
					question.mObjAnswer = readObjectAnswerList(parser, name);
				} else {
					question.mSubjAnswer = readSubjectAnswerList(parser, name);
				}				
			}
		}
		Log.d(TAG, "question = " + question);
		parser.require(XmlPullParser.END_TAG, null, "Question");
		return question;
	}
	
	private static Questions readQuestions(XmlPullParser parser) throws XmlPullParserException, IOException {
		Questions questions = new Questions();
		
		parser.require(XmlPullParser.START_TAG, null, "Questions");
		questions.mTestType = Integer.valueOf(parser.getAttributeValue(null, "test_type"));
		questions.mQuestionType = Integer.valueOf(parser.getAttributeValue(null, "question_type"));
		questions.mCount = Integer.valueOf(parser.getAttributeValue(null, "count"));
		questions.mSolvedCount = Integer.valueOf(parser.getAttributeValue(null, "solved_count"));
		Log.d(TAG, "name = " + parser.getName() + " testType = " + questions.mTestType + " questionType = " + questions.mQuestionType
				+ " count = " + questions.mCount + " solved count = " + questions.mSolvedCount);
		questions.mQuestions = new ArrayList<Question>();
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
			
			String name = parser.getName();
			if (name.equals("Question")) {
				questions.mQuestions.add(readQuestion(parser, questions.mTestType));
			}
		}
		
		return questions;
	}
	
	public static Questions generateTestSourceFromFile(Context context, int testType, int questionType) {
		Log.e(TAG, "generateTestSourceFromFile testType = " + testType + " questionType = " + questionType);
		String filename = getTestFilePath(context, testType, questionType);
		try {
			XmlPullParser parser = Xml.newPullParser();
			File dataFile = new File(filename);
			if (!dataFile.exists()) {
				Log.e(TAG, "there is no Test file(" + filename + ")");
				return null;
			}
			
			FileInputStream fis = new FileInputStream(filename);
			parser.setInput(fis, "UTF-8");
			parser.nextTag();
			
			return readQuestions(parser);
			
		} catch (Exception ex) {
			Log.e(TAG, "failed to data from test file");
			
		}
		return null;
	}
	public static Questions generateTestSource(Context context, int testType, int questionType,
			int questionCount, int exampleCount, int answerCount) {
		Log.e(TAG, "generateTestSource testType = " + testType + " questionType = " + questionType + " questionCount = " + questionCount
				+ " exampleCount = " + exampleCount + " answerCount = " + answerCount);
		
		if (testType < 0 || testType == 0 && questionType < 0) {
			Log.e(TAG, "failed to generateTestSource testType = " + testType + " questionType = " + questionType);
			return null;
		}
		
		Uri uri = null;
		String[] projections = null;
		String subjectColumnName = null;
		String indexColumnName = null;
		String typeColumnName = GrammarProviderContract.Meanings.COLUMN_NAME_TYPE;
				
		if (testType == TYPE_TEST_SUBJECTIVE) {
			questionType = TYPE_QUESTION_GRAMMAR;
		}
		
		if (questionType == TYPE_QUESTION_MEANING) {
			uri = GrammarProviderContract.Grammars.CONTENT_URI;
			subjectColumnName = GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR;
			indexColumnName = GrammarProviderContract.Grammars._ID;
			projections = new String[] {
					GrammarProviderContract.Grammars._ID,
					GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR
			};
		} else if (questionType == TYPE_QUESTION_GRAMMAR) {
			uri = GrammarProviderContract.Meanings.CONTENT_URI;
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
				
		uri = uri.buildUpon().appendQueryParameter("limit", "" + questionCount).build();
		Cursor cursor = context.getContentResolver().query(uri, projections, null, null, "RANDOM()");
		
		boolean generateSuccess = true;
		Questions questions = new Questions();
		questions.mQuestionType = questionType;
		questions.mTestType = testType;
		questions.mCount = questionCount;
		
		questions.mQuestions = new ArrayList<Question>();
		
		if (cursor != null) {
			int cursorCount = cursor.getCount();
			Log.d(TAG, "################# cursorCount = " + cursorCount);
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

		if (id < 0) {
			Log.e(TAG, "failed to getExamples id = " + id);
			return null;
		}
		
		String answerSelection = null;
		String exampleSelection = null;
		String[] selectionArgs = { String.valueOf(id) };
		String exampleColumnName = null;
		
		Uri uri = null;
		if (questionType == TYPE_QUESTION_GRAMMAR) {
			exampleColumnName = GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR;
			uri = GrammarProviderContract.Grammars.CONTENT_URI;
			
			String mappingSelection = "SELECT " + GrammarProviderContract.Mappings.COLUMN_NAME_GRAMMAR_ID
					+ " FROM " + GrammarProviderContract.Mappings.TABLE_NAME
					+ " WHERE " + GrammarProviderContract.Mappings.COLUMN_NAME_MEANING_ID + " = ?";
			
			answerSelection = GrammarProviderContract.Grammars._ID + " IN (" + mappingSelection + ")";
			exampleSelection = GrammarProviderContract.Grammars._ID + " NOT IN (" + mappingSelection + ")"; 
		} else if (questionType == TYPE_QUESTION_MEANING) {
			uri = GrammarProviderContract.Meanings.CONTENT_URI;
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
		
		Cursor cursor = context.getContentResolver().query(uri, projections, exampleSelection, selectionArgs, "RANDOM()");
		
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
		
		cursor = context.getContentResolver().query(uri, projections, answerSelection, selectionArgs, "RANDOM()");
		
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
		Log.d(TAG, "getGrammarsByMeaningId id = " + id);
		if (id < 0) {
			Log.e(TAG, "failed to getGrammarsByMeaningId id = " + id);
			return null;
		}
		
		String [] projections = {
			GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR
		};
		
		String selection = GrammarProviderContract.Grammars._ID + " IN ("
				+ "SELECT " + GrammarProviderContract.Mappings.COLUMN_NAME_GRAMMAR_ID
				+ " FROM " + GrammarProviderContract.Mappings.TABLE_NAME
				+ " WHERE " + GrammarProviderContract.Mappings.COLUMN_NAME_MEANING_ID + " = ?)";
		String[] selectionArgs = { String.valueOf(id) };
		
		Cursor cursor = context.getContentResolver().query(GrammarProviderContract.Grammars.CONTENT_URI,
				projections, selection, selectionArgs, GrammarProviderContract.Grammars.DEFAULT_SORT_ORDER);
		
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
		
		if (grammarId < 0) {
			Log.e(TAG, "failed to deleteGrammar id = " + grammarId);
			return false;
		}		
		
		String whereClause = GrammarProviderContract.Grammars._ID + " = ?";
		String[] whereArgs = { String.valueOf(grammarId) };
		
		int count = context.getContentResolver().delete(GrammarProviderContract.Grammars.CONTENT_URI, whereClause, whereArgs);
		
		return count > 0 ? true : false;
	}
	
	public static boolean exportDataToFile(Context context, Uri fileUri, Runnable callback) {
		Log.d(TAG, "exportDataToFile uri = " + fileUri);
		
		try {
			OutputStream os = context.getContentResolver().openOutputStream(fileUri, "w");
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));			
			
			writer.write("#####################################\n");
			writer.write(PREFIX_MEANING_SEPARATOR + ":" + DEFAULT_MEANING_SEPARATOR + "\n");
			writer.write("# : 명사\n");
			writer.write("@ : 동사\n");
			writer.write("$ : 형용사\n");
			writer.write("% : 부사\n");
			writer.write("! : 전치사\n");
			writer.write("* : 숙어\n");
			writer.write("#####################################\n");
			String[] projection = {
					GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR,
					GrammarProviderContract.Grammars.COLUMN_NAME_MEANING
			};
			
			Cursor cursor = context.getContentResolver().query(GrammarProviderContract.Grammars.CONTENT_URI,
					projection, null, null, GrammarProviderContract.Grammars.DEFAULT_SORT_ORDER);
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					for (int i = 0; i < cursor.getCount(); i++) {
						int grammarColumnIndex = cursor.getColumnIndex(GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR);
						int meaningColumnIndex = cursor.getColumnIndex(GrammarProviderContract.Grammars.COLUMN_NAME_MEANING);
						
						StringBuilder sb = new StringBuilder();
						String grammar = cursor.getString(grammarColumnIndex);
						String meanings = cursor.getString(meaningColumnIndex);
						sb.append(grammar + ":");
						
						String[] meaningGroup = meanings.split(IDENTIFIER_MEANING_GROUP);
						int groupSize = meaningGroup.length;
						for (int j = 0; j < groupSize; j++) {
							String[] meaning = meaningGroup[j].split(IDENTIFIER_MEANING);
							String type = getGrammarPrefixByType(Integer.valueOf(meaning[0]));
							sb.append(type+meaning[1]);
							if (j < groupSize - 1) {
								sb.append(DEFAULT_MEANING_SEPARATOR);
							}
						}
						sb.append("\n");
						writer.write(sb.toString());
						cursor.moveToNext();
					}
				}
				
				cursor.close();
			}
			
			writer.close();
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
	
	public static void deleteSavedTestFiles(Context context, boolean subj, boolean obj) {
		String objMeanFile = getTestFilePath(context, TYPE_TEST_OBJECTIVE, TYPE_QUESTION_MEANING);
		String objGrammarFile = getTestFilePath(context, TYPE_TEST_OBJECTIVE, TYPE_QUESTION_GRAMMAR);
		String subjFile = getTestFilePath(context, TYPE_TEST_SUBJECTIVE, -1);
		
		File f = null;
		if (subj || obj) {
			f = new File(objMeanFile);
			if (f.exists() && f.delete()) {
				Log.d(TAG, "delete file = " + objMeanFile);
			}
			
			f = new File(objGrammarFile);
			if (f.exists() && f.delete()) {
				Log.d(TAG, "delete file = " + objGrammarFile);
			}
		}
		
		if (subj) {
			f = new File(subjFile);
			if (f.exists() && f.delete()) {
				Log.d(TAG, "delete file = " + subjFile);
			}
		}
	}
}

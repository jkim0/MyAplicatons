package com.loyid.grammarbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlSerializer;

import com.loyid.grammarbook.GrammarUtils.Question;
import com.loyid.grammarbook.GrammarUtils.Questions;
import com.loyid.grammarbook.PrepareTestFragment.OnFragmentInteractionListener;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment
 * must implement the
 * {@link GrammarTestResultFragment.OnFragmentInteractionListener} interface to
 * handle interaction events. Use the
 * {@link GrammarTestResultFragment#newInstance} factory method to create an
 * instance of this fragment.
 * 
 */
public class GrammarTestResultFragment extends Fragment {
	private static final String TAG = "GrammarTestResultFragment";
	
	public static final String ARG_RESULT = "result";
	public static final String ARG_CORRECT_COUNT = "correct";
	public static final String ARG_INCORRECT_COUNT = "incorrect";
	
	private TextView mScoreView = null;
	private TextView mCorrectView = null;
	private TextView mIncorrectView = null;
	private ListView mCorrectionView = null;
	
	public GrammarTestResultFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		if (getArguments() != null) {
			mCorrect = getArguments().getInt(ARG_CORRECT_COUNT);
			mIncorrect = getArguments().getInt(ARG_INCORRECT_COUNT);
		}
		*/
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_grammar_test_result,
				container, false);
		mScoreView = (TextView)rootView.findViewById(R.id.score);
		mCorrectView = (TextView)rootView.findViewById(R.id.correct);
		mIncorrectView = (TextView)rootView.findViewById(R.id.incorrect);
		mCorrectionView = (ListView)rootView.findViewById(R.id.corrections);
		
		Button btnOk = (Button)rootView.findViewById(R.id.btn_ok);
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getActivity().finish();
			}
		});
		
		loadResult();
		return rootView;
	}
	
	private class LoadDataAyncTask extends AsyncTask<Questions, Void, Results> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			showProgressDialog(0);
			super.onPreExecute();
		}

		@Override
		protected Results doInBackground(Questions... params) {
			Results result = getTestResults(params[0]);
			result.saveToDatabase(getActivity());
			return result;
		}

		@Override
		protected void onPostExecute(Results results) {
			// TODO Auto-generated method stub
			dismissDialog();
			displayTestResults(results);
		}
	}
	
	private void loadResult() {
		GrammarTestActivity activity = (GrammarTestActivity)getActivity();
		Questions questions = activity.getTestResult();
		LoadDataAyncTask task = new LoadDataAyncTask();
		task.execute(questions);		
	}
	
	private Results getTestResults(Questions questions) {
		int correctCount = 0;
		int incorrectCount = 0;
		int halfScoredCount = 0;		
		
		ArrayList<Correction> corrections = new ArrayList<Correction>();
		
		for (int i= 0; i < questions.mCount; i++) {
			Question q = questions.mQuestions.get(i);
			if (q.mIsRight) {
				if (q.mTryCount > 1)
					halfScoredCount += 1;
				correctCount++;
			} else {
				Correction c = new Correction();
				c.mNumber = i + 1;
				c.mSubject = q.mSubject;
				
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j < q.mTryCount; j++) {
					if (questions.mTestType == GrammarUtils.TYPE_TEST_OBJECTIVE) {
						int answer = q.mObjAnswer.get(j);
						sb.append((answer + 1) + ". " + q.mExamples.get(answer));
					} else {
						sb.append(q.mSubjAnswer.get(j));
					}
					if (j < q.mTryCount - 1)
						sb.append("\n");
				}
				c.mAnswered = sb.toString();
				
				sb = new StringBuilder();
				int correctionCount = q.mCorrectAnswer == null ? 0 : q.mCorrectAnswer.size();
				for (int k = 0; k < correctionCount; k++) {
					int correctAnswer = q.mCorrectAnswer.get(k);
					sb.append((correctAnswer + 1) + ". " + q.mExamples.get(correctAnswer));
					if (k < correctionCount)
						sb.append("\n");
				}
				
				correctionCount = q.mCorrectAnswerStr == null ? 0 : q.mCorrectAnswerStr.size();
				for (int h = 0; h < correctionCount; h++) {
					sb.append(q.mCorrectAnswerStr.get(h));
					if (h < correctionCount)
						sb.append("\n");
				}
				
				c.mCorrection = sb.toString();
				corrections.add(c);
				
				incorrectCount++;
			}
		}
		
		Results result = new Results();
		result.mTotalCount = questions.mCount;
		result.mCorrectCount = correctCount;
		result.mIncorrectCount = incorrectCount;
		result.mHalfScoreCount = halfScoredCount;
		result.mCorrections = corrections;
		Long now = Long.valueOf(System.currentTimeMillis());
		result.mCompletedDate =  now;
		
		return result;
	}
	
	private void displayTestResults(Results results) {
		double e = 100 / results.mTotalCount;
		double score = (results.mCorrectCount - results.mHalfScoreCount) * e + results.mHalfScoreCount * (e / 2);
		mScoreView.setText(getString(R.string.label_score) + " : " + score);
		mCorrectView.setText(getString(R.string.label_corrrect) + " : " + results.mCorrectCount);
		mIncorrectView.setText(getString(R.string.label_incorrect) + " : " + results.mIncorrectCount);
		
		if (results.mIncorrectCount > 0) {
			CorrectionsArrayAdapter adapter = new CorrectionsArrayAdapter(getActivity(), R.layout.test_result_list_item, results.mCorrections);
			
			View header = getActivity().getLayoutInflater().inflate(R.layout.test_result_list_header, null, false);
			mCorrectionView.addHeaderView(header);
			mCorrectionView.setAdapter(adapter);
		}
	}
	
	private class CorrectionsArrayAdapter extends BaseAdapter {
		private ArrayList<Correction> mCorrections;
		private LayoutInflater mInflater;
		private int mResId;
		
		private CorrectionsArrayAdapter(Context context, int resId, ArrayList<Correction> corrections) {
			mCorrections = corrections;
			mResId = resId;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mCorrections.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mCorrections.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = mInflater.inflate(mResId, parent, false);
				ViewHolder holder = new ViewHolder();
				holder.mNumber = (TextView)convertView.findViewById(R.id.number);
				holder.mQuestion = (TextView)convertView.findViewById(R.id.question);
				holder.mAnswered = (TextView)convertView.findViewById(R.id.answered);
				holder.mCorrection = (TextView)convertView.findViewById(R.id.correction);
				convertView.setTag(holder);
			}
			
			bindView(position, convertView);
			
			return convertView;
		}
		
		private void bindView(int position, View view) {
			ViewHolder holder = (ViewHolder)view.getTag();
			Correction c = mCorrections.get(position);
			holder.mNumber.setText("" + c.mNumber);
			holder.mQuestion.setText(c.mSubject);
			holder.mAnswered.setText(c.mAnswered);
			holder.mCorrection.setText(c.mCorrection);
		}
		
		private class ViewHolder {
			TextView mNumber;
			TextView mQuestion;
			TextView mAnswered;
			TextView mCorrection;
		}
	}
	
	private void showProgressDialog(int type) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		
		ft.addToBackStack(null);
		
		// Create and show the dialog.
		DialogFragment newFragment = GrammarDialogFragment.newInstance(GrammarDialogFragment.DIALOG_TYPE_PROGRESS);
		Bundle args = newFragment.getArguments();
		args.putString(GrammarDialogFragment.FRAGMENT_ARGS_MESSAGE, getString(R.string.msg_progress_generate_result));
		newFragment.setCancelable(false);
		newFragment.show(ft, "dialog");
	}
	
	private void dismissDialog() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			DialogFragment df = (DialogFragment)prev;
			df.dismiss();
			ft.remove(prev);
		}
		
		ft.addToBackStack(null);
	}
	
	private class Correction {
		public int mNumber;
		public String mSubject;
		public String mAnswered;
		public String mCorrection;
	}
	
	private class Results {
		public int mTestType;
		public int mQuestionType;
		public int mTotalCount;
		public int mCorrectCount;
		public int mIncorrectCount;
		public int mHalfScoreCount;
		public long mCompletedDate;
		public ArrayList<Correction> mCorrections;
		
		public void saveToDatabase(Context context) {
			String fileName = saveCorrectionToFile(context);
			
			ContentValues values = new ContentValues();
			values.put(GrammarProviderContract.TestResult.COLUMN_NAME_TEST_TYPE, mTestType);
			values.put(GrammarProviderContract.TestResult.COLUMN_NAME_QUESTION_TYPE, mQuestionType);
			values.put(GrammarProviderContract.TestResult.COLUMN_NAME_TEST_COUNT, mTotalCount);
			values.put(GrammarProviderContract.TestResult.COLUMN_NAME_CORRECT_COUNT, mCorrectCount);
			values.put(GrammarProviderContract.TestResult.COLUMN_NAME_INCORRECT_COUNT, mIncorrectCount);
			values.put(GrammarProviderContract.TestResult.COLUMN_NAME_HALF_SCORE_COUNT, mHalfScoreCount);
			values.put(GrammarProviderContract.TestResult.COLUMN_NAME_CORRECTION_FILE_PATH, fileName);
			values.put(GrammarProviderContract.TestResult.COLUMN_NAME_COMPLETED_DATE, mCompletedDate);
			
			context.getContentResolver().insert(GrammarProviderContract.TestResult.CONTENT_URI, values);
		}
		
		private String saveCorrectionToFile(Context context) {
			String dirPath = context.getFilesDir().getAbsolutePath() + "/test_results";
			File dir = new File(dirPath);
			if( !dir.exists() && !dir.mkdirs()) {
				Log.e(TAG, "failed to make dir for data file");
				return null;
			}
			
			Long now = Long.valueOf(System.currentTimeMillis());
			
			String fileName = dirPath + "/correction@" + now + ".xml";
			File dataFile = new File(fileName);
			
			try {
				dataFile.createNewFile();
			} catch (IOException ex) {
				Log.e(TAG, "failed to create data file", ex);
				return null;
			}
			
			FileOutputStream fos = null;
			try{
				fos = new FileOutputStream(dataFile);
			}catch(FileNotFoundException e){
				Log.e(TAG, "failed to create file output stream");
				dataFile.delete();
				return null;
			}
			
			XmlSerializer serializer = Xml.newSerializer();
			boolean done = false;
			try {
				serializer.setOutput(fos, "UTF-8");
				serializer.startDocument("UTF-8", true);
				serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
				int correctionCount = mCorrections == null ? 0 : mCorrections.size();
				serializer.startTag(null, "Corretions");
				serializer.attribute(null, "count", String.valueOf(correctionCount));
				
				for (int i = 0; i < correctionCount; i++) {
					Correction correction = mCorrections.get(i);
					serializer.startTag(null, "Correction");
					serializer.attribute(null, "number", String.valueOf(correction.mNumber));
					serializer.attribute(null, "subject", correction.mSubject);
					serializer.attribute(null, "answered", correction.mAnswered);
					serializer.attribute(null, "correction", correction.mCorrection);
					serializer.endTag(null, "Correction");
				}
				serializer.endTag(null, "Corretions");
				serializer.endDocument();
				serializer.flush();
				done = true;
			} catch (Exception ex) {
				Log.e(TAG, "failed to data serialize", ex);
				done = false;
			}
			
			try {
				if (fos != null)
					fos.close();
			} catch (IOException ex) {
				Log.d(TAG, "failed to close file output stream");
			}
			
			if (done) {
				return fileName;
			} else {
				dataFile.delete();
				return null;
			}
		}
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}
}

package com.loyid.grammarbook;

import java.util.ArrayList;
import java.util.List;

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
import android.content.Context;
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
			return getTestResults(params[0]);
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
		
		ArrayList<Corrections> corrections = new ArrayList<Corrections>();
		
		for (int i= 0; i < questions.mCount; i++) {
			Question q = questions.mQuestions.get(i);
			if (q.mIsRight) {
				if (q.mTryCount > 1)
					halfScoredCount += 1;
				correctCount++;
			} else {
				Corrections c = new Corrections();
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
		result.mHalfScroedCount = halfScoredCount;
		result.mCorrections = corrections;
		
		return result;
	}
	
	private void displayTestResults(Results results) {
		double e = 100 / results.mTotalCount;
		double score = (results.mCorrectCount - results.mHalfScroedCount) * e + results.mHalfScroedCount * (e / 2);
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
		private ArrayList<Corrections> mCorrections;
		private LayoutInflater mInflater;
		private int mResId;
		
		private CorrectionsArrayAdapter(Context context, int resId, ArrayList<Corrections> corrections) {
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
			Corrections c = mCorrections.get(position);
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
	
	private class Corrections {
		public int mNumber;
		public String mSubject;
		public String mAnswered;
		public String mCorrection;
	}
	
	private class Results {
		public int mTotalCount;
		public int mCorrectCount;
		public int mIncorrectCount;
		public int mHalfScroedCount;
		public ArrayList<Corrections> mCorrections;
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

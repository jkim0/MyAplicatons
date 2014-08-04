package com.loyid.grammarbook;

import java.util.ArrayList;

import com.loyid.grammarbook.GrammarUtils.Question;
import com.loyid.grammarbook.GrammarUtils.Questions;
import com.loyid.grammarbook.PrepareTestFragment.OnFragmentInteractionListener;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment
 * must implement the {@link GrammarTestFragment.OnFragmentInteractionListener}
 * interface to handle interaction events. Use the
 * {@link GrammarTestFragment#newInstance} factory method to create an instance
 * of this fragment.
 * 
 */
public class GrammarTestFragment extends Fragment {
	private static final String TAG = "GrammarTestFragment";
	
	public static final String ARG_TEST_TYPE = "test_type";
	public static final String ARG_QUESTION_TYPE = "question_type";
	
	private int mTestType = GrammarUtils.TYPE_TEST_OBJECTIVE;
	private int mQuestionType = GrammarUtils.TYPE_QUESTION_MEANING;
	
	private int mQuestionCount = GrammarUtils.DEFAULT_TEST_COUNT;
	private int mExampleCount = GrammarUtils.DEFAULT_EXAMPLE_COUNT;
	private int mAnswerCount = GrammarUtils.DEFAULT_ANSWER_COUNT;
	
	private Questions mQuestions = null;
	private Question mCurrentQuestion = null;
	private RadioGroup mObjAnswerGroup = null;
	private EditText mSubjAnswer = null;
	private TextView mQuestionTextView = null;
	
	private OnFragmentInteractionListener mListener;
	
	public GrammarTestFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mQuestionCount = Integer.valueOf(prefs.getString("test_count", String.valueOf(GrammarUtils.DEFAULT_TEST_COUNT)));
		mExampleCount = Integer.valueOf(prefs.getString("test_example_count", String.valueOf(GrammarUtils.DEFAULT_EXAMPLE_COUNT)));
		
		if (getArguments() != null) {
			mTestType = getArguments().getInt(ARG_TEST_TYPE);
			mQuestionType = getArguments().getInt(ARG_QUESTION_TYPE);
		}
		
		Log.e(TAG, "mQuestionCount = " + mQuestionCount);
		generateTestSource();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_grammar_test, container, false);
		View objectiveArea = rootView.findViewById(R.id.objective_answer_area);
		View subjectiveArea = rootView.findViewById(R.id.subjective_answer_area);
		mQuestionTextView = (TextView)rootView.findViewById(R.id.question);
		if (mTestType == GrammarUtils.TYPE_TEST_OBJECTIVE) {
			objectiveArea.setVisibility(View.VISIBLE);
			subjectiveArea.setVisibility(View.GONE);
			RadioGroup answerGroup = (RadioGroup)rootView.findViewById(R.id.objective_answer_group);
			mObjAnswerGroup = answerGroup;
			for (int i = 0; i < answerGroup.getChildCount(); i++) {
				View child = answerGroup.getChildAt(i);
				if (i < mExampleCount) {
					child.setVisibility(View.VISIBLE);
				} else {
					child.setVisibility(View.GONE);
				}
			}
		} else {
			objectiveArea.setVisibility(View.GONE);
			subjectiveArea.setVisibility(View.VISIBLE);
			mSubjAnswer = (EditText)rootView.findViewById(R.id.subjective_answer);
		}
		
		Button btnCompleted = (Button)rootView.findViewById(R.id.btn_completed);
		btnCompleted.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBtnCompletedPressed();
			}
		}); 
		return rootView;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}
	
	private void onBtnCompletedPressed() {
		checkCorrection();
		moveToNext();
	}
	
	private int getCheckedId() {
		int id = mObjAnswerGroup.getCheckedRadioButtonId();
		switch (id) {
		case R.id.answer1:
			return 1;
		case R.id.answer2:
			return 2;
		case R.id.answer3:
			return 3;
		case R.id.answer4:
			return 4;
		case R.id.answer5:
			return 5;
		}
		return 0;		
	}
	
	private void checkCorrection() {
		boolean right = false;
		if (mTestType == GrammarUtils.TYPE_TEST_OBJECTIVE) {
			int answer = getCheckedId();
			// at this time, it just have a answer for a problem.
			right = mCurrentQuestion.mCorrectAnswer.contains(answer);
		} else {
			String answer = mSubjAnswer.getText().toString().trim();
			right = mCurrentQuestion.mCorrectAnswerStr.contains(answer);
		}
		
		mCurrentQuestion.mIsRight = right;
		mQuestions.mSolvedCount += 1;
	}
	
	private void moveToNext() {
		if (mQuestions.mSolvedCount == mQuestions.mCount) {
			onTestFinished();
			return;
		}
		
		mCurrentQuestion = mQuestions.mQuestions.get(mQuestions.mSolvedCount);
		
		mQuestionTextView.setText(mCurrentQuestion.mSubject + " (" + (mQuestions.mSolvedCount + 1) + "/" + mQuestions.mCount + ")");
		
		if (mTestType == GrammarUtils.TYPE_TEST_OBJECTIVE) {
			ArrayList<String> examples = mCurrentQuestion.mExamples;
			Log.d(TAG, "moveToNext examples = " + examples + " size = " + examples.size()
					+ " count = " +mCurrentQuestion.mExampleCount);
			for (int i = 0; i < mCurrentQuestion.mExampleCount; i++) {
				RadioButton btn = (RadioButton)mObjAnswerGroup.getChildAt(i);
				Log.d(TAG, "example = " + examples.get(i));
				btn.setText(examples.get(i));
			}
			mObjAnswerGroup.clearCheck();
		} else {
			mSubjAnswer.setText(null);
		}
	}
	
	private void generateTestSource() {
		GenerateTestAyncTask loader = new GenerateTestAyncTask();
		Log.e(TAG, "mQuestionCount = " + mQuestionCount);
		loader.execute(mTestType, mQuestionType, mQuestionCount, mExampleCount, mAnswerCount);
	}
	
	private void onGenerateCompleted(Questions questions) {
		Log.e(TAG, "onGenerateCompleted questions = " + questions);
		mQuestions = questions;
		if (questions == null) return;
		moveToNext();
	}
	
	private void onTestFinished() {
		if (mListener != null) {
			mListener.onTestFinished(mQuestions);
		}
	}
	
	private class GenerateTestAyncTask extends AsyncTask<Integer, Void, Questions> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected Questions doInBackground(Integer... params) {
			Questions questions = GrammarUtils.generateTestSource(getActivity(), params[0],
					params[1], params[2], params[3], params[4]);
			return questions;
		}

		@Override
		protected void onPostExecute(Questions result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			onGenerateCompleted(result);
			
		}
	}
}

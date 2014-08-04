package com.loyid.grammarbook;

import com.loyid.grammarbook.GrammarUtils.Question;
import com.loyid.grammarbook.GrammarUtils.Questions;
import com.loyid.grammarbook.PrepareTestFragment.OnFragmentInteractionListener;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class GrammarTestActivity extends Activity implements OnFragmentInteractionListener {
	private static final String TAG = "GrammarTestActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grammar_test);
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PrepareTestFragment()).commit();
		}
	}

	@Override
	public void onTestStarted(int testType, int questionType) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onStartTest testType = " + testType + " qustionType = " + questionType);
		GrammarTestFragment newFragment = new GrammarTestFragment();
		Bundle args = new Bundle();
		args.putInt(GrammarTestFragment.ARG_TEST_TYPE, testType);
		args.putInt(GrammarTestFragment.ARG_QUESTION_TYPE, questionType);
		newFragment.setArguments(args);
		getFragmentManager().beginTransaction()
				.replace(R.id.container, newFragment).commit();
	}

	@Override
	public void onTestFinished(Questions questions) {
		// TODO Auto-generated method stub
		GrammarTestResultFragment newFragment = new GrammarTestResultFragment();
		Bundle args = new Bundle();
		
		int correct = 0;
		int incorrect = 0;
		for (int i= 0; i < questions.mCount; i++) {
			Question q = questions.mQuestions.get(i);
			if (q.mIsRight) {
				correct++;
			} else {
				incorrect++;
			}
		}
		args.putInt(GrammarTestResultFragment.ARG_CORRECT_COUNT, correct);
		args.putInt(GrammarTestResultFragment.ARG_INCORRECT_COUNT, incorrect);
		newFragment.setArguments(args);
		getFragmentManager().beginTransaction()
				.replace(R.id.container, newFragment).commit();
	}
}

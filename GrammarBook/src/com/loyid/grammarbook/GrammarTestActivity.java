package com.loyid.grammarbook;

import com.loyid.grammarbook.GrammarUtils.Question;
import com.loyid.grammarbook.GrammarUtils.Questions;
import com.loyid.grammarbook.PrepareTestFragment.OnFragmentInteractionListener;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class GrammarTestActivity extends Activity implements OnFragmentInteractionListener {
	private static final String TAG = "GrammarTestActivity";

	private Questions mTestResult = null;
	
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
	public void onTestStarted(int testType, int questionType, boolean useSaved) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onStartTest testType = " + testType + " qustionType = " + questionType + " useSaved = " + useSaved);
		GrammarTestFragment newFragment = new GrammarTestFragment();
		Bundle args = new Bundle();
		args.putInt(GrammarTestFragment.ARG_TEST_TYPE, testType);
		args.putInt(GrammarTestFragment.ARG_QUESTION_TYPE, questionType);
		args.putBoolean(GrammarTestFragment.ARG_USE_SAVED, useSaved);
		newFragment.setArguments(args);
		getFragmentManager().beginTransaction()
				.replace(R.id.container, newFragment).commit();
	}

	@Override
	public void onTestFinished(Questions questions) {
		// TODO Auto-generated method stub
		mTestResult = questions;
		GrammarTestResultFragment newFragment = new GrammarTestResultFragment();
		getFragmentManager().beginTransaction()
				.replace(R.id.container, newFragment).commit();
	}
	
	public Questions getTestResult() {
		return mTestResult;
	}
}

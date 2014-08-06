package com.loyid.grammarbook;

import com.loyid.grammarbook.GrammarUtils.Question;
import com.loyid.grammarbook.GrammarUtils.Questions;
import com.loyid.grammarbook.PrepareTestFragment.OnFragmentInteractionListener;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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
	
	private OnFragmentInteractionListener mListener;
	
	public static final String ARG_RESULT = "result";
	public static final String ARG_CORRECT_COUNT = "correct";
	public static final String ARG_INCORRECT_COUNT = "incorrect";
	
	private TextView mScoreView = null;
	private TextView mCorrectView = null;
	private TextView mIncorrectView = null;
	private TextView mCorrectionView = null;
	
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
		mCorrectionView = (TextView)rootView.findViewById(R.id.corrections);
		
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
	
	private void loadResult() {
		GrammarTestActivity activity = (GrammarTestActivity)getActivity();
		Questions result = activity.getTestResult();
		
		int correct = 0;
		int incorrect = 0;
		int halfScores = 0;
		StringBuilder sb = null;
		for (int i= 0; i < result.mCount; i++) {
			Question q = result.mQuestions.get(i);
			if (q.mIsRight) {
				if (q.mTryCount > 1)
					halfScores += 1;
				correct++;
			} else {
				if (sb == null)
					sb = new StringBuilder();
				
				sb.append(q.mSubject + "\n");
				sb.append(" : you answerd\n");
				
				
				for (int j = 0; j < q.mTryCount; j++) {
					if (result.mTestType == GrammarUtils.TYPE_TEST_OBJECTIVE)
						sb.append("\t" + q.mExamples.get(q.mObjAnswer.get(j)));
					else
						sb.append("\t" + q.mSubjAnswer.get(j));
					sb.append("\n ");
				}
				
				sb.append(" -> correct answer is\n");
				for (int k = 0; q.mCorrectAnswer != null && k < q.mCorrectAnswer.size(); k++) {
					sb.append(q.mExamples.get(q.mCorrectAnswer.get(k)));
				}
				
				for (int h = 0; q.mCorrectAnswerStr != null && h < q.mCorrectAnswerStr.size(); h++) {
					sb.append(q.mCorrectAnswerStr.get(h));
				}
				
				sb.append("\n\n");
				
				incorrect++;
			}
		}
		
		double e = 100 / result.mCount;
		double score = (correct - halfScores) * e + halfScores * (e / 2);
		mScoreView.setText(getString(R.string.label_score) + " : " + score);
		mCorrectView.setText(getString(R.string.label_corrrect) + " : " + correct);
		mIncorrectView.setText(getString(R.string.label_incorrect) + " : " + incorrect);
		if (sb != null)
			mCorrectionView.setText(sb.toString());
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
}

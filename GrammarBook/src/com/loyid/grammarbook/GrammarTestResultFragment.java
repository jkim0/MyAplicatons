package com.loyid.grammarbook;

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
	
	private int mCorrect = 0;
	private int mIncorrect = 0;

	public GrammarTestResultFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mCorrect = getArguments().getInt(ARG_CORRECT_COUNT);
			mIncorrect = getArguments().getInt(ARG_INCORRECT_COUNT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_grammar_test_result,
				container, false);
		TextView score = (TextView)rootView.findViewById(R.id.score);
		TextView correct = (TextView)rootView.findViewById(R.id.correct);
		TextView incorrect= (TextView)rootView.findViewById(R.id.incorrect);
		
		int result = (int)((100 / (mCorrect + mIncorrect)) * mCorrect);
		score.setText(getString(R.string.label_score) + " : " + result);
		correct.setText(getString(R.string.label_corrrect) + " : " + mCorrect);
		incorrect.setText(getString(R.string.label_incorrect) + " : " + mIncorrect);
		
		Button btnOk = (Button)rootView.findViewById(R.id.btn_ok);
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getActivity().finish();
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
}

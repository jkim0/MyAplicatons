package com.loyid.grammarbook;

import java.io.File;

import com.loyid.grammarbook.GrammarUtils.Questions;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment
 * must implement the {@link PrepareTestFragment.OnFragmentInteractionListener}
 * interface to handle interaction events. Use the
 * {@link PrepareTestFragment#newInstance} factory method to create an instance
 * of this fragment.
 * 
 */
public class PrepareTestFragment extends Fragment {
	private static final String TAG = "PreapareTestFragment";
	
	private OnFragmentInteractionListener mListener;
	
	private int mSelectedTestType = GrammarUtils.TYPE_TEST_OBJECTIVE;
	private int mSelectedQuestionType = GrammarUtils.TYPE_QUESTION_MEANING;
	
	private RadioGroup mQuestionTypeGroup = null;

	public PrepareTestFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if (getArguments() != null) {
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.grammar_test, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setClass(getActivity(), GrammarPreferenceActivity.class);
			intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, "com.loyid.grammarbook.SettingsActivity$TestPreferenceFragment");
			intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_TITLE, R.string.pref_header_label_test);
			intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = (View)inflater.inflate(R.layout.fragment_preapare_test, container,
				false);
		mQuestionTypeGroup = (RadioGroup)rootView.findViewById(R.id.radio_group_question_type);
		RadioGroup testTypeGroup = (RadioGroup)rootView.findViewById(R.id.radio_group_test_type);
		OnCheckedChangeListener listener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch(group.getId()) {
				case R.id.radio_group_test_type:
					switch(checkedId) {
					case R.id.radio_type_objective:
						mQuestionTypeGroup.setVisibility(View.VISIBLE);;
						mSelectedTestType = GrammarUtils.TYPE_TEST_OBJECTIVE;
						break;
					case R.id.radiotype_subjective:
						mQuestionTypeGroup.setVisibility(View.GONE);
						mSelectedTestType = GrammarUtils.TYPE_TEST_SUBJECTIVE;
						break;
					}
					Log.d(TAG, "test type changed = " + mSelectedTestType);
					break;
				case R.id.radio_group_question_type:
					switch(checkedId) {
					case R.id.radio_type_meaning:
						mSelectedQuestionType = GrammarUtils.TYPE_QUESTION_MEANING;
						break;
					case R.id.radio_type_grammar:
						mSelectedQuestionType = GrammarUtils.TYPE_QUESTION_GRAMMAR;
						break;
					}
					Log.d(TAG, "question type changed = " + mSelectedQuestionType);
					break;
				}
			}
		};
		
		testTypeGroup.setOnCheckedChangeListener(listener);
		
		mQuestionTypeGroup.setOnCheckedChangeListener(listener);
		
		Button btnStart = (Button)rootView.findViewById(R.id.btn_start);
		btnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				checkBeforeStartTest();
			}
		});
		return rootView;
	}
	
	private void checkBeforeStartTest() {
		String filename = GrammarUtils.getTestFilePath(getActivity(), mSelectedTestType, mSelectedQuestionType);
		File file = new File(filename);
		if (file.exists()) {
			showConfirmDialog();
			return;
		}
		
		startTest(false);
	}
	
	private void startTest(boolean useSaved) {
		if (mListener != null) {
			mListener.onTestStarted(mSelectedTestType, mSelectedQuestionType, useSaved);
		}
	}
	
	private void showConfirmDialog() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		
		ft.addToBackStack(null);
		
		// Create and show the dialog.
		GrammarDialogFragment newFragment = GrammarDialogFragment.newInstance(GrammarDialogFragment.DIALOG_TYPE_YES_NO);
		newFragment.setOnClickListener(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					startTest(true);
				} else {
					startTest(false);
				}				
			}			
		});
		Bundle args = newFragment.getArguments();
		args.putString(GrammarDialogFragment.FRAGMENT_ARGS_MESSAGE, getString(R.string.msg_use_saved_dialog));
		newFragment.setCancelable(false);
		newFragment.show(ft, "dialog");
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
	
	public interface OnFragmentInteractionListener {
		public void onTestStarted(int testType, int questionType, boolean useSaved);
		public void onTestFinished(Questions question);
	}

}

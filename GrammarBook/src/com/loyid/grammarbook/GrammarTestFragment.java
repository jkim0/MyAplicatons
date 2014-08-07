package com.loyid.grammarbook;

import java.util.ArrayList;
import java.util.Locale;

import com.loyid.grammarbook.GrammarUtils.Question;
import com.loyid.grammarbook.GrammarUtils.Questions;
import com.loyid.grammarbook.PrepareTestFragment.OnFragmentInteractionListener;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment
 * must implement the {@link GrammarTestFragment.OnFragmentInteractionListener}
 * interface to handle interaction events. Use the
 * {@link GrammarTestFragment#newInstance} factory method to create an instance
 * of this fragment.
 * 
 */
public class GrammarTestFragment extends Fragment implements Callback {
	private static final String TAG = "GrammarTestFragment";
	
	public static final String ARG_TEST_TYPE = "test_type";
	public static final String ARG_QUESTION_TYPE = "question_type";
	
	private int mTestType = GrammarUtils.TYPE_TEST_OBJECTIVE;
	private int mQuestionType = GrammarUtils.TYPE_QUESTION_MEANING;
	
	private int mQuestionCount = GrammarUtils.DEFAULT_TEST_COUNT;
	private int mExampleCount = GrammarUtils.DEFAULT_EXAMPLE_COUNT;
	private int mAnswerCount = GrammarUtils.DEFAULT_ANSWER_COUNT;
	
	private static final int MSG_MOVE_TO_NEXT = 0;
	
	private Questions mQuestions = null;
	private Question mCurrentQuestion = null;
	private RadioGroup mObjAnswerGroup = null;
	private EditText mSubjAnswer = null;
	private TextView mQuestionTextView = null;
	private TextView mCorrections = null;
	
	private OnFragmentInteractionListener mListener;
	
	private Animation mBlink;
	
	private Handler mHandler = null;
	
	private AnimationListener mAnimationListener = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation animation) {
			Log.d(TAG, "onAnimationEnd");
			mHandler.sendEmptyMessage(MSG_MOVE_TO_NEXT);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			Log.d(TAG, "onAnimationRepeat");
		}

		@Override
		public void onAnimationStart(Animation animation) {
			Log.d(TAG, "onAnimationStart");
		}
		
	};
	
	public GrammarTestFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mBlink = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                R.anim.anim_blink);
		mHandler = new Handler(this);
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
			
			if (mQuestionType == GrammarUtils.TYPE_QUESTION_MEANING) {
				Button button = (Button)rootView.findViewById(R.id.btn_play);
				button.setVisibility(View.VISIBLE);
				button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						((GrammarBookApplication)getActivity().getApplicationContext()).playTTS(Locale.US,
								mCurrentQuestion.mSubject);
					}
				});
			}
			RadioGroup answerGroup = (RadioGroup)rootView.findViewById(R.id.objective_answer_group);
			answerGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					RadioButton radio = (RadioButton)group.findViewById(checkedId);
					Log.d(TAG, "onCheckedChanged checkedId = " + checkedId + " state = " + (radio != null ? radio.isChecked():false));
					if (checkedId < 0 || (radio != null && !radio.isChecked())) {
						return;
					}
					checkCorrection();
				}				
			});
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
			mCorrections = (TextView)rootView.findViewById(R.id.corrections);
			Button btnCompleted = (Button)rootView.findViewById(R.id.btn_completed);
			btnCompleted.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onBtnCompletedPressed();
				}
			});
		}
		
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
		mCurrentQuestion.mTryCount += 1;
		if (mTestType == GrammarUtils.TYPE_TEST_OBJECTIVE) {
			if (mCurrentQuestion.mObjAnswer == null) 
				mCurrentQuestion.mObjAnswer = new ArrayList<Integer>();
			
			int answer = getCheckedId();
			mCurrentQuestion.mObjAnswer.add(answer);			
			
			right = mCurrentQuestion.mCorrectAnswer.contains(answer);
			if (!right) {
				mObjAnswerGroup.clearCheck();
				mObjAnswerGroup.getChildAt(answer).setEnabled(false);
			}
		} else {
			if (mCurrentQuestion.mSubjAnswer == null) 
				mCurrentQuestion.mSubjAnswer = new ArrayList<String>();
			String answer = mSubjAnswer.getText().toString().trim();
			mCurrentQuestion.mSubjAnswer.add(answer);
			right = mCurrentQuestion.mCorrectAnswerStr.contains(answer);
		}
		mCurrentQuestion.mIsRight = right;
		
		if (!right && mCurrentQuestion.mTryCount < 2) {
			Toast.makeText(getActivity(), R.string.msg_incorrect, Toast.LENGTH_SHORT).show();
			return;
		}
		
		mQuestions.mSolvedCount += 1;
		
		mBlink.reset();
		mBlink.setAnimationListener(mAnimationListener);
		
		if (mTestType == GrammarUtils.TYPE_TEST_OBJECTIVE) {
			int size = mCurrentQuestion.mCorrectAnswer.size();
			for (int i = 0; i < size; i++) {
				RadioButton b = (RadioButton)mObjAnswerGroup.getChildAt(mCurrentQuestion.mCorrectAnswer.get(i));
				b.startAnimation(mBlink);
			}
		} else {
			StringBuilder sb = new StringBuilder();
			int size = mCurrentQuestion.mCorrectAnswerStr.size();
			for (int i = 0; i < size; i++) {
				sb.append(mCurrentQuestion.mCorrectAnswerStr.get(i));
				if (i < size - 1) {
					sb.append(",");
				}
			}
			mCorrections.setText(sb.toString());
			mCorrections.setVisibility(View.VISIBLE);
			mCorrections.startAnimation(mBlink);
		}
		//moveToNext();
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
					+ " count = " + mCurrentQuestion.mExampleCount);
			mObjAnswerGroup.clearCheck();
			for (int i = 0; i < mCurrentQuestion.mExampleCount; i++) {
				RadioButton btn = (RadioButton)mObjAnswerGroup.getChildAt(i);
				Log.d(TAG, "example = " + examples.get(i));
				btn.setText(examples.get(i));
				btn.setEnabled(true);
			}
		} else {
			mSubjAnswer.setText(null);
			mCorrections.setVisibility(View.GONE);
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

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		switch(msg.what) {
		case MSG_MOVE_TO_NEXT:
			moveToNext();
		}
		return true;
	}
}

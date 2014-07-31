package com.loyid.grammarbook;

import java.util.Locale;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loyid.grammarbook.GrammarUtils.Grammar;
import com.loyid.grammarbook.GrammarUtils.Meaning;

/**
 * A fragment representing a single Item detail screen. This fragment is either
 * contained in a {@link GrammarListActivity} in two-pane mode (on tablets) or a
 * {@link GrammarDetailActivity} on handsets.
 */
public class GrammarDetailFragment extends Fragment implements OnInitListener {
	private static final String TAG = "GrammarDetailFragment";
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_GRAMMAR_ID = "grammar_id";
	
	private Grammar mGrammarInfo = null;
	
	private TextToSpeech mTTS;

	private String[] mTypeArray = null;
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public GrammarDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mTTS = new TextToSpeech(getActivity(), this);
		
		mTypeArray = getResources().getStringArray(R.array.arry_type);

		if (getArguments().containsKey(ARG_GRAMMAR_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			loadGrammarInfo(getArguments().getLong(ARG_GRAMMAR_ID, -1));
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
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestory()");
		if (mTTS != null) {
			mTTS.shutdown();
		}
		super.onDestroy();
	}

	private void loadGrammarInfo(long id) {
		Log.d(TAG, "loadGrammarInfo id = " + id);
		LoadGrammarInfoAyncTask loader = new LoadGrammarInfoAyncTask();
		loader.execute(id);
	}
	
	private void onLoadCompleted(Grammar info) {
		Log.d(TAG, "onLoadCompleted grammarInfo = " + info.toString());
		mGrammarInfo = info;
		View rootView = this.getView();
		TextView grammar = (TextView)rootView.findViewById(R.id.grammar);
		TextView meaning = (TextView)rootView.findViewById(R.id.meaning);
		
		grammar.setText(info.mGrammar);
		
		int size = info.mMeanings.size();
		StringBuilder sb = new StringBuilder();
		int type = -1;
		boolean isFirst = true;
		for (int i = 0; i < size; i++) {
			Meaning mean = info.mMeanings.get(i);
			if (type != mean.mType) {
				if (!isFirst) {
					sb.append("\n");
				}
				
				type = mean.mType;
				isFirst = false;
				sb.append(GrammarUtils.getTypeString(getActivity(), type) + "\n");
				type = mean.mType;
			}
			sb.append("\t" + mean.mMeaning + "\n");			
		}
		meaning.setText(sb.toString());
	}
	
	private class LoadGrammarInfoAyncTask extends AsyncTask<Long, Void, Grammar> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected Grammar doInBackground(Long... params) {
			Grammar grammarInfo = GrammarUtils.getGrammarInfo(getActivity(), params[0]);
			return grammarInfo;
		}

		@Override
		protected void onPostExecute(Grammar info) {
			// TODO Auto-generated method stub
			Toast.makeText(getActivity(), R.string.msg_load_data_done, Toast.LENGTH_SHORT).show();
			super.onPostExecute(info);
			onLoadCompleted(info);
			
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_grammar_detail,
				container, false);
		
		Button btnPlay = (Button)rootView.findViewById(R.id.btn_play);
		btnPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mTTS.setLanguage(Locale.US);
				mTTS.speak(mGrammarInfo.mGrammar, TextToSpeech.QUEUE_FLUSH, null);
			}
		});

		return rootView;
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		boolean isInit = status == TextToSpeech.SUCCESS;
		int msg = isInit ? R.string.msg_init_tts_success : R.string.msg_init_tts_fail;
		Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
	}
}

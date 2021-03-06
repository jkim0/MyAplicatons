package com.loyid.grammarbook;

import java.util.Locale;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
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
import android.widget.TextView;

import com.loyid.grammarbook.GrammarUtils.Grammar;
import com.loyid.grammarbook.GrammarUtils.Meaning;

/**
 * A fragment representing a single Item detail screen. This fragment is either
 * contained in a {@link GrammarListActivity} in two-pane mode (on tablets) or a
 * {@link GrammarDetailActivity} on handsets.
 */
public class GrammarDetailFragment extends Fragment {
	private static final String TAG = "GrammarDetailFragment";
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_GRAMMAR_ID = "grammar_id";
	
	private Grammar mGrammarInfo = null;
	private long mGrammarId = -1;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public GrammarDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		
		if (getArguments().containsKey(ARG_GRAMMAR_ID)) {
			loadGrammarInfo(getArguments().getLong(ARG_GRAMMAR_ID, -1));
		}
	}
	
	public void deleteCurrentGrammar() {
		boolean result = GrammarUtils.deleteGrammar(getActivity(), mGrammarId);
		Log.d(TAG, "deleteCurrentGrammar result = " + result);
		getActivity().finish();
	}
	
	public void editCurrentGrammar() {
		long grammarId = getArguments().getLong(ARG_GRAMMAR_ID, -1);
		Intent editIntent = new Intent(getActivity(), EditGrammarActivity.class);
		editIntent.putExtra(EditGrammarActivity.EXTRA_GRAMMAR_ID, grammarId);
		
		startActivity(editIntent);
	}

	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach activity = " + activity);
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		Log.d(TAG, "onDetach()");
		super.onDetach();
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestory()");
		super.onDestroy();
	}

	private void loadGrammarInfo(long id) {
		Log.d(TAG, "loadGrammarInfo id = " + id);
		mGrammarId = id;
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
				((GrammarBookApplication)getActivity().getApplicationContext()).playTTS(Locale.US, mGrammarInfo.mGrammar);
			}
		});

		return rootView;
	}
}

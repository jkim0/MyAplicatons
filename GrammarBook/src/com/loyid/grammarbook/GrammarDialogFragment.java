package com.loyid.grammarbook;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.app.DialogFragment;

public class GrammarDialogFragment extends DialogFragment {
	private static final String TAG = "GrammarDialogFragment";
	
	public static final String FRAGMENT_ARGS_ID = "id";
	public static final String FRAGMENT_ARGS_TITLE = "title";
	public static final String FRAGMENT_ARGS_MESSAGE = "message";
	
	public static final int DIALOG_TYPE_PROGRESS = 0;
	
	public static GrammarDialogFragment newInstance(int id) {
		GrammarDialogFragment f = new GrammarDialogFragment();
		Bundle args = new Bundle();
		args.putInt(FRAGMENT_ARGS_ID, id);
		f.setArguments(args);
		return f;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int id = getArguments().getInt(FRAGMENT_ARGS_ID);
		String title = getArguments().getString(FRAGMENT_ARGS_TITLE);
		String message = getArguments().getString(FRAGMENT_ARGS_MESSAGE);
		
		switch(id) {
		case DIALOG_TYPE_PROGRESS:
			return ProgressDialog.show(getActivity(), title, message);
		}
		
		return null;
	}
}

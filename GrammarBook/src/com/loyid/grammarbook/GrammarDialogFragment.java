package com.loyid.grammarbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;

public class GrammarDialogFragment extends DialogFragment {
	private static final String TAG = "GrammarDialogFragment";
	
	public static final String FRAGMENT_ARGS_ID = "id";
	public static final String FRAGMENT_ARGS_TITLE = "title";
	public static final String FRAGMENT_ARGS_MESSAGE = "message";
	
	public static final int DIALOG_TYPE_PROGRESS = 0;
	public static final int DIALOG_TYPE_ALERT_MSG = 1;
	public static final int DIALOG_TYPE_YES_NO = 2;
	
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
		case DIALOG_TYPE_ALERT_MSG:
			return new AlertDialog.Builder(getActivity())
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(R.string.btn_label_ok, null)
				.create();
		case DIALOG_TYPE_YES_NO:
			return new AlertDialog.Builder(getActivity())
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(R.string.btn_label_yes, mOnClickListenerWapper)
				.setNegativeButton(R.string.btn_label_no, mOnClickListenerWapper)
				.create();
		}
		
		return null;
	}
	
	private OnClickListener mOnClickListenerWapper = new OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			if (mOnBtnClickListener != null) {
				mOnBtnClickListener.onClick(dialog, which);
			}
		}
	};
	
	private OnClickListener mOnBtnClickListener = null;
	public void setOnClickListener(OnClickListener listener) {
		mOnBtnClickListener = listener;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		Log.d(TAG, "onDismiss");
		setOnClickListener(null);
		super.onDismiss(dialog);
	}
}

package com.loyid.grammarbook;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class EditGrammarActivity extends ActionBarActivity {
	private static final String TAG = "EditGrammarActivity";
	private LinearLayout mAddedItemList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_grammar);
		mAddedItemList = (LinearLayout)findViewById(R.id.added_item_list);
		recalculateIndex();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_grammar, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_done) {
			saveGrammar();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onAddButtonClick(View view) {
		LinearLayout item = (LinearLayout)getLayoutInflater().inflate(R.layout.added_item_layout, mAddedItemList, false);
		Button btn = (Button)item.findViewById(R.id.btn_remove);
		btn.setTag(mAddedItemList.getChildCount());
		mAddedItemList.addView(item);
	}
	
	public void recalculateIndex() {
		for (int i = 0; i < mAddedItemList.getChildCount(); i++) {
			LinearLayout itemLayout = (LinearLayout)mAddedItemList.getChildAt(i);
			Button btn = (Button)itemLayout.findViewById(R.id.btn_remove);
			btn.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					int index = (Integer)view.getTag();
					mAddedItemList.removeViewAt(index);
					recalculateIndex();
				}
			});
			btn.setTag(i);			
		}
	}
	
	public void saveGrammar() {
		EditText grammar = (EditText)findViewById(R.id.edit_grammar);
		String strGrammar = grammar.getText().toString();
		for (int i = 0; i < mAddedItemList.getChildCount(); i++) {
			LinearLayout itemLayout = (LinearLayout)mAddedItemList.getChildAt(i);
			Spinner spinner = (Spinner)itemLayout.findViewById(R.id.spinner_type);
			EditText edit = (EditText)itemLayout.findViewById(R.id.edit_meaning);
			int type = spinner.getSelectedItemPosition();
			String meaning = edit.getText().toString();
		}
	}
}

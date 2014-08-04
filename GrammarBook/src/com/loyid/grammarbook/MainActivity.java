package com.loyid.grammarbook;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public void onMainButtonClick(View view) {
		switch(view.getId()) {
		case R.id.btn_main_add_grammar:
			startActivity(new Intent(this, EditGrammarActivity.class));
			break;
		case R.id.btn_main_my_grammar_list:
			startActivity(new Intent(this, GrammarListActivity.class));
			break;
		case R.id.btn_main_test:
			startActivity(new Intent(this, GrammarTestActivity.class));
			break;
		case R.id.btn_main_setting:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		}
	}
}

package com.loyid.grammarbook;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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
			startActivity(new Intent(this, SettingActivity.class));
			break;
		}
	}
}

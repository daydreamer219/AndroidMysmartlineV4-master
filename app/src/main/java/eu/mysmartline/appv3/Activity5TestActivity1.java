package eu.mysmartline.appv3;

import eu.mysmartline.appv3.models.MyKeys;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Menu;
import android.widget.TextView;

public class Activity5TestActivity1 extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity5_test_activity1);
		TextView textView1 = (TextView) findViewById(R.id.textView1);
		SharedPreferences prefs = getSharedPreferences(
				MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
		String testValue =  prefs.getString(MyKeys.PROPERTY_TEST_MESSAGE_VALUE, "");
		if (!testValue.isEmpty()){
			textView1.setText("Recived value = " + testValue);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity5_test_activity1, menu);
		return true;
	}

}

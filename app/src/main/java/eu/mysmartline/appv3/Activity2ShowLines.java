package eu.mysmartline.appv3;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;

import eu.mysmartline.appv3.models.LineDetailsModel;
import eu.mysmartline.appv3.models.MyKeys;

import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

/**
 * some info
 */
public class Activity2ShowLines extends Activity {

	public static final String PROPERTY_REG_ID = "registration_id";
	private List<LineDetailsModel> lines;
	private ListView listView;
	private Context context;
	/*private String logTag = MyKeys.PROPERTY_LOGTAG;
	Log.i(logTag, result);*/
    private boolean isFromPush = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity2_show_lines);
		// Show the Up button in the action bar.
        isFromPush = getIntent().getBooleanExtra("push", false);
		lines = new ArrayList<LineDetailsModel>();
		listView = (ListView) findViewById(R.id.listView);
		context = this;

		setNavigationIntention();

		GetLinesTask getLines = new GetLinesTask();
		String getLinesUrl = MyKeys.PROPERTY_HOME + "Api/getLines";
		getLines.execute(new String[] { getLinesUrl });

        findViewById(R.id.img_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFromPush){
                    Intent intent = new Intent(Activity2ShowLines.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    finish();
                }
            }
        });
        setBackProcess();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity2_show_lines, menu);
		return true;
	}



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

	private class GetLinesTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			StringBuilder response = new StringBuilder();
			for (String url : urls) {
				DefaultHttpClient client = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(url);
				try {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							1);
					// get saved gcmId
					Context context = getApplicationContext();
					SharedPreferences sharedPref = context
							.getSharedPreferences(MyKeys.MY_SMARTLINE_PREFS,
									Context.MODE_PRIVATE);
					String deviceGcmId = sharedPref.getString(
							MyKeys.PROPERTY_REG_ID, "no default");
					// set form parameters
					nameValuePairs.add(new BasicNameValuePair("deviceGcmId",
							deviceGcmId));
					httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse execute = client.execute(httpPost);
					InputStream content = execute.getEntity().getContent();
					BufferedReader buffer = new BufferedReader(
							new InputStreamReader(content));
					String s = new String();
					while ((s = buffer.readLine()) != null) {
						response.append(s);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return response.toString();
		}

		@Override
		protected void onPostExecute(String result) {
            findViewById(R.id.progressBar).setVisibility(View.GONE);
			if (result.equals("nodata")) {
				// /then no data
			} else {
				// decode json
                List<Map<String, String>> items = new ArrayList<Map<String, String>>();
				try {
                    Gson gson = new Gson();
                    LineDetailsModel[] linesArey = gson.fromJson(result,
                            LineDetailsModel[].class);

                    if (linesArey != null) {
                        // create object list and view list
                        for (LineDetailsModel item : linesArey) {
                            // set the registration url
                            item.setRegistrationUrl(MyKeys.PROPERTY_HOME
                                    + item.getRegistrationUrl());
                            lines.add(item);
                            items.add(createItem("item", item.getName()));
                        }
                    }
                }catch(Exception e){

                }
				// react to click
                /*
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parentAdapter,
							View view, int position, long id) {
						int myId = (int) id;
						LineDetailsModel details = lines.get(myId);
						Intent intent = new Intent();
						intent.setClass(Activity2ShowLines.this,
								Activity6ShowLineDetails.class);
						intent.putExtra("LineDetailsModel", details);
						startActivity(intent);
					}

				});
                */
				SimpleAdapter simpleAdpt = new SimpleAdapter(context, items,
						R.layout.row_line,
						new String[] { "item" },
						new int[] { R.id.txt_name });
				listView.setAdapter(simpleAdpt);
//                listView.setOnScrollListener(scrollListener);
			}
		}

		private HashMap<String, String> createItem(String key, String name) {
			HashMap<String, String> item = new HashMap<String, String>();
			item.put(key, name);
			return item;
		}
	}
    boolean mShowActionbar = true;
    int mPrevFirstVisibleItem = -1;
    AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (mPrevFirstVisibleItem > firstVisibleItem || firstVisibleItem == 0){
                mShowActionbar = true;
            }else if (mPrevFirstVisibleItem < firstVisibleItem){
                mShowActionbar = false;
            }
            Log.e("scroll" , "event=" + firstVisibleItem + "," + mPrevFirstVisibleItem);
            mPrevFirstVisibleItem = firstVisibleItem;

        }
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE ) {
                if (mShowActionbar) {

                }else{

                }
            }
        }

    };

	private void setNavigationIntention() {
		SharedPreferences prefs = getSharedPreferences(
				MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(MyKeys.PROPERTY_NAVIGATETO_INTENTION,
				MyKeys.PROPERTY_NAVIGATETO_SHOW_LINES);
		editor.putString(MyKeys.PROPERTY_NAVIGATETO_VALUE,
				MyKeys.PROPERTY_NAVIGATETO_SHOW_LINES);
		editor.commit();
	}

	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences prefs = getSharedPreferences(
				MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(MyKeys.PROPERTY_NAVIGATETO_VALUE, "paused");
		editor.commit();
	}

	@Override
	public void onResume() {
		super.onResume();
		setNavigationIntention();
	}
    private int REL_SWIPE_MIN_DISTANCE;
    private int REL_SWIPE_MAX_OFF_PATH;
    private int REL_SWIPE_THRESHOLD_VELOCITY;
    private void setBackProcess(){
        DisplayMetrics dm = getResources().getDisplayMetrics();
        REL_SWIPE_MIN_DISTANCE = (int)(120.0f * dm.densityDpi / 160.0f + 0.5);
        REL_SWIPE_MAX_OFF_PATH = (int)(250.0f * dm.densityDpi / 160.0f + 0.5);
        REL_SWIPE_THRESHOLD_VELOCITY = (int)(200.0f * dm.densityDpi / 160.0f + 0.5);

        final GestureDetector gestureDetector = new GestureDetector(new MyGestureDetector());
        View.OnTouchListener gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }};
        listView.setOnTouchListener(gestureListener);
    }
    private void onLTRFling() {
        findViewById(R.id.img_home).setVisibility(View.VISIBLE);
    }

    private void onRTLFling() {
        findViewById(R.id.img_home).setVisibility(View.INVISIBLE);
    }

    private void myOnItemClick(int position) {


        LineDetailsModel details = lines.get(position);
        Intent intent = new Intent();
        intent.setClass(Activity2ShowLines.this,
                Activity6ShowLineDetails.class);
        intent.putExtra("LineDetailsModel", details);
        startActivity(intent);
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        // Detect a single-click and call my own handler.
        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            int pos = listView.pointToPosition((int)e.getX(), (int)e.getY());
            myOnItemClick(pos);
            return false;
        }


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(e1.getY() - e2.getY()) > REL_SWIPE_MAX_OFF_PATH)
                return false;
            if(e1.getX() - e2.getX() > REL_SWIPE_MIN_DISTANCE &&
                    Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {
                onRTLFling();
            }  else if (e2.getX() - e1.getX() > REL_SWIPE_MIN_DISTANCE &&
                    Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {
                onLTRFling();
            }
            return false;
        }

    }
}

package eu.mysmartline.appv3;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.ListView;

import eu.mysmartline.appv3.models.LineDetailsModel;
import eu.mysmartline.appv3.models.MyKeys;

public class Activity7ShowDisplayPanel extends Activity {
	
	ListView msgList;
	ArrayList<DisplayModel> details;
	DisplayAdapter displayAdapter;
    private boolean isFromPush = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity7_show_display_panel);
        isFromPush = getIntent().getBooleanExtra("push", false);
		msgList = (ListView) findViewById(R.id.MessageList);
		details = new ArrayList<DisplayModel>();
		
		if (isFromPush){
            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(this, notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
		
		for(DisplayModel model:details){
			Log.d("tag","after: "+ model.getMessage());
		}
		DisplayCounter couner = DisplayCounter.getInstance();
		List<String>items = couner.getCoutner();
		for (String item:items){
			Log.i("tag", "value = " + item);
			details.add(buildDisplayModel(item));
		}

		displayAdapter = new DisplayAdapter(details, this);
		msgList.setAdapter(displayAdapter);
		setNavigationIntention();
        findViewById(R.id.img_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFromPush){
                    Intent intent = new Intent(Activity7ShowDisplayPanel.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    finish();
                }
            }
        });
        setBackProcess();
 //       msgList.setOnScrollListener(scrollListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity7_show_display_panel, menu);
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

	private DisplayModel buildDisplayModel(String theMessage){
		DisplayModel model = new DisplayModel();
		model.setIcon(R.drawable.ic_launcher);
		model.setMessage(theMessage);
		model.setTime(new Date().toString());
		return model;
	}
	private void setNavigationIntention() {
		SharedPreferences prefs = getSharedPreferences(
				MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(MyKeys.PROPERTY_NAVIGATETO_INTENTION,
				MyKeys.PROPERTY_NAVIGATETO_SHOW_DISPLAY_PANEL);
		editor.putString(MyKeys.PROPERTY_NAVIGATETO_VALUE,
				MyKeys.PROPERTY_NAVIGATETO_SHOW_DISPLAY_PANEL);
		editor.commit();
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
        msgList.setOnTouchListener(gestureListener);
    }
    private void onLTRFling() {
        findViewById(R.id.img_home).setVisibility(View.VISIBLE);
    }

    private void onRTLFling() {
        findViewById(R.id.img_home).setVisibility(View.INVISIBLE);
    }

    private void myOnItemClick(int position) {


    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        // Detect a single-click and call my own handler.
        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            int pos = msgList.pointToPosition((int)e.getX(), (int)e.getY());
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

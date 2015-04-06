package com.hricigor.timerapplication;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hricigor.timerapplication.SwipeDismissListViewTouchListener.DismissCallbacks;

public class TimerActivity extends Activity implements OnItemClickListener,
		DismissCallbacks, OnClickListener {
	private static final String TAG = TimerActivity.class.getSimpleName();

	private static final long NORMAL_UPDATE_RATE_MS = 200;
	private static final int MSG_UPDATE_TIME = 0;
	private static final String RUNNING_TIMER = "timer";
	private static final String BOOLEAN_TIMER = "boolean";
	private static long mInteractiveUpdateRateMs = NORMAL_UPDATE_RATE_MS;

	//UI components 
	private Button mStartStopButton;
	private Button mSubmitButton;
	private Button mNewTimerButton;
	private TextView mTimerText;
	private ListView mListView;
	
	// ListAdapter
	private TimerAdapter mTimerAdapter;

	// Database variables
	private TimerDbHelper mDbHelper;
	private SQLiteDatabase mDb;
	private Cursor mCursor;

	// Swipe listener
	private SwipeDismissListViewTouchListener mTouchListener;

	// Boolean logic
	private boolean mIsTimerActive;
	private boolean mCanDelete;

	// Timer and position of current timer in ListView
	private Timer mCurrentTimer;
	private int mCurrentPosition;

	private long mTimerTime;
	private long mStartTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timer_layout);

		// Set UI components
		initialize();

		// Get data from DB to populate ListView if data exist
		mDbHelper = new TimerDbHelper(getApplicationContext());
		mDb = mDbHelper.getWritableDatabase();
		mCursor = getCursor();

		// Use cursor populate custom TimerAdapter and set that adapter to
		// ListView
		mTimerAdapter = new TimerAdapter(getApplicationContext());
		mTimerAdapter.setDataFromCursor(mCursor);
		mListView.setAdapter(mTimerAdapter);

	}

	@Override
	protected void onResume() {

		// Attach listeners to buttons
		mStartStopButton.setOnClickListener(this);
		mNewTimerButton.setOnClickListener(this);
		mSubmitButton.setOnClickListener(this);

		// Attach listeners to ListView
		mTouchListener = new SwipeDismissListViewTouchListener(mListView, this);
		mListView.setOnItemClickListener(this);
		mListView.setOnTouchListener(mTouchListener);

		// Setting this scroll listener is required to ensure that during
		// ListView scrolling,
		// we don't look for swipes.
		mListView.setOnScrollListener(mTouchListener.makeScrollListener());

		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Saving timer if it is active when application change occur
		if (mIsTimerActive) {
			mCurrentTimer.setTimerValue(mTimerTime);
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Saving state on device rotation
		
			outState.putInt(RUNNING_TIMER, mCurrentPosition);
			outState.putBoolean(BOOLEAN_TIMER, mIsTimerActive);
		

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null) {
			
			// Set timer instances as it was before rotation
			mCurrentPosition = savedInstanceState.getInt(RUNNING_TIMER);
			if (mCurrentPosition < 0) {
				return;
			}
			
			mCurrentTimer = (Timer) mTimerAdapter.getItem(mCurrentPosition);
			
			// Run timer, if it was runnging before rotation
			if (savedInstanceState.getBoolean(BOOLEAN_TIMER)) {
				Log.i(TAG, "onRestoreInstanceState");

				mTimerText.setText(TimerUtil.convertTime(mCurrentTimer
						.getTimerValue()));

				mStartStopButton.setEnabled(true);

				mStartTime = System.currentTimeMillis();
				mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);

				mStartStopButton.setText(R.string.button_stop);
				mTimerText.setBackgroundColor(Color.RED);
				mTimerText.setTextColor(Color.BLACK);

				mListView.setEnabled(false);
				mSubmitButton.setEnabled(false);
				mNewTimerButton.setEnabled(false);

				mIsTimerActive = true;
			} else {
				selectTimer();
			}

		}

	}

	// Clear database and write new values on app shutdown
	// Need to implement Service for this action, replace Cursor with CursorLoader and update only items that have changed for better performace
	@Override
	protected void onStop() {
		clearAll();
		insertTimers();
		super.onStop();
	}

	// Deleting all timers
	private void clearAll() {
		mDb.delete(TimerDbHelper.TABLE_NAME, null, null);
	}

	// Querying database for saved timers
	private Cursor getCursor() {
		mCursor = mDb.query(TimerDbHelper.TABLE_NAME, TimerDbHelper.columns,
				null, new String[] {}, null, null, null);
		return mCursor;
	}

	// Inserting items to database
	private void insertTimers() {
		ContentValues values = new ContentValues();
		ArrayList<Timer> tempList = mTimerAdapter.getArrayList();

		for (Timer temp : tempList) {
			values.put(TimerDbHelper.TIMER_NAME, temp.getTimerName());
			values.put(TimerDbHelper.TIMER_VALUE, temp.getTimerValue());

			mDb.insert(TimerDbHelper.TABLE_NAME, null, values);

			values.clear();
		}

	}

	// Setting up needed UI components in onCreate
	private void initialize() {
		mStartStopButton = (Button) findViewById(R.id.button_start_stop);
		mSubmitButton = (Button) findViewById(R.id.button_submit);
		mNewTimerButton = (Button) findViewById(R.id.button_new_timer);

		mTimerText = (TextView) findViewById(R.id.timer_text_view);
		mListView = (ListView) findViewById(R.id.listview_timer);

		mCanDelete = false;
		// This method although is called deleteTimer, because this is
		// initialization, it would only setup initial values. Need to find
		// better name for it
		deleteTimer();
	}

	// Listeners

	@Override
	public void onClick(View v) {
		final int viewId = v.getId();

		switch (viewId) {
		case R.id.button_new_timer:
			buildDialog();
			break;

		// Temporary Start/Stop button implementation, need refactoring
		case R.id.button_start_stop:
			if (mIsTimerActive) {
				mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);

				mStartStopButton.setText(R.string.button_start);
				mTimerText.setBackgroundColor(Color.BLACK);
				mTimerText.setTextColor(Color.WHITE);

				mListView.setEnabled(true);
				mSubmitButton.setEnabled(true);
				mNewTimerButton.setEnabled(true);

				mCurrentTimer.setTimerValue(mTimerTime);
				mTimerAdapter.notifyDataSetChanged();

				mIsTimerActive = false;

			} else {
				mStartTime = System.currentTimeMillis();
				mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);

				mStartStopButton.setText(R.string.button_stop);
				mTimerText.setBackgroundColor(Color.RED);
				mTimerText.setTextColor(Color.BLACK);

				mListView.setEnabled(false);
				mSubmitButton.setEnabled(false);
				mNewTimerButton.setEnabled(false);

				mIsTimerActive = true;
			}

			break;

		// Subbmit button for deleting selected timer
		case R.id.button_submit:
			Log.i(TAG, "submitButton");
			Toast.makeText(
					getApplicationContext(),
					"Timer " + mCurrentTimer.getTimerName()
							+ " value was sent to /dev/null",
					Toast.LENGTH_SHORT).show();
			mCanDelete = true;
			deleteTimer();
			break;
		default:
			break;
		}
	}

	// Listener for ListView listening for clicks
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		mCurrentTimer = (Timer) mTimerAdapter.getItem(position);
		mCurrentPosition = position;

		selectTimer();

	}

	// If list item can be deletet by swiping, we return true or check if
	// conditions are satisfy to delete item
	@Override
	public boolean canDismiss(int position) {
		Log.i(TAG, "canDismis");

		return true;
	}

	// When swiping to delete list item
	@Override
	public void onDismiss(ListView listView, int[] reverseSortedPositions) {
		for (int position : reverseSortedPositions) {
			Log.i(TAG, "if condition");

			if (mCurrentTimer != null && mCurrentTimer.equals(mTimerAdapter.getItem(position))) {
				mTimerText.setText("No timer selected");

				mStartStopButton.setEnabled(false);
				mSubmitButton.setEnabled(false);

				mTimerText.setBackgroundColor(Color.BLACK);
				mTimerText.setTextColor(Color.WHITE);

				mCurrentTimer = null;
				mCurrentPosition = -1;
			}
			if (mCurrentPosition > position) {
				mCurrentPosition -= 1;
			}
			mTimerAdapter.delete(position);
		}

	}

	// Temporary method, need refactoring 
	private void deleteTimer() {
		mTimerText.setText("No timer selected");

		mStartStopButton.setEnabled(false);
		mSubmitButton.setEnabled(false);

		mTimerText.setBackgroundColor(Color.BLACK);
		mTimerText.setTextColor(Color.WHITE);

		if (mCanDelete) {
			mTimerAdapter.delete(mCurrentPosition);
		}

		mCurrentTimer = null;
		mCurrentPosition = -1;

		mCanDelete = false;
		mIsTimerActive = false;
		mTimerTime = -1;
	}

	// Set up display for selected or created timer
	private void selectTimer() {
		mTimerText
				.setText(TimerUtil.convertTime(mCurrentTimer.getTimerValue()));

		mTimerText.setBackgroundColor(Color.BLACK);
		mTimerText.setTextColor(Color.WHITE);

		mStartStopButton.setEnabled(true);
		mSubmitButton.setEnabled(true);
	}

	// AlertDialog to add new timer
	private void buildDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Add Timer");
		alert.setMessage("Set name for timer");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);

		// OK button for dialog for new timer
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Log.i(TAG, "newTimer");
				mCurrentTimer = new Timer(input.getText().toString(), 0L);
				mCurrentPosition = mTimerAdapter.add(mCurrentTimer);

				// Method for setting up timer created by this dialog
				selectTimer();
			}
		});

		// Cancel button on dialog for new timer
		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}

	// Heart of Timer, does all the ticking
	final Handler mUpdateTimeHandler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case MSG_UPDATE_TIME:

				long timeMs = System.currentTimeMillis();
				mTimerTime = (timeMs + mCurrentTimer.getTimerValue())
						- mStartTime;

				mTimerText.setText(TimerUtil.updateTimer(mTimerTime));
				

				long delayMs = mInteractiveUpdateRateMs
						- (timeMs % mInteractiveUpdateRateMs);
				mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME,
						delayMs);

				break;
			}
		}
	};

}

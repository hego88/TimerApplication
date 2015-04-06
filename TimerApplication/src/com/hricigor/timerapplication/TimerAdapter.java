package com.hricigor.timerapplication;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TimerAdapter extends BaseAdapter {

	private ArrayList<Timer> mTimerList = new ArrayList<Timer>();
	private LayoutInflater mInflater = null;
	private Context mContext;

	public TimerAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		return mTimerList.size();
	}

	@Override
	public Object getItem(int position) {
		return mTimerList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View newView = convertView;
		ViewHolder viewHolder;

		Timer tempTimer = mTimerList.get(position);

		if (null == convertView) {
			viewHolder = new ViewHolder();
			newView = mInflater.inflate(R.layout.list_item, null);

			viewHolder.timerName = (TextView) newView
					.findViewById(R.id.list_item_name);
			viewHolder.timerValue = (TextView) newView
					.findViewById(R.id.list_item_time);
			newView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) newView.getTag();
		}

		viewHolder.timerName.setText(tempTimer.getTimerName());
		viewHolder.timerValue.setText(TimerUtil.convertTime(tempTimer
				.getTimerValue()));

		return newView;
	}

	public int add(Timer timer) {
		mTimerList.add(timer);

		notifyDataSetChanged();

		return mTimerList.indexOf(timer);
	}

	public void delete(int position) {
		mTimerList.remove(position);
		notifyDataSetChanged();
	}

	public ArrayList<Timer> getArrayList() {
		return mTimerList;
	}

	public void setDataFromCursor(Cursor cursor) {
		while (cursor.moveToNext()) {
			mTimerList
					.add(new Timer(cursor.getString(cursor
							.getColumnIndex(TimerDbHelper.TIMER_NAME)), cursor
							.getLong(cursor
									.getColumnIndex(TimerDbHelper.TIMER_VALUE))));
		}

	}

	static class ViewHolder {
		TextView timerName;
		TextView timerValue;
	}

}

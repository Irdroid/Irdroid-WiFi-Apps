package com.microcontrollerbg.nfcremote;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * adapter to populate listview with data
 * 
 * @author ketan(Visit my <a
 *         href="http://androidsolution4u.blogspot.in/">blog</a>)
 */
public class MacrosAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<String> id;
	private ArrayList<String> mname;

	public MacrosAdapter(Context c, ArrayList<String> id,
			ArrayList<String> mname) {
		this.mContext = c;

		this.id = id;
		this.mname = mname;

	}

	public int getCount() {
		// TODO Auto-generated method stub
		return id.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public View getView(int pos, View child, ViewGroup parent) {
		Holder mHolder;
		LayoutInflater layoutInflater;
		if (child == null) {
			layoutInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			child = layoutInflater.inflate(R.layout.listcell1, null);
			mHolder = new Holder();
			mHolder.txt_id = (TextView) child.findViewById(R.id.txt_id1);
			mHolder.txt_fName = (TextView) child.findViewById(R.id.txt_fName1);

			child.setTag(mHolder);
		} else {
			mHolder = (Holder) child.getTag();
		}
		mHolder.txt_id.setText(id.get(pos));
		mHolder.txt_fName.setText(mname.get(pos));

		return child;
	}

	public class Holder {
		TextView txt_id;
		TextView txt_fName;

	}

}

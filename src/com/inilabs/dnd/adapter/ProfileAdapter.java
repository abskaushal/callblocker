package com.inilabs.dnd.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.inilabs.dnd.ProfileActivity;
import com.inilabs.dnd.R;
import com.inilabs.dnd.db.DbManager;
import com.inilabs.dnd.db.ProfileDB;
import com.inilabs.dnd.utils.ProfileEntity;
import com.inilabs.dnd.utils.Util;

public class ProfileAdapter extends ArrayAdapter<ProfileEntity>{

	private View v;
	LayoutInflater inflator;
	private Context mContext;
	private List<ProfileEntity> pList;
	private ProfileEntity pEntity;
	ProfileDB profileDB;
	Util mUtil;

	public static class ViewHolder{
		TextView pName;
		TextView time;
		LinearLayout rowLl;
		ToggleButton status;
	}

	public void setHolder(ViewHolder holder){
		holder.pName = (TextView)v.findViewById(R.id.name);
		holder.time = (TextView)v.findViewById(R.id.time);
		holder.rowLl = (LinearLayout)v.findViewById(R.id.row);
		holder.status = (ToggleButton)v.findViewById(R.id.status);
	}

	public ProfileAdapter(Context context, int resource,
			List<ProfileEntity> objects) {
		super(context, resource, objects);
		mContext = context;
		pList = objects;
		inflator = (LayoutInflater)mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		profileDB = new ProfileDB(mContext, DbManager.DATABASE_NAME, null, DbManager.DATABASE_VERSION);
		mUtil = new Util();
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		v = convertView;
		ViewHolder holder;
		if(v == null){
			v = inflator.inflate(R.layout.profile_row, null);
			holder = new ViewHolder();
			setHolder(holder);
			v.setTag(holder);
		}else{
			holder = (ViewHolder) v.getTag();
		}

		pEntity = pList.get(position);
		holder.rowLl.setTag(position);
		holder.status.setTag(position);

		holder.pName.setText(pEntity.getProfileName());
		if(pEntity.getProfileName().equals("Default")){
			holder.time.setText("Active 24x7");
		}else{
			holder.time.setText(mUtil.get12FmtTime(pEntity.getStartTime())+" - "+mUtil.get12FmtTime(pEntity.getEndTime()));
		}

		if(pEntity.getStatus() == 1){
			holder.status.setChecked(true);
		}else{
			holder.status.setChecked(false);
		}
		holder.rowLl.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int pos = (Integer) v.getTag();
				Intent _int =new Intent(mContext, ProfileActivity.class);
				_int.putExtra("name", pList.get(pos).getProfileName());
				_int.putExtra("starttime", pList.get(pos).getStartTime());
				_int.putExtra("endtime", pList.get(pos).getEndTime());
				_int.putExtra("status", pList.get(pos).getStatus());
				_int.putExtra("id", pList.get(pos).getId());
				mContext.startActivity(_int);

				//				profileDB.checkIfBlockNumber("12345");
			}
		});

		holder.status.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int pos = (Integer) v.getTag();
				ProfileEntity entity = pList.get(pos);
				if(entity.getStatus() == 1){
					pList.get(pos).setStatus(0);
					profileDB.updateProfileStatus(entity.getId(), 0);
				}else{
					pList.get(pos).setStatus(1);
					profileDB.updateProfileStatus(entity.getId(), 1);
				}
				notifyDataSetChanged();
			}
		});

		return v;
	}

}

package com.inilabs.dnd.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.inilabs.dnd.ContactEntity;
import com.inilabs.dnd.R;
import com.inilabs.dnd.db.DbManager;
import com.inilabs.dnd.db.ProfileDB;

public class RejectListAdapter extends ArrayAdapter<ContactEntity>{

	LayoutInflater mInflater;
	List<ContactEntity> mList;
	View v;
	Context mContext;
	ProfileDB pDb;

	public RejectListAdapter(Context context, int resource,
			List<ContactEntity> objects) {
		super(context, resource, objects);
		mList = objects;
		mContext = context;
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		pDb = new ProfileDB(mContext, DbManager.DATABASE_NAME, null, DbManager.DATABASE_VERSION);

	}

	public static class ViewHolder{
		TextView contact;
		TextView name;
		Button delete;
	}

	public void setHolder(ViewHolder holder){
		holder.contact = (TextView)v.findViewById(R.id.number);
		holder.name = (TextView)v.findViewById(R.id.name);
		holder.delete = (Button)v.findViewById(R.id.delete);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) 
	{

		v = convertView;
		ViewHolder holder = null;
		if(v == null){
			v = mInflater.inflate(R.layout.reject_list_row, null);
			holder = new ViewHolder();
			setHolder(holder);
			v.setTag(holder);
		}else{
			holder = (ViewHolder) v.getTag();
		}

		ContactEntity entity = mList.get(position);

		holder.name.setText(entity.getName());
		holder.contact.setText(entity.getContactNo());


		holder.delete.setTag(position);

		holder.delete.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int pos = (Integer) v.getTag();
				showDeleteDialog(pos);
			}
		});
		return v;
	}

	public void insertContactsWithProfile(String pName, String type){
		Log.v("Insert", "Insert");
		ArrayList<ContactEntity> cList = new ArrayList<ContactEntity>();
		for(int i=0; i<mList.size(); i++){
			if(mList.get(i).getIsChecked().equals("1"))
				cList.add(mList.get(i));
		}

		long res = pDb.insertContacts(cList, pName, type);
		if(res>=0){
			Toast.makeText(mContext, "Profile has been updated", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(mContext, "Problem in updating profile", Toast.LENGTH_SHORT).show();
		}
	}

	public void showDeleteDialog(final int pos){


		final Dialog deleteDia = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
		deleteDia.setContentView(R.layout.delete_confirm);
		((TextView)deleteDia.findViewById(R.id.text)).setText("Are you sure to delete '"+mList.get(pos).getContactNo()+"' from this profile?");
		((Button)deleteDia.findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				deleteDia.dismiss();
			}
		});
		((Button)deleteDia.findViewById(R.id.delete)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				long l = pDb.deleteContact(mList.get(pos));
				if(l>0){
					Toast.makeText(mContext, "Contact removed from the profile", Toast.LENGTH_LONG).show();
					mList.remove(pos);
					notifyDataSetChanged();
				}else{
					Toast.makeText(mContext, "Contact not removed", Toast.LENGTH_LONG).show();

				}
				deleteDia.dismiss();
			}
		});

		deleteDia.show();


	}

}

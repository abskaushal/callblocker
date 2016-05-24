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
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.inilabs.dnd.ContactEntity;
import com.inilabs.dnd.R;
import com.inilabs.dnd.db.DbManager;
import com.inilabs.dnd.db.ProfileDB;

public class ContactAdapter extends ArrayAdapter<ContactEntity> implements SectionIndexer{

	LayoutInflater mInflater;
	List<ContactEntity> mList;
	View v;
	Context mContext;
	ProfileDB pDb;
	private HashMap<String, Integer> alphaIndexer;
	private String[] sections;

	public ContactAdapter(Context context, int resource,
			List<ContactEntity> objects) {
		super(context, resource, objects);
		mList = objects;
		mContext = context;
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		pDb = new ProfileDB(mContext, DbManager.DATABASE_NAME, null, DbManager.DATABASE_VERSION);
		alphaIndexer = new HashMap<String, Integer>();
		for (int i = 0; i < mList.size(); i++)
		{

			String s = mList.get(i).getName().toString().substring(0, 1).toUpperCase();

			if (!alphaIndexer.containsKey(s))
				alphaIndexer.put(s, i);
		}

		Set<String> sectionLetters = alphaIndexer.keySet();
		ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
		Collections.sort(sectionList);
		sections = new String[sectionList.size()];
		for (int i = 0; i < sectionList.size(); i++)
			sections[i] = sectionList.get(i)  ;
	}

	public static class ViewHolder{
		TextView contact;
		TextView name;
		CheckBox cb;
		ImageView blocked;
	}

	public void setHolder(ViewHolder holder){
		holder.contact = (TextView)v.findViewById(R.id.number);
		holder.name = (TextView)v.findViewById(R.id.name);
		holder.cb = (CheckBox)v.findViewById(R.id.check);
		holder.blocked = (ImageView)v.findViewById(R.id.blocked);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) 
	{

		v = convertView;
		ViewHolder holder = null;
		if(v == null){
			v = mInflater.inflate(R.layout.contact_row, null);
			holder = new ViewHolder();
			setHolder(holder);
			v.setTag(holder);
		}else{
			holder = (ViewHolder) v.getTag();
		}

		ContactEntity entity = mList.get(position);

		holder.name.setText(entity.getName());
		holder.contact.setText(entity.getContactNo());


		holder.cb.setTag(position);

		holder.cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				int pos = (Integer) buttonView.getTag();
				if(isChecked){
					mList.get(pos).setIsChecked("1");
				}else{
					mList.get(pos).setIsChecked("0");
				}

			}
		});

		holder.blocked.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showBlockDialog();
			}
		});

		if(entity.getIsChecked().equals("0")){
			holder.cb.setChecked(false);
			holder.cb.setVisibility(View.VISIBLE);
			holder.blocked.setVisibility(View.GONE);
		} 
		else if(entity.getIsChecked().equals("1")){
			holder.cb.setChecked(true);
			holder.cb.setVisibility(View.VISIBLE);
			holder.blocked.setVisibility(View.GONE);
		}
		else if(entity.getIsChecked().equals("2")){
			holder.cb.setVisibility(View.GONE);
			holder.blocked.setVisibility(View.VISIBLE);
		}
		else if(entity.getIsChecked().equals("3")){
			holder.cb.setVisibility(View.GONE);
			holder.blocked.setVisibility(View.GONE);
		}

		return v;
	}

	public void showConfirmationDialog(final ArrayList<ContactEntity> clist,final String pName, final String type ){
		final Dialog deleteDia = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
		deleteDia.setContentView(R.layout.delete_confirm);
		String message = "";
		if(clist.size()== 1)
			message = clist.size()+" contact is going to be blocked.";
		else
			message = clist.size()+" contacts are going to be blocked.";
		((TextView)deleteDia.findViewById(R.id.text)).setText(message);
		((Button)deleteDia.findViewById(R.id.delete)).setText("Block");
		((Button)deleteDia.findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				deleteDia.dismiss();
			}
		});
		((Button)deleteDia.findViewById(R.id.delete)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				deleteDia.dismiss();
				long res = pDb.insertContacts(clist, pName, type);
				if(res>=0){
					for(int i=0; i<mList.size(); i++){
						if(mList.get(i).getIsChecked().equals("1"))
							mList.get(i).setIsChecked("2");
					}
					Toast.makeText(mContext, "Profile has been updated", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(mContext, "Problem in updating profile", Toast.LENGTH_SHORT).show();
				}
			}
		});

		deleteDia.show();

	}

	public void showBlockDialog(){	
		final Dialog deleteDia = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
		deleteDia.setContentView(R.layout.number_already_block);
		((Button)deleteDia.findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				deleteDia.dismiss();
			}
		});

		deleteDia.show();
	}

	public void insertContactsWithProfile(String pName, String type){
		Log.v("Insert", "Insert");
		ArrayList<ContactEntity> cList = new ArrayList<ContactEntity>();
		for(int i=0; i<mList.size(); i++){
			if(mList.get(i).getIsChecked().equals("1"))
				cList.add(mList.get(i));
		}
		showConfirmationDialog(cList, pName, type);

	}

	public int getPositionForSection(int section)
	{   
		return alphaIndexer.get(sections[section]);
	}

	public int getSectionForPosition(int position)
	{
		return 1;
	}

	public Object[] getSections()
	{
		return sections;
	}

}

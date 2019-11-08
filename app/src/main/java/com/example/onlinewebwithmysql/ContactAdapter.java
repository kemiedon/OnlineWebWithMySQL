package com.example.onlinewebwithmysql;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ContactAdapter extends BaseAdapter {
    private LayoutInflater myInflater;
    private List<Contact> itemList;

    public ContactAdapter(List<Contact> itemList, Context ctx) {
        myInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.itemList = itemList;
    }

    @Override
    public int getCount() {
        if (itemList != null)
            return itemList.size();
        return 0;
    }

    @Override
    public Contact getItem(int position) {
        if (itemList != null)
            return itemList.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (itemList != null)
            return itemList.get(position).hashCode();
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = myInflater.inflate(R.layout.list_item,null);

        Contact c = itemList.get(position);
        ImageView pic = convertView.findViewById(R.id.image);
        pic.setImageBitmap(c.getPic());
        TextView name =  convertView.findViewById(R.id.name);
        name.setText(c.getName());

        TextView phone = convertView.findViewById(R.id.phone);
        phone.setText(c.getPhoneNum());

        TextView email = (TextView) convertView.findViewById(R.id.email);
        email.setText(c.getEmail());
        TextView birthday = (TextView) convertView.findViewById(R.id.birthday);
        birthday.setText(c.getBirthday());
        return convertView;
    }
    public void setItemList(List<Contact> itemList) {
        this.itemList = itemList;
    }
    public List<Contact> getItemList() {
        return itemList;
    }
}

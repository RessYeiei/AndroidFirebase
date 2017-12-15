package com.example.apotoxin.firebaseandroid;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by apotoxin on 10/12/2017 AD.
 */

public class AdapterList extends BaseAdapter{


    private Context mContext;
    private List<User> mUserList;


    //constructor

    public AdapterList(Context mContext, List<User> mUserList) {
        this.mContext = mContext;
        this.mUserList = mUserList;
    }

    @Override
    public int getCount() {
        return mUserList.size();
    }

    @Override
    public Object getItem(int position) {
        return mUserList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View v = View.inflate(mContext,R.layout.data,null);
        TextView tvname = (TextView) v.findViewById(R.id.tv_name);
        TextView tvquote = (TextView) v.findViewById(R.id.tv_quote);

        //set text

        tvname.setText("Name : "+mUserList.get(position).getName_());
        tvquote.setText("Quote : "+mUserList.get(position).getQuote_());

        return v;
    }

}

package com.example.learn_firebase;

import android.content.Context;
import android.media.Image;
//import android.support.annotation.NonNull;
//import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BlogAdapter  extends RecyclerView.Adapter<BlogAdapter.MyViewHolder> {

    Context mCxt;
    ArrayList<BlogModel> mData = new ArrayList<>();

    BlogAdapter(Context cxt, ArrayList<BlogModel> data){
        this.mCxt = cxt;
        this.mData = data;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(mCxt).inflate(R.layout.blog_row, viewGroup, false);
        return new MyViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.img.setImageResource(R.drawable.common_full_open_on_phone);
       // myViewHolder.desc.setText(mData.get(i).mDesc);
        //myViewHolder.title.setText(mData.get(i).mTitle);

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView img;
        TextView title;
        TextView desc;
        TextView username;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            img = (ImageView) itemView.findViewById(R.id.b_img);
            title = (TextView) itemView.findViewById(R.id.b_title);
            desc = (TextView) itemView.findViewById(R.id.b_desc);
            username = (TextView) itemView.findViewById(R.id.b_username);


        }
    }
}

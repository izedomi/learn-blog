package com.example.learn_firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SingleBlogActivity extends AppCompatActivity {
    String postKey = null;

    FirebaseDatabase mDatabase;
    DatabaseReference mReferenceBlog;
    FirebaseAuth mAuth;

    ImageView imvImage;
    TextView tvTitle;
    TextView tvDescription;
    Button btnRemovePost;
    ProgressDialog mDialog;
    CollapsingToolbarLayout collapsingToolBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_blog);

        final String postKey = getIntent().getExtras().getString("post_key");
        //Toast.makeText(this, postKey, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mReferenceBlog = mDatabase.getReference().child("Blog");

        imvImage = findViewById(R.id.imv_img);
        tvTitle = findViewById(R.id.tv_title);
        tvDescription = findViewById(R.id.tv_description);
        btnRemovePost = findViewById(R.id.btn_remove_post);


        collapsingToolBar = findViewById(R.id.collapsing_tool_bar);
        collapsingToolBar.setTitleEnabled(true);

        mDialog = new ProgressDialog(this);

        mReferenceBlog.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String title = dataSnapshot.child("title").getValue().toString();
                String description = dataSnapshot.child("description").getValue().toString();
                String userId = dataSnapshot.child("user_id").getValue().toString();
                String imgUrl = dataSnapshot.child("img_url").getValue().toString();

                tvTitle.setText(title);
                tvDescription.setText(description);
                collapsingToolBar.setTitle(title);
                Glide
                        .with(SingleBlogActivity.this)
                        .load(imgUrl)
                        .centerCrop()
                        .into(imvImage);

                if(mAuth.getCurrentUser() != null){
                    if(userId.equals(mAuth.getCurrentUser().getUid().toString())){
                        btnRemovePost.setVisibility(View.VISIBLE);
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnRemovePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.setMessage("Removing Post...");
                mDialog.show();
                mReferenceBlog.child(postKey).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mDialog.dismiss();
                        Toast.makeText(SingleBlogActivity.this, "Post Removed!!!", Toast.LENGTH_SHORT).show();
                        Intent mActivity = new Intent(SingleBlogActivity.this, MainActivity.class);
                    }
                });

            }
        });
    }
}

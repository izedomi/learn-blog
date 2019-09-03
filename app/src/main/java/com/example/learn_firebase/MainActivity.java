package com.example.learn_firebase;

import android.content.Context;
import android.content.Intent;
//import android.support.annotation.NonNull;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    private FirebaseRecyclerAdapter adapter;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private DatabaseReference mReferenceUsers;
    private DatabaseReference mReferenceLikes;
    private boolean mProcessLike = false;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Firebase Blog");



        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mAuth = FirebaseAuth.getInstance(); //mAuth.signOut();
        mDatabase = FirebaseDatabase.getInstance();
        //mDatabase.setPersistenceEnabled(true);
        mReference = mDatabase.getReference().child("Blog");
        mReferenceUsers = mDatabase.getReference().child("Users");
        mReferenceLikes = mDatabase.getReference().child("Likes");

        mReference.keepSynced(true);
        mReferenceUsers.keepSynced(true);
        mReferenceUsers.keepSynced(true);
        //mReference = mDatabase.getReference();

        //new BlogModel("com.google.android.gms.tasks.zzu@57cb42", "post one", "this is post one");


        recyclerView = (RecyclerView) findViewById(R.id.rcv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        //recyclerView.setAdapter(BlogAdapter);
        fetch_blog();




    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        TextView title;
        TextView desc;
        TextView username;
        ImageButton likeIcon;
        View v;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);

            v = itemView;
            likeIcon = itemView.findViewById(R.id.b_like);

        }

        public void setTitle(String t){
            title = (TextView) v.findViewById(R.id.b_title);
            title.setText(t);

        }

        public void setDesc(String d){
            desc = (TextView) v.findViewById(R.id.b_desc);
            desc.setText(d);
        }

        public void setUsername(String u){
            username = (TextView) v.findViewById(R.id.b_username);
            username.setText(u);
        }

        public void setImg(Context c, String i){
            img = (ImageView) v.findViewById(R.id.b_img);
            Glide
                    .with(c)
                    .load(i)
                    .centerCrop()
                    .into(img);
          // Glide.
            //Picasso.with(c).load(i).into(img);
        }
    }

    public  void reDirectUser(FirebaseUser currentUser){
        if (currentUser == null) {
            Intent iLogin = new Intent(MainActivity.this, LoginActivity.class);
            //iLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(iLogin);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserAlreadyExists();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        reDirectUser(currentUser);
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.add:
                Intent i = new Intent(MainActivity.this, PostActivity.class);
                startActivity(i);
                break;
            case R.id.logout:
                Toast.makeText(this, "You Signed Out...", Toast.LENGTH_SHORT).show();
                Intent j = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(j);
                mAuth.signOut();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void fetch_blog(){
        Query query = mReference;
        //FirebaseDatabase.getInstance().getReference().child("Blog").limitToLast(50);

        FirebaseRecyclerOptions<BlogModel> options =
                new FirebaseRecyclerOptions.Builder<BlogModel>()
                        .setQuery(query, BlogModel.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<BlogModel, BlogViewHolder>(options){

            @NonNull
            @Override
            public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.blog_row, viewGroup, false);
                return new BlogViewHolder(v);
            }

            @Override
            protected void onBindViewHolder(@NonNull final BlogViewHolder holder, final int position, @NonNull BlogModel model) {
                //holder.img.setImageResource(R.drawable.ic_create_new_folder);

                final String post_key = getRef(position).getKey();

                holder.setImg(MainActivity.this, model.getImg_url());
                holder.setTitle(model.getTitle());
                holder.setDesc(model.getDescription());
                holder.setUsername(model.getUser_name());
                //holder.setDesc(model.getImg_url());

                holder.v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(MainActivity.this, "post key: " + post_key, Toast.LENGTH_SHORT).show();
                        Intent bIntent = new Intent(MainActivity.this, SingleBlogActivity.class);
                        bIntent.putExtra("post_key", post_key);
                        startActivity(bIntent);
                    }
                });

                mReferenceLikes.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(mAuth.getCurrentUser() != null){
                            if(dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid().toString())){
                                holder.likeIcon.setImageResource(R.drawable.ic_thumb_up_pink);
                            }
                            else{
                                holder.likeIcon.setImageResource(R.drawable.ic_thumb_up_gray);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                holder.likeIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mProcessLike = true;
                        mReferenceLikes.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(mProcessLike){
                                    if(dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid().toString())){
                                        holder.likeIcon.setImageResource(R.drawable.ic_thumb_up_pink);
                                        mReferenceLikes.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        mProcessLike = false;
                                    }
                                    else{
                                        holder.likeIcon.setImageResource(R.drawable.ic_thumb_up_gray);
                                        mReferenceLikes.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getEmail());
                                        mProcessLike = false;
                                    }
                                }
                                else{

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

                /*holder.title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("CLICKED TITLE", "I clicked the title" + position);
                    }
                }); */

            }
        };

        recyclerView.setAdapter(adapter);


    }

    public void checkUserAlreadyExists(){

            if(mAuth.getCurrentUser() != null){
                final String userId = mAuth.getCurrentUser().getUid();
                mReferenceUsers.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(!dataSnapshot.hasChild(userId)){

                            Intent sActivity = new Intent(MainActivity.this, AccountSetupActivity.class);
                            sActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(sActivity);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

    }


}

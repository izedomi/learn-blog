package com.example.learn_firebase;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PostActivity extends AppCompatActivity {


    EditText edtTitle;
    EditText edtDesc;
    Button btnSubmit;
    TextView textView;
    ImageButton imgButton;

    String title;
    String description;
    String imgUrl;

    private static final int REQUEST_CODE = 1;
    private Uri imageUri = null;

    private StorageReference mStorageRef;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private DatabaseReference mReferenceUser;
    private DatabaseReference getmReferenceLike;
    private FirebaseAuth mAuth;


    ProgressDialog dialog;

    public void initialize_widget(){

        btnSubmit = (Button) findViewById(R.id.submit);
        imgButton = (ImageButton) findViewById(R.id.img_button);
        edtTitle = (EditText) findViewById(R.id.title);
        edtDesc = (EditText) findViewById(R.id.description);

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference().child("Blog");
        if(mAuth.getCurrentUser() != null){
            mReferenceUser = mDatabase.getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }


        dialog = new ProgressDialog(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        setTitle("Blog Entry");

        initialize_widget();
        
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
              galleryIntent.setType("image/*");
              startActivityForResult(galleryIntent, REQUEST_CODE);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(verify_input_fields()){
                    dialog.setMessage("posting data...");
                    dialog.show();
                    //Toast.makeText(PostActivity.this, "posting data to database", Toast.LENGTH_SHORT).show();
                    start_posting();
                }
                else{
                    Toast.makeText(PostActivity.this, "Complete all fields...", Toast.LENGTH_SHORT).show();
                }



            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            imageUri = data.getData();
            imgButton.setImageURI(imageUri);
        }
    }
    


    public boolean verify_input_fields(){
        title = edtTitle.getText().toString();
        description = edtDesc.getText().toString();
        if(
                (edtTitle.getText().toString().isEmpty())
                        ||(edtDesc.getText().toString().trim().isEmpty())
                        || (imageUri == null)
        ){
            return false;
        }
        else{
            return true;
        }
    }

    public void start_posting(){
        final StorageReference photoRef = mStorageRef.child("Blog_image").child(imageUri.getLastPathSegment());
        final String user_id = mAuth.getCurrentUser().getUid();


        photoRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        // Get a URL to the uploaded content
                        //Uri downloadUrl = taskSnapshot.getDownloadUri();

                        photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri downloadPhotoUrl) {

                                mReferenceUser.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        DatabaseReference mRef = mReference.push();
                                        //DatabaseReference mRef = mReference;
                                        mRef.child("title").setValue(title);
                                        mRef.child("description").setValue(description);
                                        mRef.child("user_id").setValue(user_id);
                                        mRef.child("img_url").setValue(downloadPhotoUrl.toString());
                                        mRef.child("user_name").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                dialog.dismiss();
                                                Toast.makeText(PostActivity.this, "Upload Successful!!!", Toast.LENGTH_SHORT).show();
                                                Intent i = new Intent(PostActivity.this, MainActivity.class);
                                                startActivity(i);
                                            }

                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.i("FAILED TO UPLOAD", "user name upload failed");
                                    }
                                });

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                    }
                });
    }

    public  void reDirectUser(FirebaseUser currentUser){
        if (currentUser == null) {
            Intent iLogin = new Intent(PostActivity.this, LoginActivity.class);
            startActivity(iLogin);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        reDirectUser(currentUser);
    }



        /*

        final Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                //myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(myCalendar);
            }

        };
        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(PostActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        */


    private void updateLabel(Calendar myCalendar) {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        textView.setText(sdf.format(myCalendar.getTime()));
    }


}

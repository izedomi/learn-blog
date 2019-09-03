package com.example.learn_firebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class AccountSetupActivity extends AppCompatActivity {

    private final int GALLERY_REQUEST_CODE = 1;
    Uri imageUri;


    ImageButton ibProfileImage;
    EditText edtProfileName;
    Button btnSubmit;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mData;
    private DatabaseReference mRef;
    private StorageReference mStorageRef;

    private ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);
        setTitle("Account Setup");

        ibProfileImage = (ImageButton) findViewById(R.id.ib_setup_profile_img);
        edtProfileName = (EditText) findViewById(R.id.edt_setup_profiel_name);
        btnSubmit = (Button) findViewById(R.id.btn_submit);

        mAuth = FirebaseAuth.getInstance();
        mData = FirebaseDatabase.getInstance();
        mRef = mData.getReference().child("Users");
        mStorageRef = FirebaseStorage.getInstance().getReference();
        dialog = new ProgressDialog(this);

        ibProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("imag/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               dialog.setMessage("completing account setup...Please be patient");
               dialog.show();
                final String displayName = edtProfileName.getText().toString().trim();
                final String userId = mAuth.getCurrentUser().getUid();


                if(!TextUtils.isEmpty(displayName) && imageUri != null){
                    final StorageReference f = mStorageRef.child("Profile_Images").child(imageUri.getLastPathSegment());


                    f.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            f.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    dialog.dismiss();
                                    DatabaseReference mRefUser = mRef.child(userId);
                                    mRefUser.child("name").setValue(displayName);
                                    mRefUser.child("image").setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Intent i = new Intent(AccountSetupActivity.this, MainActivity.class);
                                            startActivity(i);
                                        }
                                    });

                                }
                            });
                            Toast.makeText(AccountSetupActivity.this, "setup complete...redirecting to blog", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast.makeText(AccountSetupActivity.this, exception.getLocalizedMessage().toString(), Toast.LENGTH_SHORT).show();
                                    Log.i("upload photo: ", exception.getMessage().toString());
                                }
                            });


                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){
            Uri sourceUri = data.getData();

            CropImage.activity(sourceUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                ibProfileImage.setImageURI(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

}

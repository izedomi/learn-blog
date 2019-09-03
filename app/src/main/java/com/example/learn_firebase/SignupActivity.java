package com.example.learn_firebase;

import android.app.ProgressDialog;
import android.content.Intent;
//import android.support.annotation.NonNull;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private final static String TAG = "Signup Activity";

    EditText editTextName;
    EditText editTextPassword;
    EditText editTextEmail;
    Button btnSubmit;
    Button btnSignin;


    private FirebaseAuth mAuth;

    private FirebaseDatabase mData;
    private DatabaseReference mRef;
    ProgressDialog mProgressDialog;

    String name, email, password;

    public void initialize_widget(){
        editTextName = (EditText) findViewById(R.id.edt_name);
        editTextEmail = (EditText) findViewById(R.id.edt_email);
        editTextPassword = (EditText) findViewById(R.id.edt_password);
        btnSubmit = (Button) findViewById(R.id.btn_submit);
        btnSignin = (Button) findViewById(R.id.btn_signin);

        mAuth = FirebaseAuth.getInstance();
        mData = FirebaseDatabase.getInstance();
        mRef = mData.getReference().child("Users");
        mProgressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        //setTitle("Signup");
        initialize_widget();

        mProgressDialog.setMessage("signing you up....please be patient");
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog.show();
                name = editTextName.getText().toString().trim();
                email = editTextEmail.getText().toString().trim();
                password = editTextPassword.getText().toString().trim();

                if(TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                    Toast.makeText(SignupActivity.this, "All fields are required!!!", Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                } else{
                    //Toast.makeText(SignupActivity.this, name + " " + email + " " + password, Toast.LENGTH_SHORT).show();
                    //mProgressDialog.dismiss();
                    sigupUser(email, password);
                }
            }
        });

        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iSignup = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(iSignup);
            }
        });

    }

    public void sigupUser(String email, String password){
        Toast.makeText(SignupActivity.this, name + " " + email + " " + password, Toast.LENGTH_SHORT).show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mProgressDialog.dismiss();

                            String userId = mAuth.getCurrentUser().getUid();

                            DatabaseReference mUserRef = mRef.child(userId);
                            mUserRef.child("name").setValue(name);
                            mUserRef.child("image").setValue("default");
                            Intent mActivity = new Intent(SignupActivity.this, MainActivity.class);
                            mActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mActivity);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Authentication failed.",
                                    Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();
                        }

                    }
                });
    }

    public  void reDirectUser(FirebaseUser currentUser){
        if (currentUser != null) {
            Intent iLogin = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(iLogin);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        reDirectUser(currentUser);
    }
}

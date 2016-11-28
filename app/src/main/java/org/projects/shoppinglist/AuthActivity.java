package org.projects.shoppinglist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AuthActivity extends AppCompatActivity implements
        View.OnClickListener {
    private static final String TAG = "EmailPassword";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");

    private EditText emailSignup;
    private EditText passwordSignup;
    private TextView emailLogged;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        context = this;

        emailSignup = (EditText) findViewById(R.id.username);
        passwordSignup = (EditText) findViewById(R.id.password);
        emailLogged = (TextView) findViewById(R.id.email);

        findViewById(R.id.logininBtn).setOnClickListener(this);
        findViewById(R.id.signupBtn).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                    AuthActivity.this.startActivity(intent);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void createAccount(final String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            String error = task.getException().getMessage();
                            Log.w(TAG,error );
                            Toast.makeText(AuthActivity.this, error,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            ref.push().setValue(new User(email));
                        }
                    }
                });
    }

    private void logIn(final String email, String password) {
        Log.d(TAG, "logIn:" + email);
        if (!validateForm()) {
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(AuthActivity.this, "signin failed",
                                    Toast.LENGTH_SHORT).show();
                            emailLogged.setText("signin failed");
                        }
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = emailSignup.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailSignup.setError("Required.");
            valid = false;
        } else {
            emailSignup.setError(null);
        }

        String password = passwordSignup.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordSignup.setError("Required.");
            valid = false;
        } else {
            passwordSignup.setError(null);
        }

        return valid;
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.signupBtn) {
            createAccount(emailSignup.getText().toString(), passwordSignup.getText().toString());
        } else if (i == R.id.logininBtn) {
            logIn(emailSignup.getText().toString(), passwordSignup.getText().toString());
        }
    }
}

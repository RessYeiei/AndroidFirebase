package com.example.apotoxin.firebaseandroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private SignInButton googleSigninBtn;
    private GoogleApiClient googleApiClient;
    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    final String TAG = "Firebase Respond";

    private ListView mylistView;
    private AdapterList adapterList;
    private List<User> mUserlist;
    ArrayList<String> name_array = new ArrayList<String>();

    private DatabaseReference mDatabase;
    private DatabaseReference mRecordsRef;

    private Button signin,signup;
    private EditText email_edt,password_edt;

    private Button dis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        signin = findViewById(R.id.signin_btn);
        signup = findViewById(R.id.signup_btn);
        email_edt = findViewById(R.id.email_edt);
        password_edt = findViewById(R.id.password_edt);



        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null)
                {
                    Intent signInIntent = new Intent(MainActivity.this,AccountActivity.class);
                    signInIntent.putExtra("type","googlesignin");
                    startActivity(signInIntent);

                }
            }
        };

        googleSigninBtn = findViewById(R.id.google_signin_btn);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MainActivity.this,"you are got an error",Toast.LENGTH_SHORT).show();

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        googleSigninBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInGoogle();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount(email_edt.getText().toString(),password_edt.getText().toString());
            }
        });
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn(email_edt.getText().toString(),password_edt.getText().toString());
            }
        });

        mRecordsRef = mDatabase.child("records");
        // Read from the database
        mRecordsRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                name_array.clear();
                Log.d("-------",dataSnapshot.toString());
                for(DataSnapshot childDataSnapshot : dataSnapshot.getChildren())
                {
                    //  Log.d("value=>>>",childDataSnapshot.getKey());

//                    Log.d("value=>>>", (String) childDataSnapshot.child("name").getValue());
//                    Log.d("value=>>>", (String) childDataSnapshot.child("quote").getValue());
//                    Log.d("-------","---");
                    String fire_data_name = (String) childDataSnapshot.child("name").getValue();
                    name_array.add(fire_data_name);

                }

                mylistView = findViewById(R.id.main_list);
                mUserlist = new ArrayList<>();
                for(int i = 0; i<name_array.size();i++){
                    mUserlist.add(new User(name_array.get(i),"please sign in for see Quote"));
                }
                adapterList = new AdapterList(getApplicationContext(),mUserlist);
                mylistView.setAdapter(adapterList);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }



    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
    }

    private void signInGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }


        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(MainActivity.this, "Sign up success.",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Sign up failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // [START_EXCLUDE]
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(MainActivity.this, "Sign in success.",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Sign in failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
        // [END sign_in_with_email]
    }



    private boolean validateForm() {
        boolean valid = true;
        Pattern patternEmail = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcherEmail = patternEmail.matcher(email_edt.getText().toString());

        String email = email_edt.getText().toString();
        if (TextUtils.isEmpty(email)) {
            email_edt.setError("Required.");
            valid = false;
        } else if (!matcherEmail.find()) {
            email_edt.setError("Invalid Email Address");
            valid = false;
        }
        else {
            email_edt.setError(null);
        }

        String password = password_edt.getText().toString();
        if (TextUtils.isEmpty(password)) {
            password_edt.setError("Required.");
            valid = false;
        }else if(password_edt.getText().toString().length()<6){
            password_edt.setError("need more than 6 char");
            valid = false;
        }
        else {
            password_edt.setError(null);
        }

        return valid;
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}

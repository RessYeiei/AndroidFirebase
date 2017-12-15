package com.example.apotoxin.firebaseandroid;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AccountActivity extends AppCompatActivity{
    private static final String TAG = "AccountActivity ===> ";
    private Button googleLogoutBtn;
    private Button addDataToFirebase;
    private EditText name, quote;
    private TextView cur_name,cur_quote,cur_uid;
    private ListView listview;
    private AdapterList adapterList;
    private List<User> mUserlist;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference mDatabase;
    private DatabaseReference mRecordsRef;

    private String uid;
    private String data_name ;
    private String data_quote ;

    ArrayList<String> name_array = new ArrayList<String>();
    ArrayList<String> quote_array = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        initView();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null)
                {
                    startActivity(new Intent(AccountActivity.this,MainActivity.class));
                }
            }
        };


        mRecordsRef = mDatabase.child("records");
        // Read from the database
        mRecordsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                name_array.clear();
                quote_array.clear();


                for(DataSnapshot childDataSnapshot : dataSnapshot.getChildren())
                {
                  //  Log.d("value=>>>",childDataSnapshot.getKey());

//                    Log.d("value=>>>", (String) childDataSnapshot.child("name").getValue());
//                    Log.d("value=>>>", (String) childDataSnapshot.child("quote").getValue());
//                    Log.d("-------","---");
                    String fire_data_name = (String) childDataSnapshot.child("name").getValue();
                    String fire_data_quote = (String) childDataSnapshot.child("quote").getValue();
                    name_array.add(fire_data_name);
                    quote_array.add(fire_data_quote);


                }
//                Log.d("value=>>>", (String) dataSnapshot.child(uid).child("name").getValue());
//                Log.d("value=
                String current_name = (String) dataSnapshot.child(uid).child("name").getValue();
                String current_quote = (String) dataSnapshot.child(uid).child("quote").getValue();
                cur_name.setText("Yours Name : "+current_name);
                cur_quote.setText("Yours Quote : "+current_quote);
                cur_uid.setText("Yours Uid : "+uid);

                listview = findViewById(R.id.list);
                mUserlist = new ArrayList<>();
                for(int i = 0; i<name_array.size();i++){
                    mUserlist.add(new User(name_array.get(i),quote_array.get(i)));
                }
                adapterList = new AdapterList(getApplicationContext(),mUserlist);
                listview.setAdapter(adapterList);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


        //button

        googleLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
            }
        });
        addDataToFirebase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!name.getText().toString().isEmpty() && !quote.getText().toString().isEmpty())
                {
                    data_name = name.getText().toString();
                    data_quote = quote.getText().toString();
                    mDatabase.child("records").child(uid).child("name").setValue(data_name);
                    mDatabase.child("records").child(uid).child("quote").setValue(data_quote);
                    name.setText("");
                    quote.setText("");
                }
                else {
                    Toast.makeText(AccountActivity.this,"please enter all data",Toast.LENGTH_SHORT).show();
                }



            }
        });

    }
    private void initView(){
        googleLogoutBtn = findViewById(R.id.google_logout_btn);
        addDataToFirebase = findViewById(R.id.add_data_btn);
        name = findViewById(R.id.data_name);
        quote = findViewById(R.id.data_quote);
        cur_name = findViewById(R.id.name_cur);
        cur_quote = findViewById(R.id.quote_cur);
        cur_uid = findViewById(R.id.uid_cur);

        uid = mAuth.getCurrentUser().getUid().toString();
    }



    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
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

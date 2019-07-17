package com.example.orbital2019;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.orbital2019.intermediate.model.UserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RegActivity extends AppCompatActivity {

    private EditText nameEditText, areaEditText, emailEditText, passwordEditText, confirmEditText;
    private Button regBtn;
    Uri imageUri;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_register);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setTitle("Register");

        //Firestore Setup
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        mAuth = FirebaseAuth.getInstance();

        //initialise components and look for them according to their IDs
        nameEditText = findViewById(R.id.name);
        areaEditText = findViewById(R.id.area);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmEditText = findViewById(R.id.confirm);

        regBtn = findViewById(R.id.register);
        progressBar = findViewById(R.id.progressBar);

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });

    }
    private void registerNewUser() {
        progressBar.setVisibility(View.VISIBLE);

        //get the actual String or text that the user type
        final String name, area, email, password, confirm;
        name = nameEditText.getText().toString();
        area = areaEditText.getText().toString();
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();
        confirm = confirmEditText.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(), "Please enter username", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(area)) {
            Toast.makeText(getApplicationContext(), "Please enter area of stay", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please enter email", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Please enter password", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(confirm)) {
            Toast.makeText(getApplicationContext(), "Please confirm password", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.logo)
                + '/' + getResources().getResourceTypeName(R.drawable.logo) + '/' + getResources().getResourceEntryName(R.drawable.logo));

        final RegActivity act = this;
        final FirebaseFirestore fs = FirebaseFirestore.getInstance();
        fs.collection(UserDetails.userDetailsKey).whereEqualTo(UserDetails.nameKey, name)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<DocumentSnapshot> docs = task.getResult().getDocuments();
                if (docs.isEmpty()) {
                    if (confirm.equals(password)) {
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.GONE);

                                            FirebaseUser user = mAuth.getCurrentUser();
                                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                    .setDisplayName(name).build();
                                            user.updateProfile(profileUpdates);

                                            //Create user details if successful.
                                            List<String> review = new ArrayList<>();
                                            List<Long> favourite = new ArrayList<>();
                                            String id = user.getUid();
                                            UserDetails currentUser = new UserDetails(id, name, area, "", 0, 0.0, review, favourite);
                                            currentUser.createEntry();
                                            while (mAuth.getCurrentUser().getDisplayName() == null) {
                                                Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                                            }
                                            progressBar.setVisibility(View.GONE);
                                            Intent intent = new Intent(act, MainActivity.class);
                                            startActivityForResult(intent, 0);

                                        } else {

                                            try {
                                                throw task.getException();
                                            } catch(FirebaseAuthWeakPasswordException e) {
                                                Toast.makeText(getApplicationContext(), "Registration failed! " +
                                                        "Password needs to be more than 6 characters!", Toast.LENGTH_LONG).show();
                                                progressBar.setVisibility(View.GONE);
                                            } catch(FirebaseAuthInvalidCredentialsException e) {
                                                Toast.makeText(getApplicationContext(), "Registration failed! " +
                                                        "Invalid email!", Toast.LENGTH_LONG).show();
                                                progressBar.setVisibility(View.GONE);
                                            } catch(FirebaseAuthUserCollisionException e) {
                                                Toast.makeText(getApplicationContext(), "Registration failed! " +
                                                        "User already exists!", Toast.LENGTH_LONG).show();
                                                progressBar.setVisibility(View.GONE);
                                            } catch(Exception e) {
                                                Log.d("testing ", "onComplete: " + task.toString());
                                            }
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(getApplicationContext(), "Password given does not match!", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Username already exist!", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

    }

}

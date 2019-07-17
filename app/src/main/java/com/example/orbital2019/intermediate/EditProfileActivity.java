package com.example.orbital2019.intermediate;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.orbital2019.LogActivity;
import com.example.orbital2019.R;
import com.example.orbital2019.intermediate.model.PostDetails;
import com.example.orbital2019.intermediate.model.UserDetails;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.List;
import java.util.Map;

import static com.example.orbital2019.intermediate.model.UserDetails.certKey;

public class EditProfileActivity extends AppCompatActivity {

    private EditText username, area;
    private Button updateButton, deleteButton, uploadButton, deleteImageButton;
    private ImageView imageView;
    private Uri imageUri;
    private StorageReference storageReference;
    private final static int PICK_IMAGE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Update Profile");

        storageReference = FirebaseStorage.getInstance().getReference();

        username = findViewById(R.id.updateUsername);
        area = findViewById(R.id.updateArea);


        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        final String name = user.getDisplayName();
        final String id = user.getUid();

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(UserDetails.userDetailsKey).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                for (final DocumentSnapshot document : documents) {
                    if (document.contains(UserDetails.idKey) && document.contains(UserDetails.nameKey)
                            && document.contains(UserDetails.areaKey) && document.contains(UserDetails.certKey)
                            && document.contains(UserDetails.numberKey) && document.contains(UserDetails.ratingKey)
                            && document.contains(UserDetails.reviewKey) && document.contains(UserDetails.favouriteKey)) {

                        final String myName = (String) document.get(UserDetails.nameKey);
                        final String myID = (String) document.get(UserDetails.idKey);
                        if (myID.equals(id)) {
                            String myArea = (String) document.get(UserDetails.areaKey);
                            final String cert = (String) document.get(certKey);
                            double rating = (double) document.get(UserDetails.ratingKey);
                            long number = (long) document.get(UserDetails.numberKey);
                            List<String> reviews = (List<String>) document.get(UserDetails.reviewKey);
                            List<Long> favourite = (List<Long>) document.get(UserDetails.favouriteKey);
                            username.setText(myName);
                            area.setText(myArea);

                            final UserDetails userDetails = new UserDetails(myID, myName, myArea, cert, number, rating, reviews, favourite);

                            uploadButton = findViewById(R.id.upload);
                            imageView = findViewById(R.id.updateCert);

                            if (cert.equals("")) {
                                imageView.setVisibility(View.GONE);
                            } else {
                                Glide.with(EditProfileActivity.this).load(cert).into(imageView);
                                imageView.setVisibility(View.VISIBLE);
                            }

                            uploadButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openGallery();
                                }
                            });

                            deleteImageButton = findViewById(R.id.delete);
                            deleteImageButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    imageView.setVisibility(View.GONE);
                                    imageUri = null;
                                }
                            });

                            updateButton = findViewById(R.id.buttonUpdateProfile);
                            updateButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(final View v) {

                                    if (TextUtils.isEmpty(username.getText().toString())) {
                                        Toast.makeText(getApplicationContext(), "Please enter username", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    if (TextUtils.isEmpty(area.getText().toString())) {
                                        Toast.makeText(getApplicationContext(), "Please enter area of stay", Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    db.collection(UserDetails.userDetailsKey).whereEqualTo(UserDetails.nameKey, username.getText().toString())
                                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            List<DocumentSnapshot> docs = task.getResult().getDocuments();
                                            if (!docs.isEmpty() && !docs.get(0).get(UserDetails.nameKey).equals(myName)) {
                                                Toast.makeText(getApplicationContext(), "Username already exist!", Toast.LENGTH_LONG).show();
                                            } else {
                                                final ProgressDialog progressDialog = new ProgressDialog(EditProfileActivity.this);
                                                progressDialog.setTitle("Updating...");
                                                progressDialog.show();

                                                userDetails.deleteEntry();

                                                db.collection(PostDetails.postDetailsKey).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        List<DocumentSnapshot> docs = task.getResult().getDocuments();
                                                        for (DocumentSnapshot doc : docs) {
                                                            if (doc.contains(PostDetails.foodNameKey) && doc.contains(PostDetails.descriptionKey)
                                                                    && doc.contains(PostDetails.priceKey) && doc.contains(PostDetails.sellerKey)
                                                                    && doc.contains(PostDetails.areaKey) && doc.contains(PostDetails.typeKey)
                                                                    && doc.contains(PostDetails.inputKey) && doc.contains(PostDetails.dateKey)) {

                                                                String seller = (String) doc.get(PostDetails.sellerKey);

                                                                if (seller.equals(id)) {

                                                                    String name = (String) doc.get(PostDetails.foodNameKey);
                                                                    String description = (String) doc.get(PostDetails.descriptionKey);
                                                                    String ingredient = (String) doc.get(PostDetails.ingredientKey);
                                                                    String price = (String) doc.get(PostDetails.priceKey);
                                                                    String sArea = (String) doc.get(PostDetails.areaKey);

                                                                    Map type = (Map) doc.get(PostDetails.typeKey);
                                                                    Boolean halal = (Boolean) type.get(PostDetails.halalKey);
                                                                    Boolean vegetarian = (Boolean) type.get(PostDetails.vegetarianKey);
                                                                    Boolean chinese = (Boolean) type.get(PostDetails.chineseKey);
                                                                    Boolean malay = (Boolean) type.get(PostDetails.malayKey);
                                                                    Boolean indian = (Boolean) type.get(PostDetails.indianKey);
                                                                    Boolean western = (Boolean) type.get(PostDetails.westernKey);
                                                                    Boolean other = (Boolean) type.get(PostDetails.otherKey);

                                                                    String image = (String) doc.get(PostDetails.imageKey);
                                                                    long input = (long) doc.get(PostDetails.inputKey);
                                                                    String date = (String) doc.get(PostDetails.dateKey);

                                                                    PostDetails oldPost = new PostDetails(name, description, ingredient, price, seller, sArea,
                                                                            halal, vegetarian, chinese, malay, indian, western, other, image, input, date);
                                                                    oldPost.deleteEntry();

                                                                    PostDetails newPost = new PostDetails(name, description, ingredient, price, seller, area.getText().toString(),
                                                                            halal, vegetarian, chinese, malay, indian, western, other, image, input, date);
                                                                    newPost.createEntry();

                                                                }

                                                            }
                                                        }
                                                    }
                                                });

                                                if (imageView.getVisibility() == View.GONE) {
                                                    UserDetails newUser = new UserDetails(id, username.getText().toString(), area.getText().toString(),
                                                            "", userDetails.getNumber(), userDetails.getRating(), userDetails.getReview(), userDetails.getFavourite());
                                                    newUser.createEntry();
                                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                            .setDisplayName(username.getText().toString()).build();
                                                    user.updateProfile(profileUpdates);
                                                    progressDialog.dismiss();
                                                    Toast.makeText(getApplicationContext(), "Profile updated successfully!", Toast.LENGTH_LONG).show();
                                                    finish();

                                                } else if (imageUri == null) {
                                                    UserDetails newUser = new UserDetails(id, username.getText().toString(), area.getText().toString(),
                                                            userDetails.getCert(), userDetails.getNumber(), userDetails.getRating(), userDetails.getReview(), userDetails.getFavourite());
                                                    newUser.createEntry();
                                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                            .setDisplayName(username.getText().toString()).build();
                                                    user.updateProfile(profileUpdates);
                                                    progressDialog.dismiss();
                                                    Toast.makeText(getApplicationContext(), "Profile updated successfully!", Toast.LENGTH_LONG).show();
                                                    finish();

                                                } else {
                                                    final StorageReference sRef = storageReference.child("uploads/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
                                                    sRef.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                                        @Override
                                                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                                            if (!task.isSuccessful()) {
                                                                throw task.getException();
                                                            }
                                                            return sRef.getDownloadUrl();
                                                        }
                                                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Uri> task) {
                                                            if (task.isSuccessful()) {
                                                                Uri downloadUri = task.getResult();
                                                                UserDetails newUser = new UserDetails(id, username.getText().toString(), area.getText().toString(),
                                                                        downloadUri.toString(), userDetails.getNumber(), userDetails.getRating(), userDetails.getReview(), userDetails.getFavourite());
                                                                newUser.createEntry();
                                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                                        .setDisplayName(username.getText().toString()).build();
                                                                user.updateProfile(profileUpdates);
                                                                progressDialog.dismiss();
                                                                Toast.makeText(getApplicationContext(), "Profile updated successfully!", Toast.LENGTH_LONG).show();
                                                                finish();
                                                            } else {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(EditProfileActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });

                                                }
                                            }
                                        }
                                    });
                                }
                            });

                            deleteButton = findViewById(R.id.buttonDeleteProfile);
                            deleteButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                                    builder.setTitle("Are you sure you want to delete account?");
                                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            final ProgressDialog progressDialog = new ProgressDialog(EditProfileActivity.this);
                                            progressDialog.setTitle("Deleting...");
                                            progressDialog.show();

                                            userDetails.deleteEntry();
                                            user.delete();

                                            db.collection(PostDetails.postDetailsKey).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    List<DocumentSnapshot> docs = task.getResult().getDocuments();
                                                    for (DocumentSnapshot doc : docs) {
                                                        if (doc.contains(PostDetails.foodNameKey) && doc.contains(PostDetails.descriptionKey) && doc.contains(PostDetails.ingredientKey)
                                                                && doc.contains(PostDetails.priceKey) && doc.contains(PostDetails.sellerKey)
                                                                && doc.contains(PostDetails.areaKey) && doc.contains(PostDetails.typeKey) && doc.contains(PostDetails.inputKey)
                                                                && doc.contains(PostDetails.dateKey)) {

                                                            String seller = (String) doc.get(PostDetails.sellerKey);

                                                            if (seller.equals(myID)) {

                                                                String name = (String) doc.get(PostDetails.foodNameKey);
                                                                String description = (String) doc.get(PostDetails.descriptionKey);
                                                                String ingredient = (String) doc.get(PostDetails.ingredientKey);
                                                                String price = (String) doc.get(PostDetails.priceKey);
                                                                String sArea = (String) doc.get(PostDetails.areaKey);

                                                                Map type = (Map) doc.get(PostDetails.typeKey);
                                                                Boolean halal = (Boolean) type.get(PostDetails.halalKey);
                                                                Boolean vegetarian = (Boolean) type.get(PostDetails.vegetarianKey);
                                                                Boolean chinese = (Boolean) type.get(PostDetails.chineseKey);
                                                                Boolean malay = (Boolean) type.get(PostDetails.malayKey);
                                                                Boolean indian = (Boolean) type.get(PostDetails.indianKey);
                                                                Boolean western = (Boolean) type.get(PostDetails.westernKey);
                                                                Boolean other = (Boolean) type.get(PostDetails.otherKey);

                                                                String image = (String) doc.get(PostDetails.imageKey);
                                                                long input = (long) doc.get(PostDetails.inputKey);
                                                                String date = (String) doc.get(PostDetails.dateKey);

                                                                PostDetails oldPost = new PostDetails(name, description, ingredient, price, seller, sArea,
                                                                        halal, vegetarian, chinese, malay, indian, western, other, image, input, date);
                                                                oldPost.deleteEntry();

                                                            }

                                                        }
                                                    }
                                                }
                                            });

                                            Intent intent = new Intent(getApplicationContext(), LogActivity.class);
                                            startActivityForResult(intent, 0);
                                            progressDialog.dismiss();
                                            Toast.makeText(getApplicationContext(), "Profile deleted successfully!", Toast.LENGTH_LONG).show();

                                        }
                                    });
                                    builder.setNegativeButton("Not now", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            dialogInterface.dismiss();
                                        }
                                    });

                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            });

                            break;
                        }
                    }
                }
            }
        });


    }

    private void openGallery() {
        Intent gallery = new Intent();
        gallery.setType("image/*");
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                imageView.setImageURI(imageUri);
                imageView.setVisibility(View.VISIBLE);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}

package com.example.orbital2019.intermediate;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.orbital2019.R;
import com.example.orbital2019.intermediate.model.InputDetails;
import com.example.orbital2019.intermediate.model.PostDetails;
import com.example.orbital2019.intermediate.model.UserDetails;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CreateNewPostActivity extends AppCompatActivity {

    private EditText nameEditText, descriptionEditText, priceEditText, ingredientEditText;
    private Button createBtn, chooseBtn, deleteBtn;
    Uri imageUri;
    private ImageView image;
    private CheckBox halalCheckBox, vegetarianCheckBox, chineseCheckBox, malayCheckBox, indianCheckBox, westernCheckBox, otherCheckBox;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    private final static int PICK_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createnewpost);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setTitle("Create New Post");

        //initialise components and look for them according to their IDs
        nameEditText = findViewById(R.id.name);
        descriptionEditText = findViewById(R.id.description);
        priceEditText = findViewById(R.id.price);
        ingredientEditText = findViewById(R.id.ingredient);

        halalCheckBox = findViewById(R.id.checkHalal);
        vegetarianCheckBox = findViewById(R.id.checkVegetarian);
        chineseCheckBox = findViewById(R.id.checkChinese);
        malayCheckBox = findViewById(R.id.checkMalay);
        indianCheckBox = findViewById(R.id.checkIndian);
        westernCheckBox = findViewById(R.id.checkWestern);
        otherCheckBox = findViewById(R.id.checkOther);

        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        createBtn = findViewById(R.id.create);
        progressBar = findViewById(R.id.progressBar);

        chooseBtn = findViewById(R.id.choose);
        deleteBtn = findViewById(R.id.delete);
        image = findViewById(R.id.imageView);

        chooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                        "://" + getResources().getResourcePackageName(R.drawable.logo)
                        + '/' + getResources().getResourceTypeName(R.drawable.logo) + '/' + getResources().getResourceEntryName(R.drawable.logo));
                image.setImageURI(imageUri);
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewPost();
            }
        });
        priceEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && !priceEditText.getText().toString().isEmpty()) {
                    try {
                        String money = priceEditText.getText().toString();
                        priceEditText.setText(String.format("%.2f", Double.parseDouble(money)));
                    } catch (NumberFormatException e) {
                        Toast.makeText(getApplicationContext(), "Invalid price", Toast.LENGTH_LONG).show();
                        return;
                    }

                }
            }
        });

    }

    private void createNewPost() {
        progressBar.setVisibility(View.VISIBLE);

        final String foodName, description, price, ingredient;
        String words;
        foodName = nameEditText.getText().toString();
        words = descriptionEditText.getText().toString();
        price = priceEditText.getText().toString();
        ingredient = ingredientEditText.getText().toString();

        if (TextUtils.isEmpty(foodName)) {
            Toast.makeText(getApplicationContext(), "Please enter food name", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(words)) {
            description = "NIL";
        } else {
            description = words;
        }
        if (TextUtils.isEmpty(ingredient)) {
            Toast.makeText(getApplicationContext(), "Please enter ingredients", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(price)) {
            Toast.makeText(getApplicationContext(), "Please enter price", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.GONE);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Creating...");
        progressDialog.show();


        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        final FirebaseUser user = mAuth.getCurrentUser();
        if(imageUri == null) {
            imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + getResources().getResourcePackageName(R.drawable.logo)
                    + '/' + getResources().getResourceTypeName(R.drawable.logo) + '/' + getResources().getResourceEntryName(R.drawable.logo));

            db.collection(InputDetails.inputDetailsKey).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                    final long order = (long) doc.get(InputDetails.orderKey);
                    InputDetails inputOrder = new InputDetails(order);
                    inputOrder.deleteEntry();
                    inputOrder = new InputDetails(order + 1);
                    inputOrder.createEntry();
                    db.collection(UserDetails.userDetailsKey).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            for (DocumentSnapshot document : documents) {
                                if (document.contains(UserDetails.idKey) && document.contains(UserDetails.nameKey)
                                        && document.contains(UserDetails.areaKey) && document.contains(UserDetails.certKey)
                                        && document.contains(UserDetails.numberKey) && document.contains(UserDetails.ratingKey)
                                        && document.contains(UserDetails.reviewKey) && document.contains(UserDetails.favouriteKey)) {
                                    String name = (String) document.get(UserDetails.nameKey);
                                    if (name.equals(user.getDisplayName())) {
                                        String area = (String) document.get(UserDetails.areaKey);
                                        Date c = new Date();
                                        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm aaa");
                                        String formattedDate = df.format(c);

                                        PostDetails post = new PostDetails(foodName, description, ingredient, price, user.getUid(), area,
                                                halalCheckBox.isChecked(), vegetarianCheckBox.isChecked(), chineseCheckBox.isChecked(), malayCheckBox.isChecked(),
                                                indianCheckBox.isChecked(), westernCheckBox.isChecked(), otherCheckBox.isChecked(), imageUri.toString(), order, formattedDate);
                                        post.createEntry();
                                        Toast.makeText(getApplicationContext(), "Create successful!", Toast.LENGTH_LONG).show();
                                        progressDialog.dismiss();
                                        finish();
                                        break;
                                    }
                                }
                            }
                        }
                    });
                }
            });

        } else {
            final StorageReference sRef = storageReference.child("uploads/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
            sRef.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
            {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return sRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>()
            {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    if (task.isSuccessful())
                    {
                        final Uri downloadUri = task.getResult();
                        progressDialog.dismiss();

                        db.collection(InputDetails.inputDetailsKey).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                                final long order = (long) doc.get(InputDetails.orderKey);
                                InputDetails inputOrder = new InputDetails(order);
                                inputOrder.deleteEntry();
                                inputOrder = new InputDetails(order + 1);
                                inputOrder.createEntry();
                                db.collection(UserDetails.userDetailsKey).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                                        for (DocumentSnapshot document : documents) {
                                            if (document.contains(UserDetails.idKey) && document.contains(UserDetails.nameKey)
                                                    && document.contains(UserDetails.areaKey) && document.contains(UserDetails.certKey)
                                                    && document.contains(UserDetails.numberKey) && document.contains(UserDetails.ratingKey)
                                                    && document.contains(UserDetails.reviewKey) && document.contains(UserDetails.favouriteKey)) {
                                                String name = (String) document.get(UserDetails.nameKey);
                                                if (name.equals(user.getDisplayName())) {
                                                    String area = (String) document.get(UserDetails.areaKey);
                                                    Date c = new Date();
                                                    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm aaa");
                                                    String formattedDate = df.format(c);

                                                    PostDetails post = new PostDetails(foodName, description, ingredient, price, user.getUid(), area,
                                                            halalCheckBox.isChecked(), vegetarianCheckBox.isChecked(), chineseCheckBox.isChecked(), malayCheckBox.isChecked(),
                                                            indianCheckBox.isChecked(), westernCheckBox.isChecked(), otherCheckBox.isChecked(), downloadUri.toString(), order, formattedDate);
                                                    post.createEntry();
                                                    progressDialog.dismiss();
                                                    Toast.makeText(getApplicationContext(), "Create successful!", Toast.LENGTH_LONG).show();
                                                    finish();
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        });

                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(CreateNewPostActivity.this, "Upload failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

        }

        progressBar.setVisibility(View.GONE);

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
                            image.setImageURI(imageUri);

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

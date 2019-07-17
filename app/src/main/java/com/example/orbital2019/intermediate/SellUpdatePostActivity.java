package com.example.orbital2019.intermediate;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.orbital2019.R;
import com.example.orbital2019.intermediate.model.PostDetails;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SellUpdatePostActivity extends AppCompatActivity {

    private EditText foodName, description, price, ingredient;
    private Button updateButton, chooseButton, deleteImageButton;
    private CheckBox halalCheckBox, vegetarianCheckBox, chineseCheckBox, malayCheckBox, indianCheckBox, westernCheckBox, otherCheckBox;
    private ImageView imageView;
    private Uri imageUri;
    private StorageReference storageReference;
    private final static int PICK_IMAGE = 3;
    private String logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sellupdatepost);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Update/Delete Post");

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        final PostDetails postDetails = (PostDetails) bundle.getSerializable("postDetails");

        storageReference = FirebaseStorage.getInstance().getReference();

        foodName = findViewById(R.id.updateFoodName);
        foodName.setText(postDetails.getFoodName());
        description = findViewById(R.id.updateDescription);
        description.setText(postDetails.getDescription());
        ingredient = findViewById(R.id.updateIngredient);
        ingredient.setText(postDetails.getIngredient());
        price = findViewById(R.id.updatePrice);
        price.setText(String.format("%.2f",Double.parseDouble(postDetails.getPrice())));
        price.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && !price.getText().toString().isEmpty()) {
                    String money = price.getText().toString();
                    price.setText(String.format("%.2f", Double.parseDouble(money)));
                }
            }
        });

        halalCheckBox = findViewById(R.id.updateHalal);
        if (postDetails.getHalal()) {
            halalCheckBox.setChecked(true);
        }
        vegetarianCheckBox = findViewById(R.id.updateVegetarian);
        if (postDetails.getVegetarian()) {
            vegetarianCheckBox.setChecked(true);
        }
        chineseCheckBox = findViewById(R.id.updateChinese);
        if (postDetails.getChinese()) {
            chineseCheckBox.setChecked(true);
        }
        malayCheckBox = findViewById(R.id.updateMalay);
        if (postDetails.getMalay()) {
            malayCheckBox.setChecked(true);
        }
        indianCheckBox = findViewById(R.id.updateIndian);
        if (postDetails.getIndian()) {
            indianCheckBox.setChecked(true);
        }
        westernCheckBox = findViewById(R.id.updateWestern);
        if (postDetails.getWestern()) {
            westernCheckBox.setChecked(true);
        }
        otherCheckBox = findViewById(R.id.updateOther);
        if (postDetails.getOther()) {
            otherCheckBox.setChecked(true);
        }

        chooseButton = findViewById(R.id.choose);
        imageView = findViewById(R.id.updateImage);
        Glide.with(this).load(postDetails.getImage()).into(imageView);

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        deleteImageButton = findViewById(R.id.delete);
        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                        "://" + getResources().getResourcePackageName(R.drawable.logo)
                        + '/' + getResources().getResourceTypeName(R.drawable.logo) + '/' + getResources().getResourceEntryName(R.drawable.logo));
                logo = imageUri.toString();
                imageView.setImageURI(imageUri);

            }
        });

        updateButton = findViewById(R.id.buttonUpdatePost);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                postDetails.deleteEntry();

                final ProgressDialog progressDialog = new ProgressDialog(SellUpdatePostActivity.this);
                progressDialog.setTitle("Updating...");
                progressDialog.show();


                if (description.getText().toString().equals("")) {
                    description.setText("NIL");
                }

                if (imageUri == null) {
                    PostDetails post = new PostDetails(foodName.getText().toString(), description.getText().toString(), ingredient.getText().toString(), price.getText().toString(),
                            postDetails.getSeller(), postDetails.getArea(), halalCheckBox.isChecked(), vegetarianCheckBox.isChecked(), chineseCheckBox.isChecked(),
                            malayCheckBox.isChecked(), indianCheckBox.isChecked(), westernCheckBox.isChecked(), otherCheckBox.isChecked(), postDetails.getImage(), postDetails.getInput(), postDetails.getDate());
                    post.createEntry();
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Post updated successfully!", Toast.LENGTH_LONG).show();
                    finish();

                } else if (imageUri.toString().equals(logo)) {
                    PostDetails post = new PostDetails(foodName.getText().toString(), description.getText().toString(), ingredient.getText().toString(), price.getText().toString(),
                            postDetails.getSeller(), postDetails.getArea(), halalCheckBox.isChecked(), vegetarianCheckBox.isChecked(), chineseCheckBox.isChecked(),
                            malayCheckBox.isChecked(), indianCheckBox.isChecked(), westernCheckBox.isChecked(), otherCheckBox.isChecked(), logo, postDetails.getInput(), postDetails.getDate());
                    post.createEntry();
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Post updated successfully!", Toast.LENGTH_LONG).show();
                    finish();

                } else {
                    final StorageReference sRef = storageReference.child("uploads/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
                    sRef.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
                    {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                        {
                            if (!task.isSuccessful())
                            {
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
                                Uri downloadUri = task.getResult();
                                progressDialog.dismiss();

                                PostDetails post = new PostDetails(foodName.getText().toString(), description.getText().toString(), ingredient.getText().toString(), price.getText().toString(),
                                        postDetails.getSeller(), postDetails.getArea(), halalCheckBox.isChecked(), vegetarianCheckBox.isChecked(), chineseCheckBox.isChecked(),
                                        malayCheckBox.isChecked(), indianCheckBox.isChecked(), westernCheckBox.isChecked(), otherCheckBox.isChecked(), downloadUri.toString(), postDetails.getInput(), postDetails.getDate());
                                post.createEntry();
                                Toast.makeText(getApplicationContext(), "Post updated successfully!", Toast.LENGTH_LONG).show();
                                finish();
                            } else
                            {
                                progressDialog.dismiss();
                                Toast.makeText(SellUpdatePostActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

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

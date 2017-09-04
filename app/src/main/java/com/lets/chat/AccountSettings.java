package com.lets.chat;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.doctoror.particlesdrawable.ParticlesDrawable;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountSettings extends AppCompatActivity implements View.OnClickListener {

    TextView displayName, userStatus;
    ImageView edit_name, edit_status, edit_pic;
    CircleImageView circleImageView;
    private final ParticlesDrawable mDrawable = new ParticlesDrawable();


    String name, status;
    Uri imageUri;
    public static final int GALLERY_REQ = 1;
    ProgressDialog progressDialog;

    DatabaseReference mDatabase;
    FirebaseUser mCurrentUser;
    StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
      //  findViewById(R.id.particles).setBackground(mDrawable);

        displayName = (TextView) findViewById(R.id.settings_displayName);
        userStatus = (TextView) findViewById(R.id.profile_status);
        edit_name = (ImageView) findViewById(R.id.settings_edit_name);
        edit_status = (ImageView) findViewById(R.id.settings_edit_status);
        edit_pic = (ImageView) findViewById(R.id.settings_edit_pic);
        circleImageView = (CircleImageView) findViewById(R.id.settings_user_image);
        edit_name.setOnClickListener(this);
        edit_status.setOnClickListener(this);
        edit_pic.setOnClickListener(this);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUser.getUid());
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                name = dataSnapshot.child("name").getValue().toString();
                status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                updateUi(name, status, image, thumb_image);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //  setProgressDialog();
        mStorage = FirebaseStorage.getInstance().getReference();
    }


  /*  private void setProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image");
        progressDialog.setTitle("Please wait till we process the image");
        progressDialog.setCanceledOnTouchOutside(false);
       // progressDialog.show();

    }*/

  /*  @Override
    protected void onStart() {
        super.onStart();
        mDrawable.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDrawable.stop();
    }*/


    private void updateUi(String name, String status, String image, String thumb_image) {
        displayName.setText(name);
        userStatus.setText(status);
        // if image url is empty , apply the default avatar instead of empty image view
        if (!image.equals("placeHolder")) {
            Picasso.with(this)
                    .load(image)
                    .placeholder(R.drawable.noun_323186_cc)
                    .into(circleImageView);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.settings_edit_name:
                editName();
                break;
            case R.id.settings_edit_status:
                editStatus();
                break;
            case R.id.settings_edit_pic:
                editPic();
                break;
        }

    }

    private void editPic() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQ && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();
            //   profile_pic.setImageURI(imageUri);
            CropImage.activity(imageUri)
                    .setFixAspectRatio(true)
                    .setAspectRatio(1, 1)
                    // .setMaxCropResultSize(9600,9600)
                    // .setCropShape(CropImageView.CropShape.OVAL)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);


        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                //  setProgressDialog();
                //   progressDialog.show();

                imageUri = result.getUri();
                circleImageView.setImageURI(imageUri);


                StorageReference thumb_Path = null;
                byte[] thumb_byte = null;
                try {
                    File thumb_filePath = new File(imageUri.getPath());
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    thumb_byte = baos.toByteArray();

                    //  thumb_Path = mStorage.child("profile_images").child("thumbs").child(mCurrentUser.getUid() + ".jpg");


                } catch (IOException e) {
                    e.printStackTrace();
                }


                uploadToStorage(imageUri, thumb_byte);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, result.getError().getMessage(), Toast.LENGTH_LONG).show();
                //  Exception exception = result.getError();
                //  progressDialog.dismiss();

            }
        }
    }

    private void uploadToStorage(Uri imageUri, final byte[] thumb_byte) {

        StorageReference filePath = mStorage.child("profile_images").child(mCurrentUser.getUid() + ".jpg");
        final StorageReference thumb_filePath = mStorage.child("thumbs").child(mCurrentUser.getUid() + ".jpg");

        filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                final String imageUrl = taskSnapshot.getDownloadUrl().toString(); // original image

                UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte); // thumbnail
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        String thumb_url = taskSnapshot.getDownloadUrl().toString();

                        Map map = new HashMap<>();
                        map.put("image", imageUrl);
                        map.put("thumb_image", thumb_url);

                        mDatabase.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //  progressDialog.dismiss();
                                Toast.makeText(AccountSettings.this, "updated", Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                });


              /*  mDatabase.child("image").setValue(imageUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(AccountSettings.this, "updated", Toast.LENGTH_LONG).show();
                    }
                });*/


            }
        });
    }


  /*  public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }*/

    private void editName() {
        final EditText input = new EditText(this);
        input.setText(name);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Edit Name")
                .setMessage("")// to add more space
                .setView(input)
                .setPositiveButton("save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String updated_name = input.getEditableText().toString().toString();
                        mDatabase.child("name").setValue(updated_name).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(AccountSettings.this, "updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }


    private void editStatus() {
        final EditText input = new EditText(this);
        input.setText(status);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Edit Status")
                .setMessage("")// to add more space
                .setView(input)
                .setPositiveButton("save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String updated_status = input.getEditableText().toString().toString();
                        mDatabase.child("status").setValue(updated_status).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(AccountSettings.this, "updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }
}

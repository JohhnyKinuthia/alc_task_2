package com.example.travelmantics;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    public static final int PICTURE_CONST = 42;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    EditText txtTitle, txtPrice, txtDescription;
    TravelDeal deal;
    ImageView imageView;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if (FirebaseUtil.isAdmin) {
            menu.findItem(R.id.save_menu).setVisible(true);
            menu.findItem(R.id.delete_menu).setVisible(true);
            enableEditTexts(true);
            findViewById(R.id.upload_btn).setEnabled(true);
        } else {
            menu.findItem(R.id.save_menu).setVisible(false);
            menu.findItem(R.id.delete_menu).setVisible(false);
            enableEditTexts(false);
            findViewById(R.id.upload_btn).setEnabled(false);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        txtDescription = findViewById(R.id.txtDescription);
        txtPrice = findViewById(R.id.txtPrice);
        txtTitle = findViewById(R.id.txtTitle);
        imageView = findViewById(R.id.image);
        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if (deal == null) {
            deal = new TravelDeal();
        }
        this.deal = deal;
        txtDescription.setText(deal.getDescription());
        txtTitle.setText(deal.getTitle());
        txtPrice.setText(deal.getPrice());
        viewImage(deal.getImageUrl());
        Button button = findViewById(R.id.upload_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent, "Insert Picture"), PICTURE_CONST);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Deal Saved", Toast.LENGTH_LONG).show();
                launchListActivity();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this, "Deal deleted successfully", Toast.LENGTH_LONG).show();
                launchListActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_CONST && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            final StorageReference ref = FirebaseUtil.mStorageReference.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("IMAGEURL", "onSuccess: uri= " + uri.toString());
                            String url = uri.toString();
                            String name = taskSnapshot.getStorage().getPath();
                            deal.setImageUrl(url);
                            deal.setImageName(name);
                            viewImage(url);
                        }
                    });
                }
            });
        }
    }

    private void clean() {
        txtTitle.setText("");
        txtPrice.setText("");
        txtDescription.setText("");
        txtTitle.requestFocus();
    }

    private void saveDeal() {
        this.deal.setTitle(txtTitle.getText().toString());
        this.deal.setDescription(txtDescription.getText().toString());
        this.deal.setPrice(txtPrice.getText().toString());
        if (deal.getId() == null)
            mDatabaseReference.push().setValue(this.deal);
        else {
            mDatabaseReference.child(this.deal.getId()).setValue(this.deal);
        }
    }

    private void viewImage(String url) {
        if(url!=null && url.isEmpty()==false){
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get().load(url).resize(width, (int) (width*2./3)).centerCrop().into(imageView);
        }
    }
    private void deleteDeal() {
        if (deal == null) {
            Toast.makeText(this, "This note doesn't Exist", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabaseReference.child(this.deal.getId()).removeValue();
        if(deal.getImageName()!=null && deal.getImageName().isEmpty()==false){
            StorageReference ref = FirebaseUtil.mStorageReference.child(deal.getImageName());
            ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete Image", "successfully deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Delete Image", "An error occurred!");
                }
            });
        }
    }

    private void launchListActivity() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    private void enableEditTexts(boolean isEnabled) {
        txtPrice.setEnabled(isEnabled);
        txtTitle.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
    }
}

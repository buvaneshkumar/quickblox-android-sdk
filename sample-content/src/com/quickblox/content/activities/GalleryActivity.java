package com.quickblox.content.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.quickblox.content.R;
import com.quickblox.content.adapter.GalleryAdapter;
import com.quickblox.content.helper.DataHolder;
import com.quickblox.content.utils.ImageHelper;
import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.result.QBFileUploadTaskResult;

import java.io.File;
import java.io.IOException;

public class GalleryActivity extends Activity implements AdapterView.OnItemClickListener {

    private final String POSITION = "position";
    private final boolean PUBLIC_ACCESS_TRUE = true;

    private ProgressDialog progressDialog;
    private GridView galleryGridView;
    private GalleryAdapter galleryAdapter;
    private ImageHelper imageHelper;
    private ImageView selectedImageImageView;

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_new_image_button:
                imageHelper.getImage();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        startShowImgActivity(position);
    }

    private void startShowImgActivity(int position) {
        Intent intent = new Intent(this, ShowImageActivity.class);
        intent.putExtra(POSITION, position);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        initUI();
        initProgressDialog();
        initGalleryView();

        imageHelper = new ImageHelper(this);
    }

    private void initUI() {
        galleryGridView = (GridView) findViewById(R.id.gallery_gridview);
        selectedImageImageView = (ImageView) findViewById(R.id.image_imageview);
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.please_waite));
        progressDialog.setCancelable(false);
    }

    private void initGalleryView() {
        galleryAdapter = new GalleryAdapter(this);
        galleryGridView.setAdapter(galleryAdapter);
        galleryGridView.setOnItemClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri originalUri = data.getData();
            selectedImageImageView.setImageURI(originalUri);
            selectedImageImageView.setVisibility(View.VISIBLE);
            progressDialog.show();
            downloadSelectedImage();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void downloadSelectedImage() {
        File imageFile = null;

        try {
            imageFile = imageHelper.getFileFromImageView(selectedImageImageView);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // ================= QuickBlox ===== Step 3 =================
        // Upload new file
        // QBContent.uploadFileTask consist of tree query : Create a file, Upload file, Declaring file uploaded
        QBContent.uploadFileTask(imageFile, PUBLIC_ACCESS_TRUE, new QBCallback() {
            @Override
            public void onComplete(Result result) {
                if (result.isSuccess()) {
                    QBFileUploadTaskResult qbFileUploadTaskResultq = (QBFileUploadTaskResult) result;
                    DataHolder.getDataHolder().addQbFile(qbFileUploadTaskResultq.getFile());
                    selectedImageImageView.setVisibility(View.GONE);
                    galleryAdapter.notifyDataSetChanged();
                }
                progressDialog.hide();
            }

            @Override
            public void onComplete(Result result, Object o) {
            }
        });
    }
}
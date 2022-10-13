package com.scanlibrary;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsProvider;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class PickImageFragment extends Fragment {
    private View view;
    private RelativeLayout cameraButton;
    private RelativeLayout galleryButton;
    private Uri fileUri;
    private IScanner scanner;
    private static final int MY_CAMERA_REQUEST_CODE = 10;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof IScanner)) {
            throw new ClassCastException("Activity must implement IScanner");
        }
        this.scanner = (IScanner) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.pick_image_fragment, null);
        init();
        return view;
    }

    private void init() {
        ContextWrapper cw = new ContextWrapper(getActivity());
        ScanConstants.IMAGE_PATH = cw.getFilesDir().getPath() + "/SCAN_DOC";

        cameraButton = (RelativeLayout) view.findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new CameraButtonClickListener());
        galleryButton = (RelativeLayout) view.findViewById(R.id.selectButton);
        galleryButton.setOnClickListener(new GalleryClickListener());
        if (isIntentPreferenceSet()) {
            handleIntentPreference();
        } else {
            getActivity().finish();
        }
    }

    private void clearTempImages() {
        try {
            File tempFolder = new File(ScanConstants.IMAGE_PATH);
            for (File f : Objects.requireNonNull(tempFolder.listFiles())) {
                f.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleIntentPreference() {
        int preference = getIntentPreference();
        if (preference == ScanConstants.OPEN_CAMERA) {
            openCamera();
        } else if (preference == ScanConstants.OPEN_MEDIA) {
            openMediaContent();
        } else if (preference == ScanConstants.OPEN_FOLDER) {
            SharedPreferences sp = getActivity().getSharedPreferences(ScanConstants.SELECT_FOLDER_IMG, Context.MODE_PRIVATE);
            String s = sp.getString(ScanConstants.SELECT_FOLDER_IMG_URI, null);
            try {
                postImagePick(getBitmap(Uri.parse(s)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isIntentPreferenceSet() {
        int preference = getArguments().getInt(ScanConstants.OPEN_INTENT_PREFERENCE, 0);
        return preference != 0;
    }

    private int getIntentPreference() {
        int preference = getArguments().getInt(ScanConstants.OPEN_INTENT_PREFERENCE, 0);
        return preference;
    }


    private class CameraButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            openCamera();
        }
    }

    private class GalleryClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            openMediaContent();
        }
    }

    public void openMediaContent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, ScanConstants.PICKFILE_REQUEST_CODE);
    }

    public void openCamera() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            File file = createImageFile();
            boolean isDirectoryCreated = file.getParentFile().mkdirs();
            Log.i("", "File Path : " + file);
            Log.d("", "openCamera: isDirectoryCreated: " + isDirectoryCreated);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri tempFileUri = FileProvider.getUriForFile(getActivity(),
                        "com.technobd.document_organizer.provider", file);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempFileUri);
                Log.i("", "CAMERA URI: " + tempFileUri);
            } else {
                Uri tempFileUri = Uri.fromFile(file);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempFileUri);
            }
            startActivityForResult(cameraIntent, ScanConstants.START_CAMERA_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
    }

    private File createImageFile() {
        clearTempImages();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = new File(ScanConstants.IMAGE_PATH, timeStamp + ".jpg");
        fileUri = Uri.fromFile(file);
        return file;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("", "onActivityResult" + resultCode);
        Bitmap bitmap = null;
        if (resultCode == RESULT_OK) {
            try {
                switch (requestCode) {
                    case ScanConstants.START_CAMERA_REQUEST_CODE:
                        bitmap = getBitmap(fileUri);
                        break;

                    case ScanConstants.PICKFILE_REQUEST_CODE:
                    case ScanConstants.PICK_FOLDER_REQUEST_CODE:
                        bitmap = getBitmap(data.getData());
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            getActivity().finish();
        }
        if (bitmap != null) {
            postImagePick(bitmap);
        }
    }


    protected void postImagePick(Bitmap bitmap) {
        Uri uri = Utils.getUri(getActivity(), bitmap);
        bitmap.recycle();
        scanner.onBitmapSelect(uri);
    }

    private Bitmap getBitmap(Uri selectedimg) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        /*here define image quality
        * 0 to 100 when inSampleSize = 0 that time image quality is original*/
        options.inSampleSize = 2;
        AssetFileDescriptor fileDescriptor = null;
        fileDescriptor =
                getActivity().getContentResolver().openAssetFileDescriptor(selectedimg, "r");
        Bitmap original
                = BitmapFactory.decodeFileDescriptor(
                fileDescriptor.getFileDescriptor(), null, options);
        return original;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getActivity(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}
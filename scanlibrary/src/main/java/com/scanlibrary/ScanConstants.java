package com.scanlibrary;

import android.os.Environment;

import java.io.File;

public class ScanConstants {

    public final static int PICKFILE_REQUEST_CODE = 1;
    public final static int START_CAMERA_REQUEST_CODE = 2;
    public final static int PICK_FOLDER_REQUEST_CODE = 3;
    public final static String OPEN_INTENT_PREFERENCE = "selectContent";
    public final static String IMAGE_BASE_PATH_EXTRA = "ImageBasePath";
    public final static int OPEN_CAMERA = 4;
    public final static int OPEN_MEDIA = 5;
    public final static int OPEN_FOLDER = 6;
    public final static String SELECT_FOLDER_IMG = "selectImage";
    public final static String SELECT_FOLDER_IMG_URI = "selectImage";
    public final static String SCANNED_RESULT = "scannedResult";
    /*when we create a directory using this line of code, then needed "MANAGE_EXTERNAL_STORAGE" permission, otherwise can't create directory*/
    //public final static String IMAGE_PATH = Environment.getExternalStorageDirectory().getPath() + "/SCAN_DOC";
    /*when we create a directory using this line of code, that time no needed "MANAGE_EXTERNAL_STORAGE" permission */
    //public final static String IMAGE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SCAN_DOC";
    public static String IMAGE_PATH = "";
    public final static String SELECTED_BITMAP = "selectedBitmap";
}

package com.scanlibrary;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import androidx.cardview.widget.CardView;
import java.io.IOException;

public class ResultFragment extends Fragment {

    private View view;
    private ImageView scannedImageView;
    private Bitmap original;
    private Bitmap transformed;
    private static ProgressDialogFragment progressDialogFragment;

    public ResultFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.result_layout, null);
        init();
        return view;
    }

    private void init() {
        scannedImageView = (ImageView) view.findViewById(R.id.scannedImage);
        Button originalButton = (Button) view.findViewById(R.id.original);
        originalButton.setOnClickListener(new OriginalButtonClickListener());
        Button magicColorButton = (Button) view.findViewById(R.id.magicColor);
        magicColorButton.setOnClickListener(new MagicColorButtonClickListener());
        Button grayModeButton = (Button) view.findViewById(R.id.grayMode);
        grayModeButton.setOnClickListener(new GrayButtonClickListener());
        Button bwButton = (Button) view.findViewById(R.id.BWMode);
        bwButton.setOnClickListener(new BWButtonClickListener());
        Bitmap bitmap = getBitmap();
        setScannedImage(bitmap);
        CardView cvDoneBtn = (CardView) view.findViewById(R.id.cvDoneBtn);
        cvDoneBtn.setOnClickListener(new DoneButtonClickListener());

        ImageView imgBack = (ImageView) view.findViewById(R.id.imgBack);
        imgBack.setOnClickListener(new Back());

        ImageView rotateClockwise = view.findViewById(R.id.rotateClockwise);
        rotateClockwise.setOnClickListener(new RotateClockwise());

        ImageView rotateAntiClockwise = view.findViewById(R.id.rotateAntiClockwise);
        rotateAntiClockwise.setOnClickListener(new RotateAntiClockwise());
    }

    private Bitmap getBitmap() {
        Uri uri = getUri();
        try {
            original = Utils.getBitmap(getActivity(), uri);
            getActivity().getContentResolver().delete(uri, null, null);
            return original;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Uri getUri() {
        return getArguments().getParcelable(ScanConstants.SCANNED_RESULT);
    }

    public void setScannedImage(Bitmap scannedImage) {
        scannedImageView.setImageBitmap(scannedImage);
    }

    private class DoneButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showProgressDialog(getResources().getString(R.string.loading));
            AsyncTask.execute(() -> {
                try {
                    Intent data = new Intent();
                    Bitmap bitmap = transformed;
                    if (bitmap == null) {
                        bitmap = original;
                    }
                    Uri uri = Utils.getUri(getActivity(), bitmap);
                    data.putExtra(ScanConstants.SCANNED_RESULT, uri);
                    getActivity().setResult(Activity.RESULT_OK, data);
                    original.recycle();
                    System.gc();
                    getActivity().runOnUiThread(() -> {
                        getActivity().finish();
                        dismissDialog();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private class Back implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            //getActivity().finish();
            getFragmentManager().popBackStackImmediate();
        }
    }

    private class RotateClockwise implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            AsyncTask.execute(() -> {
                try {
                    transformed = Utils.rotateImage(original, 90);
                    original = transformed;
                    getActivity().runOnUiThread(() -> scannedImageView.setImageBitmap(original));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private class RotateAntiClockwise implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            AsyncTask.execute(() -> {
                try {
                    transformed = Utils.rotateImage(original, -90);
                    original = transformed;
                    getActivity().runOnUiThread(() -> scannedImageView.setImageBitmap(original));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private class BWButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(() -> {
                try {
                    transformed = ((ScanActivity) getActivity()).getBWBitmap(original);
                } catch (final OutOfMemoryError e) {
                    getActivity().runOnUiThread(() -> {
                        transformed = original;
                        scannedImageView.setImageBitmap(original);
                        e.printStackTrace();
                        dismissDialog();
                        onClick(v);
                    });
                }
                getActivity().runOnUiThread(() -> {
                    scannedImageView.setImageBitmap(transformed);
                    dismissDialog();
                });
            });
        }
    }

    private class MagicColorButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(() -> {
                try {
                    transformed = ((ScanActivity) getActivity()).getMagicColorBitmap(original);
                } catch (final OutOfMemoryError e) {
                    getActivity().runOnUiThread(() -> {
                        transformed = original;
                        scannedImageView.setImageBitmap(original);
                        e.printStackTrace();
                        dismissDialog();
                        onClick(v);
                    });
                }
                getActivity().runOnUiThread(() -> {
                    scannedImageView.setImageBitmap(transformed);
                    dismissDialog();
                });
            });
        }
    }

    private class OriginalButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                showProgressDialog(getResources().getString(R.string.applying_filter));
                transformed = original;
                scannedImageView.setImageBitmap(original);
                dismissDialog();
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                dismissDialog();
            }
        }
    }

    private class GrayButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(() -> {
                try {
                    transformed = ((ScanActivity) getActivity()).getGrayBitmap(original);
                } catch (final OutOfMemoryError e) {
                    getActivity().runOnUiThread(() -> {
                        transformed = original;
                        scannedImageView.setImageBitmap(original);
                        e.printStackTrace();
                        dismissDialog();
                        onClick(v);
                    });
                }
                getActivity().runOnUiThread(() -> {
                    scannedImageView.setImageBitmap(transformed);
                    dismissDialog();
                });
            });
        }
    }

    protected synchronized void showProgressDialog(String message) {
        if (progressDialogFragment != null && progressDialogFragment.isVisible()) {
            // Before creating another loading dialog, close all opened loading dialogs (if any)
            progressDialogFragment.dismissAllowingStateLoss();
        }
        progressDialogFragment = null;
        progressDialogFragment = new ProgressDialogFragment(message);
        FragmentManager fm = getFragmentManager();
        progressDialogFragment.show(fm, ProgressDialogFragment.class.toString());
    }

    protected synchronized void dismissDialog() {
        progressDialogFragment.dismissAllowingStateLoss();
    }
}
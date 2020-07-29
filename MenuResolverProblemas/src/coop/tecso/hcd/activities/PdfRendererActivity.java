package coop.tecso.hcd.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import coop.tecso.hcd.R;
import coop.tecso.hcd.application.HCDigitalApplication;

/**
 * This fragment has a big {@ImageView} that shows PDF pages, and 2
 * {@link android.widget.Button}s to move between pages. We use a
 * {@link android.graphics.pdf.PdfRenderer} to render PDF pages as
 * {@link android.graphics.Bitmap}s.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class PdfRendererActivity extends Activity implements View.OnClickListener{

    private static final String LOG_TAG = PdfRendererActivity.class.getSimpleName();

    /**
     * Key string for saving the state of current page index.
     */
    private static final String STATE_CURRENT_PAGE_INDEX = "current_page_index";

    /**
     * File descriptor of the PDF.
     */
    private ParcelFileDescriptor mFileDescriptor;

    /**
     * {@link android.graphics.pdf.PdfRenderer} to render the PDF.
     */
    private PdfRenderer mPdfRenderer;

    /**
     * Page that is currently shown on the screen.
     */
    private PdfRenderer.Page mCurrentPage;

    /**
     * {@link android.widget.ImageView} that shows a PDF page as a {@link android.graphics.Bitmap}
     */
    private ImageView mImageView;

    /**
     * {@link android.widget.Button} to move to the previous page.
     */
    private ImageView mButtonPrevious;
    private ImageView mButtonZoomin;
    private ImageView mButtonZoomout;
    private ImageView mButtonNext;
    private static float ZOOM_SCALE = 100;
    private float currentZoomLevel = ZOOM_SCALE;

    /**
     * PDF page index
     */
    private int mPageIndex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        HCDigitalApplication appState = (HCDigitalApplication) getApplicationContext();

        setContentView(R.layout.pdf_reader);

        // Retain view references.
        mImageView = findViewById(R.id.image);
        mButtonPrevious = findViewById(R.id.previous);
        mButtonNext = findViewById(R.id.next);
        mButtonZoomin = findViewById(R.id.zoomin);
        mButtonZoomout = findViewById(R.id.zoomout);
        HorizontalScrollView mHorizontalScrollView = findViewById(R.id.horizontalScrollView1);

        // Bind events.
        mButtonPrevious.setOnClickListener(this);
        mButtonNext.setOnClickListener(this);
        mButtonZoomin.setOnClickListener(this);
        mButtonZoomout.setOnClickListener(this);

        mPageIndex = 0;
        // If there is a savedInstanceState (screen orientations, etc.), we restore the page index.
        if (savedInstanceState != null) {
            mPageIndex = savedInstanceState.getInt(STATE_CURRENT_PAGE_INDEX, 0);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        try {
            byte[] bytes = this.getIntent().getByteArrayExtra("pdf");
            openRenderer(bytes);
            showPage(mPageIndex);
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    protected void onStop() {
        try {
            closeRenderer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    /**
     * Closes the {@link android.graphics.pdf.PdfRenderer} and related resources.
     *
     * @throws java.io.IOException When the PDF file cannot be closed.
     */
    private void closeRenderer() throws IOException {
        if (mCurrentPage != null) {
            mCurrentPage.close();
            mCurrentPage = null;
        }
        if (mPdfRenderer != null) {
            mPdfRenderer.close();
        }
        if (mFileDescriptor != null) {
            mFileDescriptor.close();
        }
    }

    /**
     * Sets up a {@link android.graphics.pdf.PdfRenderer} and related resources.
     */
    private void openRenderer(byte[] bytes) throws IOException {
        //Obtener el archivo
        // In this sample, we read a PDF from the assets directory.
        String sdcard = Environment.getExternalStorageDirectory().getPath();
        File file = new File(sdcard,new Date().getTime()+".pdf");

        FileOutputStream fos = new FileOutputStream(file);

        fos.write(bytes);

        ParcelFileDescriptor mFileDescriptor = null;

        mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        // This is the PdfRenderer we use to render the PDF.
        if (mFileDescriptor != null) {
            mPdfRenderer = new PdfRenderer(mFileDescriptor);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.previous: {
                // Move to the previous page
                currentZoomLevel = ZOOM_SCALE;
                showPage(mCurrentPage.getIndex() - 1);
                break;
            }
            case R.id.next: {
                // Move to the next page
                currentZoomLevel = ZOOM_SCALE;
                showPage(mCurrentPage.getIndex() + 1);
                break;
            }
            case R.id.zoomout: {
                // Move to the next page
                currentZoomLevel = currentZoomLevel-ZOOM_SCALE;
                showPage(mCurrentPage.getIndex());
                break;
            }
            case R.id.zoomin: {
                // Move to the next page
                currentZoomLevel = currentZoomLevel+ZOOM_SCALE;
                showPage(mCurrentPage.getIndex());
                break;
            }
        }
    }

    /**
     * Zoom level for zoom matrix depends on screen density (dpiAdjustedZoomLevel), but width and height of bitmap depends only on pixel size and don't depend on DPI
     * Shows the specified page of PDF to the screen.
     *
     * @param index The page index.
     */
    private void showPage(int index) {
        if (mPdfRenderer.getPageCount() <= index) {
            return;
        }
        // Make sure to close the current page before opening another one.
        if (mCurrentPage != null) {
            mCurrentPage.close();
        }
        // Use `openPage` to open a specific page in PDF.
        mCurrentPage = mPdfRenderer.openPage(index);
        // Important: the destination bitmap must be ARGB (not RGB).
        int newWidth = (int) (mCurrentPage.getWidth() + currentZoomLevel);
        int newHeight = (int) (mCurrentPage.getHeight() + currentZoomLevel);

        if(newWidth > 300 && newWidth < 3500){
            Bitmap bitmap = Bitmap.createBitmap(
                    newWidth,
                    newHeight,
                    Bitmap.Config.ARGB_8888);

            // Here, we render the page onto the Bitmap.
            // To render a portion of the page, use the second and third parameter. Pass nulls to get
            // the default result.
            // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
            mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            // We are ready to show the Bitmap to user.
            mImageView.setImageBitmap(bitmap);
            updateUi();
            System.gc();
        }
    }

    /**
     * Updates the state of 2 control buttons in response to the current page index.
     */
    private void updateUi() {
        int index = mCurrentPage.getIndex();
        int pageCount = mPdfRenderer.getPageCount();
        if (pageCount == 1) {
            mButtonPrevious.setVisibility(View.GONE);
            mButtonNext.setVisibility(View.GONE);
        } else {
            mButtonPrevious.setEnabled(0 != index);
            mButtonNext.setEnabled(index + 1 < pageCount);
        }
        if (currentZoomLevel == 2) {
            mButtonZoomout.setActivated(false);
        } else {
            mButtonZoomout.setActivated(true);
        }
    }
}

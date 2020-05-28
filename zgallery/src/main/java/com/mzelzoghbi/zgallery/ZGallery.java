package com.mzelzoghbi.zgallery;

import android.content.Context;
import android.content.Intent;

import com.mzelzoghbi.zgallery.activities.ZGalleryActivity;
import com.mzelzoghbi.zgallery.entities.ZColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mohamedzakaria on 8/11/16.
 */
public class ZGallery {
    private Context mActivity;
    private ArrayList<String> imagesURLs;
    private String title;
    private int spanCount = 2;
    private int toolbarColor = -1;
    private int imgPlaceHolderResId = -1;
    private ZColor color;
    private int selectedImgPosition;
    private ZColor backgroundColor;

    private ZGallery() {
    }

    private ZGallery(Context activity, ArrayList<String> imagesURLs) {
        this.imagesURLs = imagesURLs;
        this.mActivity = activity;
    }

    /**
     * @param activity   Refrence from current activity
     * @param imagesURLs Image URLs to be displayed
     */
    public static ZGallery with(Context activity, List<String> imagesURLs) {
        return new ZGallery(activity, new ArrayList<>(imagesURLs));
    }

    /**
     * Set z_toolbar title
     *
     * @param title
     * @return
     */
    public ZGallery setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Setting z_toolbar Color ResourceId
     *
     * @param colorResId
     * @return
     */
    public ZGallery setToolbarColorResId(int colorResId) {
        this.toolbarColor = colorResId;
        return this;
    }

    /**
     * Setting z_toolbar color
     *
     * @param color enum color may be black or white
     * @return
     */
    public ZGallery setToolbarTitleColor(ZColor color) {
        this.color = color;
        return this;
    }

    /**
     * Setting starting position
     *
     * @param selectedImgPosition
     * @return
     */
    public ZGallery setSelectedImgPosition(int selectedImgPosition) {
        this.selectedImgPosition = selectedImgPosition;
        return this;
    }

    public ZGallery setGalleryBackgroundColor(ZColor backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    /**
     * Start the gallery activity with builder settings
     */
    public void show() {
        Intent gridActivity = new Intent(mActivity, ZGalleryActivity.class);
        gridActivity.putExtra(Constants.IntentPassingParams.IMAGES, imagesURLs);
        gridActivity.putExtra(Constants.IntentPassingParams.TITLE, title);
        gridActivity.putExtra(Constants.IntentPassingParams.TOOLBAR_COLOR_ID, toolbarColor);
        gridActivity.putExtra(Constants.IntentPassingParams.TOOLBAR_TITLE_COLOR, color);
        gridActivity.putExtra(Constants.IntentPassingParams.SELECTED_IMG_POS, selectedImgPosition);
        gridActivity.putExtra(Constants.IntentPassingParams.BG_COLOR, backgroundColor);
        mActivity.startActivity(gridActivity);
    }
}

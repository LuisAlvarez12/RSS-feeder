package com.example.xyzreader.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBar;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import static android.support.v7.graphics.Palette.generate;

/**
 * Created by luisalvarez on 4/13/17.
 */


public class DynamicNetworkImageView extends android.support.v7.widget.AppCompatImageView {
    private String mUrl;
    private int mDefaultImageId;
    private int mErrorImageId;
    private ImageLoader mImageLoader;
    private ImageLoader.ImageContainer mImageContainer;
    private View breakPoint;
    private float mAspectRatio = 1.5f;


    public DynamicNetworkImageView(Context context) {
        this(context, (AttributeSet)null);
    }

    public DynamicNetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DynamicNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAspectRatio(float aspectRatio) {
        mAspectRatio = aspectRatio;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth, (int) (measuredWidth / mAspectRatio));
    }


    public void setImageUrl(String url, ImageLoader imageLoader) {
        this.mUrl = url;
        this.mImageLoader = imageLoader;
        this.loadImageIfNecessary(false);
    }

    public void setImageUrl(String url, ImageLoader imageLoader, View b) {
        this.mUrl = url;
        this.mImageLoader = imageLoader;
        this.loadImageIfNecessary(false);
        this.breakPoint = b;
    }

    public void setDefaultImageResId(int defaultImage) {
        this.mDefaultImageId = defaultImage;
    }

    public void setErrorImageResId(int errorImage) {
        this.mErrorImageId = errorImage;
    }

    private void loadImageIfNecessary(final boolean isInLayoutPass) {
        int width = this.getWidth();
        int height = this.getHeight();
        boolean isFullyWrapContent = this.getLayoutParams().height == -2 && this.getLayoutParams().width == -2;
        if(width != 0 || height != 0 || isFullyWrapContent) {
            if(TextUtils.isEmpty(this.mUrl)) {
                if(this.mImageContainer != null) {
                    this.mImageContainer.cancelRequest();
                    this.mImageContainer = null;
                }

                this.setImageBitmap((Bitmap)null);
            } else {
                if(this.mImageContainer != null && this.mImageContainer.getRequestUrl() != null) {
                    if(this.mImageContainer.getRequestUrl().equals(this.mUrl)) {
                        return;
                    }
                    this.mImageContainer.cancelRequest();
                    this.setImageBitmap((Bitmap)null);
                }

                ImageLoader.ImageContainer newContainer = this.mImageLoader.get(this.mUrl, new ImageLoader.ImageListener() {
                    public void onErrorResponse(VolleyError error) {
                        if(DynamicNetworkImageView.this.mErrorImageId != 0) {
                            DynamicNetworkImageView.this.setImageResource(DynamicNetworkImageView.this.mErrorImageId);
                        }

                    }

                    public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                        if(isImmediate && isInLayoutPass) {
                            DynamicNetworkImageView.this.post(new Runnable() {
                                public void run() {
                                    onResponse(response, false);
                                }
                            });
                        } else {
                            if(response.getBitmap() != null) {
                                DynamicNetworkImageView.this.setImageBitmap(response.getBitmap());
                                Palette p = Palette.generate(response.getBitmap());
                                breakPoint.setBackgroundColor(p.getDominantSwatch().getRgb());
                            } else if(DynamicNetworkImageView.this.mDefaultImageId != 0) {
                                DynamicNetworkImageView.this.setImageResource(DynamicNetworkImageView.this.mDefaultImageId);
                            }

                        }
                    }
                });
                this.mImageContainer = newContainer;
            }
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.loadImageIfNecessary(true);
    }

    protected void onDetachedFromWindow() {
        if(this.mImageContainer != null) {
            this.mImageContainer.cancelRequest();
            this.setImageBitmap((Bitmap)null);
            this.mImageContainer = null;
        }

        super.onDetachedFromWindow();
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        this.invalidate();
    }
}

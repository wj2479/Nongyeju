package com.qdhc.ny.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.qdhc.ny.R;


/**
 * Created by hedge_hog on 2015/6/11.
 */
public class CRatingBar extends LinearLayout {
    private boolean mClickable;
    private int starCount;
    private int starMarginRight;
    private OnRatingChangeListener onRatingChangeListener;
    private float starImageSize;
    private Drawable starEmptyDrawable;
    private Drawable starFillDrawable;
    private int count;

    public void setOnRatingChangeListener(OnRatingChangeListener onRatingChangeListener) {
        this.onRatingChangeListener = onRatingChangeListener;
    }

    public void setmClickable(boolean clickable) {
        this.mClickable = clickable;
    }

    public void setStarFillDrawable(Drawable starFillDrawable) {
        this.starFillDrawable = starFillDrawable;
    }

    public void setStarEmptyDrawable(Drawable starEmptyDrawable) {
        this.starEmptyDrawable = starEmptyDrawable;
    }

    public void setStarImageSize(float starImageSize) {
        this.starImageSize = starImageSize;
    }


    /**
     * @param context
     * @param attrs
     */
    @SuppressWarnings("ResourceType")
    public CRatingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.HORIZONTAL);
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.CRatingBar);
        starImageSize = mTypedArray.getDimension(R.styleable.CRatingBar_starImageSize, 20);
        starCount = mTypedArray.getInteger(R.styleable.CRatingBar_starCount, 5);
        starMarginRight = mTypedArray.getInteger(R.styleable.CRatingBar_starMarginRight, 10);
        starEmptyDrawable = mTypedArray.getDrawable(R.styleable.CRatingBar_starEmpty);
        starFillDrawable = mTypedArray.getDrawable(R.styleable.CRatingBar_starFill);
        mClickable = mTypedArray.getBoolean(1,false);
        for (int i = 0; i < starCount; ++i) {
            ImageView imageView = getStarImageView(context, attrs);
            imageView.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mClickable) {
                                setStar(indexOfChild(v) + 1);
                                if (onRatingChangeListener != null) {
                                    onRatingChangeListener.onRatingChange(indexOfChild(v) + 1);
                                }
                            }

                        }
                    }
            );
            addView(imageView);
        }
    }

    /**
     * @param context
     * @param attrs
     * @return
     */
    private ImageView getStarImageView(Context context, AttributeSet attrs) {
        ImageView imageView = new ImageView(context);
        ViewGroup.LayoutParams para = new ViewGroup.LayoutParams(
                Math.round(starImageSize),
                Math.round(starImageSize)
        );
        imageView.setLayoutParams(para);
        imageView.setPadding(0, 0, starMarginRight, 0);
        imageView.setImageDrawable(starEmptyDrawable);
        imageView.setMaxWidth(10);
        imageView.setMaxHeight(10);
        return imageView;

    }

    /**
     * setting start
     *
     * @param starCount
     */
    public void setStar(int starCount) {
        starCount = starCount > this.starCount ? this.starCount : starCount;
        starCount = starCount < 0 ? 0 : starCount;
        for (int i = 0; i < starCount; ++i) {
            ((ImageView) getChildAt(i)).setImageDrawable(starFillDrawable);
        }

        for (int i = this.starCount - 1; i >= starCount; --i) {
            ((ImageView) getChildAt(i)).setImageDrawable(starEmptyDrawable);
        }
        count = starCount;

    }

    public int getStarCount() {
        return count;
    }

    /**
     * change stat listener
     */
    public interface OnRatingChangeListener {

        void onRatingChange(int RatingCount);

    }

}

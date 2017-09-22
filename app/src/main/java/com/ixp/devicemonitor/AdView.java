package com.ixp.devicemonitor;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ixp.util.AdItem;
import com.ixp.util.Configs;

import java.lang.ref.WeakReference;
import java.security.KeyStore;

public class AdView extends LinearLayout implements View.OnClickListener {

    public interface AdAdapter {
        int getCount();

        AdItem getItem(int index);
    }

    private AdAdapter mAdAdapter;
    private int mTimeInterval = 5000;
    private int mCurrentImageIndex;
    private AdHandler mHandler;
    private RelativeLayout mMainView;
    private ImageView mImageView;
    private Button mCloseButton;
    private OnClickListener mImageClickListener;

    public AdView(Context context) {
        super(context);
        init(context);
    }

    public AdView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        mHandler = new AdHandler(this);
        mMainView = (RelativeLayout)LayoutInflater.from(context).inflate(R.layout.layout_adview, null);
        addView(mMainView, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mImageView = (ImageView) mMainView.findViewById(R.id.image);
        mCloseButton = (Button) mMainView.findViewById(R.id.btn_close);
        mImageView.setScaleType(ImageView.ScaleType.FIT_XY);

        mCloseButton.setOnClickListener(this);
        mImageView.setOnClickListener(this);
    }

    private void showNext() {
        if (mAdAdapter == null || mAdAdapter.getCount() < 1)
            return;
        mCurrentImageIndex = (mCurrentImageIndex + 1) % mAdAdapter.getCount();
        showAd(mCurrentImageIndex);
    }


    private void showAd(int index) {
        if (mAdAdapter == null || mAdAdapter.getCount() < 1)
            return;
        mImageView.setImageBitmap(mAdAdapter.getItem(index).image);

        mHandler.sendEmptyMessageDelayed(0, mTimeInterval);
    }

    public void setAdAdapter(AdAdapter adAdapter) {
        mAdAdapter = adAdapter;
        mCurrentImageIndex = 0;
        showAd(mCurrentImageIndex);
    }

    public void setTimeInterval(int interval) {
        mTimeInterval = interval;
    }

    public void hideCloseButton() {
        mCloseButton.setVisibility(View.INVISIBLE);
    }

    public String getCurrentUrl() {
        if (mAdAdapter == null || mAdAdapter.getCount() < 1)
            return  null;
        return mAdAdapter.getItem(mCurrentImageIndex).url;
    }

    public void setOnImageClickListener(OnClickListener listener) {
        mImageClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (Configs.DEBUG) {
            Log.d("result", "click view is " + v);
        }
        switch (v.getId()) {
            case R.id.image:
                if (mImageClickListener != null) {
                    mImageClickListener.onClick(v);
                }
                break;
            case R.id.btn_close:
                setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    public static class AdHandler extends Handler {
        private WeakReference<AdView> mAdView;

        public AdHandler(AdView view) {
            mAdView = new WeakReference<AdView>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            AdView adView = mAdView.get();

            if (adView != null) {
                adView.showNext();
            }
        }
    }

}

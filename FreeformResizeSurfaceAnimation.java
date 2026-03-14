package com.android.server.wm;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Pools;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

public class FreeformResizeSurfaceAnimation{
    private static final String TAG = FreeformResizeSurfaceAnimation.class.getSimpleName();
    private static final String RESIZING_BACKGROUND_SURFACE_NAME = "ResizingBackground";

    private SurfaceControl mHostLeash;
    private SurfaceControl mBackgroundLeash;
    //
    private SurfaceControl mIconLeash;
    private SurfaceControl mBufferLayer;
    private TransactionPool mTransactionPool = new TransactionPool();
    private ValueAnimator mAnimator;
    private Task mTask;
    private int mDuration = 5000;
    private int mDelay = 200;
    private Rect mBounds;

    public static final Interpolator LINEAR_OUT_SLOW_IN = new PathInterpolator(0f, 0.5f, 0.8f, 1f);

    private FreeformResizeSurfaceAnimation() {
    }

    private static class Holder {
        private static final FreeformResizeSurfaceAnimation INSTANCE = new FreeformResizeSurfaceAnimation();
    }
    public static FreeformResizeSurfaceAnimation getInstance() {
        return Holder.INSTANCE;
    }

    public void startAnimation(Task task,int from,int to,boolean boundsScale,Listener listener) {
        mTask = task;
        if (mTask == null) return;

        if (mAnimator != null && mAnimator.isRunning()){
            mAnimator.cancel();
            return;
        }

        mBounds = mTask.getBounds();
        mHostLeash = mTask.getDisplayArea().getSurfaceControl();

        SurfaceControl.Transaction t = mTransactionPool.acquire();
        //icon
        createBufferLayerFromBitmap(t);
        // background
        createBackgroundLayer(t);
        t.apply();

        mAnimator = ValueAnimator
                .ofInt(from, to)
                .setDuration(mDuration);
        mAnimator.setInterpolator(LINEAR_OUT_SLOW_IN);

        mAnimator.addUpdateListener(
                animation -> updateBounds(
                        (int) animation.getAnimatedValue())
        );

        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (listener != null) listener.onResizeScaleDown();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null) listener.onResizeScaleUp();
                mTask.mAtmService.mH.postDelayed(()->{
                    release();
                    mAnimator = null;
                },(boundsScale?mDelay:0));
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                release();
                mAnimator = null;
            }
        });
        if (!boundsScale) mAnimator.setStartDelay(mDelay);
        if (mAnimator != null) mAnimator.start();
    }



    private void updateBounds(int animatedValue) {
        SurfaceControl.Transaction t = mTransactionPool.acquire();
        t.setCrop(mBackgroundLeash, animatedValue, mBounds.top,mBounds.right,mBounds.bottom);
        t.setCrop(mIconLeash, animatedValue, mBounds.top,mBounds.right,mBounds.bottom);
        t.setPosition(mBufferLayer, (mBounds.right + animatedValue)/2, mBounds.centerY());
        t.apply();
    }

    private void release() {
        SurfaceControl.Transaction t = mTransactionPool.acquire();
        if (mBackgroundLeash != null) {
            t.remove(mBackgroundLeash);
            mBackgroundLeash = null;
        }
        if(mBufferLayer != null){
            t.remove(mBufferLayer);
            mBufferLayer = null;
        }
        if (mIconLeash != null) {
            t.remove(mIconLeash);
            mIconLeash = null;
        }
        t.apply();
        mTransactionPool.release(t);
    }

    /**
     * 从 Bitmap 创建 Buffer 层
     */
    private void createBufferLayerFromBitmap(SurfaceControl.Transaction t) {
        Drawable drawable = loadAppInfoIcon();
        if (drawable == null) return;
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (width <= 0 || height <= 0) {
            width = height = 50;
        }

        mIconLeash = new SurfaceControl.Builder()
                .setName("IconAnimationLayer")
                .setContainerLayer()
                .setParent(mHostLeash)
                .build();

        mBufferLayer = new SurfaceControl.Builder()
                .setName("IconBuffer")
                .setBufferSize(width, height)
                .setFormat(android.graphics.PixelFormat.RGBA_8888)
                .setParent(mIconLeash)
                .build();

        Surface mSurface = new Surface(mBufferLayer);
        Canvas canvas = mSurface.lockCanvas(null);
        if (canvas != null) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            mSurface.unlockCanvasAndPost(canvas);
        }
        t.setPosition(mBufferLayer, mBounds.centerX(), mBounds.centerY());
        t.show(mBufferLayer); // BufferLayer 必须显式 show

        t.setLayer(mIconLeash, Integer.MAX_VALUE)
                .setPosition(mIconLeash, 0, 0)
                .setCrop(mIconLeash,mBounds)
                .setVisibility(mIconLeash, true);// Container 设置可见（如果父级不可见则必要）
    }

    /**
     *
     */
    private void createBackgroundLayer(SurfaceControl.Transaction t) {
        mBackgroundLeash = makeColorLayer(mHostLeash,
                RESIZING_BACKGROUND_SURFACE_NAME);
        t.setColor(mBackgroundLeash, Color.valueOf(Color.WHITE).getComponents())
                .setLayer(mBackgroundLeash, Integer.MAX_VALUE - 2)
                .setPosition(mBackgroundLeash, 0,0)
                .setCrop(mBackgroundLeash, mBounds)
                .setCornerRadius(mBackgroundLeash,10)
                .setVisibility(mBackgroundLeash, true);
    }

    private Drawable loadAppInfoIcon() {
        try {
            ActivityInfo info = mTask.topRunningActivity().info;
            ApplicationInfo appInfo = info.applicationInfo;
            Resources resources = mTask.mAtmService.mContext.getPackageManager()
                    .getResourcesForApplication(appInfo);
            return resources.getDrawableForDensity(appInfo.icon, resources.getConfiguration().densityDpi);
        } catch (Exception exc) {
            Log.w(TAG, "Failed to load app icon", exc);
        }
        return null;
    }

    /*
    *
    * */
    public static SurfaceControl makeColorLayer(SurfaceControl host, String name) {
        return new SurfaceControl.Builder()
                .setParent(host)
                .setColorLayer()
                .setName(name)
                .setCallsite("FreeformResizeSurfaceAnimation.makeColorLayer")
                .build();
    }

    /*
    * 管理 Transaction
    * */
    public class TransactionPool {
        private final Pools.SynchronizedPool<SurfaceControl.Transaction> mTransactionPool =
                new Pools.SynchronizedPool<>(2);

        public TransactionPool() {
        }

        public SurfaceControl.Transaction acquire() {
            SurfaceControl.Transaction t = mTransactionPool.acquire();
            if (t == null) {
                return new SurfaceControl.Transaction();
            }
            return t;
        }

        public void release(SurfaceControl.Transaction t) {
            if (!mTransactionPool.release(t)) {
                t.close();
            }
        }
    }

    /*
     * 回调
     * */
    static class Listener{
        void onResizeScaleDown(){}
        void onResizeScaleUp(){}
    }


}

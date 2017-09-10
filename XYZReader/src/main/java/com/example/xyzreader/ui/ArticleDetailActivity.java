package com.example.xyzreader.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.EvenBusMessageBuilder;

import org.greenrobot.eventbus.EventBus;

import java.util.HashSet;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends FragmentActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor mCursor;
    private long mStartId;
    private int currentPosition;

    private long mSelectedItemId;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
    private int mTopInset;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private View mUpButtonContainer;
    private View mUpButton;
    private Activity context;
    private int pagerid;
    private HashSet firstLoadingPositions=new HashSet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EvenBusMessageBuilder.ArticlesDetailMessage stickyEvent = EventBus.getDefault().getStickyEvent(EvenBusMessageBuilder.ArticlesDetailMessage.class);
        setContentView(R.layout.activity_article_detail);
        mPager = (ViewPager) findViewById(R.id.vpager);
        if (stickyEvent != null) {
            mCursor = stickyEvent.getArticleCursor();
            currentPosition = stickyEvent.getCurrentPosition();
            mPagerAdapter = new MyPagerAdapter(getFragmentManager());
            mPager.setAdapter(mPagerAdapter);
            mPager.setCurrentItem(currentPosition);
        }
        context = this;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();
        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToPosition(-1);
            // TODO: optimize
            while (mCursor.moveToNext()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    break;
                }
            }
            mStartId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
//            if (position == currentPosition) {
//                Bundle bundle = new Bundle();
//                bundle.putBoolean("StartPage", true);
//                bundle.putString("title", mCursor.getString(ArticleLoader.Query.TITLE));
//                bundle.putString("author", mCursor.getString(ArticleLoader.Query.AUTHOR));
//                bundle.putString("body", mCursor.getString(ArticleLoader.Query.BODY));
//                bundle.putString("photo_url", mCursor.getString(ArticleLoader.Query.PHOTO_URL));
//                bundle.putString("published_date", mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE));
//                currentPosition = -1;
//                return ArticleDetailMdFragment.newInstance(bundle);
//                return ArticleDetailMdFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));

//            } else {
                return ArticleDetailMdFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
//            }
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }

    }
}

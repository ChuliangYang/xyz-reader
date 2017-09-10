package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.transition.Explode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.EvenBusMessageBuilder;
import com.example.xyzreader.databinding.FragmentArticleDetailMdBinding;

import org.greenrobot.eventbus.EventBus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailMdFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final String ARG_ITEM_ID = "item_id";
    private static final String TAG = "ArticleDetailFragment";
    protected com.muzakki.ahmad.widget.CollapsingToolbarLayout collapsingToolbarLayout;
    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private ImageView mPhotoView;
    private boolean mIsCard = false;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);
    private AppBarLayout app_bar_layout;
    private Cursor cursor;
    private Boolean isStartPage;
    private Toolbar toolbar;
    private FloatingActionButton floatingActionButton;
    private String subTitle;
    private ImageView iv_scrim;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailMdFragment() {
    }

    public static ArticleDetailMdFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailMdFragment fragment = new ArticleDetailMdFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

//    public static ArticleDetailMdFragment newInstance(Bundle bundle) {
//        ArticleDetailMdFragment fragment = new ArticleDetailMdFragment();
//        fragment.setArguments(bundle);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRetainInstance(true);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
        isStartPage = getArguments().getBoolean("StartPage", false);

        setHasOptionsMenu(true);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentArticleDetailMdBinding fragmentArticleDetailMdBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_article_detail_md, container, false);

        if (!isStartPage) {
            getLoaderManager().initLoader(0, null, this);
        }
        collapsingToolbarLayout = fragmentArticleDetailMdBinding.collapsingToolbarLayout;
        app_bar_layout = fragmentArticleDetailMdBinding.appBarLayout;
        toolbar = fragmentArticleDetailMdBinding.toolbar;
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        toolbar.inflateMenu(R.menu.detail);
        toolbar.getMenu().getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
                return true;
            }
        });
        final Drawable navigationIcon = toolbar.getNavigationIcon();
        floatingActionButton = fragmentArticleDetailMdBinding.fabShare;
        iv_scrim = fragmentArticleDetailMdBinding.ivScrim;
        app_bar_layout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (floatingActionButton.getVisibility() == View.VISIBLE) {
                    toolbar.getMenu().getItem(0).setVisible(false);
                } else {
                    toolbar.getMenu().getItem(0).setVisible(true);
                }

                if (Math.abs(verticalOffset) - appBarLayout.getTotalScrollRange() == 0) {
                    iv_scrim.setVisibility(View.GONE);
                } else if (verticalOffset == 0) {
                    iv_scrim.setVisibility(View.VISIBLE);
                } else {
                    iv_scrim.setVisibility(View.GONE);
                }
            }
        });

        mRootView = fragmentArticleDetailMdBinding.getRoot();
        mPhotoView = fragmentArticleDetailMdBinding.photo;
        fragmentArticleDetailMdBinding.fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });
        bindViews();
        return mRootView;
    }

    private Date parsePublishedDate() {
        try {
            String date = "";
            if (isStartPage) {
                date = getArguments().getString("published_date");
            } else {
                date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            }
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);


        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null && !isStartPage) {
            collapsingToolbarLayout.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));
            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                subTitle = DateUtils.getRelativeTimeSpanString(
                        publishedDate.getTime(),
                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL).toString() + " by "
                        + mCursor.getString(ArticleLoader.Query.AUTHOR);
                collapsingToolbarLayout.setSubtitle(subTitle);

            } else {
                // If date is before 1902, just show the string
                subTitle = outputFormat.format(publishedDate) + " by "
                        + mCursor.getString(ArticleLoader.Query.AUTHOR);
                collapsingToolbarLayout.setSubtitle(
                        subTitle
                );
            }
            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />")));
            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                Palette p = Palette.generate(bitmap, 12);
                                mMutedColor = p.getDarkMutedColor(0xFF333333);
                                mPhotoView.setImageBitmap(imageContainer.getBitmap());
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
        } else if (isStartPage) {
            collapsingToolbarLayout.setTitle(getArguments().getString("title"));
            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                subTitle = DateUtils.getRelativeTimeSpanString(
                        publishedDate.getTime(),
                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL).toString() + " by "
                        + getArguments().getString("author");
                collapsingToolbarLayout.setSubtitle(subTitle
                );

            } else {
                // If date is before 1902, just show the string
                subTitle = outputFormat.format(publishedDate) + " by "
                        + getArguments().getString("author");
                collapsingToolbarLayout.setSubtitle(subTitle

                );
            }
            bodyView.setText(Html.fromHtml(getArguments().getString("body").replaceAll("(\r\n|\n)", "<br />")));
            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(getArguments().getString("photo_url"), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                Palette p = Palette.generate(bitmap, 12);
                                mMutedColor = p.getDarkMutedColor(0xFF333333);
                                mPhotoView.setImageBitmap(imageContainer.getBitmap());
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
            EventBus.getDefault().post(EvenBusMessageBuilder.DismissProgressDialogMessage.create());
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        bindViews();
    }
}

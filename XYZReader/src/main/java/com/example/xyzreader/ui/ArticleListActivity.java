package com.example.xyzreader.ui;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.EvenBusMessageBuilder;
import com.example.xyzreader.data.UpdaterService;
import com.example.xyzreader.helper.WidgetHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ArticleListActivity.class.toString();
    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);
    private LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks;
    private Context context;
    private ProgressDialog progressDialog;
    private boolean mIsRefreshing = false;
    private Cursor mCursor;
    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
        context = this;
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        loaderCallbacks = this;
        progressDialog = WidgetHelper.createProgressDialog(this);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoaderManager().initLoader(0, null, loaderCallbacks);
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.theme_primary_dark, R.color.theme_accent, R.color.theme_primary);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            refresh();
        }
    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
        EventBus.getDefault().unregister(this);
    }

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mSwipeRefreshLayout.setRefreshing(false);
        Adapter adapter = new Adapter(cursor);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        mCursor=cursor;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EvenBusMessageBuilder.DismissProgressDialogMessage event) {
        if (event != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public DynamicHeightNetworkImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;

        public ViewHolder(View view) {
            super(view);
            thumbnailView = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
        }
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private Cursor mCursor;

        public Adapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(ArticleLoader.Query._ID);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    if (!progressDialog.isShowing()) {
//                        progressDialog.show();
//                    }
//                    getLoaderManager().initLoader(4, null, new AllArticleLoadCallBack(context, getItemId(vh.getAdapterPosition())));
                    long mStartId=getItemId(vh.getAdapterPosition());
                    if (mStartId > 0) {
                        mCursor.moveToPosition(-1);
                        // TODO: optimize
                        while (mCursor.moveToNext()) {
                            if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                                final int position = mCursor.getPosition();
                                EventBus.getDefault().postSticky(EvenBusMessageBuilder.BuildDetailMessage(mCursor, position));
                                startActivity(new Intent(ArticleListActivity.this, ArticleDetailActivity.class));
                                break;
                            }
                        }
                    }
                }
            });
            return vh;
        }

        private Date parsePublishedDate() {
            try {
                String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
                return dateFormat.parse(date);
            } catch (ParseException ex) {
                Log.e(TAG, ex.getMessage());
                Log.i(TAG, "passing today's date");
                return new Date();
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {

                holder.subtitleView.setText(Html.fromHtml(
                        mCursor.getString(ArticleLoader.Query.AUTHOR)));
            } else {
                holder.subtitleView.setText(Html.fromHtml(
                        mCursor.getString(ArticleLoader.Query.AUTHOR)));
            }
            holder.thumbnailView.setImageUrl(
                    mCursor.getString(ArticleLoader.Query.THUMB_URL),
                    ImageLoaderHelper.getInstance(ArticleListActivity.this).getImageLoader());
            holder.thumbnailView.setAspectRatio(Math.min(1.0f, mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO)));
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    public class AllArticleLoadCallBack implements LoaderManager.LoaderCallbacks<Cursor> {
        private long mStartId;
        private Cursor mCursor;
        private Context context;

        public AllArticleLoadCallBack(Context context, long itemId) {
            this.context = context;
            mStartId = itemId;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return ArticleLoader.newAllArticlesInstance(context);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            mCursor = cursor;
            // Select the start ID
            if (mStartId > 0) {
                mCursor.moveToPosition(-1);
                // TODO: optimize
                while (mCursor.moveToNext()) {
                    if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                        final int position = mCursor.getPosition();
                        EventBus.getDefault().postSticky(EvenBusMessageBuilder.BuildDetailMessage(mCursor, position));
//                        if (progressDialog.isShowing()) {progressDialog.dismiss();}
                        startActivity(new Intent(ArticleListActivity.this, ArticleDetailActivity.class));
                        break;
                    }
                }
                mStartId = 0;
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }
}

/*
 * Copyright (c) 2014, David Forsythe
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of Luchadeer nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.dforsyth.android.luchadeer.ui.game;


import android.app.Fragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;

import org.dforsyth.android.luchadeer.ImageGalleryActivity;
import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.model.giantbomb.Game;
import org.dforsyth.android.luchadeer.model.giantbomb.Image;
import org.dforsyth.android.luchadeer.model.giantbomb.Platform;
import org.dforsyth.android.luchadeer.model.giantbomb.SimpleNamedObject;
import org.dforsyth.android.luchadeer.model.giantbomb.Video;
import org.dforsyth.android.luchadeer.net.LuchadeerApi;
import org.dforsyth.android.luchadeer.persist.LuchadeerPersist;
import org.dforsyth.android.luchadeer.ui.util.BezelImageView;
import org.dforsyth.android.luchadeer.ui.util.ObservableScrollView;
import org.dforsyth.android.luchadeer.ui.util.ParallaxScrollView;
import org.dforsyth.android.luchadeer.ui.util.UiUtil;
import org.dforsyth.android.luchadeer.util.IntentUtil;
import org.dforsyth.android.luchadeer.util.LoaderResult;
import org.dforsyth.android.luchadeer.util.Util;
import org.dforsyth.android.ravioli.RavioliRequest;
import org.dforsyth.android.ravioli.RavioliResponse;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class GameDetailFragment extends Fragment implements
        ObservableScrollView.OnScrollChangedListener,
        DrawerLayout.DrawerListener,
        LoaderManager.LoaderCallbacks<LoaderResult<Game>> {
    private static final String TAG = GameDetailFragment.class.getName();

    private static final String ARG_GAME_ID = "game_id";
    private static final String STATE_GAME = "game";
    private static final String STATE_SIMILAR_GAMES = "similar_games";
    private static final String STATE_RELATED_VIDEOS = "related_videos";

    private static final int GAME_LOADER_ID = 2;

    private ActionBarActivity mActivity;

    private Game mGame;
    private int mGameId;

    private Bundle mSimilarGamesBundle;
    private Bundle mVideosBundle;

    private LuchadeerApi mApi;
    private LuchadeerPersist mPersist;

    private ParallaxScrollView mScrollView;
    private View mStickyHeader;
    private ImageView mSlideImage;
    private FrameLayout mHeaderImageContainer;
    private GameDetailImage mHeaderImage;
    private TextView mName;
    private TextView mDeck;
    private WebView mDescription;
    private TextView mReleaseDate;

    private LinearLayout mPlatforms;
    private LinearLayout mDevelopers;
    private LinearLayout mPublishers;

    private LinearLayout mSimilarGames;
    private ArrayList<View> mSimilarGameViews;
    private int mLoadedGameIndex;

    private LinearLayout mVideos;
    private ArrayList<View> mVideoViews;
    private int mLoadedVideoIndex;

    private ActionBar mActionBar;

    private ShareActionProvider mShareProvider;
    private View mShareView;
    private MenuItem mFavoriteMenuItem;
    private MenuItem mShowOnWebMenuItem;

    private View mLoadingView;

    private Drawable mActionBarBackground;

    private Animation mSlideInAnim;
    private Animation mSlideOutAnim;

    private boolean mSlideImageShown;
    private boolean mSlideEnabled;
    private boolean mDetailsShown;
    private boolean mIsFavorite;

    private OnDetailLoadFailedListener mOnDetailLoadFailedListener;

    public static GameDetailFragment newInstance(int gameId) {
        GameDetailFragment fragment = new GameDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_GAME_ID, gameId);
        fragment.setArguments(args);
        return fragment;
    }

    public GameDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGameId = getArguments().getInt(ARG_GAME_ID);
        mActivity = (ActionBarActivity) getActivity();
        mApi = LuchadeerApi.getInstance(mActivity.getApplicationContext());
        mPersist = LuchadeerPersist.getInstance(mActivity.getApplicationContext());

        mOnDetailLoadFailedListener = (OnDetailLoadFailedListener) mActivity;

        setHasOptionsMenu(true);
    }

    private void setupAnimations() {
        mSlideInAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in);
        mSlideOutAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out);
    }

    private void setupActionBarBackgroundDrawable() {
        LayerDrawable layers = (LayerDrawable) getResources().getDrawable(R.drawable.actionbar_layers);
        mActionBar.setBackgroundDrawable(layers);
        mActionBarBackground = layers.getDrawable(1);

        if (mActivity.getWindow().hasFeature(Window.FEATURE_ACTION_BAR_OVERLAY)) {
            mActionBarBackground.setAlpha(0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FrameLayout rootView = (FrameLayout) inflater.inflate(R.layout.fragment_game_detail, container, false);

        mLoadingView = inflater.inflate(R.layout.list_loading_item, null, false);

        // TODO turn this into a listview to speed up view build
        mScrollView = (ParallaxScrollView) rootView.findViewById(R.id.scrollview);

        mStickyHeader = mScrollView.findViewById(R.id.game_header);
        mHeaderImageContainer = (FrameLayout) mScrollView.findViewById(R.id.header_container);
        mHeaderImage = (GameDetailImage) mScrollView.findViewById(R.id.primary_image);
        mSlideImage = (ImageView) mScrollView.findViewById(R.id.slide_image);
        mName = (TextView) mScrollView.findViewById(R.id.game_name);
        mDeck = (TextView) mScrollView.findViewById(R.id.game_deck);
        mDescription = (WebView) mScrollView.findViewById(R.id.game_description);
        mReleaseDate = (TextView) mScrollView.findViewById(R.id.game_release_date);
        mPlatforms = (LinearLayout) mScrollView.findViewById(R.id.platforms);
        mDevelopers = (LinearLayout) mScrollView.findViewById(R.id.developers);
        mPublishers = (LinearLayout) mScrollView.findViewById(R.id.publishers);

        mSimilarGames = (LinearLayout) mScrollView.findViewById(R.id.similar_games);

        mVideos = (LinearLayout) mScrollView.findViewById(R.id.videos);

        mScrollView.setOnScrollChangedListener(this);

        mScrollView.setVisibility(View.GONE);

        rootView.addView(mLoadingView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");

        mActionBar = mActivity.getSupportActionBar();

        setupActionBarBackgroundDrawable();
        setupAnimations();

        Game game = null;
        if (savedInstanceState != null) {
            game = savedInstanceState.getParcelable(STATE_GAME);
            mSimilarGamesBundle = savedInstanceState.getBundle(STATE_SIMILAR_GAMES);
            mVideosBundle = savedInstanceState.getBundle(STATE_RELATED_VIDEOS);
        } else {
            mSimilarGamesBundle = new Bundle();
            mVideosBundle = new Bundle();
        }

        if (game != null) {
            onGameRequestComplete(game);
        } else {
            getLoaderManager().initLoader(GAME_LOADER_ID, null, this);
        }

        checkIfGameIsFavorite();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.game_detail, menu);

        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        setupShareProviderIntent();

        mShareView = MenuItemCompat.getActionView(item);
        mShowOnWebMenuItem = menu.findItem(R.id.menu_item_show_on_web);
        mFavoriteMenuItem = menu.findItem(R.id.menu_item_favorite);

        // if we already know we're a favorite, update the icon (since we wouldn't have been able to
        // do it in setFavorite)
        if (mIsFavorite) {
            mFavoriteMenuItem.setIcon(R.drawable.ic_favorite_white_24dp);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
            return true;
        }

        // TODO disable these button and enable them when set details is shown, rather than monitoring
        // selection like this
        if (mDetailsShown) {
            if (item.getItemId() == R.id.menu_item_show_on_web) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mGame.getSiteDetailUrl()));
                startActivity(browserIntent);
                return true;
            } else if (item.getItemId() == R.id.menu_item_favorite) {
                if (!mIsFavorite) {
                    addFavorite();
                } else {
                    removeFavorite();
                }
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_GAME, mGame);
        outState.putBundle(STATE_SIMILAR_GAMES, mSimilarGamesBundle);
        outState.putBundle(STATE_RELATED_VIDEOS, mVideosBundle);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // this will cancel related games and videos requests
        mApi.cancelRequests(this);
    }

    public void onScrollChanged(int l, int t, int oldl, int oldt) {
        // check if we're in an overlay theme (mainactivity vs gamedetailactivity)
        boolean isOverlayTheme = mActivity.getWindow().hasFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        // height of the image container -- if there is no image, this should be 0
        int primaryImageHeight = mHeaderImageContainer.getHeight();

        // height of the actionbar
        int actionBarHeight = mActionBar.getHeight();
        int actionBarPadHeight = (isOverlayTheme ? actionBarHeight : 0);

        // this is how far we can scroll before we need to start translating the sticky element
        int headerScrollHeight = primaryImageHeight - actionBarPadHeight;

        // if we're in an overlay theme, play with the actionbar alpha
        if (isOverlayTheme) {
            float scrollRatio = headerScrollHeight != 0 ? (float) Math.min(Math.max(t, 0), headerScrollHeight) / headerScrollHeight : 1;
            int alpha = (int) (scrollRatio * 255);
            mActionBarBackground.setAlpha(alpha);
        }

        // handle the sticky view
        if (t > headerScrollHeight) {
            mStickyHeader.setTranslationY(t - primaryImageHeight + actionBarPadHeight);

            if (!mSlideImageShown && mSlideEnabled) {
                mSlideImage.setVisibility(View.VISIBLE);
                mSlideImage.startAnimation(mSlideInAnim);
                mSlideImageShown = true;
            }
        } else if (mStickyHeader.getTranslationY() != 0) {
            mStickyHeader.setTranslationY(0);

            if (mSlideImageShown) {
                mSlideImage.setVisibility(View.GONE);
                mSlideImage.startAnimation(mSlideOutAnim);
                mSlideImageShown = false;
            }
        }

        // load related videos and games a little bit ahead of when they will
        // appear on the screen.

        if (mVideoViews != null && mLoadedVideoIndex < mVideoViews.size()) {
            int vh = mVideoViews.get(0).getHeight();
            int vt = mVideos.getTop() + mVideoViews.get(0).getTop();

            int bottom = t + mScrollView.getHeight();
            if (bottom > vt) {
                int max = (bottom - vt) / vh;
                max = Math.min(max, mVideoViews.size());
                if (mLoadedVideoIndex < max) {
                    for (View v : mVideoViews.subList(mLoadedVideoIndex, max)) {
                        Video video = (Video) v.getTag();
                        Log.d(TAG, "loading video: " + video.getId());
                        requestRelatedVideo(video.getId(), (BezelImageView) v.findViewById(R.id.detail_image));
                    }
                    mLoadedVideoIndex = max;
                }
            }
        }

        if (mSimilarGameViews != null && mLoadedGameIndex < mSimilarGameViews.size()) {
            int vh = mSimilarGameViews.get(0).getHeight();
            int vt = mSimilarGames.getTop() + mSimilarGameViews.get(0).getTop();

            int bottom = t + mScrollView.getHeight();
            if (bottom > vt) {
                int max = (bottom - vt) / vh;
                max = Math.min(max, mSimilarGameViews.size());
                if (mLoadedGameIndex < max) {
                    for (View v : mSimilarGameViews.subList(mLoadedGameIndex, max)) {
                        Game game = (Game) v.getTag();
                        Log.d(TAG, "loading game: " + game.getId());
                        requestSimilarGame(game.getId(), (BezelImageView) v.findViewById(R.id.detail_image));
                    }
                    mLoadedGameIndex = max;
                }
            }
        }
    }

    private void onGameRequestFailed() {
        mOnDetailLoadFailedListener.onDetailLoadFailed();
    }

    private static final int[] DETAIL_IMAGE_VIEWS = {
            R.id.first_extra_image,
            R.id.second_extra_image,
            R.id.third_extra_image,
    };

    private void onGameRequestComplete(final Game game) {
        mGame = game;

        LayoutInflater inflater = LayoutInflater.from(mActivity);

        final Image primaryImage = mGame.getImage();
        if (primaryImage != null) {
            mHeaderImage.setVisibility(View.VISIBLE);
            mHeaderImage.setImageUrl(primaryImage.getSuperUrl(), mApi.getImageLoader());
            mHeaderImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mGame != null && mGame.getImages() != null) {
                        Intent intent = new Intent(getActivity(), ImageGalleryActivity.class);
                        intent.putParcelableArrayListExtra(ImageGalleryActivity.EXTRA_IMAGES, mGame.getImages());
                        intent.putExtra(ImageGalleryActivity.EXTRA_START_URL, primaryImage.getSuperUrl());
                        startActivity(intent);
                    }
                }
            });

            BezelImageView b = (BezelImageView) mSlideImage;
            b.setImageUrl(primaryImage.getSuperUrl(), mApi.getImageLoader());
            mSlideImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mScrollView.smoothScrollTo(0, 0);
                }
            });

            mSlideEnabled = true;

            mScrollView.subscribeView(mHeaderImage);
        } else {
            // if we arent in an overlay theme, shrink the header container minimum height
            if (!getActivity().getWindow().hasFeature(Window.FEATURE_ACTION_BAR_OVERLAY)) {
                mHeaderImageContainer.setMinimumHeight(0);
            }
        }

        mName.setText(game.getName());

        String deckText = game.getDeck();
        if (deckText != null) {
            mDeck.setVisibility(View.VISIBLE);
            mDeck.setText(deckText);
        }

        String descriptionText = game.getDescription();
        if (descriptionText != null) {
            descriptionText = descriptionText.trim();
            if (descriptionText.length() > 0) {
                mDescription.setVisibility(View.VISIBLE);
                mDescription.loadDataWithBaseURL(
                        getString(R.string.giant_bomb_web_uri),
                        mGame.getDescription(),
                        "text/html",
                        "utf-8",
                        null);
                mDescription.getSettings().setLoadsImagesAutomatically(false);
                mDescription.getSettings().setDefaultFontSize(14);
                mDescription.setVerticalScrollBarEnabled(false);
                mDescription.setWebViewClient(new WebViewClient(){
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        if (url != null) {
                            view.getContext().startActivity(
                                    new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
            }
        }

        List<Image> images = game.getImages();
        if (images != null) {
            for (int i = 0, populatedImages = 0; i < images.size() && populatedImages < DETAIL_IMAGE_VIEWS.length; ++i) {
                final Image image = images.get(i);
                if (primaryImage != null && image.getSuperUrl().equals(primaryImage.getSuperUrl())) {
                    continue;
                }
                NetworkImageView imageView = (NetworkImageView) mScrollView.findViewById(DETAIL_IMAGE_VIEWS[populatedImages]);
                imageView.setImageUrl(image.getSuperUrl(), mApi.getImageLoader());
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mGame.getImages() != null) {
                            Intent intent = new Intent(getActivity(), ImageGalleryActivity.class);
                            intent.putParcelableArrayListExtra(ImageGalleryActivity.EXTRA_IMAGES, mGame.getImages());
                            intent.putExtra(ImageGalleryActivity.EXTRA_START_URL, image.getSuperUrl());
                            startActivity(intent);
                        }
                    }
                });
                imageView.setVisibility(View.VISIBLE);
                ++populatedImages;
            }
        }

        Date originalReleaseDate = game.getOriginalReleaseDate();
        if (originalReleaseDate != null) {
            DateFormat instance;
            instance = DateFormat.getDateInstance();
            String releaseDate = String.format("Release: %s", instance.format(originalReleaseDate));
            mReleaseDate.setText(releaseDate);
        } else if (game.getExpectedReleaseDateString() != null) {
            String expectedRelease = String.format("Expected Release: %s", game.getExpectedReleaseDateString());
            mReleaseDate.setText(expectedRelease);
        } else {
            mReleaseDate.setText("Release: N/A");
        }

        List<Platform> platforms = game.getPlatforms();
        if (platforms != null && platforms.size() > 0) {
            mPlatforms.setVisibility(View.VISIBLE);
            for (Platform platform : platforms) {
                // add a text view per developer
                TextView tv = (TextView) inflater.inflate(R.layout.detail_list_item, mPlatforms, false);
                tv.setText(platform.getName());
                mPlatforms.addView(tv);
            }
        }

        List<SimpleNamedObject> developers = game.getDevelopers();
        if (developers != null && developers.size() > 0) {
            mDevelopers.setVisibility(View.VISIBLE);
            for (SimpleNamedObject developer : developers) {
                TextView tv = (TextView) inflater.inflate(R.layout.detail_list_item, mDevelopers, false);
                tv.setText(developer.getName());
                mDevelopers.addView(tv);
            }
        }

        List<SimpleNamedObject> publishers = game.getPublishers();
        if (publishers != null && publishers.size() > 0) {
            mPublishers.setVisibility(View.VISIBLE);
            for (SimpleNamedObject publisher : publishers) {
                TextView tv = (TextView) inflater.inflate(R.layout.detail_list_item, mPublishers, false);
                tv.setText(publisher.getName());
                mPublishers.addView(tv);
            }
        }

        List<Game> games = game.getSimilarGames();
        if (games != null && games.size() > 0) {
            mSimilarGames.setVisibility(View.VISIBLE);
            mSimilarGameViews = new ArrayList<View>();

            for (final Game similar : games) {
                LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.image_detail_list_item, mSimilarGames, false);
                TextView tv = (TextView) ll.findViewById(R.id.detail_text);
                tv.setText(similar.getName());

                ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(IntentUtil.getGameDetailActivityIntent(mActivity, similar.getId()));
                    }
                });

                mSimilarGames.addView(ll);

                ll.setTag(similar);
                mSimilarGameViews.add(ll);
            }
        }

        List<Video> videos = game.getVideos();
        if (videos != null && videos.size() > 0) {
            mVideos.setVisibility(View.VISIBLE);
            mVideoViews = new ArrayList<View>();

            for (final Video video : videos) {
                LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.image_detail_list_item, mVideos, false);
                TextView tv = (TextView) ll.findViewById(R.id.detail_text);
                tv.setText(video.getName());

                ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(IntentUtil.getVideoDetailActivityIntent(mActivity, video.getId()));
                    }
                });

                mVideos.addView(ll);

                ll.setTag(video);
                mVideoViews.add(ll);
            }
        }

        setupShareProviderIntent();
        showDetails(true);
    }

    private void requestSimilarGame(final int gameId, final BezelImageView view) {
        if (!mSimilarGamesBundle.containsKey(String.valueOf(gameId))) {
            mApi.getGame(
                gameId
            ).requestAsync(
                this,
                new RavioliRequest.Callbacks<LuchadeerApi.GiantBombResponse<Game>>() {
                    @Override
                    public void onSuccess(RavioliResponse<LuchadeerApi.GiantBombResponse<Game>> response) {
                        LuchadeerApi.GiantBombResponse<Game> gameResponse = response.getDecoded();
                        if (!gameResponse.statusIsOK()) {
                            return;
                        }
                        onSimilarGameRequestComplete(gameResponse.getResults(), view);
                    }

                    @Override
                    public void onFailure(VolleyError error) {
                        Log.d(TAG, "fetching similar game failed");
                    }
                }
            );
        } else {
            onSimilarGameRequestComplete((Game) mSimilarGamesBundle.get(String.valueOf(gameId)), view);
        }

    }

    private void onSimilarGameRequestComplete(Game game, BezelImageView view) {
        // XXX It would be  alot smarter to use this game object to load an activity on button
        // click, especially with how slow the game api requests tend to be... maybe the request cache
        // is good enough here
        mSimilarGamesBundle.putParcelable(Integer.toString(game.getId()), game);
        Image image = game.getImage();
        if (image != null) {
            view.setImageUrl(image.getSuperUrl(), mApi.getImageLoader());
        }
    }

    private void requestRelatedVideo(int videoId, final BezelImageView view) {
        if (!mVideosBundle.containsKey(String.valueOf(videoId))) {
            mApi.getVideo(
                videoId
            ).requestAsync(
                this,
                new RavioliRequest.Callbacks<LuchadeerApi.GiantBombResponse<Video>>() {
                    @Override
                    public void onSuccess(RavioliResponse<LuchadeerApi.GiantBombResponse<Video>> response) {
                        onRelatedVideoRequestComplete(response.getDecoded().getResults(), view);
                    }

                    @Override
                    public void onFailure(VolleyError error) {
                        Log.d(TAG, "fetching related video failed");
                    }
                }
            );
        } else {
            onRelatedVideoRequestComplete((Video) mVideosBundle.get(String.valueOf(videoId)), view);
        }


    }

    private void onRelatedVideoRequestComplete(Video video, BezelImageView view) {
        mVideosBundle.putParcelable(Integer.toString(video.getId()), video);
        Image image = video.getImage();
        if (image != null) {
            view.setImageUrl(image.getSuperUrl(), mApi.getImageLoader());
        }
    }

    private void checkIfGameIsFavorite() {
        mPersist.gameIsFavorite(getLoaderManager(), mGameId, new LuchadeerPersist.ExistsResultListener() {
            @Override
            public void onExistsResult(boolean exists) {
                setFavorite(exists);
            }
        });
    }

    public void setFavorite(boolean favorite) {
        if (favorite == mIsFavorite) {
            return;
        }
        mIsFavorite = favorite;
        if (mFavoriteMenuItem != null) {
            if (mIsFavorite) {
                mFavoriteMenuItem.setIcon(R.drawable.ic_favorite_white_24dp);
            } else {
                mFavoriteMenuItem.setIcon(R.drawable.ic_favorite_outline_white_24dp);
            }
        }
    }

    private void addFavorite() {
        mPersist.addGameFavorite(mGame);
        setFavorite(true);
        Toast.makeText(mActivity,  getString(R.string.favorite_added), Toast.LENGTH_SHORT).show();
    }

    private void removeFavorite() {
        mPersist.removeGameFavorite(mGameId);
        setFavorite(false);
        Toast.makeText(mActivity, getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show();
    }

    private void showDetails(boolean showDetails) {
        if (showDetails == mDetailsShown) {
            return;
        }

        mDetailsShown = showDetails;

        // TODO: clean things up so we can turn on these animations
        if (mDetailsShown) {
            mLoadingView.setVisibility(View.GONE);

            mScrollView.setVisibility(View.VISIBLE);
        } else {
            mLoadingView.setVisibility(View.VISIBLE);

            mScrollView.setVisibility(View.GONE);
        }

        // fire scroll to update the ui
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                int x = mScrollView.getScrollX();
                int y = mScrollView.getScrollY();
                onScrollChanged(x, y, x, y);
            }
        });
    }

    private void setupShareProviderIntent() {
        if (mGame == null || mShareProvider == null) {
            return;
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                String.format("Check out \"%s\" on Giant Bomb! %s", mGame.getName(), mGame.getSiteDetailUrl()));
        shareIntent.setType("text/plain");
        mShareProvider.setShareIntent(shareIntent);
    }

    public void onDrawerSlide(View view, float offset) {
        UiUtil.actionBarItemOnDrawerSlide(mActivity.findViewById(R.id.menu_item_favorite), offset);
        UiUtil.actionBarItemOnDrawerSlide(mActivity.findViewById(R.id.menu_item_show_on_web), offset);
        UiUtil.actionBarItemOnDrawerSlide(mShareView, offset);
    }

    @Override
    public void onDrawerOpened(View view) {
        UiUtil.actionBarItemOnDrawerOpened(mActivity.findViewById(R.id.menu_item_favorite));
        UiUtil.actionBarItemOnDrawerOpened(mActivity.findViewById(R.id.menu_item_show_on_web));
        UiUtil.actionBarItemOnDrawerOpened(mShareView);
    }

    @Override
    public void onDrawerClosed(View view) {
        UiUtil.actionBarItemOnDrawerClosed(mActivity.findViewById(R.id.menu_item_favorite));
        UiUtil.actionBarItemOnDrawerClosed(mActivity.findViewById(R.id.menu_item_show_on_web));
        UiUtil.actionBarItemOnDrawerClosed(mShareView);
    }

    @Override
    public void onDrawerStateChanged(int i) {

    }

    @Override
    public Loader<LoaderResult<Game>> onCreateLoader(int i, Bundle bundle) {
        return new GameLoader(getActivity(), mGameId);
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<Game>> gameLoader, LoaderResult<Game> result) {
        Log.d(TAG, "onLoadFinished");
        Game game = result.getResult();
        Exception error = result.getError();

        if (game != null) {
            onGameRequestComplete(game);
        } else {
            Util.handleRequestError(getActivity(), error);
            onGameRequestFailed();
        }
        // we're done with this loader
        getLoaderManager().destroyLoader(GAME_LOADER_ID);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<Game>> gameLoader) {
    }

    public interface OnDetailLoadFailedListener {
        public void onDetailLoadFailed();
    }

    private static class GameLoader extends AsyncTaskLoader<LoaderResult<Game>> {
        private int mGameId;

        public GameLoader(Context context, int gameId) {
            super(context);
            mGameId = gameId;
        }

        @Override
        public LoaderResult<Game> loadInBackground() {
            LuchadeerApi api = LuchadeerApi.getInstance(getContext());

            RavioliResponse<LuchadeerApi.GiantBombResponse<Game>> response = null;
            Exception error = null;

            try {
                response = api.getGame(
                    mGameId
                ).request(this);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                error = e;
            }

            return new LoaderResult<Game>(response == null ? null : response.getDecoded().getResults(), error);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }
    }
}

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

package org.dforsyth.android.luchadeer.ui.favorites;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.net.LuchadeerApi;
import org.dforsyth.android.luchadeer.persist.LuchadeerPersist;
import org.dforsyth.android.luchadeer.persist.db.LuchadeerContract;
import org.dforsyth.android.luchadeer.persist.provider.BatchContentObserver;
import org.dforsyth.android.luchadeer.ui.util.BezelImageView;


public abstract class FavoritesListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = FavoritesListFragment.class.getName();

    private SimpleCursorAdapter mAdapter;
    // private Activity mActivity;
    private ListView mListView;

    // private ActionMode mActionMode;

    private ActionModeListener mActionModeListener;

    private BatchContentObserver mFavoritesObserver;

    private LuchadeerApi mApi;
    private LuchadeerPersist mPersist;


    public abstract Uri getContentUri();
    public abstract String[] getProjection();
    public abstract String getEmptyText();
    public abstract void startActivityForGiantBombId(int giantBombId);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(getEmptyText());

        mListView = getListView();
        mApi = LuchadeerApi.getInstance(getActivity().getApplicationContext());
        mPersist = LuchadeerPersist.getInstance(getActivity().getApplicationContext());
        setupContextMenu();
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mFavoritesObserver = new BatchContentObserver(new BatchContentObserver.OnChangeListener() {
            @Override
            public void onChange(boolean selfChange) {
                // TODO: it would be cooler if we kept our scroll position
                setListShown(false);
                setListAdapter(null);

                restartLoader();
            }
        });

        // this activity exists before mActivity is set :o

        activity.getContentResolver().registerContentObserver(
                getContentUri(),
                true,
                mFavoritesObserver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "detached!");
        if (mFavoritesObserver != null) {
            getActivity().getContentResolver().unregisterContentObserver(mFavoritesObserver);
        }
    }

    private class FavoritesCursorAdapter extends SimpleCursorAdapter {
        public FavoritesCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            mApi = LuchadeerApi.getInstance(getActivity());
        }

        @Override
        public void setViewImage(ImageView v, String value) {
            BezelImageView iv = (BezelImageView) v;
            Log.d(TAG, value);
            iv.setImageUrl(value, mApi.getImageLoader());
        }
    }

    public void onQueryComplete(Cursor cursor) {
        mAdapter = new FavoritesCursorAdapter(
                getActivity(),
                R.layout.simple_list_item,
                cursor,
                new String[]{LuchadeerContract.ContentColumns.NAME, LuchadeerContract.ContentColumns.SUPER_IMAGE_URL},
                new int[]{R.id.result_name, R.id.result_image},
                0);
        setListAdapter(mAdapter);
        Log.d(TAG, "onQueryComplete");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                getContentUri(),
                getProjection(),
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> objectLoader, Cursor c) {
        if (c == null) {
            return;
        }
        onQueryComplete(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> objectLoader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // super.onListItemClick(l, v, position, id);
        Cursor cursor = (Cursor) l.getItemAtPosition(position);
        int giantBombId = cursor.getInt(cursor.getColumnIndex(LuchadeerContract.ContentColumns.GIANT_BOMB_ID));
        startActivityForGiantBombId(giantBombId);
    }

    public void removeFavorite(int giantBombId) {
        mPersist.removeFavorite(
                getContentUri(),
                giantBombId
        );
    };

    private void setupContextMenu() {
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                // mListView.setItemChecked(i, b);
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.list_remove_context, menu);

                if (mActionModeListener != null) {
                    mActionModeListener.onCreateActionMode();
                }

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_item_remove) {
                    // no count check because a user can hit remove unless something is selected.
                    setListShown(false);

                    SparseBooleanArray array = mListView.getCheckedItemPositions();

                    for (int i = 0; i < array.size(); ++i) {
                        if (array.valueAt(i)) {
                            int key = array.keyAt(i);
                            Cursor cursor = (Cursor) mListView.getItemAtPosition(key);
                            int idx = cursor.getColumnIndex(LuchadeerContract.ContentColumns.GIANT_BOMB_ID);
                            int giantBombId = cursor.getInt(idx);

                            removeFavorite(giantBombId);
                        }
                    }
                    actionMode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

                if (mActionModeListener != null) {
                    mActionModeListener.onDestroyActionMode();
                }
            }
        });
    }

    public interface ActionModeListener {
        public void onCreateActionMode();
        public void onDestroyActionMode();
    }

    public void setActionModeListener(ActionModeListener actionModeListener) {
        mActionModeListener = actionModeListener;
    }
}

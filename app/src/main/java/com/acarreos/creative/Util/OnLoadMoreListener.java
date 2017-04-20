package com.acarreos.creative.Util;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by EnmanuelPc on 27/11/2015.
 */
public class OnLoadMoreListener extends RecyclerView.OnScrollListener {

    int pastVisiblesItems, visibleItemCount, totalItemCount;
    boolean loading = true;
    OnEndListListener endListListener;

    public OnLoadMoreListener(OnEndListListener endListListener) {
        this.endListListener = endListListener;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (dy > 0) //check for scroll down
        {
            visibleItemCount = recyclerView.getLayoutManager().getChildCount();
            totalItemCount = recyclerView.getLayoutManager().getItemCount();
            pastVisiblesItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

            if (loading) {
                if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                    loading = false;
                    endListListener.onLoadMore();
                    /*Toast.makeText(getActivity(), "Buscando más envíos...", Toast.LENGTH_SHORT).show();
                    cargarEnvios();*/
                }
            }
        }
    }

    public void enableLoading() {
        loading = true;
    }

    public interface OnEndListListener {
        void onLoadMore();
    }

}

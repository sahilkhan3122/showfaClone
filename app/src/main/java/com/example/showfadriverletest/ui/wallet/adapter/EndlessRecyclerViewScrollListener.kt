package com.example.showfadriverletest.ui.wallet.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class EndlessRecyclerViewScrollListener(private val mLinearLayoutManager: LinearLayoutManager) :
    RecyclerView.OnScrollListener() {
    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private val visibleThreshold = 1

    // The current offset index of data you have loaded
     var currentPage = 0

    // The total number of items in the dataset after the last load
    private var previousTotalItemCount = 0

    // True if we are still waiting for the last set of data to load.
    private var loading = true

    // Sets the starting page index
    private val startingPageIndex = 1
    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
        val lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition()
        val totalItemCount = mLinearLayoutManager.itemCount

        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if (totalItemCount < previousTotalItemCount) {
            currentPage = startingPageIndex
            previousTotalItemCount = totalItemCount
            if (totalItemCount == 0) {
                loading = true
            }
        }
        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.
        if (loading && totalItemCount > previousTotalItemCount) {
            loading = false
            previousTotalItemCount = totalItemCount
        }

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        if (!loading && totalItemCount <= lastVisibleItem + visibleThreshold) {
            currentPage++
            onLoadMore(currentPage, totalItemCount)
            loading = true
        }
    }

    abstract fun onLoadMore(page: Int, totalItemsCount: Int)
}
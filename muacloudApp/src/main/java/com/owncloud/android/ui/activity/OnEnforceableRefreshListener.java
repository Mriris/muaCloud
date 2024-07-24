

package com.owncloud.android.ui.activity;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public interface OnEnforceableRefreshListener extends SwipeRefreshLayout.OnRefreshListener {

    void onRefresh(boolean enforced);

}


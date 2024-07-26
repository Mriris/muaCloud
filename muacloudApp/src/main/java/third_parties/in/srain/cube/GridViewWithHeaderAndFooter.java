package third_parties.in.srain.cube;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

import java.util.ArrayList;


public class GridViewWithHeaderAndFooter extends GridView {

    private static class FixedViewInfo {

        public View view;
        ViewGroup viewContainer;

        public Object data;

        boolean isSelectable;
    }

    private View mViewForMeasureRowHeight = null;
    private int mRowHeight = -1;

    private ArrayList<FixedViewInfo> mHeaderViewInfos = new ArrayList<>();
    private ArrayList<FixedViewInfo> mFooterViewInfos = new ArrayList<>();

    private void initHeaderGridView() {
    }

    public GridViewWithHeaderAndFooter(Context context) {
        super(context);
        initHeaderGridView();
    }

    public GridViewWithHeaderAndFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initHeaderGridView();
    }

    public GridViewWithHeaderAndFooter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initHeaderGridView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ListAdapter adapter = getAdapter();
        if (adapter instanceof HeaderViewGridAdapter) {
            ((HeaderViewGridAdapter) adapter).setNumColumns(getNumColumnsCompatible());
            invalidateRowHeight();
            ((HeaderViewGridAdapter) adapter).setRowHeight(getRowHeight());
        }
    }

    @Override
    public void setClipChildren(boolean clipChildren) {

    }


    public void addHeaderView(View v) {
        addHeaderView(v, null, true);
    }


    public void addHeaderView(View v, Object data, boolean isSelectable) {
        ListAdapter adapter = getAdapter();
        if (adapter != null && !(adapter instanceof HeaderViewGridAdapter)) {
            throw new IllegalStateException(
                    "Cannot add header view to grid -- setAdapter has already been called.");
        }

        ViewGroup.LayoutParams lyp = v.getLayoutParams();

        FixedViewInfo info = new FixedViewInfo();
        FrameLayout fl = new FullWidthFixedViewLayout(getContext());

        if (lyp != null) {
            v.setLayoutParams(new FrameLayout.LayoutParams(lyp.width, lyp.height));
            fl.setLayoutParams(new LayoutParams(lyp.width, lyp.height));
        }
        fl.addView(v);
        info.view = v;
        info.viewContainer = fl;
        info.data = data;
        info.isSelectable = isSelectable;
        mHeaderViewInfos.add(info);


        if (adapter != null) {
            ((HeaderViewGridAdapter) adapter).notifyDataSetChanged();
        }
    }

    public void addFooterView(View v) {
        addFooterView(v, null, true);
    }

    public void addFooterView(View v, Object data, boolean isSelectable) {
        ListAdapter mAdapter = getAdapter();
        if (mAdapter != null && !(mAdapter instanceof HeaderViewGridAdapter)) {
            throw new IllegalStateException(
                    "Cannot add header view to grid -- setAdapter has already been called.");
        }

        ViewGroup.LayoutParams lyp = v.getLayoutParams();

        FixedViewInfo info = new FixedViewInfo();
        FrameLayout fl = new FullWidthFixedViewLayout(getContext());

        if (lyp != null) {
            v.setLayoutParams(new FrameLayout.LayoutParams(lyp.width, lyp.height));
            fl.setLayoutParams(new LayoutParams(lyp.width, lyp.height));
        }
        fl.addView(v);
        info.view = v;
        info.viewContainer = fl;
        info.data = data;
        info.isSelectable = isSelectable;
        mFooterViewInfos.add(info);

        if (mAdapter != null) {
            ((HeaderViewGridAdapter) mAdapter).notifyDataSetChanged();
        }
    }

    public int getHeaderViewCount() {
        return mHeaderViewInfos.size();
    }

    public int getFooterViewCount() {
        return mFooterViewInfos.size();
    }


    public boolean removeHeaderView(View v) {
        if (mHeaderViewInfos.size() > 0) {
            boolean result = false;
            ListAdapter adapter = getAdapter();
            if (adapter != null && ((HeaderViewGridAdapter) adapter).removeHeader(v)) {
                result = true;
            }
            removeFixedViewInfo(v, mHeaderViewInfos);
            return result;
        }
        return false;
    }


    public boolean removeFooterView(View v) {
        if (mFooterViewInfos.size() > 0) {
            boolean result = false;
            ListAdapter adapter = getAdapter();
            if (adapter != null && ((HeaderViewGridAdapter) adapter).removeFooter(v)) {
                result = true;
            }
            removeFixedViewInfo(v, mFooterViewInfos);
            return result;
        }
        return false;
    }

    private void removeFixedViewInfo(View v, ArrayList<FixedViewInfo> where) {
        int len = where.size();
        for (int i = 0; i < len; ++i) {
            FixedViewInfo info = where.get(i);
            if (info.view == v) {
                where.remove(i);
                break;
            }
        }
    }

    private int getNumColumnsCompatible() {
        return super.getNumColumns();
    }

    private int getColumnWidthCompatible() {
        return super.getColumnWidth();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mViewForMeasureRowHeight = null;
    }

    public void invalidateRowHeight() {
        mRowHeight = -1;
    }

    public int getRowHeight() {
        if (mRowHeight > 0) {
            return mRowHeight;
        }
        ListAdapter adapter = getAdapter();
        int numColumns = getNumColumnsCompatible();

        if (adapter == null || adapter.getCount() <= numColumns * (mHeaderViewInfos.size() + mFooterViewInfos.size())) {
            return -1;
        }
        int mColumnWidth = getColumnWidthCompatible();
        View view = getAdapter().getView(numColumns * mHeaderViewInfos.size(), mViewForMeasureRowHeight, this);
        LayoutParams p = (LayoutParams) view.getLayoutParams();
        if (p == null) {
            p = new LayoutParams(-1, -2, 0);
            view.setLayoutParams(p);
        }
        int childHeightSpec = getChildMeasureSpec(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 0, p.height);
        int childWidthSpec = getChildMeasureSpec(
                MeasureSpec.makeMeasureSpec(mColumnWidth, MeasureSpec.EXACTLY), 0, p.width);
        view.measure(childWidthSpec, childHeightSpec);
        mViewForMeasureRowHeight = view;
        mRowHeight = view.getMeasuredHeight();
        return mRowHeight;
    }

    public void tryToScrollToBottomSmoothly() {
        int lastPos = getAdapter().getCount() - 1;
        smoothScrollToPositionFromTop(lastPos, 0);
    }

    public void tryToScrollToBottomSmoothly(int duration) {
        int lastPos = getAdapter().getCount() - 1;
        smoothScrollToPositionFromTop(lastPos, 0, duration);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (mHeaderViewInfos.size() > 0 || mFooterViewInfos.size() > 0) {
            HeaderViewGridAdapter headerViewGridAdapter = new HeaderViewGridAdapter(mHeaderViewInfos,
                    mFooterViewInfos, adapter);
            int numColumns = getNumColumnsCompatible();
            if (numColumns > 1) {
                headerViewGridAdapter.setNumColumns(numColumns);
            }
            headerViewGridAdapter.setRowHeight(getRowHeight());
            super.setAdapter(headerViewGridAdapter);
        } else {
            super.setAdapter(adapter);
        }
    }


    private class FullWidthFixedViewLayout extends FrameLayout {

        public FullWidthFixedViewLayout(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            int realLeft = GridViewWithHeaderAndFooter.this.getPaddingLeft() + getPaddingLeft();

            if (realLeft != left) {
                offsetLeftAndRight(realLeft - left);
            }
            super.onLayout(changed, left, top, right, bottom);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int targetWidth = GridViewWithHeaderAndFooter.this.getMeasuredWidth()
                    - GridViewWithHeaderAndFooter.this.getPaddingLeft()
                    - GridViewWithHeaderAndFooter.this.getPaddingRight();
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(targetWidth,
                    MeasureSpec.getMode(widthMeasureSpec));
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public void setNumColumns(int numColumns) {
        super.setNumColumns(numColumns);
        ListAdapter adapter = getAdapter();
        if (adapter instanceof HeaderViewGridAdapter) {
            ((HeaderViewGridAdapter) adapter).setNumColumns(numColumns);
        }
    }


    private static class HeaderViewGridAdapter implements WrapperListAdapter, Filterable {


        private final DataSetObservable mDataSetObservable = new DataSetObservable();
        private final ListAdapter mAdapter;
        static final ArrayList<FixedViewInfo> EMPTY_INFO_LIST =
                new ArrayList<>();

        ArrayList<FixedViewInfo> mHeaderViewInfos;
        ArrayList<FixedViewInfo> mFooterViewInfos;
        private int mNumColumns = 1;
        private int mRowHeight = -1;
        boolean mAreAllFixedViewsSelectable;
        private final boolean mIsFilterable;
        private boolean mCachePlaceHoldView = true;

        private boolean mCacheFirstHeaderView = false;

        public HeaderViewGridAdapter(ArrayList<FixedViewInfo> headerViewInfos, ArrayList<FixedViewInfo> footViewInfos
                , ListAdapter adapter) {
            mAdapter = adapter;
            mIsFilterable = adapter instanceof Filterable;
            if (headerViewInfos == null) {
                mHeaderViewInfos = EMPTY_INFO_LIST;
            } else {
                mHeaderViewInfos = headerViewInfos;
            }

            if (footViewInfos == null) {
                mFooterViewInfos = EMPTY_INFO_LIST;
            } else {
                mFooterViewInfos = footViewInfos;
            }
            mAreAllFixedViewsSelectable = areAllListInfosSelectable(mHeaderViewInfos)
                    && areAllListInfosSelectable(mFooterViewInfos);
        }

        void setNumColumns(int numColumns) {
            if (numColumns < 1) {
                return;
            }
            if (mNumColumns != numColumns) {
                mNumColumns = numColumns;
                notifyDataSetChanged();
            }
        }

        void setRowHeight(int height) {
            mRowHeight = height;
        }

        public int getHeadersCount() {
            return mHeaderViewInfos.size();
        }

        int getFootersCount() {
            return mFooterViewInfos.size();
        }

        @Override
        public boolean isEmpty() {
            return (mAdapter == null || mAdapter.isEmpty()) && getHeadersCount() == 0 && getFootersCount() == 0;
        }

        private boolean areAllListInfosSelectable(ArrayList<FixedViewInfo> infos) {
            if (infos != null) {
                for (FixedViewInfo info : infos) {
                    if (!info.isSelectable) {
                        return false;
                    }
                }
            }
            return true;
        }

        boolean removeHeader(View v) {
            for (int i = 0; i < mHeaderViewInfos.size(); i++) {
                FixedViewInfo info = mHeaderViewInfos.get(i);
                if (info.view == v) {
                    mHeaderViewInfos.remove(i);
                    mAreAllFixedViewsSelectable =
                            areAllListInfosSelectable(mHeaderViewInfos) && areAllListInfosSelectable(mFooterViewInfos);
                    mDataSetObservable.notifyChanged();
                    return true;
                }
            }
            return false;
        }

        boolean removeFooter(View v) {
            for (int i = 0; i < mFooterViewInfos.size(); i++) {
                FixedViewInfo info = mFooterViewInfos.get(i);
                if (info.view == v) {
                    mFooterViewInfos.remove(i);
                    mAreAllFixedViewsSelectable =
                            areAllListInfosSelectable(mHeaderViewInfos) && areAllListInfosSelectable(mFooterViewInfos);
                    mDataSetObservable.notifyChanged();
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getCount() {
            if (mAdapter != null) {
                return (getFootersCount() + getHeadersCount()) * mNumColumns + getAdapterAndPlaceHolderCount();
            } else {
                return (getFootersCount() + getHeadersCount()) * mNumColumns;
            }
        }

        @Override
        public boolean areAllItemsEnabled() {
            if (mAdapter != null) {
                return mAreAllFixedViewsSelectable && mAdapter.areAllItemsEnabled();
            } else {
                return true;
            }
        }

        private int getAdapterAndPlaceHolderCount() {
            return (int) (Math.ceil(1f * mAdapter.getCount() / mNumColumns) * mNumColumns);
        }

        @Override
        public boolean isEnabled(int position) {

            int numHeadersAndPlaceholders = getHeadersCount() * mNumColumns;
            if (position < numHeadersAndPlaceholders) {
                return position % mNumColumns == 0
                        && mHeaderViewInfos.get(position / mNumColumns).isSelectable;
            }

            final int adjPosition = position - numHeadersAndPlaceholders;
            int adapterCount = 0;
            if (mAdapter != null) {
                adapterCount = getAdapterAndPlaceHolderCount();
                if (adjPosition < adapterCount) {
                    return adjPosition < mAdapter.getCount() && mAdapter.isEnabled(adjPosition);
                }
            }

            final int footerPosition = adjPosition - adapterCount;
            return footerPosition % mNumColumns == 0
                    && mFooterViewInfos.get(footerPosition / mNumColumns).isSelectable;
        }

        @Override
        public Object getItem(int position) {

            int numHeadersAndPlaceholders = getHeadersCount() * mNumColumns;
            if (position < numHeadersAndPlaceholders) {
                if (position % mNumColumns == 0) {
                    return mHeaderViewInfos.get(position / mNumColumns).data;
                }
                return null;
            }

            final int adjPosition = position - numHeadersAndPlaceholders;
            int adapterCount = 0;
            if (mAdapter != null) {
                adapterCount = getAdapterAndPlaceHolderCount();
                if (adjPosition < adapterCount) {
                    if (adjPosition < mAdapter.getCount()) {
                        return mAdapter.getItem(adjPosition);
                    } else {
                        return null;
                    }
                }
            }

            final int footerPosition = adjPosition - adapterCount;
            if (footerPosition % mNumColumns == 0) {
                return mFooterViewInfos.get(footerPosition).data;
            } else {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            int numHeadersAndPlaceholders = getHeadersCount() * mNumColumns;
            if (mAdapter != null && position >= numHeadersAndPlaceholders) {
                int adjPosition = position - numHeadersAndPlaceholders;
                int adapterCount = mAdapter.getCount();
                if (adjPosition < adapterCount) {
                    return mAdapter.getItemId(adjPosition);
                }
            }
            return -1;
        }

        @Override
        public boolean hasStableIds() {
            if (mAdapter != null) {
                return mAdapter.hasStableIds();
            }
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            int numHeadersAndPlaceholders = getHeadersCount() * mNumColumns;
            if (position < numHeadersAndPlaceholders) {
                View headerViewContainer = mHeaderViewInfos
                        .get(position / mNumColumns).viewContainer;
                if (position % mNumColumns == 0) {
                    return headerViewContainer;
                } else {
                    if (convertView == null) {
                        convertView = new View(parent.getContext());
                    }


                    convertView.setVisibility(View.INVISIBLE);
                    convertView.setMinimumHeight(headerViewContainer.getHeight());
                    return convertView;
                }
            }

            final int adjPosition = position - numHeadersAndPlaceholders;
            int adapterCount = 0;
            if (mAdapter != null) {
                adapterCount = getAdapterAndPlaceHolderCount();
                if (adjPosition < adapterCount) {
                    if (adjPosition < mAdapter.getCount()) {
                        return mAdapter.getView(adjPosition, convertView, parent);
                    } else {
                        if (convertView == null) {
                            convertView = new View(parent.getContext());
                        }
                        convertView.setVisibility(View.INVISIBLE);
                        convertView.setMinimumHeight(mRowHeight);
                        return convertView;
                    }
                }
            }

            final int footerPosition = adjPosition - adapterCount;
            if (footerPosition < getCount()) {
                View footViewContainer = mFooterViewInfos
                        .get(footerPosition / mNumColumns).viewContainer;
                if (position % mNumColumns == 0) {
                    return footViewContainer;
                } else {
                    if (convertView == null) {
                        convertView = new View(parent.getContext());
                    }


                    convertView.setVisibility(View.INVISIBLE);
                    convertView.setMinimumHeight(footViewContainer.getHeight());
                    return convertView;
                }
            }
            throw new ArrayIndexOutOfBoundsException(position);
        }

        @Override
        public int getItemViewType(int position) {

            final int numHeadersAndPlaceholders = getHeadersCount() * mNumColumns;
            final int adapterViewTypeStart = mAdapter == null ? 0 : mAdapter.getViewTypeCount() - 1;
            int type = AdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER;
            if (mCachePlaceHoldView) {

                if (position < numHeadersAndPlaceholders) {
                    if (position == 0) {
                        if (mCacheFirstHeaderView) {
                            type = adapterViewTypeStart + mHeaderViewInfos.size() + mFooterViewInfos.size() + 1 + 1;
                        }
                    }
                    if (position % mNumColumns != 0) {
                        type = adapterViewTypeStart + (position / mNumColumns + 1);
                    }
                }
            }

            final int adjPosition = position - numHeadersAndPlaceholders;
            int adapterCount = 0;
            if (mAdapter != null) {
                adapterCount = getAdapterAndPlaceHolderCount();
                if (adjPosition >= 0 && adjPosition < adapterCount) {
                    if (adjPosition < mAdapter.getCount()) {
                        type = mAdapter.getItemViewType(adjPosition);
                    } else {
                        if (mCachePlaceHoldView) {
                            type = adapterViewTypeStart + mHeaderViewInfos.size() + 1;
                        }
                    }
                }
            }

            if (mCachePlaceHoldView) {

                final int footerPosition = adjPosition - adapterCount;
                if (footerPosition >= 0 && footerPosition < getCount() && (footerPosition % mNumColumns) != 0) {
                    type = adapterViewTypeStart + mHeaderViewInfos.size() + 1 + (footerPosition / mNumColumns + 1);
                }
            }
            return type;
        }


        @Override
        public int getViewTypeCount() {
            int count = mAdapter == null ? 1 : mAdapter.getViewTypeCount();
            if (mCachePlaceHoldView) {
                int offset = mHeaderViewInfos.size() + 1 + mFooterViewInfos.size();
                if (mCacheFirstHeaderView) {
                    offset += 1;
                }
                count += offset;
            }
            return count;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            mDataSetObservable.registerObserver(observer);
            if (mAdapter != null) {
                mAdapter.registerDataSetObserver(observer);
            }
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            mDataSetObservable.unregisterObserver(observer);
            if (mAdapter != null) {
                mAdapter.unregisterDataSetObserver(observer);
            }
        }

        @Override
        public Filter getFilter() {
            if (mIsFilterable) {
                return ((Filterable) mAdapter).getFilter();
            }
            return null;
        }

        @Override
        public ListAdapter getWrappedAdapter() {
            return mAdapter;
        }

        public void notifyDataSetChanged() {
            mDataSetObservable.notifyChanged();
        }
    }


    public void setSelectionFromTop(int position, int y) {
        if (getAdapter() == null) {
            return;
        }

        setSelection(position);
    }

}

package com.blingbling.flowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by BlingBling on 2017-03-14.
 */

public class FlowLayout extends ViewGroup {

    private boolean mEqually;
    private int mHorizontalSpacing;
    private int mVerticalSpacing;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
        mEqually = a.getBoolean(R.styleable.FlowLayout_flow_equally, true);
        mHorizontalSpacing = a.getDimensionPixelOffset(R.styleable.FlowLayout_flow_horizontalSpacing, dp2px(4));
        mVerticalSpacing = a.getDimensionPixelOffset(R.styleable.FlowLayout_flow_verticalSpacing, dp2px(4));
        a.recycle();
    }

    private int dp2px(int dp) {
        return (int) (getContext().getResources().getDisplayMetrics().density * dp);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        final int layoutWidth = widthSize - getPaddingLeft() - getPaddingRight();
        if (layoutWidth <= 0) {
            return;
        }

        int width = getPaddingLeft() + getPaddingRight();
        int height = getPaddingTop() + getPaddingBottom();
        int lineWidth = 0;
        int lineHeight = 0;

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int[] wh = null;
        int childWidth, childHeight;
        int childWidthMeasureSpec = 0, childHeightMeasureSpec = 0;
        for (int i = 0, count = getChildCount(); i < count; i++) {
            final View view = getChildAt(i);
            if (view.getVisibility() == GONE) {
                continue;
            }
            if (mEqually) {
                if (wh == null) {
                    //重新计算，平均分配
                    wh = getMaxWH();
                    int oneRowItemCount = (layoutWidth + mHorizontalSpacing) / (mHorizontalSpacing + wh[0]);
                    int newWidth = (layoutWidth - (oneRowItemCount - 1) * mHorizontalSpacing) / oneRowItemCount;
                    wh[0] = newWidth;

                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(wh[0], MeasureSpec.EXACTLY);
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(wh[1], MeasureSpec.EXACTLY);
                }
                childWidth = wh[0];
                childHeight = wh[1];
                getChildAt(i).measure(childWidthMeasureSpec, childHeightMeasureSpec);
            } else {
                childWidth = view.getMeasuredWidth();
                childHeight = view.getMeasuredHeight();
            }
            if (i == 0) {
                lineWidth = getPaddingLeft() + getPaddingRight() + childWidth;
                lineHeight = childHeight;
            } else {
                //判断是否需要换行
                if (lineWidth + mHorizontalSpacing + childWidth > widthSize) {
                    width = Math.max(lineWidth, width);// 取最大的宽度
                    lineWidth = getPaddingLeft() + getPaddingRight() + childWidth; // 重新开启新行，开始记录
                    // 叠加当前高度，
                    height += mVerticalSpacing + lineHeight;
                    // 开启记录下一行的高度
                    lineHeight = childHeight;
                } else {
                    lineWidth = lineWidth + mHorizontalSpacing + childWidth;
                    lineHeight = Math.max(lineHeight, childHeight);
                }
            }
            // 如果是最后一个，则将当前记录的最大宽度和当前lineWidth做比较
            if (i == count - 1) {
                width = Math.max(width, lineWidth);
                height += lineHeight;
            }
        }
        setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? widthSize : width,
                heightMode == MeasureSpec.EXACTLY ? heightSize : height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int layoutWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        if (layoutWidth <= 0) {
            return;
        }
        int childWidth, childHeight;

        int left = getPaddingLeft();
        int top = getPaddingTop();
        final int[] wh = getMaxWH();
        int lineHeight = 0;
        for (int i = 0, count = getChildCount(); i < count; i++) {
            final View view = getChildAt(i);
            if (view.getVisibility() == GONE) {
                continue;
            }
            if (mEqually) {
                childWidth = wh[0];
                childHeight = wh[1];
            } else {
                childWidth = view.getMeasuredWidth();
                childHeight = view.getMeasuredHeight();
            }
            if (i == 0) {
                view.layout(left, top, left + childWidth, top + childHeight);
                lineHeight = childHeight;
            } else {
                //判断是否需要换行
                if (left + mHorizontalSpacing + childWidth > layoutWidth + getPaddingLeft()) {
                    left = getPaddingLeft();
                    top = top + mVerticalSpacing + lineHeight;
                    lineHeight = childHeight;
                } else {
                    left = left + mHorizontalSpacing;
                    lineHeight = Math.max(lineHeight, childHeight);
                }
                view.layout(left, top, left + childWidth, top + childHeight);
            }
            left += childWidth;
        }
    }

    private int[] getMaxWH() {
        int maxWidth = 0;
        int maxHeight = 0;
        for (int i = 0, count = getChildCount(); i < count; i++) {
            final View view = getChildAt(i);
            if (view.getVisibility() == GONE) {
                continue;
            }
            maxWidth = Math.max(maxWidth, view.getMeasuredWidth());
            maxHeight = Math.max(maxHeight, view.getMeasuredHeight());
        }
        return new int[]{maxWidth, maxHeight};
    }
}

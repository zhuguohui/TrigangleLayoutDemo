package com.zgh.triganglelayoutdemo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;


import com.zgh.triganglelayoutdemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 三角形Layout（可通过设置MinLineNumber变成梯形）
 * Created by zhuguohui on 2017/9/28.
 */

public class TriangleLayout extends ViewGroup {
    public static final int AUTO_MAX = -1;
    //自动计算padding
    public static final int AUTO_PADDING = -1;
    //一行最少的个数
    int mMinLineNumber = 1;
    //item 间距 (上下左右)
    int mItemWidthPadding = 0;
    int mItemHeightPadding = 0;
    boolean autoWidthPadding = true;
    boolean autoHeightPadding = true;
    //期望最大行item的个数
    int mWantMaxLineItemSize = 10;
    //实际最大行item的个数
    int mRealMaxLineItemSize = 0;
    //行数
    int mLineSize = 0;
    private List<LineInfo> mLines = new ArrayList<>();
    int mItemWidth;
    int mItemHeight;
    //每两行相差的个数
    int mStep = 2;

    boolean isRegularTriangle = true;

    public TriangleLayout(Context context) {
        this(context, null);
    }

    public TriangleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TriangleLayout);
        TypedValue widthPaddingValue = array.peekValue(R.styleable.TriangleLayout_rl_item_width_padding);
        if (widthPaddingValue != null) {
            if (widthPaddingValue.type == TypedValue.TYPE_DIMENSION) {
                mItemWidthPadding = array.getDimensionPixelSize(R.styleable.TriangleLayout_rl_item_width_padding, 0);
                if (mItemWidthPadding < 0) {
                    throw new IllegalArgumentException("ItemWidthPadding must  be a positive number");
                }
                autoWidthPadding = false;
            } else {
                autoWidthPadding = true;
                mItemWidthPadding = 0;
            }
        }
        TypedValue heightPaddingValue = array.peekValue(R.styleable.TriangleLayout_rl_item_height_padding);
        if (heightPaddingValue != null) {
            if (heightPaddingValue.type == TypedValue.TYPE_DIMENSION) {
                mItemHeightPadding = array.getDimensionPixelSize(R.styleable.TriangleLayout_rl_item_height_padding, 0);
                if (mItemHeightPadding < 0) {
                    throw new IllegalArgumentException("ItemHeightPadding must  be a positive number");
                }
                autoHeightPadding = false;
            } else {
                autoHeightPadding = true;
                mItemHeightPadding = 0;
            }
        }

        //解析step
        mStep = array.getInteger(R.styleable.TriangleLayout_rl_step, 1);
        if (mStep < 0) {
            throw new IllegalArgumentException("step must  be a positive number");
        }

        //解析style
        int style = array.getInteger(R.styleable.TriangleLayout_rl_style, 0);
        if (style != 0) {
            isRegularTriangle = false;
        }
        //解析maxLineItemSize
        mWantMaxLineItemSize = array.getInteger(R.styleable.TriangleLayout_rl_max_line_item_size, AUTO_MAX);

        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //计算一共的行数
        calculateLineSize();
        if (getChildCount() == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //遍历多有View获取实际的最大值
        int childWidthMeasureSpec = widthMeasureSpec;
        int childHeightMeasureSpec = heightMeasureSpec;
        if (widthMode != MeasureSpec.UNSPECIFIED) {
            //计算一个item最大的宽度
            int itemMaxIdealWidth = 0;
            if (autoWidthPadding) {
                //先不考虑padding，后面计算
                itemMaxIdealWidth = widthSize / mRealMaxLineItemSize;
            } else {
                itemMaxIdealWidth = (widthSize - (mRealMaxLineItemSize + 1) * mItemWidthPadding) / mRealMaxLineItemSize;
            }
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(itemMaxIdealWidth, MeasureSpec.AT_MOST);
        }
        if (heightMode != MeasureSpec.UNSPECIFIED) {
            int itemMaxIdealHeight = 0;
            if (autoHeightPadding) {
                //先不考虑padding，后面计算
                itemMaxIdealHeight = heightSize / mLineSize;
            } else {
                itemMaxIdealHeight = (heightSize - (mLineSize + 1) * mItemHeightPadding) / mLineSize;
            }
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(itemMaxIdealHeight, MeasureSpec.AT_MOST);

        }
        int realChildMaxWidth = 0;
        int realChildMaxHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).measure(childWidthMeasureSpec, childHeightMeasureSpec);
            int childWidth = getChildAt(i).getMeasuredWidth();
            int childHeight = getChildAt(i).getMeasuredHeight();
            if (childWidth > realChildMaxWidth) {
                realChildMaxWidth = childWidth;
            }
            if (childHeight > realChildMaxHeight) {
                realChildMaxHeight = childHeight;
            }
        }
        mItemWidth = realChildMaxWidth;
        mItemHeight = realChildMaxHeight;
        if (autoWidthPadding) {
            //确定最终的padding;
            mItemWidthPadding = (widthSize - mRealMaxLineItemSize * mItemWidth) / (mRealMaxLineItemSize + 1);
        }

        if (autoHeightPadding) {
            mItemHeightPadding = (heightSize - mLineSize * mItemHeight) / (mLineSize + 1);
        }

        //根据最大值设置
        int mWidth = mRealMaxLineItemSize * mItemWidth + (mRealMaxLineItemSize + 1) * mItemWidthPadding;
        int mHeight = mLineSize * (mItemHeight + mItemHeightPadding) + mItemHeightPadding;

        setMeasuredDimension(mWidth, mHeight);

    }

    /**
     * 计算一共有多少行
     */
    private void calculateLineSize() {
        int count = getChildCount();
        mLines.clear();
        if (count == 0) {
            mLineSize = 0;
            return;
        } else {
            //标识是否从多到少进行计算
            boolean MaxToMin = false;
            if (mWantMaxLineItemSize != AUTO_MAX) {
                MaxToMin = true;
                mRealMaxLineItemSize = mWantMaxLineItemSize;
            }
            int lineNumber = MaxToMin ? mWantMaxLineItemSize : mMinLineNumber;//当前行的个数
            int sum = lineNumber;//所以行的个数
            int lineSize = 1;
            LineInfo firstLine = new LineInfo();
            firstLine.lineNumber = 1;
            firstLine.begin = 0;
            firstLine.end = lineNumber - 1;
            mLines.add(firstLine);
            while (sum < count) {
                LineInfo lineInfo = new LineInfo();
                if (MaxToMin) {
                    lineNumber -= mStep;
                } else {
                    lineNumber += mStep;
                }
                lineInfo.begin = sum;
                sum += lineNumber;
                lineInfo.end = sum - 1;
                lineSize++;
                lineInfo.lineNumber = lineSize;
                mLines.add(lineInfo);
            }
            mLineSize = lineSize;
            if (!MaxToMin) {
                //保存实际的最大大小
                mRealMaxLineItemSize = lineNumber;
                //因为draw相关的函数是在MaxToMin模式下完成的
                //所以在MinToMax的时候需要将行号倒置
                for (int i = 1; i <= mLineSize; i++) {
                    mLines.get(mLines.size() - i).lineNumber = i;
                }
            }
            //对最后一行的结束位置进行调整，因为可能超出边界
            mLines.get(mLines.size() - 1).end = count - 1;
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (isRegularTriangle) {
            layoutDownToTop(l, t, r, b);
        } else {
            layoutTopToDown(l, t, r, b);
        }
    }

    /**
     * 自上而下的布局
     *
     * @param l
     * @param t
     * @param r
     * @param b
     */
    private void layoutTopToDown(int l, int t, int r, int b) {

        for (LineInfo info : mLines) {
            info.layoutChildTopToDown(l, t, r, b);
        }
    }

    private void layoutDownToTop(int l, int t, int r, int b) {
        for (LineInfo info : mLines) {
            info.layoutChildDownToTop(l, t, r, b);
        }
    }

    /**
     * 保存每一行的信息
     */
    private class LineInfo {
        //所在行数 从1开始
        int lineNumber;
        //负责布局的孩子在child中的索引，前后闭区间[begin,end]
        int begin = -1, end = -1;

        public void layoutChildTopToDown(int l, int t, int r, int b) {
            int mLeft = l + mItemWidthPadding + (lineNumber - 1) * (mItemWidth + mItemWidthPadding) * mStep / 2;
            int mTop = t + (lineNumber - 1) * (mItemHeightPadding + mItemHeight) + mItemHeightPadding;

            if (begin < 0 || end < 0) {
                return;
            }
            int index = 0;
            for (int i = begin; i <= end; i++) {
                View view = getChildAt(i);
                int height = view.getMeasuredHeight();
                int width = view.getMeasuredWidth();
                //计算中间点根据中间点确定left；
                int middleWidth = mLeft + index * (mItemWidthPadding + mItemWidth) + mItemWidth / 2;
                int middleHeight = mTop + mItemHeight / 2;
                int cLeft = middleWidth - width / 2;
                int cTop = middleHeight - height / 2;
                int cRight = cLeft + width;
                int cDown = cTop + height;
                view.layout(cLeft, cTop, cRight, cDown);
                index++;

            }
        }

        public void layoutChildDownToTop(int l, int t, int r, int b) {
            int mLeft = l + mItemWidthPadding + (lineNumber - 1) * ((mItemWidth + mItemWidthPadding) * mStep / 2);
            int mTop = t + (mLineSize - lineNumber) * (mItemHeightPadding + mItemHeight) + mItemHeightPadding;

            if (begin < 0 || end < 0) {
                return;
            }
            int index = 0;
            for (int i = begin; i <= end; i++) {
                View view = getChildAt(i);
                int height = view.getMeasuredHeight();
                int width = view.getMeasuredWidth();
                //计算中间点根据中间点确定left；
                int middleWidth = mLeft + index * (mItemWidthPadding + mItemWidth) + mItemWidth / 2;
                int middleHeight = mTop + mItemHeight / 2;
                int cLeft = middleWidth - width / 2;
                int cTop = middleHeight - height / 2;
                int cRight = cLeft + width;
                int cDown = cTop + height;
                view.layout(cLeft, cTop, cRight, cDown);
                index++;

            }
        }
    }

    public boolean isRegularTriangle() {
        return isRegularTriangle;
    }

    public void setRegularTriangle(boolean regularTriangle) {
        isRegularTriangle = regularTriangle;
        requestLayout();
    }

    public void setStep(int mStep) {
        if (mStep != this.mStep) {
            if (mStep < 0) {
                mStep = 0;
            }
            this.mStep = mStep;
            requestLayout();
        }
    }

    public int getStep() {
        return mStep;
    }

    public void setMaxLineItemSize(int mWantMaxLineItemSize) {
        if (this.mWantMaxLineItemSize != mWantMaxLineItemSize) {
            if (mWantMaxLineItemSize < 0 && mWantMaxLineItemSize != AUTO_MAX)
                mWantMaxLineItemSize = 0;
            this.mWantMaxLineItemSize = mWantMaxLineItemSize;
            requestLayout();
        }
    }

    public int getMaxLineItemSize() {
        return mWantMaxLineItemSize;
    }
}

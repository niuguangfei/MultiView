package cn.alien95.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import cn.alien95.view.adapter.Adapter;
import cn.alien95.view.ui.ViewImageActivity;
import cn.alien95.view.util.MessageNotify;
import cn.alien95.view.util.Util;


/**
 * Created by linlongxin on 2015/12/28.
 */
public class MultiView extends ViewGroup {

    private static final String TAG = "MultiView";
    public static boolean isDataFromAdapter = false;
    private int width, height;
    private int childWidth, childHeight;
    private int divideSpace;
    private int placeholder;
    private Adapter adapter;
    private List<String> data;
    private int childCount;

    public MultiView(Context context) {
        super(context, null);
    }

    public MultiView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Util.init(context);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MultiView);
        divideSpace = (int) typedArray.getDimension(R.styleable.MultiView_divideSpace, Util.dip2px(4));
        placeholder = typedArray.getResourceId(R.styleable.MultiView_placeholder, -1);
        typedArray.recycle();
    }

    //测量自己的大小
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG, "onMeasure");

        childCount = getChildCount();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST) {
            Log.i(TAG, "fuck--widthMode");

        }
        if (heightMode == MeasureSpec.AT_MOST) {
            Log.i(TAG, "fuck--heightMode");
        }

        if (childCount == 1) {
            childWidth = width - divideSpace * 2;
            height = width;
        } else if (childCount == 2) {
            childWidth = (width - divideSpace * 3) / 2;
            height = childWidth + divideSpace * 2;
        } else if (childCount == 4) {
            childWidth = (width - divideSpace * 3) / 2;
            height = childWidth * 2 + divideSpace * 3;
        } else {
            /**
             * 九宫格模式
             */
            childWidth = (width - divideSpace * 4) / 3;
            if (childCount < 9) {
                if (childCount % 3 == 0) {
                    height = childWidth * childCount / 3 + divideSpace * (childCount / 3 + 1);
                } else
                    height = childWidth * (childCount / 3 + 1) + divideSpace * (childCount / 3 + 2);
            } else {
                height = width;
            }
        }

        childHeight = childWidth;

        /**
         * 全所有的child都用AT_MOST模式，而child的width和height仅仅只是建议
         */
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
        measureChildren(childWidthMeasureSpec, childHeightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.i(TAG, "onLayout");

//        childWidth = getChildAt(0).getMeasuredWidth();
//        childHeight = getChildAt(0).getMeasuredHeight();

        if (childCount == 1) {
            getChildAt(0).layout(divideSpace, divideSpace, childWidth + divideSpace, childWidth + divideSpace);
        } else if (childCount == 2) {
            getChildAt(0).layout(divideSpace, divideSpace, (childWidth + divideSpace), (childWidth + divideSpace));
            getChildAt(1).layout((childWidth + divideSpace * 2), divideSpace, childWidth * 2 + divideSpace * 2, (childWidth + divideSpace));
        } else if (childCount == 4) {
            for (int i = 0; i < 4; i++) {
                getChildAt(i).layout(divideSpace * (i % 2 + 1) + childWidth * (i % 2), i / 2 * childWidth + divideSpace * (i / 2 + 1),
                        divideSpace * (i % 2 + 1) + childWidth * (i % 2 + 1), divideSpace * (i / 2 + 1) + (i / 2 + 1) * childWidth);
            }
        } else {
            if (childCount <= 9) {
                for (int i = 0; i < childCount; i++) {
                    getChildAt(i).layout(divideSpace * (i % 3 + 1) + childWidth * (i % 3), i / 3 * childWidth + divideSpace * (i / 3 + 1),
                            divideSpace * (i % 3 + 1) + childWidth * (i % 3 + 1), divideSpace * (i / 3 + 1) + (i / 3 + 1) * childWidth);
                }
            } else {
                getChildAt(9).layout(divideSpace * 3 + childWidth * 2, 2 * childWidth + divideSpace * 3,
                        divideSpace * 3 + childWidth * 3, divideSpace * 3 + 3 * childWidth);
            }
        }
    }

    /**
     * 设置adapter，同时设置注册MessageNotify
     *
     * @param adapter
     */
    public void setAdapter(Adapter adapter) {
        isDataFromAdapter = true;
        this.adapter = adapter;
        addViews();
        Method method = null;
        try {
            method = MultiView.class.getMethod("addViews");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        MessageNotify.getInstance().registerEvent(this, method);
    }

    /**
     * 添加adapter中所有的view,这里必须是public，因为抵用了MessageNotify.getInstance().registerEvent()，不然其他类不能调用
     */
    public void addViews() {
        if (isDataFromAdapter) {
            if (adapter.getCount() > 9) {
                for (int i = 0; i < 9; i++) {
                    addView(adapter.getView(this, i));
                }
                addOverNumView(9);
            } else
                for (int i = 0; i < adapter.getCount(); i++) {
                    addView(adapter.getView(this, i));
                }
        }
    }

    /**
     * 同上
     *
     * @param data
     */
    public void setImages(List<String> data) {
        isDataFromAdapter = false;
        this.data = data;
        if (data.size() > 9) {
            for (int i = 0; i < 9; i++) {  //前面8个item
                addView(getImageView(data.get(i), i));
            }
            addOverNumView(9);  //第9的个item
        } else {
            for (int i = 0; i < data.size(); i++) {
                addView(getImageView(data.get(i), i));
            }
        }
    }

    /**
     * 当数据是死数据时：推荐使用此方法
     *
     * @param data 数据集合
     */
    public void setImages(String[] data) {
        setImages(Arrays.asList(data));
    }

    /**
     * 设置最后一个view
     */
    public void addOverNumView(int position) {

        TextView textView = new TextView(getContext());
        textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        textView.setTextSize(24);
        textView.setTextColor(Color.parseColor("#ffffff"));
        textView.setBackgroundColor(Color.parseColor("#33000000"));
        textView.setGravity(Gravity.CENTER);
        if (isDataFromAdapter) {
            textView.setText("+" + (adapter.getCount() - 9));
        } else
            textView.setText("+" + (data.size() - 9));

        addView(textView, position);
        Log.i(TAG, "添加最后一个view");
    }

    /**
     * 获取一个ImageView
     *
     * @param url
     * @return
     */
    public ImageView getImageView(String url, final int position) {
        ImageView img = new ImageView(getContext());
        img.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (placeholder != -1) {
            Glide.with(getContext())
                    .load(url)
                    .placeholder(placeholder)
                    .into(img);
        } else {
            Glide.with(getContext())
                    .load(url)
                    .into(img);
        }

        img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ViewImageActivity.class);
                intent.putExtra(ViewImageActivity.IMAGE_NUM, position);
                intent.putExtra(ViewImageActivity.IMAGES_DATA_LIST, (Serializable) data);
                getContext().startActivity(intent);
            }
        });
        return img;
    }

}

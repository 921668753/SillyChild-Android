package com.sillykid.app.adapter.mine.myorder.orderevaluation;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.common.cklibrary.utils.MathUtil;
import com.kymjs.common.Log;
import com.kymjs.common.StringUtils;
import com.lzy.imagepicker.bean.ImageItem;
import com.sillykid.app.R;
import com.sillykid.app.adapter.ImagePickerAdapter;
import com.sillykid.app.constant.NumericConstants;
import com.sillykid.app.entity.mine.myorder.goodorder.orderevaluation.PublishedeEvaluationBean.DataBean.CommentVoBean.MemberCommentExtsBean;
import com.sillykid.app.utils.GlideImageLoader;

import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.androidcommon.adapter.BGAAdapterViewAdapter;
import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;

/**
 * 发表评价 适配器
 * https://github.com/WKaKa/Assess/tree/master/Assess/app
 * Created by Admin on 2017/8/15.
 */

public class PublishedeEvaluationViewAdapter extends BGAAdapterViewAdapter<MemberCommentExtsBean> {


    //用于退出 Activity,避免 Countdown，造成资源浪费。
    private SparseArray<ImagePickerAdapter> imagePickerAdapterCounters;
    private SparseArray<List<ImageItem>> selImageListCounters;

    private OnStatusListener onStatusListener;

    public PublishedeEvaluationViewAdapter(Context context) {
        super(context, R.layout.item_publishedeevaluation);
        this.imagePickerAdapterCounters = new SparseArray<>();
        this.selImageListCounters = new SparseArray<>();
    }

    @Override
    protected void setItemChildListener(BGAViewHolderHelper helper) {
        super.setItemChildListener(helper);
    }

    @Override
    public void fillData(BGAViewHolderHelper viewHolderHelper, int position, MemberCommentExtsBean listBean) {
        GlideImageLoader.glideOrdinaryLoader(mContext, listBean.getImage(), (ImageView) viewHolderHelper.getView(R.id.img_good), R.mipmap.placeholderfigure1);
        viewHolderHelper.setText(R.id.tv_goodName, listBean.getName());
        viewHolderHelper.setText(R.id.tv_goodDescribe, listBean.getSpecs());
        viewHolderHelper.setText(R.id.tv_money, mContext.getString(R.string.renminbi) + MathUtil.keepTwo(StringUtils.toDouble(listBean.getPrice())));
        EditText et_goodsSatisfactory = (EditText) viewHolderHelper.getView(R.id.et_goodsSatisfactory);
        et_goodsSatisfactory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                listBean.setContent(editable + "");
            }
        });
        RecyclerView recyclerView = (RecyclerView) viewHolderHelper.getView(R.id.recyclerView);
        List<ImageItem> selImageList = new ArrayList<>();
        ImagePickerAdapter adapter = new ImagePickerAdapter(mContext, selImageList, NumericConstants.MAXPICTURE, R.mipmap.feedback_add_pictures);
        adapter.setOnItemClickListener(new ImagePickerAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                onStatusListener.onSetStatusListener(view, position);
            }
        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 5);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        selImageListCounters.put(recyclerView.hashCode(), selImageList);
        imagePickerAdapterCounters.put(recyclerView.hashCode(), adapter);
    }

    public void setOnStatusListener(OnStatusListener onStatusListener) {
        this.onStatusListener = onStatusListener;
    }

    public interface OnStatusListener {
        void onSetStatusListener(View view, int position);
        //  void onDeleteListener(int pos, int tagPos);
    }


//    @Override
//    public void onItemClick(View view, int position) {
//        switch (position) {
//            case NumericConstants.IMAGE_ITEM_ADD:
//                //打开选择,本次允许选择的数量
//                Intent intent1 = new Intent(mContext, ImageGridActivity.class);
//                /* 如果需要进入选择的时候显示已经选中的图片，
//                 * 详情请查看ImagePickerActivity
//                 * */
////                intent1.putExtra(ImageGridActivity.EXTRAS_IMAGES,images);
//                ((Activity) mContext).startActivityForResult(intent1, NumericConstants.REQUEST_CODE_SELECT);
//                break;
//            default:
//                ImagePickerAdapter adapter = imagePickerAdapterCounters.get(((RecyclerView) view.getParent().getParent()).getRootView().hashCode());
//                List<ImageItem> selImageList = selImageListCounters.get(((RecyclerView) view.getParent().getParent()).getRootView().hashCode());
//                if (view.getId() == R.id.iv_delete) {
//                    if (selImageList != null && selImageList.size() > position) {
//                        selImageList.remove(position);
//                        adapter.setImages(selImageList);
//                    }
//                } else {
//                    //打开预览
//                    Intent intentPreview = new Intent(mContext, ImagePreviewDelActivity.class);
//                    intentPreview.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, (ArrayList<ImageItem>) adapter.getImages());
//                    intentPreview.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);
//                    intentPreview.putExtra(ImagePicker.EXTRA_FROM_ITEMS, true);
//                    ((Activity) mContext).startActivityForResult(intentPreview, NumericConstants.REQUEST_CODE_PREVIEW);
//                }
//                break;
//        }
//    }


    @Override
    public void clear() {
        super.clear();
        if (imagePickerAdapterCounters != null && selImageListCounters == null) {
            Log.e("TAG", "size :  " + imagePickerAdapterCounters.size());
            for (int i = 0, length = imagePickerAdapterCounters.size(); i < length; i++) {
                ImagePickerAdapter cdt = imagePickerAdapterCounters.get(imagePickerAdapterCounters.keyAt(i));
                if (cdt != null) {
                    cdt.setImages(null);
                    cdt = null;
                }
            }
        }
        if (imagePickerAdapterCounters == null && selImageListCounters != null) {
            Log.e("TAG", "size :  " + selImageListCounters.size());
            for (int i = 0, length = selImageListCounters.size(); i < length; i++) {
                List<ImageItem> cdt = selImageListCounters.get(selImageListCounters.keyAt(i));
                if (cdt != null) {
                    cdt.clear();
                    cdt = null;
                }
            }
        }
        if (imagePickerAdapterCounters != null && selImageListCounters != null) {
            Log.e("TAG", "size :  " + imagePickerAdapterCounters.size());
            for (int i = 0, length = imagePickerAdapterCounters.size(); i < length; i++) {
                ImagePickerAdapter cdt = imagePickerAdapterCounters.get(imagePickerAdapterCounters.keyAt(i));
                if (cdt != null) {
                    cdt.setImages(null);
                    cdt = null;
                }
            }
            for (int i = 0, length = selImageListCounters.size(); i < length; i++) {
                List<ImageItem> cdt = selImageListCounters.get(selImageListCounters.keyAt(i));
                if (cdt != null) {
                    cdt.clear();
                    cdt = null;
                }
            }
        }

    }


}
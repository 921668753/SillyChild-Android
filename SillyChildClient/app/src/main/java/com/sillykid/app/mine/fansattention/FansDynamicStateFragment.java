package com.sillykid.app.mine.fansattention;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.cklibrary.common.BaseFragment;
import com.common.cklibrary.common.BindView;
import com.common.cklibrary.common.StringConstants;
import com.common.cklibrary.common.ViewInject;
import com.common.cklibrary.utils.JsonUtil;
import com.kymjs.common.PreferenceHelper;
import com.sillykid.app.R;
import com.sillykid.app.adapter.MasonryAdapter;
import com.sillykid.app.constant.NumericConstants;
import com.sillykid.app.custominterfaces.FragmentJumpBetween;
import com.sillykid.app.dialog.PublicPromptDialog;
import com.sillykid.app.entity.DynamicStateBean;
import com.sillykid.app.entity.DynamicStateBean.ResultBean.ListBean;
import com.sillykid.app.entity.main.UserInfoBean;
import com.sillykid.app.homepage.dynamics.DynamicsDetailsActivity;
import com.sillykid.app.loginregister.LoginActivity;
import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**我的发布中的动态
 * Created by Administrator on 2017/9/2.
 */

public class FansDynamicStateFragment extends BaseFragment implements FansAttentionContract.View,MasonryAdapter.MasonryItemOnClickListener,PullLoadMoreRecyclerView.PullLoadMoreListener{
    private FansInfoActivity aty;

    @BindView(id = R.id.pullLoadMoreRecyclerView)
    private PullLoadMoreRecyclerView pullLoadMoreRecyclerView;

    /**
     * 错误提示页
     */
    @BindView(id = R.id.ll_commonError, click = true)
    private LinearLayout ll_commonError;
    @BindView(id = R.id.img_err)
    private ImageView img_err;
    @BindView(id = R.id.tv_hintText)
    private TextView tv_hintText;

    private List<ListBean> listbean;
    private MasonryAdapter adapter;
    private int pagenum;
    private int totalPages=1;
    private boolean isloadmore;
    private DynamicStateBean dsBean;
    private boolean isresume;
    private Intent jumpintent;
    private PublicPromptDialog publicPromptDialog;
    private int currentpostion=0;//当前操作的位置
    private UserInfoBean userInfoBean;
    private FragmentJumpBetween fragmentJumpBetween;

    @Override
    protected View inflaterView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        aty = (FansInfoActivity) getActivity();
        return inflater.inflate(R.layout.fragment_strate,container, false);
    }

    @Override
    protected void initData() {
        super.initData();
        mPresenter=new FansAttentionPresenter(this);
        fragmentJumpBetween=new FragmentJumpBetween() {
            @Override
            public void fragmentPosition() {
                pullLoadMoreRecyclerView.refresh();
            }
            @Override
            public void doAttention() {
                showLoadingDialog(getString(R.string.submissionLoad));
                ((FansAttentionPresenter)mPresenter).attentionOrNo(aty.getFansuserid(),0);
            }

            @Override
            public void doCancleAttention() {
                showLoadingDialog(getString(R.string.submissionLoad));
                ((FansAttentionPresenter)mPresenter).attentionOrNo(aty.getFansuserid(),1);
            }
        };
        pullLoadMoreRecyclerView.setStaggeredGridLayout(2);
        listbean=new ArrayList<>();
        adapter=new MasonryAdapter(aty,listbean,this,false);
        pullLoadMoreRecyclerView.setAdapter(adapter);
        //设置item之间的间隔
        pullLoadMoreRecyclerView.addItemDecoration(aty.getSpacesItemDecoration());
        pullLoadMoreRecyclerView.setOnPullLoadMoreListener(this);
        pullLoadMoreRecyclerView.setFooterViewText(getString(R.string.load_more_text));
        pullLoadMoreRecyclerView.setFooterViewTextColor(R.color.titletextcolors);
        pullLoadMoreRecyclerView.setFooterViewBackgroundColor(R.color.background);
    }

    @Override
    protected void initWidget(View parentView) {
        super.initWidget(parentView);
        refreshListBean(null);
    }

    @Override
    protected void widgetClick(View v) {
        super.widgetClick(v);
        switch (v.getId()){
            case R.id.ll_commonError:
                if (tv_hintText.getText().toString().equals(getString(R.string.login1))) {
                    PreferenceHelper.write(aty, StringConstants.FILENAME, "id", 0);
                    PreferenceHelper.write(aty, StringConstants.FILENAME, "accessToken", "");
                    PreferenceHelper.write(aty, StringConstants.FILENAME, "refreshToken", "");
                    PreferenceHelper.write(aty, StringConstants.FILENAME, "expireTime", "0");
                    PreferenceHelper.write(aty, StringConstants.FILENAME, "timeBefore", "0");
                    //   PreferenceHelper.write(aty, StringConstants.FILENAME, "refreshName", "getCompanyGuideMessageFragment");
                    Intent intent = new Intent(aty, LoginActivity.class);
                    aty.showActivity(aty, intent);
                    break;
                }
                //  ViewInject.toast("onBGARefreshLayoutBeginRefreshing");
                pullLoadMoreRecyclerView.refresh();
                break;
        }
    }

    @Override
    public void setPresenter(FansAttentionContract.Presenter presenter) {
        mPresenter=presenter;
    }

    @Override
    public void getSuccess(String success, int flag) {
        switch (flag) {
            case 1:
                ViewInject.toast(getString(R.string.attentionSuccess));
                pullLoadMoreRecyclerView.refresh();
                break;
            case 2:
                ViewInject.toast(getString(R.string.focusSuccess));
                pullLoadMoreRecyclerView.refresh();
                break;
            case 3:
                userInfoBean = (UserInfoBean) JsonUtil.getInstance().json2Obj(success, UserInfoBean.class);
                if (userInfoBean != null && userInfoBean.getData() != null) {
                    aty.initAmount(userInfoBean);
                }
                ((FansAttentionPresenter) mPresenter).getOtherInfo(aty.getFansuserid(), "1", pagenum);
                break;
            default:
                dismissLoadingDialog();
                pullLoadMoreRecyclerView.setPullLoadMoreCompleted();
                dsBean=(DynamicStateBean) JsonUtil.json2Obj(success, DynamicStateBean.class);
                if (dsBean==null){
                    refreshListBean(null);
                    ViewInject.toast(getString(R.string.otherError));
                    return;
                }
                if (dsBean.getData()==null||dsBean.getData().getList()==null||dsBean.getData().getList().size()==0){
                    if (isloadmore){
                        ViewInject.toast(getString(R.string.noMoreData));
                    }else{
                        refreshListBean(null);
                    }
                    return;
                }
                pagenum=dsBean.getData().getP();
                totalPages=dsBean.getData().getTotalPages();
                List<ListBean> datalist = dsBean.getData().getList();
                refreshListBean(datalist);
                break;
        }
    }

    @Override
    public void errorMsg(String msg, int flag) {
        if (flag==0){
            pullLoadMoreRecyclerView.setPullLoadMoreCompleted();
            pullLoadMoreRecyclerView.setVisibility(View.GONE);
            ll_commonError.setVisibility(View.VISIBLE);
            dismissLoadingDialog();
            if (isLogin(msg)) {
                PreferenceHelper.write(aty, StringConstants.FILENAME, "isRefreshMineFragment", false);
                PreferenceHelper.write(aty, StringConstants.FILENAME, "isReLogin", true);
                dismissLoadingDialog();
                tv_hintText.setText(getString(R.string.login1));
                //       aty.showActivity(aty, LoginActivity.class);
                return;
            }
            tv_hintText.setText(msg);
        }else{
            dismissLoadingDialog();
            ViewInject.toast(msg);
        }
    }

    /**
     * 刷新数据
     */
    private void refreshListBean(List<ListBean> addlist){
        if (isloadmore){
            if (pagenum==1){
                listbean.clear();
                if (addlist!=null&&addlist.size()>0){
                    listbean.addAll(addlist);
                    if (addlist.size()<NumericConstants.LOADCOUNT){
                        ViewInject.toast(getString(R.string.noMoreData));
                    }
                }
            }else{
                int listbeansize = listbean.size();
                for (int i=((pagenum-1)*NumericConstants.LOADCOUNT);i<listbeansize;i++){
                    listbean.remove(i);
                }
                if (addlist!=null&&addlist.size()>0){
                    listbean.addAll(addlist);
                    if (addlist.size()<NumericConstants.LOADCOUNT){
                        ViewInject.toast(getString(R.string.noMoreData));
                    }
                }
            }
        }else{
            listbean.clear();
            if (addlist!=null&&addlist.size()>0){
                listbean.addAll(addlist);
            }
        }
        adapter.notifyDataSetChanged();
    }


    @Override
    public void masonryOnItemClick(View view, int postion) {
//        if (postion==0){
//            aty.showActivity(aty,ReleaseDynamicStateActivity.class);
//        }else{
        if (jumpintent==null) jumpintent=new Intent(aty, DynamicsDetailsActivity.class);
        jumpintent.putExtra("act_id",listbean.get(postion).getId());
        aty.showActivity(aty,jumpintent);
//        }
    }

    @Override
    public void masonryOnLongItemClick(View view, int postion) {
//        currentpostion=postion;
//        //删除
//        if (postion!=0){
//            if (publicPromptDialog ==null){
//                publicPromptDialog =new PublicPromptDialog(aty) {
//                    @Override
//                    public void doAction() {
//                        showLoadingDialog(getString(R.string.submissionLoad));
//                        ((FansAttentionPresenter)mPresenter).doDelete(listbean.get(postion).getId(),1);
//                    }
//                };
//            }
//            publicPromptDialog.show();
//            publicPromptDialog.setContent(getString(R.string.deleteTrends));
//            publicPromptDialog.setBtnContent(getString(R.string.delete));
//        }
    }

    @Override
    public void onRefresh() {
        //刷新
        isloadmore=false;
        pagenum=NumericConstants.START_PAGE_NUMBER;
        ((FansAttentionPresenter)mPresenter).baseInfo(aty.getFansuserid());
    }

    @Override
    public void onLoadMore() {
        //加载更多
        isloadmore=true;
        if (pagenum<=totalPages){
            ((FansAttentionPresenter)mPresenter).getOtherInfo(aty.getFansuserid(),"3",pagenum+1);
        }else{
            pullLoadMoreRecyclerView.setPullLoadMoreCompleted();
            ViewInject.toast(getString(R.string.noMoreData));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isresume=true;
        if (aty.getChageIcon()==0){
            pullLoadMoreRecyclerView.refresh();
        }
    }

    public FragmentJumpBetween getFragmentJumpBetween() {
        return fragmentJumpBetween;
    }
}

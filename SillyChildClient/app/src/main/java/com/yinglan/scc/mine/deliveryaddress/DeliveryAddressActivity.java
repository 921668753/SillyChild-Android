package com.yinglan.scc.mine.deliveryaddress;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.common.cklibrary.common.BaseActivity;
import com.common.cklibrary.common.BindView;
import com.common.cklibrary.common.ViewInject;
import com.common.cklibrary.utils.ActivityTitleUtils;
import com.common.cklibrary.utils.JsonUtil;
import com.common.cklibrary.utils.RefreshLayoutUtil;
import com.yinglan.scc.R;
import com.yinglan.scc.adapter.mine.deliveryaddress.DeliveryAddressViewAdapter;
import com.yinglan.scc.entity.mine.deliveryaddress.DeliveryAddressBean;
import com.yinglan.scc.loginregister.LoginActivity;
import com.yinglan.scc.mine.deliveryaddress.dialog.DeleteAddressDialog;

import cn.bingoogolapple.androidcommon.adapter.BGAOnItemChildClickListener;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;

import static com.yinglan.scc.constant.NumericConstants.REQUEST_CODE;

/**
 * 收货地址
 * Created by Administrator on 2017/9/2.
 */

public class DeliveryAddressActivity extends BaseActivity implements DeliveryAddressContract.View, BGARefreshLayout.BGARefreshLayoutDelegate, AdapterView.OnItemClickListener, BGAOnItemChildClickListener {

    private DeliveryAddressContract.Presenter mPresenter;

    @BindView(id = R.id.mRefreshLayout)
    private BGARefreshLayout mRefreshLayout;

    @BindView(id = R.id.lv_address)
    private ListView lv_address;

    @BindView(id = R.id.tv_newaddress, click = true)
    private TextView tv_newaddress;

    private DeliveryAddressViewAdapter mAdapter;

    /**
     * 错误提示页
     */
    @BindView(id = R.id.ll_commonError)
    private LinearLayout ll_commonError;

    @BindView(id = R.id.img_err)
    private ImageView img_err;

    @BindView(id = R.id.tv_hintText)
    private TextView tv_hintText;

    @BindView(id = R.id.tv_button, click = true)
    private TextView tv_button;

    private DeleteAddressDialog deleteAddressDialog = null;
    private int positionItem = 0;


    @Override
    public void setRootView() {
        setContentView(R.layout.activity_deliveryaddress);
    }

    @Override
    public void initData() {
        super.initData();
        mPresenter = new DeliveryAddressPresenter(this);
        mAdapter = new DeliveryAddressViewAdapter(this);
        initDeleteAddressDialog();
    }

    @Override
    public void initWidget() {
        super.initWidget();
        initTitle();
        RefreshLayoutUtil.initRefreshLayout(mRefreshLayout, this, aty, false);
        lv_address.setAdapter(mAdapter);
        lv_address.setOnItemClickListener(this);
        mAdapter.setOnItemChildClickListener(this);
    }

    @Override
    public void widgetClick(View v) {
        super.widgetClick(v);
        switch (v.getId()) {
            case R.id.tv_newaddress:
                showActivity(this, AddNewAddressActivity.class);
                break;
            case R.id.tv_button:
                if (tv_button.getText().toString().contains(getString(R.string.retry))) {
                    mRefreshLayout.beginRefreshing();
                    return;
                }
                showActivity(this, LoginActivity.class);
                break;
        }
    }

    /**
     * 设置标题
     */
    public void initTitle() {
        ActivityTitleUtils.initToolbar(aty, getString(R.string.deliveryAddress), true, R.id.titlebar);
    }


    @Override
    public void setPresenter(DeliveryAddressContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void getSuccess(String success, int flag) {
        if (flag == 0) {
            mRefreshLayout.setPullDownRefreshEnable(true);
            mRefreshLayout.setVisibility(View.VISIBLE);
            ll_commonError.setVisibility(View.GONE);
            DeliveryAddressBean deliveryAddressBean = (DeliveryAddressBean) JsonUtil.json2Obj(success, DeliveryAddressBean.class);
            if (deliveryAddressBean == null || deliveryAddressBean.getData() == null || deliveryAddressBean.getData().getAddressList().size() <= 0) {
                errorMsg(getString(R.string.noAddress), 1);
                return;
            }
            mRefreshLayout.setPullDownRefreshEnable(true);
            mAdapter.clear();
            mAdapter.addNewData(deliveryAddressBean.getData().getAddressList());
            dismissLoadingDialog();
        } else if (flag == 1) {
            mAdapter.removeItem(positionItem);
        } else if (flag == 2) {
            mRefreshLayout.beginRefreshing();
        }
    }

    @Override
    public void errorMsg(String msg, int flag) {
        dismissLoadingDialog();
        //   if (flag == 0) {
        mRefreshLayout.setPullDownRefreshEnable(false);
        mRefreshLayout.setVisibility(View.GONE);
        ll_commonError.setVisibility(View.VISIBLE);
        tv_hintText.setVisibility(View.VISIBLE);
        tv_button.setVisibility(View.VISIBLE);
        if (isLogin(msg)) {
            img_err.setImageResource(R.mipmap.no_login);
            tv_hintText.setVisibility(View.GONE);
            tv_button.setText(getString(R.string.login));
            //   ViewInject.toast(getString(R.string.reloginPrompting));
            showActivity(this, LoginActivity.class);
            return;
        } else if (msg.contains(getString(R.string.checkNetwork))) {
            img_err.setImageResource(R.mipmap.no_network);
            tv_hintText.setText(msg);
            tv_button.setText(getString(R.string.retry));
        } else if (msg.contains(getString(R.string.noAddress))) {
            img_err.setImageResource(R.mipmap.no_data);
            tv_hintText.setText(msg);
            tv_button.setVisibility(View.GONE);
        } else {
            img_err.setImageResource(R.mipmap.no_data);
            tv_hintText.setText(msg);
            tv_button.setText(getString(R.string.retry));
        }
//        } else {
//            ViewInject.toast(msg);
//        }

    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        mRefreshLayout.endRefreshing();
        showLoadingDialog(getString(R.string.dataLoad));
        ((DeliveryAddressContract.Presenter) mAdapter).getAddressList();
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        //编辑地址
        Intent intent = new Intent(aty, AddNewAddressActivity.class);
        intent.putExtra("addr_id", mAdapter.getItem(position).getAddr_id());
        intent.putExtra("title", getString(R.string.editAddress));
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onItemChildClick(ViewGroup parent, View childView, int position) {
        switch (childView.getId()) {
            case R.id.ll_setdefault:
                //设置默认地址
                showLoadingDialog(getString(R.string.dataLoad));
                ((DeliveryAddressContract.Presenter) mPresenter).setDefaultAddress(mAdapter.getItem(position).getAddr_id());
                break;
            case R.id.ll_deliveryaddressedit:
                //编辑地址
                Intent intent = new Intent(aty, AddNewAddressActivity.class);
                intent.putExtra("addr_id", mAdapter.getItem(position).getAddr_id());
                intent.putExtra("title", getString(R.string.editAddress));
                startActivityForResult(intent, REQUEST_CODE);
            case R.id.ll_deliveryaddressdelete:
                if (deleteAddressDialog == null) {
                    initDeleteAddressDialog();
                }
                positionItem = position;
                if (deleteAddressDialog != null && !deleteAddressDialog.isShowing()) {
                    deleteAddressDialog.show();
                    deleteAddressDialog.setAddressId(mAdapter.getItem(position).getAddr_id());
                }
                break;

        }
    }

    /**
     * 删除地址
     */
    public void initDeleteAddressDialog() {
        deleteAddressDialog = new DeleteAddressDialog(this, 0) {
            @Override
            public void deleteAddressDo(int addressId) {
                //删除地址
                showLoadingDialog(getString(R.string.deleteLoad));
                ((DeliveryAddressContract.Presenter) mPresenter).postDeleteAddress(addressId);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            mRefreshLayout.beginRefreshing();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deleteAddressDialog != null) {
            deleteAddressDialog.cancel();
        }
        deleteAddressDialog = null;
        mAdapter.clear();
        mAdapter = null;
    }
}

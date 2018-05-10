package com.yinglan.scc.mine.mywallet.coupons;

import android.content.Intent;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.cklibrary.common.BaseActivity;
import com.common.cklibrary.common.BaseFragment;
import com.common.cklibrary.common.BindView;
import com.yinglan.scc.R;
import com.yinglan.scc.homepage.BannerDetailsActivity;

import cn.bingoogolapple.titlebar.BGATitleBar;

/**
 * 优惠券
 * Created by Administrator on 2017/9/2.
 */

public class CouponsActivity extends BaseActivity {
    private UnusedFragment unusedFragment;
    private UsedFragment usedFragment;
    private ExpiredFragment expiredFragment;
    private int chageIcon;

    @BindView(id = R.id.titlebar)
    private BGATitleBar titlebar;

    @BindView(id = R.id.ll_unuser, click = true)
    private LinearLayout ll_unuser;
    @BindView(id = R.id.tv_unuser)
    private TextView tv_unuser;
    @BindView(id = R.id.v_unuser)
    private View v_unuser;

    @BindView(id = R.id.ll_useed, click = true)
    private LinearLayout ll_useed;
    @BindView(id = R.id.tv_useed)
    private TextView tv_useed;
    @BindView(id = R.id.v_useed)
    private View v_useed;

    @BindView(id = R.id.ll_expired, click = true)
    private LinearLayout ll_expired;
    @BindView(id = R.id.tv_expired)
    private TextView tv_expired;
    @BindView(id = R.id.v_expired)
    private View v_expired;

    @BindView(id = R.id.coupons_content)
    private FrameLayout coupons_content;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_coupons);
    }

    @Override
    public void initData() {
        super.initData();
        unusedFragment = new UnusedFragment();
        usedFragment = new UsedFragment();
        expiredFragment = new ExpiredFragment();
        chageIcon = getIntent().getIntExtra("chageIcon", 0);

    }

    @Override
    public void initWidget() {
        super.initWidget();
        initTitle();
        initColors();

    }

    /**
     * 设置标题
     */
    public void initTitle() {
        titlebar.setTitleText(R.string.coupons);
        titlebar.setRightText(R.string.instructions);
        titlebar.getRightCtv().setTextColor(getResources().getColor(R.color.greenColors));
        titlebar.getRightCtv().setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        BGATitleBar.SimpleDelegate simpleDelegate = new BGATitleBar.SimpleDelegate() {
            @Override
            public void onClickLeftCtv() {
                super.onClickLeftCtv();
                aty.finish();
            }

            @Override
            public void onClickRightCtv() {
                super.onClickRightCtv();
                Intent intent = new Intent(aty, BannerDetailsActivity.class);
                intent.putExtra("title", "");
                intent.putExtra("url", "");
                showActivity(aty, intent);
            }
        };
        titlebar.setDelegate(simpleDelegate);
    }


    @Override
    public void widgetClick(View v) {
        super.widgetClick(v);
        switch (v.getId()) {
            case R.id.ll_unuser:
                cleanColors(0);
                break;
            case R.id.ll_useed:
                cleanColors(1);
                break;
            case R.id.ll_expired:
                cleanColors(2);
                break;
        }
    }

    /**
     * 清除颜色，并添加颜色
     */
    @SuppressWarnings("deprecation")
    public void cleanColors(int position) {
        if (position != chageIcon) {
            switch (chageIcon) {
                case 0:
                    tv_unuser.setTextColor(getResources().getColor(R.color.tabColors));
                    v_unuser.setBackgroundResource(android.R.color.transparent);
                    break;
                case 1:
                    tv_useed.setTextColor(getResources().getColor(R.color.tabColors));
                    v_useed.setBackgroundResource(android.R.color.transparent);
                    break;
                case 2:
                    tv_expired.setTextColor(getResources().getColor(R.color.tabColors));
                    v_expired.setBackgroundResource(android.R.color.transparent);
                    break;
            }
            chageIcon = position;
            switch (chageIcon) {
                case 0:
                    tv_unuser.setTextColor(getResources().getColor(R.color.greenColors));
                    v_unuser.setBackgroundResource(R.color.greenColors);
                    changeFragment(unusedFragment);
                    break;
                case 1:
                    tv_useed.setTextColor(getResources().getColor(R.color.greenColors));
                    v_useed.setBackgroundResource(R.color.greenColors);
                    changeFragment(usedFragment);
                    break;
                case 2:
                    tv_expired.setTextColor(getResources().getColor(R.color.greenColors));
                    v_expired.setBackgroundResource(R.color.greenColors);
                    changeFragment(expiredFragment);
                    break;
            }
        }

    }

    /**
     * 初始化颜色
     */
    public void initColors() {
        switch (chageIcon) {
            case 0:
                tv_unuser.setTextColor(getResources().getColor(R.color.greenColors));
                v_unuser.setBackgroundResource(R.color.greenColors);
                changeFragment(unusedFragment);
                break;
            case 1:
                tv_useed.setTextColor(getResources().getColor(R.color.greenColors));
                v_useed.setBackgroundResource(R.color.greenColors);
                changeFragment(usedFragment);
                break;
            case 2:
                tv_expired.setTextColor(getResources().getColor(R.color.greenColors));
                v_expired.setBackgroundResource(R.color.greenColors);
                changeFragment(expiredFragment);
                break;
        }
    }

    public void changeFragment(BaseFragment targetFragment) {
        super.changeFragment(R.id.coupons_content, targetFragment);
    }
}

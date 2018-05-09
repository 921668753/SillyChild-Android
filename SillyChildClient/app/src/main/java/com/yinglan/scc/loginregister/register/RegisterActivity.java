package com.yinglan.scc.loginregister.register;

import android.content.Intent;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.cklibrary.common.BaseActivity;
import com.common.cklibrary.common.BindView;
import com.common.cklibrary.common.KJActivityStack;
import com.common.cklibrary.common.StringConstants;
import com.common.cklibrary.common.ViewInject;
import com.common.cklibrary.utils.JsonUtil;
import com.kymjs.common.PreferenceHelper;
import com.yinglan.scc.R;
import com.yinglan.scc.entity.LoginBean;
import com.yinglan.scc.loginregister.LoginActivity;

/**
 * 手机号注册
 * Created by Admin on 2017/8/10.
 */

public class RegisterActivity extends BaseActivity implements RegisterContract.View {


    /**
     * 返回
     */
    @BindView(id = R.id.img_back, click = true)
    private ImageView img_back;


    /**
     * 倒计时内部类
     */
    private TimeCount time;


    /**
     * 手机号
     */
    @BindView(id = R.id.et_accountNumber)
    private EditText et_accountNumber;

    /**
     * 密码
     */
    @BindView(id = R.id.et_pwd)
    private EditText et_pwd;

    /**
     * 验证码
     */
    @BindView(id = R.id.et_code)
    private EditText et_code;
    /**
     * 获取验证码
     */
    @BindView(id = R.id.tv_code, click = true)
    private TextView tv_code;

    /**
     * 注册
     */
    @BindView(id = R.id.tv_registe, click = true)
    private TextView tv_registe;

    /**
     * 注册协议
     */
    @BindView(id = R.id.tv_agreement, click = true)
    private TextView tv_agreement;


    /**
     * opt	String
     * 验证码类型 reg=注册 restpwd=找回密码 login=登陆 bind=绑定手机号.
     */
    private String opt = "reg";


    @Override
    public void setRootView() {
        setContentView(R.layout.activity_register);
    }

    /**
     * 初始化数据
     */
    @Override
    public void initData() {
        super.initData();
        mPresenter = new RegisterPresenter(this);
        time = new TimeCount(60000, 1000);// 构造CountDownTimer对象
    }


    @Override
    public void widgetClick(View v) {
        super.widgetClick(v);
        switch (v.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tv_code:
                showLoadingDialog(getString(R.string.sendingLoad));
                ((RegisterContract.Presenter) mPresenter).postCode(et_accountNumber.getText().toString(), opt);
                break;
            case R.id.tv_registe:
                tv_registe.setEnabled(false);
                showLoadingDialog(getString(R.string.submissionLoad));
                if (opt.equals("reg")) {
                    ((RegisterContract.Presenter) mPresenter).postRegister(et_accountNumber.getText().toString(), et_code.getText().toString(), et_pwd.getText().toString());
                }
                break;
            case R.id.tv_agreement:
                // 注册协议
                showActivity(aty, RegistrationAgreementActivity.class);
                break;
            default:
                break;
        }


    }

    /* 定义一个倒计时的内部类 */
    @SuppressWarnings("deprecation")
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {// 计时完毕时触发
            tv_code.setText("重新验证");
            tv_code.setClickable(true);
            tv_code.setTextColor(getResources().getColor(R.color.greenColors));
            tv_code.setBackgroundResource(R.drawable.shape_code);
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示
            tv_code.setClickable(false);
            tv_code.setText(millisUntilFinished / 1000 + "秒");
            tv_code.setTextColor(getResources().getColor(R.color.hintColors));
            tv_code.setBackgroundResource(R.drawable.shape_code1);
        }
    }

    @Override
    public void getSuccess(String s, int flag) {

        tv_registe.setEnabled(true);
        if (flag == 0) {
            dismissLoadingDialog();
            ViewInject.toast(getString(R.string.testget));
            time.start();
        } else if (flag == 1) {
            LoginBean bean = (LoginBean) JsonUtil.getInstance().json2Obj(s, LoginBean.class);
            PreferenceHelper.write(aty, StringConstants.FILENAME, "loginBean", s);
            //   PreferenceHelper.write(aty, StringConstants.FILENAME, "isRefreshMineFragment1", true);
            PreferenceHelper.write(aty, StringConstants.FILENAME, "accountNumber", et_accountNumber.getText().toString());
            PreferenceHelper.write(aty, StringConstants.FILENAME, "userId", bean.getResult().getUser_id());
            PreferenceHelper.write(aty, StringConstants.FILENAME, "mobile", bean.getResult().getMobile());
            PreferenceHelper.write(aty, StringConstants.FILENAME, "head_pic", bean.getResult().getHead_pic());
            PreferenceHelper.write(aty, StringConstants.FILENAME, "nickname", bean.getResult().getNickname());
            PreferenceHelper.write(aty, StringConstants.FILENAME, "accessToken", bean.getResult().getToken());
            PreferenceHelper.write(aty, StringConstants.FILENAME, "expireTime", bean.getResult().getExpireTime());
            PreferenceHelper.write(aty, StringConstants.FILENAME, "countroy_code", bean.getResult().getCountroy_code());
            PreferenceHelper.write(aty, StringConstants.FILENAME, "timeBefore", System.currentTimeMillis() + "");
            PreferenceHelper.write(aty, StringConstants.FILENAME, "hx_user_name", bean.getResult().getHx_user_name());
            PreferenceHelper.write(aty, StringConstants.FILENAME, "hx_password", bean.getResult().getHx_password());
            //   PreferenceHelper.write(aty, StringConstants.FILENAME, "isRefreshMineFragment", true);
            PreferenceHelper.write(aty, StringConstants.FILENAME, "isReLogin", true);
            ((RegisterContract.Presenter) mPresenter).loginHuanXin(bean.getResult().getHx_user_name(), bean.getResult().getHx_password());
//            dismissLoadingDialog();
//            KJActivityStack.create().finishActivity(LoginActivity.class);
//            aty.finish();
        } else if (flag == 2) {
            dismissLoadingDialog();
            PreferenceHelper.write(aty, StringConstants.FILENAME, "isRefreshMineFragment", true);
            PreferenceHelper.write(aty, StringConstants.FILENAME, "isRefreshMineFragment1", true);
            PreferenceHelper.write(aty, StringConstants.FILENAME, "isReLogin", false);
            KJActivityStack.create().finishActivity(LoginActivity.class);
            aty.finish();
        }
    }

    @Override
    public void errorMsg(String msg, int flag) {
        dismissLoadingDialog();
        aty.runOnUiThread(new Runnable() {
            public void run() {
                ViewInject.toast(msg);
            }
        });
        //   ViewInject.toast(msg);
        tv_registe.setEnabled(true);
    }


    @Override
    public void setPresenter(RegisterContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        time.cancel();
        time = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {// 如果等于1
            // 说明是我们的那次请求
            // 目的：区分请求，不同的请求要做不同的处理
//            countroy_code = data.getStringExtra("areaCode");
//            tv_areaCode.setText("+" + countroy_code);
        }
    }
}
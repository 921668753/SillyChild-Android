package com.sillykid.app.mine.fansattention;

import android.content.Intent;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.cklibrary.common.BaseActivity;
import com.common.cklibrary.common.BaseFragment;
import com.common.cklibrary.common.BindView;
import com.common.cklibrary.common.KJActivityStack;
import com.sillykid.app.R;
import com.sillykid.app.dialog.PublicPromptDialog;
import com.sillykid.app.entity.main.UserInfoBean;
import com.sillykid.app.utils.GlideImageLoader;

/**
 * 粉丝个人信息
 * Created by Administrator on 2017/9/2.
 */

public class FansInfoActivity extends BaseActivity {

    @BindView(id=R.id.iv_back ,click = true)
    private ImageView iv_back;

    @BindView(id=R.id.iv_minetouxiang )
    private ImageView iv_minetouxiang;

    @BindView(id=R.id.iv_minetype )
    private ImageView iv_minetype;

    @BindView(id=R.id.tv_mineusername)
    private TextView tv_mineusername;

    @BindView(id=R.id.iv_minesexicon )
    private ImageView iv_minesexicon;

    @BindView(id = R.id.tv_address)
    private TextView tv_address;
    @BindView(id = R.id.tv_attention , click = true)
    private TextView tv_attention;

    @BindView(id = R.id.ll_fensi , click = true)
    private LinearLayout ll_fensi;
    @BindView(id = R.id.tv_minefsnum)
    private TextView tv_minefsnum;

    @BindView(id = R.id.ll_guanzhu , click = true)
    private LinearLayout ll_guanzhu;
    @BindView(id = R.id.tv_minegznum)
    private TextView tv_minegznum;

    @BindView(id=R.id.tv_minebznum )
    private TextView tv_minebznum;

    @BindView(id=R.id.tv_minebscnum )
    private TextView tv_minebscnum;

    @BindView(id=R.id.ll_dynamicstate ,click = true)
    private LinearLayout ll_dynamicstate;
    @BindView(id=R.id.tv_dynamicstate )
    private TextView tv_dynamicstate;
    @BindView(id=R.id.v_dynamicstate )
    private View v_dynamicstate;

    @BindView(id=R.id.ll_strate ,click = true)
    private LinearLayout ll_strate;
    @BindView(id=R.id.tv_strate )
    private TextView tv_strate;
    @BindView(id=R.id.v_strate )
    private View v_strate;

    @BindView(id=R.id.ll_collectiondynamic ,click = true)
    private LinearLayout ll_collectiondynamic;
    @BindView(id=R.id.tv_collectiondynamic )
    private TextView tv_collectiondynamic;
    @BindView(id=R.id.v_collectiondynamic)
    private View v_collectiondynamic;

    @BindView(id=R.id.ll_collectionstrate ,click = true)
    private LinearLayout ll_collectionstrate;
    @BindView(id=R.id.tv_collectionstrate )
    private TextView tv_collectionstrate;
    @BindView(id=R.id.v_collectionstrate )
    private View v_collectionstrate;

    private int chageIcon=0;
    private FansDynamicStateFragment dynamicStateFragment;
    private FansStrateFragment strateFragment;
    private FansCollectionDynamicFragment collectionDynamicFragment;
    private FansCollectionStrateFragment collectionStrateFragment;
    private SpacesItemDecoration spacesItemDecoration;
    private String country;
    private String city;
    private String address;
    private String fansuserid;
    private int isattention=0;
    private PublicPromptDialog publicPromptDialog;
    private Intent intentjump;


    @Override
    public void setRootView() {
        setContentView(R.layout.activity_myrelease);
    }

    @Override
    public void initData() {
        super.initData();
        spacesItemDecoration=new SpacesItemDecoration(5);
        fansuserid=getIntent().getStringExtra("fansuserid");
        initDefaultInfo();
//        reSetData();
    }

    @Override
    public void initWidget() {
        super.initWidget();
        dynamicStateFragment=new FansDynamicStateFragment();
        strateFragment=new FansStrateFragment();
        collectionDynamicFragment=new FansCollectionDynamicFragment();
        collectionStrateFragment=new FansCollectionStrateFragment();
        chageIcon = aty.getIntent().getIntExtra("chageIcon", 0);
        cleanColors();

    }

    @Override
    public void widgetClick(View v) {
        super.widgetClick(v);
        switch (v.getId()){
            case R.id.ll_dynamicstate:
                chageIcon=0;
                cleanColors();
                break;
            case R.id.ll_strate:
                chageIcon=1;
                cleanColors();
                break;
            case R.id.ll_collectiondynamic:
                chageIcon=2;
                cleanColors();
                break;
            case R.id.ll_collectionstrate:
                chageIcon=3;
                cleanColors();
                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_attention:
                doFragmentAttention();
                break;
            case R.id.ll_fensi:
                KJActivityStack.create().finishActivity(FansAttentionActivity.class);
                intentjump=new Intent(aty, FansAttentionActivity.class);
                intentjump.putExtra("chageIcon",0);
                intentjump.putExtra("userid",fansuserid);
                showActivity(aty, intentjump);
                break;
            case R.id.ll_guanzhu:
                KJActivityStack.create().finishActivity(FansAttentionActivity.class);
                intentjump=new Intent(aty, FansAttentionActivity.class);
                intentjump.putExtra("chageIcon",1);
                intentjump.putExtra("userid",fansuserid);
                showActivity(aty, intentjump);
                break;
        }
    }

    /**
     * 清除颜色，并添加颜色
     */
    @SuppressWarnings("deprecation")
    public void cleanColors() {
        tv_dynamicstate.setTextColor(getResources().getColor(R.color.tabColors));
        v_dynamicstate.setBackgroundResource(android.R.color.transparent);
        tv_strate.setTextColor(getResources().getColor(R.color.tabColors));
        v_strate.setBackgroundResource(android.R.color.transparent);
        tv_collectiondynamic.setTextColor(getResources().getColor(R.color.tabColors));
        v_collectiondynamic.setBackgroundResource(android.R.color.transparent);
        tv_collectionstrate.setTextColor(getResources().getColor(R.color.tabColors));
        v_collectionstrate.setBackgroundResource(android.R.color.transparent);
        if (chageIcon == 0) {
            tv_dynamicstate.setTextColor(getResources().getColor(R.color.greenColors));
            v_dynamicstate.setBackgroundResource(R.color.greenColors);
            changeFragment(dynamicStateFragment);
            if (dynamicStateFragment.getFragmentJumpBetween()!=null)dynamicStateFragment.getFragmentJumpBetween().fragmentPosition();
        } else if (chageIcon == 1) {
            tv_strate.setTextColor(getResources().getColor(R.color.greenColors));
            v_strate.setBackgroundResource(R.color.greenColors);
            changeFragment(strateFragment);
            if (strateFragment.getFragmentJumpBetween()!=null)strateFragment.getFragmentJumpBetween().fragmentPosition();
        } else if (chageIcon == 2) {
            tv_collectiondynamic.setTextColor(getResources().getColor(R.color.greenColors));
            v_collectiondynamic.setBackgroundResource(R.color.greenColors);
            changeFragment(collectionDynamicFragment);
            if (collectionDynamicFragment.getFragmentJumpBetween()!=null)collectionDynamicFragment.getFragmentJumpBetween().fragmentPosition();
        }else if (chageIcon == 3) {
            tv_collectionstrate.setTextColor(getResources().getColor(R.color.greenColors));
            v_collectionstrate.setBackgroundResource(R.color.greenColors);
            changeFragment(collectionStrateFragment);
            if (collectionStrateFragment.getFragmentJumpBetween()!=null)collectionStrateFragment.getFragmentJumpBetween().fragmentPosition();
        }
    }

    public void changeFragment(BaseFragment targetFragment) {
        super.changeFragment(R.id.fl_release, targetFragment);
    }

    /**
     * 将显示的个人信息设置到默认状态
     */
    private void initDefaultInfo(){
        tv_mineusername.setText(getString(R.string.loginOrRegister));
        tv_address.setText("");
        iv_minesexicon.setVisibility(View.GONE);
        iv_minetype.setVisibility(View.INVISIBLE);
        iv_minetouxiang.setImageResource(R.mipmap.avatar);
        tv_minefsnum.setText("0");
        tv_minegznum.setText("0");
        tv_minebznum.setText("0");
        tv_minebscnum.setText("0");
    }

    /**
     * 重新填充数据
     */
//    private void reSetData(){
//        boolean isReLogin=PreferenceHelper.readBoolean(aty, StringConstants.FILENAME, "isReLogin", false);
//        if (isReLogin){
//            initDefaultInfo();
//        }else{
//            String head_pic = PreferenceHelper.readString(aty, StringConstants.FILENAME, "head_pic");
//            if (TextUtils.isEmpty(head_pic)){
//                iv_minetouxiang.setImageResource(R.mipmap.avatar);
//            }else{
//                GlideImageLoader.glideLoader(aty,head_pic,iv_minetouxiang,0);
//            }
//            tv_mineusername.setText(PreferenceHelper.readString(aty, StringConstants.FILENAME, "nickname"));
//            country=PreferenceHelper.readString(aty, StringConstants.FILENAME, "country");
//            city=PreferenceHelper.readString(aty, StringConstants.FILENAME, "city");
//            address="";
//            if (!TextUtils.isEmpty(country)){
//                address+=country+"•";
//            }
//            if (!TextUtils.isEmpty(city)){
//                address+=city;
//            }
//            tv_address.setText(address);
//
//            int level=PreferenceHelper.readInt(aty, StringConstants.FILENAME, "level");
//            switch (level) {
//                case 1:
//                    iv_minetype.setImageResource(R.mipmap.minepthyxxx);
//                    break;
//                case 2:
//                    iv_minetype.setImageResource(R.mipmap.mineviphyxxx);
//                    break;
//                case 3:
//                    iv_minetype.setImageResource(R.mipmap.minezjthyxxx);
//                    break;
//                case 4:
//                    iv_minetype.setImageResource(R.mipmap.minegjhyxxx);
//                    break;
//                case 5:
//                    iv_minetype.setImageResource(R.mipmap.minecjvipxxx);
//                    break;
//                case 6:
//                    iv_minetype.setImageResource(R.mipmap.minezzvipxxx);
//                    break;
//                default:
//                    iv_minetype.setVisibility(View.INVISIBLE);
//                    break;
//            }
//            int sex=PreferenceHelper.readInt(aty, StringConstants.FILENAME, "sex");
//            switch (sex) {
//                case 1:
//                    iv_minesexicon.setVisibility(View.VISIBLE);
//                    iv_minesexicon.setImageResource(R.mipmap.minenanxxx);
//                    break;
//                case 2:
//                    iv_minesexicon.setVisibility(View.VISIBLE);
//                    iv_minesexicon.setImageResource(R.mipmap.minenvxxx);
//                    break;
//                case 0:
//                    iv_minesexicon.setVisibility(View.GONE);
//                    break;
//            }
//            tv_minefsnum.setText(PreferenceHelper.readString(aty, StringConstants.FILENAME, "fans_num"));
//            tv_minegznum.setText(PreferenceHelper.readString(aty, StringConstants.FILENAME, "attention_num"));
//            tv_minebznum.setText(PreferenceHelper.readString(aty, StringConstants.FILENAME, "good_num"));
//            tv_minebscnum.setText(PreferenceHelper.readString(aty, StringConstants.FILENAME, "collection_num"));
//        }
//    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        reSetData();
    }

    /**
     * 瀑布流数据间距
     */
    public static class SpacesItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public SpacesItemDecoration(int space) {
            this.space=space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left=space;
            outRect.right=space;
            outRect.bottom=space;
            outRect.top=space;
        }
    }

    public SpacesItemDecoration getSpacesItemDecoration() {
        return spacesItemDecoration;
    }

    public int getChageIcon() {
        return chageIcon;
    }

    public void initAmount(UserInfoBean userInfoBean){
//        GlideImageLoader.glideLoader(aty,userInfoBean.getData().getHead_pic(),iv_minetouxiang,0);
//        tv_mineusername.setText(userInfoBean.getData().getNickname());
//
//            country=userInfoBean.getData().getCountry();
//            city=userInfoBean.getData().getCity();
//            address="";
//            if (!TextUtils.isEmpty(country)){
//                address+=country+"•";
//            }
//            if (!TextUtils.isEmpty(city)){
//                address+=city;
//            }
//            tv_address.setText(address);
//
//            tv_attention.setVisibility(View.VISIBLE);
//            isattention=userInfoBean.getData().getIsAttention();
//            if (isattention==0){
//                tv_attention.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.mipmap.icon_guanzhu_add),null,null,null);
//                tv_attention.setText(getString(R.string.follow));
//            }else{
//                tv_attention.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.mipmap.icon_guanzhu_over),null,null,null);
//                tv_attention.setText(getString(R.string.followed));
//            }
//
//            iv_minetype.setVisibility(View.VISIBLE);
//            int level=userInfoBean.getData().getLevel();
//            switch (level) {
//                case 1:
//                    iv_minetype.setImageResource(R.mipmap.minepthyxxx);
//                    break;
//                case 2:
//                    iv_minetype.setImageResource(R.mipmap.mineviphyxxx);
//                    break;
//                case 3:
//                    iv_minetype.setImageResource(R.mipmap.minezjthyxxx);
//                    break;
//                case 4:
//                    iv_minetype.setImageResource(R.mipmap.minegjhyxxx);
//                    break;
//                case 5:
//                    iv_minetype.setImageResource(R.mipmap.minecjvipxxx);
//                    break;
//                case 6:
//                    iv_minetype.setImageResource(R.mipmap.minezzvipxxx);
//                    break;
//                default:
//                    iv_minetype.setVisibility(View.INVISIBLE);
//                    break;
//            }
//            int sex=userInfoBean.getData().getSex();
//            switch (sex) {
//                case 1:
//                    iv_minesexicon.setVisibility(View.VISIBLE);
//                    iv_minesexicon.setImageResource(R.mipmap.minenanxxx);
//                    break;
//                case 2:
//                    iv_minesexicon.setVisibility(View.VISIBLE);
//                    iv_minesexicon.setImageResource(R.mipmap.minenvxxx);
//                    break;
//                case 0:
//                    iv_minesexicon.setVisibility(View.GONE);
//                    break;
//            }
//
//        if (TextUtils.isEmpty(userInfoBean.getData().getFans_num())){
//            tv_minefsnum.setText("0");
//        }else{
//            tv_minefsnum.setText(userInfoBean.getData().getFans_num());
//        }
//
//        if (TextUtils.isEmpty(userInfoBean.getData().getAttention_num())){
//            tv_minegznum.setText("0");
//        }else{
//            tv_minegznum.setText(userInfoBean.getData().getAttention_num());
//        }
//
//        if (TextUtils.isEmpty(userInfoBean.getData().getGood_num())){
//            tv_minebznum.setText("0");
//        }else{
//            tv_minebznum.setText(userInfoBean.getData().getGood_num());
//        }
//
//        if (TextUtils.isEmpty(userInfoBean.getData().getCollection_num())){
//            tv_minebscnum.setText("0");
//        }else{
//            tv_minebscnum.setText(userInfoBean.getData().getCollection_num());
//        }
    }

    public String getFansuserid() {
        return fansuserid;
    }

    private void doFragmentAttention(){
        if (isattention==0){
            switch (chageIcon){
                case 0:
                    dynamicStateFragment.getFragmentJumpBetween().doAttention();
                    break;
                case 1:
                    strateFragment.getFragmentJumpBetween().doAttention();
                    break;
                case 2:
                    collectionDynamicFragment.getFragmentJumpBetween().doAttention();
                    break;
                case 3:
                    collectionStrateFragment.getFragmentJumpBetween().doAttention();
                    break;
            }
        }else{
            showPrompting();
        }

    }

    private void showPrompting(){
        if (publicPromptDialog ==null){
            publicPromptDialog =new PublicPromptDialog(aty) {
                @Override
                public void doAction() {
                    switch (chageIcon){
                        case 0:
                            dynamicStateFragment.getFragmentJumpBetween().doCancleAttention();
                            break;
                        case 1:
                            strateFragment.getFragmentJumpBetween().doCancleAttention();
                            break;
                        case 2:
                            collectionDynamicFragment.getFragmentJumpBetween().doCancleAttention();
                            break;
                        case 3:
                            collectionStrateFragment.getFragmentJumpBetween().doCancleAttention();
                            break;
                    }
                }
            };
        }
        publicPromptDialog.show();
        publicPromptDialog.setContent(getString(R.string.cancelAttention));
        publicPromptDialog.setBtnContent(getString(R.string.confirm));
        publicPromptDialog.setTitle(getResources().getColor(R.color.phonenumbercolor),18);
    }

}


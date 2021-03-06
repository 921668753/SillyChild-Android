package com.sillykid.app.retrofit;


import android.content.Context;

import com.common.cklibrary.common.KJActivityStack;
import com.common.cklibrary.common.StringConstants;
import com.common.cklibrary.utils.JsonUtil;
import com.common.cklibrary.utils.httputil.HttpRequest;
import com.common.cklibrary.utils.httputil.HttpUtilParams;
import com.common.cklibrary.utils.httputil.ResponseListener;
import com.common.cklibrary.utils.rx.MsgEvent;
import com.common.cklibrary.utils.rx.RxBus;
import com.kymjs.common.FileUtils;
import com.kymjs.common.Log;
import com.kymjs.common.NetworkUtils;
import com.kymjs.common.PreferenceHelper;
import com.kymjs.common.StringUtils;
import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.client.HttpParams;
import com.kymjs.rxvolley.client.ProgressListener;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.sillykid.app.R;
import com.sillykid.app.constant.NumericConstants;
import com.sillykid.app.constant.StringNewConstants;
import com.sillykid.app.constant.URLConstants;
import com.sillykid.app.entity.loginregister.LoginBean;
import com.sillykid.app.entity.startpage.QiNiuKeyBean;
import com.sillykid.app.message.interactivemessage.imuitl.UserUtil;
import com.sillykid.app.retrofit.uploadimg.UploadManagerUtil;


import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.common.cklibrary.utils.httputil.HttpRequest.doFailure;
import static com.common.cklibrary.utils.httputil.HttpRequest.requestPostFORMHttp;


/**
 * Created by ruitu on 2016/9/17.
 */

public class RequestClient {

    /**
     * 上传头像图片
     */
    public static void upLoadImg(Context context, File file, int type, ResponseListener<String> listener) {
        long nowTime = System.currentTimeMillis();
        String qiNiuImgTime = PreferenceHelper.readString(context, StringConstants.FILENAME, "qiNiuImgTime", "");
        long qiNiuImgTime1 = 0;
        if (StringUtils.isEmpty(qiNiuImgTime)) {
            qiNiuImgTime1 = 0;
        } else {
            qiNiuImgTime1 = Long.decode(qiNiuImgTime);
        }
        long refreshTime = nowTime - qiNiuImgTime1 - (8 * 60 * 60 * 1000);
        if (refreshTime <= 0) {
            upLoadImgQiNiuYun(context, file, listener);
            return;
        }
        HttpParams httpParams = HttpUtilParams.getInstance().getHttpParams();
        getQiNiuKey(context, httpParams, new ResponseListener<String>() {
            @Override
            public void onSuccess(String response) {
                QiNiuKeyBean qiNiuKeyBean = (QiNiuKeyBean) JsonUtil.getInstance().json2Obj(response, QiNiuKeyBean.class);
                if (qiNiuKeyBean == null && StringUtils.isEmpty(qiNiuKeyBean.getData().getAuthToken())) {
                    listener.onFailure(context.getString(R.string.serverReturnsDataNullJsonError));
                    return;
                }
                PreferenceHelper.write(context, StringConstants.FILENAME, "qiNiuToken", qiNiuKeyBean.getData().getAuthToken());
                PreferenceHelper.write(context, StringConstants.FILENAME, "qiNiuImgHost", qiNiuKeyBean.getData().getHost());
                PreferenceHelper.write(context, StringConstants.FILENAME, "qiNiuImgTime", String.valueOf(System.currentTimeMillis()));
                upLoadImgQiNiuYun(context, file, listener);
            }

            @Override
            public void onFailure(String msg) {
                listener.onFailure(msg);
            }
        });

    }


    /**
     * 获取七牛云Token
     */
    private static void upLoadImgQiNiuYun(Context context, File file, ResponseListener<String> listener) {
        String token = PreferenceHelper.readString(context, StringConstants.FILENAME, "qiNiuToken");
        //     if (type == 0) {
        String key = "SHZS_" + UserUtil.getRcId(context) + "_" + file.getName();
        Log.d("ReadFragment", "key" + key);
        //参数 图片路径,图片名,token,成功的回调
        UploadManagerUtil.getInstance().getUploadManager().put(file.getPath(), key, token, new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo responseInfo, JSONObject jsonObject) {
                Log.d("ReadFragment", "key" + key + "responseInfo" + JsonUtil.obj2JsonString(responseInfo) + "jsObj:" + String.valueOf(jsonObject));
                if (responseInfo.isOK()) {
                    String host = PreferenceHelper.readString(context, StringConstants.FILENAME, "qiNiuImgHost");
                    String headpicPath = host + key;
                    Log.i("ReadFragment", "complete: " + headpicPath);
                    listener.onSuccess(headpicPath);
                    return;
                }
                listener.onFailure(context.getString(R.string.failedUploadPicture));
            }
        }, null);
    }


    /**
     * 获取七牛云Token
     */

    public static void getQiNiuKey(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.QINIUKEY, httpParams, listener);
            }
        }, listener);

    }

    /**
     * 百度定位
     */
    public static void postBaiDuInfo(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestPostFORMHttp(context, URLConstants.BAIDUYUN, httpParams, listener);
    }

    public static void postBaiDuGetdateInfo(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestPostFORMHttp(context, URLConstants.BAIDUGETDATE, httpParams, listener);
    }

    public static void postBaiDuUpdateInfo(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestPostFORMHttp(context, URLConstants.BAIDUUPDATE, httpParams, listener);
    }

    /**
     * 根据融云token获取头像性别昵称
     */
    public static void getRongCloud(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.SYSRONGCLOUD, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 刷新Token
     */
    public static void doRefreshToken(Context context, String refreshToken, TokenCallback callback, ResponseListener listener) {
        Log.d("tag", "doRefreshToken");
        HttpParams params = HttpUtilParams.getInstance().getHttpParams();
        params.put("token", refreshToken);
        requestPostFORMHttp(context, URLConstants.REFRESHTOKEN, params, new ResponseListener<String>() {
            @Override
            public void onSuccess(String response) {
                LoginBean response1 = (LoginBean) JsonUtil.getInstance().json2Obj(response, LoginBean.class);
//                PreferenceHelper.write(context, StringConstants.FILENAME, "userId", response1.getResult().getUser_id());
//                PreferenceHelper.write(context, StringConstants.FILENAME, "accessToken", response1.getResult().getToken());
//                PreferenceHelper.write(context, StringConstants.FILENAME, "hx_user_name", response1.getResult().getHx_user_name());
//                PreferenceHelper.write(context, StringConstants.FILENAME, "expireTime", response1.getResult().getExpireTime());
//                PreferenceHelper.write(context, StringConstants.FILENAME, "timeBefore", System.currentTimeMillis() + "");
                for (int i = 0; i < unDoList.size(); i++) {
                    unDoList.get(i).execute();
                }
                unDoList.clear();
                isRefresh = false;
                callback.execute();
            }

            @Override
            public void onFailure(String msg) {
                unDoList.clear();
                isRefresh = false;
                PreferenceHelper.write(context, StringConstants.FILENAME, "userId", 0);
                PreferenceHelper.write(context, StringConstants.FILENAME, "accessToken", "");
                PreferenceHelper.write(context, StringConstants.FILENAME, "expireTime", "0");
                PreferenceHelper.write(context, StringConstants.FILENAME, "timeBefore", "0");
                listener.onFailure(NumericConstants.TOLINGIN + "");
            }
        });
    }

    /**
     * 应用配置参数
     */
    public static void getAppConfig(Context context, HttpParams httpParams, final ResponseListener<String> listener) {
        HttpRequest.requestGetHttp(context, URLConstants.APPCONFIG, httpParams, listener);
    }

    /**
     * 登录
     */
    public static void postLogin(Context context, HttpParams httpParams, final ResponseListener<String> listener) {
        HttpRequest.requestPostFORMHttp(context, URLConstants.USERLOGIN, httpParams, listener);
    }

    /**
     * 第三方登录
     */
    public static void postThirdLogin(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestPostFORMHttp(context, URLConstants.USERTHIRDLOGIN, httpParams, listener);
    }

    /**
     * 绑定手机
     */
    public static void postBindingPhone(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestPostFORMHttp(context, URLConstants.REGISTER, httpParams, listener);
    }

    /**
     * 获取第三方登录验证码
     */
    public static void postThirdCode(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestPostFORMHttp(context, URLConstants.THIRDCODE, httpParams, listener);
    }

    /**
     * 发送验证码
     */
    public static void postCaptcha(Context context, HttpParams httpParams, final ResponseListener<String> listener) {
        HttpRequest.requestPostFORMHttp(context, URLConstants.SENDREGISTER, httpParams, listener);
    }

    /**
     * 短信验证码【找回、修改密码】
     */
    public static void postSendFindCode(Context context, HttpParams httpParams, final ResponseListener<String> listener) {
        HttpRequest.requestPostFORMHttp(context, URLConstants.SENDFINFDCODE, httpParams, listener);
    }


    /**
     * 发送邮箱验证码
     */
    public static void postEmailCaptcha(HttpParams httpParams, final ResponseListener<String> listener) {
        //  HttpRequest.requestPostFORMHttp(URLConstants.SENDEMAILCAPTCHA, httpParams, listener);
    }

    /**
     * 绑定邮箱
     */
    public static void postBindEmail(HttpParams httpParams, final ResponseListener<String> listener) {
        // HttpRequest.requestPostFORMHttp(URLConstants.BINDEMAIL, httpParams, listener);
    }

    /**
     * 绑定邮箱
     */
    public static void postChangeEmail(HttpParams httpParams, final ResponseListener<String> listener) {
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //  HttpRequest.requestPostFORMHttp(URLConstants.CHANGEEMAIL, httpParams, listener);
//            }
//        }, listener);


    }

    /**
     * 注册
     */
    public static void postRegister(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestPostFORMHttp(context, URLConstants.REGISTER, httpParams, listener);
    }

    /**
     * 得到国家区号
     */
    public static void getCountryNumber(HttpParams httpParams, ResponseListener<String> listener) {
        //   HttpRequest.requestGetHttp(URLConstants.COUNTRYNUMBER, httpParams, listener);
    }

    /**
     * 更改密码【手机】
     */
    public static void postResetpwd(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestPostFORMHttp(context, URLConstants.USERRESTPWD, httpParams, listener);
    }

//    public static void getForgetPasswordByMail(HttpParams httpParams, final ResponseListener<String> listener) {
//        HttpRequest.requestPostFORMHttp(URLConstants.FORTGRTBYMAIL, httpParams, listener);
//    }

    /**
     * 获取首页信息
     */
    public static void getHomePage(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestGetHttp(context, URLConstants.HOMEPAGE, httpParams, false, listener);
    }

    /**
     * 获取分类广告
     */
    public static void getAdvCat(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestGetHttp(context, URLConstants.ADVCAT, httpParams, false, listener);
    }

    /**
     * 首页 活动
     */
    public static void getActivities(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestGetHttp(context, URLConstants.ACTIVITYGOOD, httpParams, false, listener);
    }

    /**
     * 首页---更多分类
     */
    public static void getClassification(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestGetHttp(context, URLConstants.GOODSCATLIST, httpParams, false, listener);
    }

    /**
     * 首页---更多分类----商品列表
     */
    public static void getGoodsList(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestPostFORMHttp(context, URLConstants.GOODSLIST, httpParams, listener);
    }

    /**
     * 首页---更多分类----商品列表----店铺首页
     */
    public static void getStoreImage(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestGetHttp(context, URLConstants.STOREIMAGE, httpParams, listener);
    }

    public static void getStoreIndexGoods(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestGetHttp(context, URLConstants.STOREINDEXGOODS, httpParams, false, listener);
    }

    /**
     * 首页---更多分类----商品列表----店铺商品
     */
    public static void getStoreGoodsList(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestGetHttp(context, URLConstants.STOREGOODSLIST, httpParams, false, listener);
    }

    /**
     * 首页---更多分类----商品列表----商品详情
     */
    public static void getGoodDetail(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
        httpParams.putHeaders("Cookie", cookies);
        HttpRequest.requestGetHttp(context, URLConstants.GOODDETAIL, httpParams, false, listener);
    }

    /**
     * 首页---更多分类----商品列表----商品详情---获取评论列表
     */
    public static void getCommentList(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestGetHttp(context, URLConstants.COMMENTLIST, httpParams, false, listener);
    }

    /**
     * 首页---更多分类----商品列表----商品详情----收藏商品
     */
    public static void postFavoriteAdd(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.FAVORITADD, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 首页---更多分类----商品列表----商品详情----取消收藏商品
     */
    public static void postUnfavorite(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.UNFAVORIT, httpParams, listener);
            }
        }, listener);
    }


    /**
     * 首页---更多分类----商品列表----商品详情----取消收藏商品
     */
    public static void postOrderBuyNow(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.ORDERBUYNOW, httpParams, listener);
            }
        }, listener);
    }


    /**
     * 首页---更多分类----商品列表----商品详情----商品规格
     */
    public static void getGoodsSpec(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestGetHttp(context, URLConstants.GOODSSPEC, httpParams, false, listener);
    }

    /**
     * 首页---更多分类----商品列表----商品详情----商品规格--由规格数组获取货品的参数
     */
    public static void getGoodsProductSpec(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestGetHttp(context, URLConstants.GOODSPRODUCTSPEC, httpParams, false, listener);
    }

    /**
     * 首页---更多分类----商品列表----商品详情----商品规格--获取商品剩余规格
     */
    public static void getGoodsProductSpecLeft(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        HttpRequest.requestGetHttp(context, URLConstants.GOODSPRODUCTSPECLEFT, httpParams, false, listener);
    }

    /**
     * 得到地区的热门城市
     */
    public static void getChildHotCity(HttpParams httpParams, int id, ResponseListener<String> listener) {
        // HttpRequest.requestGetHttp(URLConstants.CHILDHOTCITY + "&id=" + id, httpParams, false, listener);
    }


    /**
     * 得到国外地区的首级列表
     */
    public static void getIndexCity(HttpParams httpParams, final ResponseListener<String> listener) {
        // HttpRequest.requestGetHttp(URLConstants.INDEXCITY, httpParams, true, listener);
    }

    /**
     * 得到所有的国家
     */
    public static void getAllCountry(HttpParams httpParams, final ResponseListener<String> listener) {
        //   HttpRequest.requestGetHttp(URLConstants.ALLCOUNTRY1, httpParams, true, listener);
    }


    /**
     * 得到国外地区的子级列表
     */
    public static void getChildCity(HttpParams httpParams, int parent_id, final ResponseListener<String> listener) {
        //  HttpRequest.requestGetHttp(URLConstants.CHILDCITY + "&parent_id=" + parent_id, httpParams, true, listener);
    }

    /**
     * 得到国内全部城市
     */
    public static void getAllCityInHttp(HttpParams httpParams, final ResponseListener<String> listener) {
        //  HttpRequest.requestGetHttp(URLConstants.ALLCITY, httpParams, true, listener);
    }

    /**
     * 得到全部城市
     */
    public static void getAllCityByCountryId(HttpParams httpParams, int countryId, final ResponseListener<String> listener) {
        if (countryId == 0) {
            //    HttpRequest.requestGetHttp(URLConstants.GETALLCOUNTRYCITY, httpParams, true, listener);
        } else {
            //   HttpRequest.requestGetHttp(URLConstants.GETALLCITYBYCOUNTRY + "&countryId=" + countryId, httpParams, true, listener);
        }
    }

    /**
     * 得到热门城市
     */
    public static void getHotCityByCountryId(HttpParams httpParams, int countryId, final ResponseListener<String> listener) {
        if (countryId == 0) {
            //  HttpRequest.requestGetHttp(URLConstants.GETHOTCITYBYCOUNTRY, httpParams, true, listener);
        } else {
            //  HttpRequest.requestGetHttp(URLConstants.GETHOTCITYBYCOUNTRY + "&countryId=" + countryId, httpParams, true, listener);
        }
    }


    /**
     * 搜索城市
     */
    public static void getSearchCity(HttpParams httpParams, String name, final ResponseListener<String> listener) {
//        try {
//         //   HttpRequest.requestGetHttp(URLConstants.SEARCHCITY + "&name=" + URLEncoder.encode(name, "utf-8"), httpParams, false, listener);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 首页----当地达人列表
     */
    public static void getLocalTalent(HttpParams httpParams, int page, String city, final ResponseListener<String> listener) {
//        try {
//            HttpRequest.requestGetHttp(URLConstants.LOCALTALENT + "&p=" + page + "&pageSize=10" + "&city=" + URLEncoder.encode(city, "utf-8"), httpParams, listener);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
    }


    /**
     * 首页----达人详情
     */
    public static void getLocalTalentDetails(HttpParams httpParams, String talent_id, final ResponseListener<String> listener) {
//        String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//        HttpRequest.requestGetHttp(URLConstants.TALENTDETAILS + "&talent_id=" + talent_id + "&token=" + accessToken, httpParams, listener);
    }

    /**
     * 首页----当地达人点赞
     */
    public static void postLocalTalentPraise(HttpParams httpParams, final ResponseListener<String> listener) {

        Log.d("tag", "postLocalTalentPraise");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //   HttpRequest.requestPostFORMHttp(URLConstants.LOCALTALENTPRAISE, httpParams, listener);
//            }
//        }, listener);


    }


    /**
     * 首页----热门攻略
     */
    public static void getHotStrategy(HttpParams httpParams, String city, int page, final ResponseListener<String> listener) {
//        try {
//         //   HttpRequest.requestGetHttp(URLConstants.HOTSTRATEGY + "&p=" + page + "&city=" + URLEncoder.encode(city, "utf-8"), httpParams, listener);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 出行----地区攻略
     */
    public static void getStrategy(HttpParams httpParams, int page, String country_name, final ResponseListener<String> listener) {
//        try {
//           // HttpRequest.requestGetHttp(URLConstants.STRATEGY + "&p=" + page + "&country_name=" + URLEncoder.encode(country_name, "utf-8"), httpParams, listener);
//        } catch (UnsupportedEncodingException e) {
//
//        }
    }


    /**
     * 出行----地区选择
     */
    public static void getVisa(HttpParams httpParams, final ResponseListener<String> listener) {
        //  HttpRequest.requestGetHttp(URLConstants.ALLCOUNTRY, httpParams, listener);
    }


    /**
     * 首页----攻略详情
     */
    public static void getStrategyDetails(HttpParams httpParams, int id, final ResponseListener<String> listener) {
//        String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//        HttpRequest.requestGetHttp(URLConstants.HOTGUIDEDETAIL + "&guide_id=" + id + "&token=" + accessToken, httpParams, listener);
    }

    /**
     * 首页----收藏攻略/取消
     */
    public static void collectStrategy(HttpParams httpParams, int id, int type, final ResponseListener<String> listener) {
        Log.d("tag", "collectStrategy");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                if (type == 0) {
//                    httpParams.put("token", accessToken);
//                    httpParams.put("id", id);
//                    //    HttpRequest.requestPostFORMHttp(URLConstants.COLLECTSTRATEGY, httpParams, listener);
//                } else {
//                    //     HttpRequest.requestDeleteHttp(URLConstants.COLLECTSTRATEGY + "&token=" + accessToken + "&id=" + id, httpParams, listener);
//                }
//            }
//        }, listener);


    }

    /**
     * 首页----攻略詳情-----点赞攻略
     */
    public static void praiseStrategyDetails(HttpParams httpParams, String id, int type, final ResponseListener<String> listener) {
        Log.d("tag", "collectStrategy");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
////                if (type == 0) {
//                httpParams.put("token", accessToken);
//                httpParams.put("guide_id", id);
//                //   HttpRequest.requestPostFORMHttp(URLConstants.STRATEGYPRAISE, httpParams, listener);
////                } else {
////                    HttpRequest.requestDeleteHttp(URLConstants.COLLECTSTRATEGY + "&token=" + accessToken + "&id=" + id, httpParams, listener);
////                }
//            }
//        }, listener);
    }

    /**
     * 首页----包车定制
     */
    public static void getCharterCustom(HttpParams httpParams, String city, final ResponseListener<String> listener) {
        if (StringUtils.isEmpty(city)) {
            //    HttpRequest.requestGetHttp(URLConstants.CHARTERCUSTOM, httpParams, listener);
            return;
        }
//        try {
//            HttpRequest.requestGetHttp(URLConstants.CHARTERCUSTOM + "&city=" + URLEncoder.encode(city, "utf-8"), httpParams, listener);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 首页----包车定制---搜索司导
     */
    public static void getSearchDriver(HttpParams httpParams, String search, final ResponseListener<String> listener) {
//        try {
//           // HttpRequest.requestGetHttp(URLConstants.SEARCHDRIVER + "&search=" + URLEncoder.encode(search, "utf-8"), httpParams, listener);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
    }


    /**
     * 首页----包车定制---搜索司导
     */
    public static void getFindDriver(HttpParams httpParams, String search, final ResponseListener<String> listener) {
//        try {
//            HttpRequest.requestGetHttp(URLConstants.FINDDRIVER + "&search=" + URLEncoder.encode(search, "utf-8"), httpParams, listener);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
    }


    /**
     * 首页----包车定制---包车列表
     */
    public static void getPackCarProduct(HttpParams httpParams, final ResponseListener<String> listener) {
        // HttpRequest.requestGetHttp(URLConstants.PACKCARPRODUCT, httpParams, listener);
    }

    /**
     * 首页----包车定制---车型类型列表
     */
    public static void getCarWhere(HttpParams httpParams, final ResponseListener<String> listener) {
        //  HttpRequest.requestGetHttp(URLConstants.GETCARWHERE, httpParams, listener);
    }

    /**
     * 首页----包车定制---包车产品详情
     */
    public static void getCharterDetails(HttpParams httpParams, final ResponseListener<String> listener) {
        String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
        httpParams.put("token", accessToken);
        //   HttpRequest.requestGetHttp(URLConstants.PACKCARPRODUCT, httpParams, listener);
    }

    /**
     * 首页----包车定制---收藏包车产品
     */
    public static void postCollectCharter(HttpParams httpParams, String id, int type, final ResponseListener<String> listener) {
        Log.d("tag", "postCollectCharter");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
////                if (type == 0) {
////                    httpParams.put("token", accessToken);
////                    httpParams.put("id", id);
////                    HttpRequest.requestPostFORMHttp(URLConstants.COLLECTCHARTER, httpParams, listener);
////                } else {
////                    HttpRequest.requestDeleteHttp(URLConstants.COLLECTCHARTER + "&token=" + accessToken + "&id=" + id, httpParams, listener);
////                }
//
//            }
//        }, listener);
    }


    /**
     * 首页----包车定制--- 改退|费用补偿
     */
    public static void getCompensationChangeBack(HttpParams httpParams, int id, final ResponseListener<String> listener) {
        //  HttpRequest.requestGetHttp(URLConstants.RECHARGEDESC + "&id=" + id, httpParams, listener);
    }


    public static void getUnsubscribeCost(HttpParams httpParams, int type, final ResponseListener<String> listener) {
        //  HttpRequest.requestGetHttp(URLConstants.RECHARGEDESC + "&type=" + type, httpParams, listener);
    }


    /**
     * 首页----包车定制-----按天包车游
     */
    public static void postRentCarByDay(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "postRentCarByDay");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //  HttpRequest.requestPostFORMHttp(URLConstants.RENTCARBYDAY, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 首页----包车定制-----单次接送
     */
    public static void postOncePickup(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "postOncePickup");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //  HttpRequest.requestPostFORMHttp(URLConstants.ONCEPICKUP, httpParams, listener);
//            }
//        }, listener);
    }


    /**
     * 首页----包车定制-----接机
     */
    public static void postReceiveAirport(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "postReceiveAirport");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //  HttpRequest.requestPostFORMHttp(URLConstants.RECEVIVEAIRPORT, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 首页----包车定制-----送机
     */
    public static void postSendAirport(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "postSendAirport");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //    HttpRequest.requestPostFORMHttp(URLConstants.SENDARIPORT, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 首页----包车定制-----私人定制
     */
    public static void postPrivateMake(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "postPrivateMake");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //    HttpRequest.requestPostFORMHttp(URLConstants.PRIVATEMAKE, httpParams, listener);
//            }
//        }, listener);
    }


    /**
     * 首页----包车定制-----私人定制
     */
    public static void getDriverPackConfig(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "getDriverPackConfig");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
        // HttpRequest.requestGetHttp(URLConstants.DRIVERPACKCONFIG, httpParams, listener);
        //  }
        // }, listener);
    }

    /**
     * 首页----包车定制-----私人定制---得到私人定制的行程详情
     */
    public static void getPrivateDetail(HttpParams httpParams, String air_id, final ResponseListener<String> listener) {
        Log.d("tag", "getPrivateDetail");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                //   httpParams.put("token", accessToken);
//                //       HttpRequest.requestGetHttp(URLConstants.GETPRIVATEDETAIL + "&air_id=" + air_id + "&token=" + accessToken, httpParams, listener);
//            }
//        }, listener);
    }


    /**
     * 首页----包车定制-----私人定制---保存用户私人定制信息
     */
    public static void postSaveUserPrivate(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "postSaveUserPrivate");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //  HttpRequest.requestPostFORMHttp(URLConstants.SAVEUSERPRIVATE, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 首页----包车定制----精品路线
     */
    public static void getQualityLine(HttpParams httpParams, int page, String seat_num, String car_level, String line_buy_num, String city, final ResponseListener<String> listener) {
//        try {
//            //    HttpRequest.requestGetHttp(URLConstants.QUALITYLINE + "&p=" + page + "&city=" + URLEncoder.encode(city, "utf-8") + "&seat_num=" + seat_num + "&car_level=" + car_level + "&line_buy_num=" + line_buy_num, httpParams, listener);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
    }


    /**
     * 首页----包车定制----精品路线详情
     */
    public static void getRouteDetails(HttpParams httpParams, String id, final ResponseListener<String> listener) {
        String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
        //    HttpRequest.requestGetHttp(URLConstants.ROUTEDETAILS1 + "&id=" + id + "&token=" + accessToken, httpParams, listener);
    }

    /**
     * 首页----包车定制----收藏路线
     */
    public static void postCollectLine(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "postCollectLine");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //       HttpRequest.requestPostFORMHttp(URLConstants.COLLECTLINE, httpParams, listener);
//            }
//        }, listener);

    }


    /**
     * 首页----包车定制---接送机---得到车型信息
     */
    public static void getCarInfo(HttpParams httpParams, final ResponseListener<String> listener) {
        //  HttpRequest.requestGetHttp(URLConstants.CARINFO, httpParams, listener);
    }


    /**
     * 首页----包车定制---接送机---得到车辆品牌列表
     */
    public static void getCarBrand(HttpParams httpParams, final ResponseListener<String> listener) {
        // HttpRequest.requestGetHttp(URLConstants.GETCARBRAND, httpParams, listener);
    }


    /**
     * 首页----包车定制---精品路线---提交订单
     */
    public static void postConfirmOrder(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "postConfirmOrder");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //         HttpRequest.requestPostFORMHttp(URLConstants.CONFIRMORDER, httpParams, listener);
//            }
//        }, listener);
    }


    /**
     * 首页----包车定制---全部司导
     */
    public static void getAllCompanyGuide(HttpParams httpParams, int page, String time, String city, String partner_num, final ResponseListener<String> listener) {
//        try {
//            HttpRequest.requestGetHttp(URLConstants.ALLCOMPANYGUIDE + "&p=" + page + "&pageSize=10" + "&date=" + time + "&city=" + URLEncoder.encode(city, "utf-8") + "&partner_num=" + partner_num, httpParams, listener);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
    }


    /**
     * 首页----包车定制---全部司导---司导详情
     */
    public static void getCompanyGuideDetails(HttpParams httpParams, String drv_id, final ResponseListener<String> listener) {
//        HttpRequest.requestGetHttp(URLConstants.COMPANYGUIDEDETAILS + "&seller_id=" + drv_id, httpParams, listener);
    }


    /**
     * 首页----全部动态
     */
    public static void getAllDynamics(HttpParams httpParams, final ResponseListener<String> listener) {
        //  HttpRequest.requestGetHttp(URLConstants.ALLDYNAMICS, httpParams, listener);
    }

    /**
     * 首页-----动态详情
     */
    public static void getDynamicsDetails(HttpParams httpParams, String id, final ResponseListener<String> listener) {
        String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//        HttpRequest.requestGetHttp(URLConstants.GETDYNAMICDETAIL + "&id=" + id + "&token=" + accessToken, httpParams, listener);
    }

    /**
     * 首页-----动态详情----关注
     */
    public static void getAttention(HttpParams httpParams, String id, int isAttention, final ResponseListener<String> listener) {

//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
////                if (isAttention == 0) {
////                    HttpRequest.requestPostFORMHttp(URLConstants.ATTENTION + "&userId=" + id + "&token=" + accessToken, httpParams, listener);
////                } else {
////                    HttpRequest.requestDeleteHttp(URLConstants.ATTENTION + "&userId=" + id + "&token=" + accessToken, httpParams, listener);
////                }
//            }
//        }, listener);

    }

    /**
     * 首页-----动态详情----点赞
     */
    public static void praiseDynamicsDetails(HttpParams httpParams, String id, int isPraise, final ResponseListener<String> listener) {

//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                //   if (isPraise == 0) {
//                httpParams.put("article_id", id);
//                httpParams.put("token", accessToken);
//                //  HttpRequest.requestPostFORMHttp(URLConstants.PRAISEDYNAMICS, httpParams, listener);
////                } else {
////                    HttpRequest.requestDeleteHttp(URLConstants.PRAISEDYNAMICS + "&article_id=" + id + "&token=" + accessToken, httpParams, listener);
////                }
//            }
//        }, listener);

    }


    /**
     * 首页-----动态详情----收藏
     */
    public static void collectDynamic(HttpParams httpParams, String id, int isCollectDynamic, final ResponseListener<String> listener) {
//
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
////                if (isCollectDynamic == 0) {
////                    HttpRequest.requestPostFORMHttp(URLConstants.COLLECTDYNAMIC + "&id=" + id + "&token=" + accessToken, httpParams, listener);
////                } else {
////                    HttpRequest.requestDeleteHttp(URLConstants.COLLECTDYNAMIC + "&id=" + id + "&token=" + accessToken, httpParams, listener);
////                }
//            }
//        }, listener);

    }

    /**
     * 首页-----动态详情----最新动态评论(回复)
     */
    public static void newActionComment(HttpParams httpParams, final ResponseListener<String> listener) {

//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //   HttpRequest.requestPostFORMHttp(URLConstants.NEWACTIONCOMMENT, httpParams, listener);
//            }
//        }, listener);

    }

    /**
     * 首页-----动态详情---- 对评论进行点赞
     */
    public static void praiseDynamicsDetailsComment(HttpParams httpParams, String id, int isPraise, final ResponseListener<String> listener) {

//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
////                if (isPraise == 0) {
//                httpParams.put("comment_id", id);
//                httpParams.put("token", accessToken);
//                //      HttpRequest.requestPostFORMHttp(URLConstants.DOGOODBYCOMMENT, httpParams, listener);
////                } else {
////                    HttpRequest.requestDeleteHttp(URLConstants.DOGOODBYCOMMENT + "&comment_id=" + id + "&token=" + accessToken, httpParams, listener);
////                }
//            }
//        }, listener);

    }

    /**
     * 首页-----动态详情---- 得到动态评论
     */
    public static void getDynamicsCommentaries(HttpParams httpParams, final ResponseListener<String> listener) {
        String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
        httpParams.put("token", accessToken);
        //    HttpRequest.requestGetHttp(URLConstants.DYNAMICSCOMMENTARIES, httpParams, listener);
    }


    /**
     * 获取系统消息列表
     */
    public static void getSystemMessage(HttpParams httpParams, int page, final ResponseListener<String> listener) {
        String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//        httpParams.put("token", accessToken);
//        HttpRequest.requestGetHttp(URLConstants.SYSTEMMESSAGELIST + "&p=" + page + "&token=" + accessToken, httpParams, listener);
    }

    /**
     * 获取系统消息列表，不使用KJActivityStack类
     */
    public static void getSystemMessageWithContext(Context context, HttpParams httpParams, int page, final ResponseListener<String> listener) {
        String accessToken = PreferenceHelper.readString(context, StringConstants.FILENAME, "accessToken");
//        httpParams.put("token", accessToken);
        HttpRequest.requestGetHttpWithContext(context, URLConstants.SYSTEMMESSAGELIST + "&p=" + page + "&token=" + accessToken, httpParams, listener);
    }


    /**
     * 获取系统消息首页
     */
    public static void getSystemMessage(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.NEWLISTBUYTITLE, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 获取消息列表
     */
    public static void getSystemMessageList(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.NEWTITLE, httpParams, listener);
            }
        }, listener);
    }


    /**
     * 选中某条消息并设为已读
     */
    public static void getSystemMessageDetails(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.NEWSELECT, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 设置系统消息已读
     */
    public static void getReadMessage(HttpParams httpParams, String id, final ResponseListener<String> listener) {
//        String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//        httpParams.put("token", accessToken);
        //    HttpRequest.requestGetHttp(URLConstants.READMESSAGE + "&id=" + id, httpParams, listener);
    }

    /**
     * 获取得到进行中订单关联的环信用户列表
     */
    public static void getHxUserList(HttpParams httpParams, final ResponseListener<String> listener) {
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //     HttpRequest.requestGetHttp(URLConstants.HXUSERLIST, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 获取得到进行中订单关联的环信用户列表,不使用KJActivityStack
     */
    public static void getHxUserListWithContext(Context context, HttpParams httpParams, final ResponseListener<String> listener) {
//        doServerWithContext(context, new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(context, StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //    HttpRequest.requestGetHttpWithContext(context,URLConstants.HXUSERLIST, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 出行
     */
    public static void getTrip(HttpParams httpParams, final ResponseListener<String> listener) {
        //HttpRequest.requestGetHttp(URLConstants.TRIP, httpParams, listener);
    }


    /**
     * 获取用户信息
     */
    public static void getInfo(Context context, HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "getInfo");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.USERINFO, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 更新用户信息时不修改省市区
     */
    public static void postSaveInfo(Context context, HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "getInfo");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostHttp(context, URLConstants.SAVEINFO, httpParams, listener);
            }
        }, listener);
    }


    /**
     * 获取收货地址列表
     */
    public static void getAddressList(Context context, HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "getAddressList");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.ADDRESSLIST, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 设置默认收货地址
     */
    public static void postDefaultAddress(Context context, HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "postDefaultAddress");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.DEFAULTADDRESS, httpParams, listener);
            }
        }, listener);

    }

    /**
     * 删除收货地址
     */
    public static void postDeleteAddress(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "postDeleteAddress");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.DELETEADDRESS, httpParams, listener);
            }
        }, listener);

    }

    /**
     * 获取详细收货地址
     */
    public static void getAddress(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getAddress");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.ADDRESS, httpParams, listener);
            }
        }, listener);

    }


    /**
     * 根据父id获取地址列表
     */
    public static void getRegionList(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getRegionList");
        HttpRequest.requestGetHttp(context, URLConstants.REGIONLIST, httpParams, listener);
    }

    /**
     * 根据父id获取地址列表
     */
    public static void getAddressRegionList(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getAddressRegionList");
        HttpRequest.requestGetHttp(context, URLConstants.ADDRESSREGIONLIST, httpParams, listener);
    }

    /**
     * 编辑收货地址
     */
    public static void postEditAddress(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "postDeleteAddress");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.EDITADDRESS, httpParams, listener);
            }
        }, listener);
    }


    /**
     * 添加认收货地址
     */
    public static void postAddAddress(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "postDeleteAddress");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.ADDADDRESS, httpParams, listener);
            }
        }, listener);

    }


    /**
     * 更改傻孩子账号
     */
    public static void changeShzCode(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "changeShzCode");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //      HttpRequest.requestPostFORMHttp(URLConstants.CHANGESHZCODE, httpParams, listener);
//            }
//        }, listener);
    }


    /**
     * 获取钱包余额
     */
    public static void getMyWallet(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getMyWallet");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.PURSEGET, httpParams, listener);
            }
        }, listener);
    }


    /**
     * 获取账户钱包明细
     */
    public static void getAccountDetail(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getAccountDetail");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.PURSEDETAIL, httpParams, listener);
            }
        }, listener);
    }


    /**
     * 获取收藏商品列表
     */
    public static void getFavoriteGoodList(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getFavoriteGoodList");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.FAVORITEGOODLIST, httpParams, listener);
            }
        }, listener);
    }


    /**
     * 我的收藏   取消收藏
     */
    public static void postUnFavoriteGood(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "postUnFavoriteGood");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.UNFAVORITEGOOD, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 我的收藏   添加到购物车
     */
    public static void postAddCartGood(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "postAddCartGood");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.AGGCARTGOOD, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 获取购物车商品列表
     */
    public static void getCartList(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getCartList");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.CARTLIST, httpParams, listener);
            }
        }, listener);
    }


    /**
     * 删除购物车中的某项
     */
    public static void postCartDelete(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getCartDelete");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.CARTDELETE, httpParams, listener);
            }
        }, listener);
    }


    /**
     * 更新购物车某项商品数量
     */
    public static void postCartUpdate(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getCartUpdate");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.CARTUPDATE, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 获取购物车商品列表
     */
    public static void getCartBalance(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getCartBalance");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.CARTBALANCE, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 创建付款订单
     */
    public static void postCreateOrder(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "postCreateOrder");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.CREATEORDER, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 订单支付信息接口
     */
    public static void getOnlinePay(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getOnlinePay");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.ONLINEPAY, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 获取订单简要信息
     */
    public static void getOrderSimple(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getOrderSimple");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.ONLINESIMPLE, httpParams, listener);
            }
        }, listener);
    }


    /**
     * 显示订单列表
     */
    public static void getOrderList(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getOrderList");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.ORDERLIST, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 取消订单
     */
    public static void postOrderCancel(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "postOrderCancel");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.ORDERCANCEL, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 确认收货
     */
    public static void postOrderConfirm(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "postOrderConfirm");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.ORDERCONFIRM, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 提醒发货
     */
    public static void postOrderRemind(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "postOrderRemind");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.ORDERREMIND, httpParams, listener);
            }
        }, listener);
    }


    /**
     * 获取订单详情
     */
    public static void getOrderDetail(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getOrderDetail");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.ORDERDETAIL, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 发表评论
     */
    public static void postCommentCreate(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "postCommentCreate");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostHttp(context, URLConstants.COMMENTCREATE, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 获取售后信息
     */
    public static void getOrderRefund(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getOrderRefund");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.ORDERREFUND, httpParams, listener);
            }
        }, listener);
    }


    /**
     * 提交售后信息
     */
    public static void postOrderRefund(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "postOrderRefund");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.ORDERREFUND, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 退货类型,退货原因
     */
    public static void getOrderRefundList(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getOrderRefundList");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.ORDERREFUNDLIST, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 获取售后退款金额（由退货数目获取退款金额）
     */
    public static void getOrderRefundMoney(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getOrderRefundList");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.ORDERREFUNDMONEY, httpParams, listener);
            }
        }, listener);
    }


    /**
     * 售后详情
     */
    public static void getSellBackDetail(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getSellBackDetail");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.SELLBACKDETAIL, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 服务详情
     */
    public static void getSellBackService(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getSellBackService");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.SELLBACKSERVICE, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 包车订单确认结束
     */
    public static void finishOrder(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "getOnlineService");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //     HttpRequest.requestPostFORMHttp(URLConstants.CONFIRMFINISH, httpParams, listener);
//            }
//        }, listener);

    }

    /**
     * 提交包车订单评论
     */
    public static void upEvaluation(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "getOnlineService");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //     HttpRequest.requestPostFORMHttp(URLConstants.UPEVALUATION, httpParams, listener);
//            }
//        }, listener);

    }

    /**
     * 提交包车订单评论,仅限于订单类型为：线路订单
     */
    public static void upEvaluationLine(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "getOnlineService");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //       HttpRequest.requestPostFORMHttp(URLConstants.UPEVALUATIONLINE, httpParams, listener);
//            }
//        }, listener);

    }

    /**
     * 查看包车订单评论
     */
    public static void seeEvaluationDetail(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "getOnlineService");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //      HttpRequest.requestGetHttp(URLConstants.UPEVALUATION, httpParams, listener);
//            }
//        }, listener);

    }

    /**
     * 获取订单列表的数量统计信息
     */
    public static void getOrderAroundHttp(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "getOnlineService");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("m", "Api");
//                httpParams.put("c", "PackOrder");
//                httpParams.put("a", "getOrderAround");
//                httpParams.put("token", accessToken);
//                //     HttpRequest.requestGetHttp(URLConstants.APIURLFORPAY, httpParams, listener);
//            }
//        }, listener);

    }

    /**
     * 删除未付款的订单
     */
    public static void delPackOrderHttp(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "getOnlineService");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //       HttpRequest.requestPostFORMHttp(URLConstants.DELETENOPAYORDER, httpParams, listener);
//            }
//        }, listener);

    }


    /**
     * 显示订单详情
     */
    public static void getOrderInfo(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "getOrderInfo");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //      HttpRequest.requestGetHttp(URLConstants.SHOWORDERINFO, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 包车订单详情
     */
    public static void postCharterOrderInfo(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "getOrderInfo");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //       HttpRequest.requestPostFORMHttp(URLConstants.SHOWCHARTERORDERINFO, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 查看凭证
     */
    public static void getShowCerPic(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "getShowCerPic");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //      HttpRequest.requestGetHttp(URLConstants.SHOWCERPIC, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 获取微信支付参数
     */
    public static void getWxPay(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "getWxPay");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //    HttpRequest.requestGetHttp(URLConstants.WXPAY, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 通过余额支付
     */
    public static void postScorePay(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "postScorePay");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //   HttpRequest.requestPostFORMHttp(URLConstants.SCOREPAY, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 获取支付宝支付参数
     */
    public static void getAlipay(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "getAlipay");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //    HttpRequest.requestGetHttp(URLConstants.ALIPAY, httpParams, listener);
//            }
//        }, listener);
    }


    /**
     * 上传支付凭证
     */
    public static void uploadCerPic(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "uploadCerPic");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //    HttpRequest.requestPostFORMHttp(URLConstants.UPLOADCERPIC, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 获取优惠券列表
     */
    public static void getCouponsList(Context context, HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "getCouponsList");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.COUPONS, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 余额提现
     */
    public static void postWithdrawal(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "postWithdrawal");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.PURSECASH, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 银行卡列表
     */
    public static void getMyBankCard(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getMyBankCard");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.PURSELIST, httpParams, listener);
            }
        }, listener);
    }


    /**
     * 获取银行列表
     */
    public static void getBank(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getBank");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestGetHttp(context, URLConstants.PURSEBANK, httpParams, listener);
            }
        }, listener);
    }


    /**
     * 删除银行卡
     */
    public static void postRemoveBank(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "getMyBankCard");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.PURSEREMOVE, httpParams, listener);
            }
        }, listener);
    }


    /**
     * 设置默认银行卡
     */
    public static void postPurseDefault(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "postPurseDefault");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.PURSEDEFAULT, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 添加银行卡
     */
    public static void postAddBankCard(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "postAddBankCard");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.PURSEADD, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 充值
     */
    public static void postRecharge(Context context, HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "postRecharge");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.ONLINEREC, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 查看账户明细
     */
    public static void getPayRecord(HttpParams httpParams, final ResponseListener<String> listener) {
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //      HttpRequest.requestGetHttp(URLConstants.SHOWPAYRECORD, httpParams, listener);
//            }
//        }, listener);
    }


    /**
     * 我的动态列表and我收藏的动态列表
     */
    public static void getDynamics(HttpParams httpParams, final ResponseListener<String> listener) {
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //      HttpRequest.requestGetHttp(URLConstants.APIURLFORPAY, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 我的动态列表,删除动态
     */
    public static void deleteDynamicState(HttpParams httpParams, String id, final ResponseListener<String> listener) {
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                //     HttpRequest.requestDeleteHttp(URLConstants.PULISHDYNAMIC + "&token=" + accessToken + "&id=" + id, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 我的发布，收藏动态列表,删除收藏的动态
     */
    public static void deleteCollectionDynamicState(HttpParams httpParams, String id, final ResponseListener<String> listener) {
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                //      HttpRequest.requestDeleteHttp(URLConstants.DELETECOLLECTIONDYNAMIC + "&token=" + accessToken + "&id=" + id, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 发布我的动态
     */
    public static void postDynamic(HttpParams httpParams, final ResponseListener<String> listener) {
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //       HttpRequest.requestPostFORMHttp(URLConstants.PULISHDYNAMIC, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 我的攻略列表
     */
    public static void getStrates(HttpParams httpParams, final ResponseListener<String> listener) {
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //    HttpRequest.requestGetHttp(URLConstants.APIURLFORPAY, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 我的攻略列表,删除攻略
     */
    public static void deleteStrate(HttpParams httpParams, String id, final ResponseListener<String> listener) {
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                //     HttpRequest.requestDeleteHttp(URLConstants.PULISHSTRATE + "&token=" + accessToken + "&id=" + id, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 我的发布，收藏攻略列表,删除收藏的攻略
     */
    public static void deleteCollectionStrate(HttpParams httpParams, String id, final ResponseListener<String> listener) {
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                //       HttpRequest.requestDeleteHttp(URLConstants.DELETECOLLECTIONSTRATE + "&token=" + accessToken + "&id=" + id, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 发布我的攻略
     */
    public static void postStrate(HttpParams httpParams, final ResponseListener<String> listener) {
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //      HttpRequest.requestPostFORMHttp(URLConstants.PULISHSTRATE, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 获取粉丝列表
     */
    public static void getAttentionMeListHttp(HttpParams httpParams, final ResponseListener<String> listener) {
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //      HttpRequest.requestGetHttp(URLConstants.APIURLFORPAY, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 获取关注列表
     */
    public static void getAttentionListHttp(HttpParams httpParams, final ResponseListener<String> listener) {
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //     HttpRequest.requestGetHttp(URLConstants.APIURLFORPAY, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 获取他人信息
     */
    public static void baseInfoHttp(HttpParams httpParams, final ResponseListener<String> listener) {
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //      HttpRequest.requestGetHttp(URLConstants.APIURLFORPAY, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 获取他人动态等信息
     */
    public static void getOtherInfoHttp(HttpParams httpParams, final ResponseListener<String> listener) {
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                //      HttpRequest.requestGetHttp(URLConstants.APIURLFORPAY, httpParams, listener);
//            }
//        }, listener);
    }


    /**
     * 我的 VIP紧急电话
     */
    public static void getVIPServicePhoneHttp(HttpParams httpParams, final ResponseListener<String> listener) {
        Log.d("tag", "getOnlineService");
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                //    HttpRequest.requestGetHttp(URLConstants.VIPPHONE, httpParams, listener);
//            }
//        }, listener);

    }


    /**
     * 修改密码
     */
    public static void postChangePassword(HttpParams httpParams, final ResponseListener<String> listener) {
//        doServer(new TokenCallback() {
//            @Override
//            public void execute() {
//                String accessToken = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "accessToken");
//                if (StringUtils.isEmpty(accessToken)) {
//                    listener.onFailure(NumericConstants.TOLINGIN + "");
//                    return;
//                }
//                httpParams.put("token", accessToken);
//                //     HttpRequest.requestPostFORMHttp(URLConstants.UPDATEPWD, httpParams, listener);
//            }
//        }, listener);
    }

    /**
     * 下载App
     */
    @SuppressWarnings("unchecked")
    public static void downloadApp(String updateAppUrl, ProgressListener progressListener, final ResponseListener<String> listener) {
        RxVolley.download(FileUtils.getSaveFolder(StringConstants.DOWNLOADPATH).getAbsolutePath() + StringNewConstants.APKNAME, updateAppUrl, progressListener, new HttpCallback() {
            @Override
            public void onSuccess(String t) {
                listener.onSuccess(FileUtils.getSaveFolder(StringConstants.DOWNLOADPATH).getAbsolutePath() + StringNewConstants.APKNAME);
            }

            @Override
            public void onFailure(int errorNo, String strMsg) {
                Log.d(errorNo + "====failure" + strMsg);
                doFailure(errorNo, strMsg, listener);
            }
        });
    }

    /**
     * 提交意见反馈
     */
    public static void postAdvice(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        Log.d("tag", "postAddBankCard");
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.ADVICEPOST, httpParams, listener);
            }
        }, listener);
    }

    /**
     * 获取会员登录状态
     */
    public static void getIsLogin(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
        if (StringUtils.isEmpty(cookies)) {
            listener.onFailure(NumericConstants.TOLINGIN + "");
            return;
        }
        httpParams.putHeaders("Cookie", cookies);
        HttpRequest.requestGetHttp(context, URLConstants.ISLOGIN, httpParams, listener);
    }

    /**
     * 退出登录
     */
    public static void postLogout(Context context, HttpParams httpParams, ResponseListener<String> listener) {
        doServer(context, new TokenCallback() {
            @Override
            public void execute() {
                String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
                if (StringUtils.isEmpty(cookies)) {
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                    return;
                }
                httpParams.putHeaders("Cookie", cookies);
                HttpRequest.requestPostFORMHttp(context, URLConstants.LOGOUT, httpParams, listener);
            }
        }, listener);
    }


    /**
     * 刷新token回调
     */
    public static boolean isRefresh = false;

    public static void doServer(Context context, final TokenCallback callback, ResponseListener listener) {
        if (!NetworkUtils.isNetWorkAvailable(context)) {
            doFailure(-1, "NetWork err", listener);
            return;
        }
        Log.d("tag", "isNetWorkAvailable" + true);
        String cookies = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "Cookie", "");
        if (StringUtils.isEmpty(cookies)) {
            Log.d("tag", "onFailure");
            UserUtil.clearUserInfo(context);
            if (!(context.getClass().getName().contains("MainActivity") || context.getClass().getName().contains("MyOrderActivity") || context.getClass().getName().contains("MineFragment") || context.getClass().getName().contains("Mine1Fragment"))) {
                /**
                 * 发送消息
                 */
                RxBus.getInstance().post(new MsgEvent<String>("RxBusLogOutEvent"));
            }
            listener.onFailure(NumericConstants.TOLINGIN + "");
            return;
        }
        long nowTime = System.currentTimeMillis();
        String expireTime = PreferenceHelper.readString(context, StringConstants.FILENAME, "expireTime", "");
        long expireTime1 = 0;
        if (StringUtils.isEmpty(expireTime)) {
            expireTime1 = 0;
        } else {
            expireTime1 = Long.decode(expireTime);
        }
        long refreshTime = expireTime1 * 1000 - nowTime - -200000;
        Log.d("tag", "onSuccess" + refreshTime);
        Log.d("tag", "onSuccess1" + nowTime);
        if (refreshTime >= 0) {
            if (isRefresh) {
                unDoList.add(callback);
                return;
            }
            isRefresh = true;
            String refreshToken = PreferenceHelper.readString(context, StringConstants.FILENAME, "accessToken");
            doRefreshToken(context, refreshToken, callback, listener);
        } else {
            HttpParams params = HttpUtilParams.getInstance().getHttpParams();
            getIsLogin(context, params, new ResponseListener<String>() {
                @Override
                public void onSuccess(String response) {
                    Log.d("tag", "onSuccess");
                    callback.execute();
                }

                @Override
                public void onFailure(String msg) {
                    UserUtil.clearUserInfo(context);
                    listener.onFailure(NumericConstants.TOLINGIN + "");
                }
            });
        }
    }


    public interface TokenCallback {
        void execute();
    }

    private static List<TokenCallback> unDoList = new ArrayList<>();

}

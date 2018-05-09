package com.yinglan.scc.mine.personaldata;

import com.common.cklibrary.common.KJActivityStack;
import com.common.cklibrary.common.StringConstants;
import com.common.cklibrary.utils.BitmapCoreUtil;
import com.common.cklibrary.utils.DataCleanManager;
import com.common.cklibrary.utils.httputil.HttpUtilParams;
import com.common.cklibrary.utils.httputil.ResponseListener;
import com.kymjs.common.StringUtils;
import com.kymjs.rxvolley.client.HttpParams;
import com.nanchen.compresshelper.FileUtil;
import com.yinglan.scc.R;
import com.yinglan.scc.retrofit.RequestClient;

import java.io.File;

/**
 * Created by ruitu on 2016/9/24.
 */

public class PersonalDataPresenter implements PersonalDataContract.Presenter {
    private PersonalDataContract.View mView;

    public PersonalDataPresenter(PersonalDataContract.View view) {
        mView = view;
        mView.setPresenter(this);
    }


    @Override
    public void getInfo() {
        HttpParams httpParams = HttpUtilParams.getInstance().getHttpParams();
        RequestClient.getInfo(httpParams, new ResponseListener<String>() {
            @Override
            public void onSuccess(String response) {
                mView.getSuccess(response, 0);
            }

            @Override
            public void onFailure(String msg) {
                mView.errorMsg(msg, 0);
            }
        });
    }

    @Override
    public void setupInfo(String paramname, String voule, int resultsource) {
        HttpParams httpParams = HttpUtilParams.getInstance().getHttpParams();
        httpParams.put(paramname, voule);
        RequestClient.putInfo(httpParams, new ResponseListener<String>() {
            @Override
            public void onSuccess(String response) {
                mView.getSuccess(response, resultsource);
            }

            @Override
            public void onFailure(String msg) {
                mView.errorMsg(msg, resultsource);
            }
        });
    }

    @Override
    public void setupInfo(String country, String countryvoule, String city, String cityvoule, int resultsource) {
        HttpParams httpParams = HttpUtilParams.getInstance().getHttpParams();
        httpParams.put(country, countryvoule);
        httpParams.put(city, cityvoule);
        RequestClient.putInfoForAddress(httpParams, new ResponseListener<String>() {
            @Override
            public void onSuccess(String response) {
                mView.getSuccess(response, resultsource);
            }

            @Override
            public void onFailure(String msg) {
                mView.errorMsg(msg, resultsource);
            }
        });
    }

    @Override
    public void upPictures(String path, int resultsource) {
        if (StringUtils.isEmpty(path)) {
            mView.errorMsg(KJActivityStack.create().topActivity().getString(R.string.noData), 0);
            return;
        }
        File oldFile = new File(path);
        if (!(FileUtil.isFileExists(oldFile))) {
            mView.errorMsg(KJActivityStack.create().topActivity().getString(R.string.imagePathError), 0);
            return;
        }
        long fileSize = 0;
        try {
            fileSize = DataCleanManager.getFileSize(oldFile);
        } catch (Exception e) {
            e.printStackTrace();
            fileSize = 0;
        }
        if (fileSize >= StringConstants.COMPRESSION_SIZE) {
            oldFile = BitmapCoreUtil.customCompression(oldFile);
        }
        HttpParams httpParams = HttpUtilParams.getInstance().getHttpParams();
        httpParams.put("file", oldFile);
        RequestClient.upLoadImg(httpParams, 0, new ResponseListener<String>() {
            @Override
            public void onSuccess(String response) {
                mView.getSuccess(response, resultsource);
            }

            @Override
            public void onFailure(String msg) {
                mView.errorMsg(msg, resultsource);
            }
        });
    }

    @Override
    public void changeShzCode(String shz_code) {
        HttpParams httpParams = HttpUtilParams.getInstance().getHttpParams();
        httpParams.put("shz_code", shz_code);
        RequestClient.changeShzCode(httpParams, new ResponseListener<String>() {
            @Override
            public void onSuccess(String response) {
                mView.getSuccess(response, 5);
            }

            @Override
            public void onFailure(String msg) {
                mView.errorMsg(msg, 5);
            }
        });
    }
}

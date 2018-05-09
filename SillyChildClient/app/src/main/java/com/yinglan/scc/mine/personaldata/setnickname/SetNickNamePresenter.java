package com.yinglan.scc.mine.personaldata.setnickname;

import android.text.TextUtils;

import com.common.cklibrary.common.KJActivityStack;
import com.common.cklibrary.utils.httputil.HttpUtilParams;
import com.common.cklibrary.utils.httputil.ResponseListener;
import com.kymjs.rxvolley.client.HttpParams;
import com.yinglan.scc.R;
import com.yinglan.scc.retrofit.RequestClient;

/**
 * Created by ruitu on 2016/9/24.
 */

public class SetNickNamePresenter implements SetNickNameContract.Presenter {
    private SetNickNameContract.View mView;

    public SetNickNamePresenter(SetNickNameContract.View view) {
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void setupInfo(String newnickname) {
        if (TextUtils.isEmpty(newnickname)){
            mView.errorMsg(KJActivityStack.create().topActivity().getString(R.string.nicknamehint),0);
            return;
        }

        HttpParams httpParams = HttpUtilParams.getInstance().getHttpParams();
        httpParams.put("nickname",newnickname);
        RequestClient.putInfo(httpParams, new ResponseListener<String>() {
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
}
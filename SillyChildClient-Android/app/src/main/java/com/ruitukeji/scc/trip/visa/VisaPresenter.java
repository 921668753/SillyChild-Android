package com.ruitukeji.scc.trip.visa;

import com.common.cklibrary.utils.httputil.HttpUtilParams;
import com.common.cklibrary.utils.httputil.ResponseListener;
import com.kymjs.rxvolley.client.HttpParams;
import com.ruitukeji.scc.retrofit.RequestClient;
import com.ruitukeji.scc.trip.visa.VisaContract;

/**
 * Created by ruitu on 2016/9/24.
 */

public class VisaPresenter implements VisaContract.Presenter {
    private VisaContract.View mView;

    public VisaPresenter(VisaContract.View view) {
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void getVisa() {
        HttpParams httpParams = HttpUtilParams.getInstance().getHttpParams();
        RequestClient.getVisa(httpParams, new ResponseListener<String>() {
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

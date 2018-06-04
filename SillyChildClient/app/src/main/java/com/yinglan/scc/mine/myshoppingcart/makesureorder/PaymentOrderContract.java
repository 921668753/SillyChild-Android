package com.yinglan.scc.mine.myshoppingcart.makesureorder;


import com.common.cklibrary.common.BasePresenter;
import com.common.cklibrary.common.BaseView;

/**
 * Created by Administrator on 2017/2/15.
 */

public interface PaymentOrderContract {

    interface Presenter extends BasePresenter {

        /**
         * 订单支付信息接口
         */
        void getOnlinePay(String order_id, String pay_type);
    }

    interface View extends BaseView<Presenter, String> {

    }

}

package com.yinglan.scc.mine.mycollection;

import com.common.cklibrary.common.BasePresenter;
import com.common.cklibrary.common.BaseView;
import com.yinglan.scc.mine.myorder.MyOrderActivity;

/**
 * Created by ruitu on 2016/9/24.
 */

public interface MyCollectionContract {
    interface Presenter extends BasePresenter {

        /**
         * 获取收藏商品列表
         */
        void getFavoriteGoodList(int page);

        /**
         * 取消收藏
         */
        void postUnFavoriteGood(int goodsid);

        /**
         * 加入购物车
         */
        void postAddCartGood(int goodsid);
    }

    interface View extends BaseView<Presenter, String> {
    }

}



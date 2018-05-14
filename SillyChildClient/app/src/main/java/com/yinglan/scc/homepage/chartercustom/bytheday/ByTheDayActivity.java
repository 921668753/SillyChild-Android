package com.yinglan.scc.homepage.chartercustom.bytheday;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.view.OptionsPickerView;
import com.bigkoo.pickerview.view.TimePickerView;
import com.common.cklibrary.common.BaseActivity;
import com.common.cklibrary.common.BindView;
import com.common.cklibrary.common.ViewInject;
import com.common.cklibrary.utils.JsonUtil;
import com.kymjs.common.StringUtils;
import com.yinglan.scc.R;
import com.yinglan.scc.dialog.CalendarControlByTheDayBouncedDialog;
import com.yinglan.scc.dialog.chartercustom.CompensationChangeBackDialog;
import com.yinglan.scc.dialog.chartercustom.CostsThatDialog;
import com.yinglan.scc.entity.AirportDropOffBean;
import com.yinglan.scc.entity.CarBrandBean;
import com.yinglan.scc.loginregister.LoginActivity;
import com.yinglan.scc.mine.myorder.orderdetails.CharterOrderDetailsActivity;
import com.yinglan.scc.utils.SoftKeyboardUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.bingoogolapple.titlebar.BGATitleBar;

/**
 * 按天包车游
 * Created by Admin on 2017/8/16.
 */

public class ByTheDayActivity extends BaseActivity implements ByTheDayContract.View {

    @BindView(id = R.id.titlebar)
    private BGATitleBar titlebar;

    /**
     * 出发地
     */
    @BindView(id = R.id.et_origin)
    EditText et_origin;

    /**
     * 目的地
     */
    @BindView(id = R.id.ll_destination, click = true)
    LinearLayout ll_destination;

    @BindView(id = R.id.et_destination)
    EditText et_destination;

    /**
     * 包车日期
     */
    @BindView(id = R.id.ll_charterDate, click = true)
    LinearLayout ll_charterDate;

    @BindView(id = R.id.tv_selectStartEndDateCar)
    TextView tv_selectStartEndDateCar;

    /**
     * 选择车型
     */
    @BindView(id = R.id.ll_selectModels, click = true)
    LinearLayout ll_selectModels;

    @BindView(id = R.id.tv_selectModels)
    TextView tv_selectModels;

    /**
     * 司导工号
     */
    @BindView(id = R.id.et_workNumber)
    EditText et_workNumber;


    /**
     * 选择出行人数
     */
    @BindView(id = R.id.tv_subtractionAdult, click = true)
    TextView tv_subtractionAdult;
    @BindView(id = R.id.tv_adultNum)
    TextView tv_adultNum;
    @BindView(id = R.id.tv_addAdult, click = true)
    TextView tv_addAdult;

    @BindView(id = R.id.tv_subtractionChildren, click = true)
    TextView tv_subtractionChildren;

    @BindView(id = R.id.tv_childrenNum)
    TextView tv_childrenNum;

    @BindView(id = R.id.tv_addChildren, click = true)
    TextView tv_addChildren;

    /**
     * 行李
     */
    @BindView(id = R.id.tv_subtractionBags, click = true)
    TextView tv_subtractionBags;
    @BindView(id = R.id.tv_bags)
    TextView tv_bags;
    @BindView(id = R.id.tv_addBags, click = true)
    TextView tv_addBags;

    @BindView(id = R.id.tv_subtractionBags1, click = true)
    TextView tv_subtractionBags1;
    @BindView(id = R.id.tv_bags1)
    TextView tv_bags1;
    @BindView(id = R.id.tv_addBags1, click = true)
    TextView tv_addBags1;

    @BindView(id = R.id.tv_subtractionBags2, click = true)
    TextView tv_subtractionBags2;
    @BindView(id = R.id.tv_bags2)
    TextView tv_bags2;
    @BindView(id = R.id.tv_addBags2, click = true)
    TextView tv_addBags2;

    @BindView(id = R.id.tv_subtractionBags3, click = true)
    TextView tv_subtractionBags3;
    @BindView(id = R.id.tv_bags3)
    TextView tv_bags3;
    @BindView(id = R.id.tv_addBags3, click = true)
    TextView tv_addBags3;


    /**
     * 姓名
     */
    @BindView(id = R.id.et_name)
    EditText et_name;
    /**
     * 联系方式
     */
    @BindView(id = R.id.et_contactWay)
    EditText et_contactWay;


    @BindView(id = R.id.ll_costsThat, click = true)
    LinearLayout ll_costsThat;


    @BindView(id = R.id.ll_compensationChangeBack, click = true)
    LinearLayout ll_compensationChangeBack;

    @BindView(id = R.id.tv_compensationChangeBack)
    TextView tv_compensationChangeBack;

    /**
     * 备注
     */
    @BindView(id = R.id.et_remark)
    EditText et_remark;


    /**
     * 提交
     */
    @BindView(id = R.id.tv_submit, click = true)
    TextView tv_submit;

    private TimePickerView pvTime = null;
    private OptionsPickerView pvOptions = null;

    private String type = "rent_car_by_day";

//    private String car_type_id = "";

    private CarBrandBean carBrandBean;

    private String con_car_seat_num;

    private long pack_time = 0;
    private String pack_time1 = "";
    private CompensationChangeBackDialog compensationChangeBackDialog;
    private CostsThatDialog costsThatDialog;
    private String carTypeName;
    private int pcpid = 0;
    private CalendarControlByTheDayBouncedDialog calendarControlByTheDayBouncedDialog;


    @Override
    public void setRootView() {
        setContentView(R.layout.activity_bytheday);
    }

    @Override
    public void initData() {
        super.initData();
//        String selectCity = PreferenceHelper.readString(KJActivityStack.create().topActivity(), StringConstants.FILENAME, "selectCity", "");
//        if (StringUtils.isEmpty(selectCity) || selectCity.contains(KJActivityStack.create().topActivity().getString(R.string.locateFailure))) {
//            ViewInject.toast(KJActivityStack.create().topActivity().getString(R.string.locateFailure) + KJActivityStack.create().topActivity().getString(R.string.travelCity1), 0);
//            finish();
//            return;
//        }
        calendarControlByTheDayBouncedDialog = new CalendarControlByTheDayBouncedDialog(this) {
            @Override
            public void timeList(String dateStr, String timeList) {
                tv_selectStartEndDateCar.setText(dateStr);
                pack_time1 = timeList;
            }
        };
        mPresenter = new ByTheDayPresenter(this);
        pcpid = aty.getIntent().getIntExtra("pcpId", 0);
//        car_type_id = aty.getIntent().getStringExtra("carTypeId");
        carTypeName = aty.getIntent().getStringExtra("carTypeName");
        con_car_seat_num = aty.getIntent().getStringExtra("carSeatTotal");
        String costStatement = aty.getIntent().getStringExtra("costStatement");
        String costCompensation = aty.getIntent().getStringExtra("costCompensation");
        String costCompensationLevel = aty.getIntent().getStringExtra("costCompensationLevel");
        tv_compensationChangeBack.setText(costCompensationLevel);
        compensationChangeBackDialog = new CompensationChangeBackDialog(this, costCompensation);
        compensationChangeBackDialog.setCanceledOnTouchOutside(false);
        costsThatDialog = new CostsThatDialog(this, costStatement);
        costsThatDialog.setCanceledOnTouchOutside(false);
//        showLoadingDialog(aty.getString(R.string.dataLoad));
//        ((ByTheDayContract.Presenter) mPresenter).getCarBrand();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initWidget() {
        super.initWidget();
        BGATitleBar.SimpleDelegate simpleDelegate = new BGATitleBar.SimpleDelegate() {
            @Override
            public void onClickLeftCtv() {
                super.onClickLeftCtv();
                aty.finish();
            }

            @Override
            public void onClickRightCtv() {
                super.onClickRightCtv();
                ((ByTheDayContract.Presenter) mPresenter).isLogin();
            }
        };
        titlebar.setTitleText(getString(R.string.byTheDay));
        titlebar.setRightText("");
        titlebar.setRightDrawable(getResources().getDrawable(R.mipmap.kefu));
        titlebar.setDelegate(simpleDelegate);
        departureTime();
        tv_selectModels.setText(con_car_seat_num + getString(R.string.peopleseat) + carTypeName);
      //  StringNewUtils.setFilters(et_remark);
    }


    @Override
    public void setPresenter(ByTheDayContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void widgetClick(View v) {
        super.widgetClick(v);
        switch (v.getId()) {
//            case R.id.ll_destination:
//                Intent intent = new Intent(this, AddressPOISearchActivity.class);
//                startActivity(intent);
//                break;
            case R.id.ll_charterDate:
                SoftKeyboardUtils.packUpKeyboard(this);
                calendarControlByTheDayBouncedDialog.show();
                //  pvTime.show(tv_selectStartEndDateCar);
                break;
//            case R.id.ll_selectModels:
//                SoftKeyboardUtils.packUpKeyboard(this);
//                pvOptions.show(tv_selectModels);
//                break;
            case R.id.tv_subtractionAdult:
                subtractionNum(tv_subtractionAdult, tv_adultNum);
                break;
            case R.id.tv_addAdult:
                addNum(tv_subtractionAdult, tv_adultNum);
                break;
            case R.id.tv_subtractionChildren:
                subtractionNum(tv_subtractionChildren, tv_childrenNum);
                break;
            case R.id.tv_addChildren:
                addNum(tv_subtractionChildren, tv_childrenNum);
                break;
            case R.id.tv_subtractionBags:
                subtractionNum(tv_subtractionBags, tv_bags);
                break;
            case R.id.tv_addBags:
                addNum(tv_subtractionBags, tv_bags);
                break;
            case R.id.tv_subtractionBags1:
                subtractionNum(tv_subtractionBags1, tv_bags1);
                break;
            case R.id.tv_addBags1:
                addNum(tv_subtractionBags1, tv_bags1);
                break;
            case R.id.tv_subtractionBags2:
                subtractionNum(tv_subtractionBags2, tv_bags2);
                break;
            case R.id.tv_addBags2:
                addNum(tv_subtractionBags2, tv_bags2);
                break;
            case R.id.tv_subtractionBags3:
                subtractionNum(tv_subtractionBags3, tv_bags3);
                break;
            case R.id.tv_addBags3:
                addNum(tv_subtractionBags3, tv_bags3);
                break;

            case R.id.ll_costsThat:
                costsThatDialog.show();
                break;

            case R.id.ll_compensationChangeBack:
                compensationChangeBackDialog.show();
                break;
            case R.id.tv_submit:
                showLoadingDialog(getString(R.string.submissionLoad));
                ((ByTheDayContract.Presenter) mPresenter).postByTheDay(type, et_origin.getText().toString().trim(), et_destination.getText().toString().trim(), pack_time1, null, et_workNumber.getText().toString().trim(), tv_adultNum.getText().toString().trim(), tv_childrenNum.getText().toString().trim(),
                        tv_bags.getText().toString().trim(), tv_bags1.getText().toString().trim(), tv_bags2.getText().toString().trim(), tv_bags3.getText().toString().trim(), et_name.getText().toString().trim(), et_contactWay.getText().toString().trim(), et_remark.getText().toString().trim(), con_car_seat_num, pcpid);
                break;
        }
    }


    /**
     * @param textView 所要减的控件
     */
    @SuppressWarnings("deprecation")
    private void subtractionNum(TextView textView, TextView textView1) {
        if (StringUtils.toInt(textView1.getText().toString(), 0) - 1 <= 0) {
            textView.setTextColor(aty.getResources().getColor(R.color.hintColors));
            textView.setBackgroundResource(R.drawable.shape_code1);
            textView1.setText("0");
        } else {
            textView.setTextColor(aty.getResources().getColor(R.color.greenColors));
            textView.setBackgroundResource(R.drawable.shape_code);
            textView1.setText(String.valueOf(StringUtils.toInt(textView1.getText().toString(), 0) - 1));
        }
    }

    /**
     * @param textView 所要加的控件
     */
    @SuppressWarnings("deprecation")
    private void addNum(TextView textView, TextView textView1) {
        textView.setTextColor(aty.getResources().getColor(R.color.greenColors));
        textView.setBackgroundResource(R.drawable.shape_code);
        textView1.setText(String.valueOf(StringUtils.toInt(textView1.getText().toString(), 0) + 1));
    }

    /**
     * 选择时间的控件
     */
    private void departureTime() {
        //TimePicker
        //控制时间范围
        boolean[] type = new boolean[]{true, true, true, true, true, false};
        Calendar calendar = Calendar.getInstance();
        Calendar startDate = Calendar.getInstance();
        startDate.set(calendar.get(Calendar.YEAR) - 1, calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        Calendar endDate = Calendar.getInstance();
        endDate.set(calendar.get(Calendar.YEAR) + 1, calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        pvTime = null;
//        pvTime = new TimePickerView.Builder(aty, new TimePickerView.OnTimeSelectListener() {
//
//            @Override
//            public void onTimeSelect(Date date, View v) {//选中事件回调
//                if (date.getTime() <= System.currentTimeMillis()) {
//                    ViewInject.toast(aty.getString(R.string.greateThanCurrentTime));
//                    return;
//                }
//                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//                pack_time = date.getTime() / 1000;
//                ((TextView) v).setText(format.format(date));
//            }
//        }).setType(type).setRangDate(startDate, endDate).setLabel("", "", "", "", "", "").build();
//        pvTime.setDate(calendar);
    }


    @Override
    public void getSuccess(String success, int flag) {
        if (flag == 0) {
            // ViewInject.toast("提交成功！");
            //     PreferenceHelper.write(aty, StringConstants.FILENAME, "chatMessage", 4);
            AirportDropOffBean airportDropOffBean = (AirportDropOffBean) JsonUtil.getInstance().json2Obj(success, AirportDropOffBean.class);

//            Intent intent = new Intent(aty, OverleafActivity.class);
//            intent.putExtra("origin", et_origin.getText().toString().trim());
//            intent.putExtra("destination", et_destination.getText().toString().trim());
//            intent.putExtra("selectStartEndDateCar", tv_selectStartEndDateCar.getText().toString().trim());
//            intent.putExtra("selectModels", tv_selectModels.getText().toString().trim());
//            intent.putExtra("workNumber", et_workNumber.getText().toString().trim());
//            intent.putExtra("adultNum", tv_adultNum.getText().toString().trim());
//            intent.putExtra("childrenNum", tv_childrenNum.getText().toString().trim());
//            intent.putExtra("id", airportDropOffBean.getResult().getId());
//            intent.putExtra("twenty_four", tv_bags3.getText().toString());
//            intent.putExtra("twenty_six", tv_bags2.getText().toString());
//            intent.putExtra("twenty_eight", tv_bags1.getText().toString());
//            intent.putExtra("thirty", tv_bags.getText().toString());
//
//            intent.putExtra("name", et_name.getText().toString().trim());
//            intent.putExtra("contactWay", et_contactWay.getText().toString());
//            intent.putExtra("remark", et_remark.getText().toString().trim());
//            intent.putExtra("chatMessage", "4");
//            showActivity(aty, intent);
            Intent intent = new Intent(aty, CharterOrderDetailsActivity.class);
            // intent.putExtra("paymoney", tv_price.getText().toString());
            intent.putExtra("airid", airportDropOffBean.getResult().getId());
            skipActivity(aty, intent);
        } else if (flag == 1) {
            carBrandBean = (CarBrandBean) JsonUtil.json2Obj(success, CarBrandBean.class);
            if (carBrandBean.getResult().getList() != null && carBrandBean.getResult().getList().size() > 0) {
                List<CarBrandBean.ResultBean.ListBean> list = carBrandBean.getResult().getList();
                for (int i = 0; i < list.size(); i++) {
                    String seatNum = list.get(i).getSeatNum();
                    String name = list.get(i).getName();
                    if (StringUtils.isEmpty(name)) {
                        list.get(i).setName(seatNum + getString(R.string.peopleseat));
                    } else {
                        list.get(i).setName(seatNum + getString(R.string.peopleseat) + "(" + name + ")");
                    }
                }
                pvOptions.setPicker(list);
            }
        } else if (flag == 2) {
           // showActivity(aty, OverleafActivity.class);
        }
        dismissLoadingDialog();

    }


    @Override
    public void errorMsg(String msg, int flag) {
        dismissLoadingDialog();
        if (isLogin(msg)) {
            showActivity(aty, LoginActivity.class);
            return;
        }
        ViewInject.toast(msg);
    }


    /**
     * 判断政策
     */
//    public String changeBack(String compensationChangeBack) {
//        String policy = "";
//        if (compensationChangeBack != null && compensationChangeBack.startsWith("cover_img_k")) {
//            policy = getString(R.string.looseFitting);
//        } else if (compensationChangeBack != null && compensationChangeBack.startsWith("cover_img_y")) {
//            policy = getString(R.string.rigor);
//        } else if (compensationChangeBack != null && compensationChangeBack.startsWith("cover_img_z")) {
//            policy = getString(R.string.middling);
//        } else {
//            policy = getString(R.string.unsubscribe);
//        }
//        return policy;
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compensationChangeBackDialog = null;
        costsThatDialog = null;
    }
}

package com.flj.latte.ec.main.personal.address;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.flj.latte.delegates.LatteDelegate;
import com.flj.latte.ec.R;
import com.flj.latte.ec.common.http.api.API;
import com.flj.latte.ec.common.util.ToastUtil;
import com.flj.latte.net.RestClient;
import com.flj.latte.net.callback.IFailure;
import com.flj.latte.net.callback.ISuccess;
import com.flj.latte.ui.recycler.MultipleFields;
import com.flj.latte.ui.recycler.MultipleItemEntity;
import com.flj.latte.util.log.LatteLogger;
import com.flj.latte.util.storage.LattePreference;
import com.lljjcoder.Interface.OnCityItemClickListener;
import com.lljjcoder.bean.CityBean;
import com.lljjcoder.bean.DistrictBean;
import com.lljjcoder.bean.ProvinceBean;
import com.lljjcoder.citywheel.CityConfig;
import com.lljjcoder.style.citypickerview.CityPickerView;

import java.util.WeakHashMap;


/**
 * author: yb
 * date  : 2017/12/21.
 */

public class UpdateressAddDelegate extends LatteDelegate implements ISuccess, View.OnClickListener {
    //申明对象
    CityPickerView mPicker = new CityPickerView();
    private int provinceId;
    private int cityId;
    private int districtId;
    private String receiverName;
    private String receiverPhone;
    private String receiverMobile;
    private String receiverAddress;
    private String receiverZip;
    private String receiverlocation;
    private SwitchCompat switch_button;
    private TextView address_city;
    private boolean isDefault = false;
    private int id = -1;
    AppCompatTextView title;
    private AppCompatButton update;
    private static final String ADDRESS_ID = "ADDRESS_ID";
    MultipleItemEntity entity;

    @Override
    public Object setLayout() {
        return R.layout.delegate_address_add;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, @NonNull View rootView) {
        final Bundle args = getArguments();
        if (args != null) {
            receiverName = args.getString("receiverName");
            isDefault = args.getBoolean("isDefault");
            id = args.getInt("id");
            provinceId = args.getInt("provinceId");
            cityId = args.getInt("cityId");
            districtId = args.getInt("districtId");
            receiverMobile = args.getString("receiverMobile");
            receiverAddress = args.getString("receiverAddress");
            receiverlocation = args.getString("receiverlocation");
            receiverZip = args.getString("receiverZip");
        }
        initView(entity);
        /**
         * 预先加载仿iOS滚轮实现的全部数据
         */
        mPicker.init(UpdateressAddDelegate.this.getContext());

    }

    private void initView(MultipleItemEntity addressId) {
        $(R.id.address_city_layout).setOnClickListener(this);
        update = $(R.id.btn_address_save);
        address_city = $(R.id.address_city);
        title = $(R.id.tv_title);
        title.setText("修改地址");
        setText($(R.id.address_receiverName), receiverName);
        setText($(R.id.address_receiverMobile), receiverMobile);
        setText($(R.id.address_city), receiverlocation);
        setText($(R.id.address_address_detail), receiverAddress);
        setText($(R.id.address_code), receiverZip);
        switch_button = $(R.id.switch_button);
        switch_button.setChecked(isDefault);
        update.setOnClickListener(this);
        update.setText("更新");
        // setText($(R.id.))
    }

    public static UpdateressAddDelegate create(MultipleItemEntity entity) {
        final Bundle args = new Bundle();
        args.putString("receiverName", entity.getField(MultipleFields.NAME));
        args.putBoolean("isDefault", entity.getField(MultipleFields.TAG));
        args.putInt("id", entity.getField(MultipleFields.ID));
        args.putInt("provinceId", entity.getField(AddressItemFields.PROVINCEID));
        args.putInt("cityId", entity.getField(AddressItemFields.CITYID));
        args.putString("receiverMobile", entity.getField(AddressItemFields.PHONE));
        args.putString("receiverAddress", entity.getField(AddressItemFields.ADDRESS));
        args.putString("receiverlocation", entity.getField(AddressItemFields.LOCATION));
        args.putInt("districtId", entity.getField(AddressItemFields.DISTRICTID));
        args.putString("receiverZip", entity.getField(AddressItemFields.RECEIVERZIP));
        final UpdateressAddDelegate delegate = new UpdateressAddDelegate();
        delegate.setArguments(args);
        return delegate;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.address_city_layout) {
            getSupportDelegate().hideSoftInput();
            showAddress();
        } else if (i == R.id.btn_address_save) {
            commitPersonlAddress();
        }
    }

    private void commitPersonlAddress() {
        receiverName = getText($(R.id.address_receiverName));
        receiverPhone = getText($(R.id.address_receiverMobile));
        receiverMobile = getText($(R.id.address_receiverMobile));
        receiverAddress = getText($(R.id.address_address_detail));
        receiverZip = getText($(R.id.address_code));

        if (StringUtils.isEmpty(receiverName)) {
            ToastUtil.showToast(getContext(), "请输入用户名");
            return;
        }
        if (StringUtils.isEmpty(receiverPhone)) {
            ToastUtil.showToast(getContext(), "请输入手机号");
            return;
        }
        if (StringUtils.isEmpty(receiverAddress)) {
            ToastUtil.showToast(getContext(), "请输入详细地址");
            return;
        }
        if (StringUtils.isEmpty(receiverZip)) {
            ToastUtil.showToast(getContext(), "请输入邮编");
            return;
        }
        final String addressUrl = API.Config.getDomain() + API.CONFIGNEE_UPDATE;
        LatteLogger.d("addressUrl", addressUrl);
        final WeakHashMap<String, Object> address = new WeakHashMap<>();
        final Long mUserId = LattePreference.getCustomAppProfileLong("userId");
        address.put("userId", mUserId);
        address.put("receiverName", receiverName);
        address.put("receiverPhone", receiverPhone);
        address.put("receiverMobile", receiverMobile);
        address.put("provinceId", provinceId);
        address.put("cityId", cityId);
        address.put("districtId", districtId);
        address.put("receiverAddress", receiverAddress);
        address.put("receiverZip", receiverZip);
        address.put("id", id);
        address.put("receiverDefault", switch_button.isChecked() ? 1 : 0);
        final String jsonString = JSON.toJSONString(address);
        LogUtils.e("jsonString", jsonString);
        RestClient.builder()
                .url(addressUrl)
                .loader(getContext())
                .raw(jsonString)
                .success(this)
                .failure(new IFailure() {
                    @Override
                    public void onFailure() {
                        ToastUtil.show(getContext(), "连接超时");
                    }
                })
                .build()
                .post();
    }

    private String getText(EditText view) {
        return view.getText().toString().trim();
    }

    private void setText(TextView view, String msg) {
        view.setText(msg);
    }

    @Override
    public void onSuccess(String response) {
        LogUtils.e("jsonString", response);
        if (response.contains("success")) {
            //加载地址界面
            ToastUtil.show(getContext(), "更新成功");
            setFragmentResult(Activity.RESULT_OK, null);
            getSupportDelegate().pop();
        } else {
            ToastUtil.show(getContext(), "更新失败");
        }
    }

    private void showAddress() {
        if (null != address_city)
            address_city.setText("");
        //添加默认的配置，不需要自己定义
        CityConfig cityConfig = new CityConfig.Builder().build();
        cityConfig.setShowGAT(true);
        mPicker.setConfig(cityConfig);
        //监听选择点击事件及返回结果
        mPicker.setOnCityItemClickListener(new OnCityItemClickListener() {
            @Override
            public void onSelected(ProvinceBean province, CityBean city, DistrictBean district) {
                //省份
                if (province != null) {
                    provinceId = Integer.valueOf(province.getId());
                    //provinceId += province.getName();
                    address_city.append(province.getName());
                }
                //城市
                if (city != null) {
                    cityId = Integer.valueOf(city.getId());
                    Log.d("Test", cityId + "--" + cityId);
                    address_city.append(city.getName());
                }
                //地区
                if (district != null) {
                    districtId = Integer.valueOf(district.getId());
                    Log.d("Test", districtId + "--" + districtId);
                    address_city.append(district.getName().equalsIgnoreCase(city.getName()) ? "" : district.getName());
                }
            }

            @Override
            public void onCancel() {

            }
        });
        //显示
        mPicker.showCityPicker();
    }


}

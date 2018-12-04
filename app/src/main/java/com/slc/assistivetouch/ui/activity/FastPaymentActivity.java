package com.slc.assistivetouch.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.slc.assistivetouch.R;
import com.slc.assistivetouch.model.payment.PaymentConstant;
import com.slc.code.ui.activity.BaseActivity;

public class FastPaymentActivity extends BaseActivity implements OnClickListener {

    public Object setLayout() {
        return R.layout.activity_fast_payment;
    }

    public void onBindView(@Nullable Bundle savedInstanceState) {
        setShowStatusBarEmbellishView(false);
        LinearLayout wx_scan = findViewById(R.id.wx_scan);
        wx_scan.setOnClickListener(this);
        LinearLayout wx_payment_code = findViewById(R.id.wx_payment_code);
        wx_payment_code.setOnClickListener(this);
        LinearLayout wx_collect_money_code = findViewById(R.id.wx_collect_money_code);
        wx_collect_money_code.setOnClickListener(this);
        LinearLayout alipay_scan = findViewById(R.id.alipay_scan);
        alipay_scan.setOnClickListener(this);
        LinearLayout alipay_payment_code = findViewById(R.id.alipay_payment_code);
        alipay_payment_code.setOnClickListener(this);
        LinearLayout alipay_collect_money_code = findViewById(R.id.alipay_collect_money_code);
        alipay_collect_money_code.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.alipay_collect_money_code:
                PaymentConstant.openAlipayPayment(PaymentConstant.ALIPAY_SHOUKUAN);
                break;
            case R.id.alipay_payment_code:
                PaymentConstant.openAlipayPayment(PaymentConstant.ALIPAY_FUKUAN);
                break;
            case R.id.alipay_scan:
                PaymentConstant.openAlipayPayment(PaymentConstant.ALIPAY_SCAN);
                break;
            case R.id.wx_collect_money_code:
                PaymentConstant.openWxPayment(PaymentConstant.WX_SHOUKUAN);
                break;
            case R.id.wx_payment_code:
                PaymentConstant.openWxPayment(PaymentConstant.WX_FUKUAN);
                break;
            case R.id.wx_scan:
                PaymentConstant.openWxPayment(PaymentConstant.WX_SCAN);
                break;
        }
        finish();
    }
}

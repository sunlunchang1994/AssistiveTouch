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
    private LinearLayout alipay_collect_money_code;
    private LinearLayout alipay_payment_code;
    private LinearLayout alipay_scan;
    private LinearLayout wx_collect_money_code;
    private LinearLayout wx_payment_code;
    private LinearLayout wx_scan;

    public Object setLayout() {
        return R.layout.activity_fast_payment;
    }

    public void onBindView(@Nullable Bundle savedInstanceState) {
        setShowStatusBarEmbellishView(false);
        this.wx_scan = findViewById(R.id.wx_scan);
        this.wx_scan.setOnClickListener(this);
        this.wx_payment_code = findViewById(R.id.wx_payment_code);
        this.wx_payment_code.setOnClickListener(this);
        this.wx_collect_money_code = findViewById(R.id.wx_collect_money_code);
        this.wx_collect_money_code.setOnClickListener(this);
        this.alipay_scan = findViewById(R.id.alipay_scan);
        this.alipay_scan.setOnClickListener(this);
        this.alipay_payment_code = findViewById(R.id.alipay_payment_code);
        this.alipay_payment_code.setOnClickListener(this);
        this.alipay_collect_money_code = findViewById(R.id.alipay_collect_money_code);
        this.alipay_collect_money_code.setOnClickListener(this);
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

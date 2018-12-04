package com.slc.assistivetouch.model.payment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.slc.assistivetouch.R;
import com.slc.assistivetouch.model.root.RootPerformer;
import com.slc.code.app.AppData;

import java.net.URISyntaxException;

public class PaymentConstant {
    public static final String ACTION_PAYMENT_ALIPAY = "com.slc.planet.payment.alipay";
    public static final String ACTION_PAYMENT_WX = "com.slc.planet.payment.wx";
    public static final int ALIPAY_FUKUAN = 4;
    public static final int ALIPAY_SCAN = 3;
    public static final int ALIPAY_SHOUKUAN = 5;
    public static final String PAYMENT_KEY = "paymentKey";
    public static final String[] PAYMENT_URL = new String[]{"com.tencent.mm.plugin.scanner.ui.BaseScanUI", "com.tencent.mm.plugin.offline.ui.WalletOfflineCoinPurseUI", "com.tencent.mm.plugin.collect.ui.CollectMainUI", "alipayqr://platformapi/startapp?saId=10000007", "alipayqr://platformapi/startapp?saId=20000056", "alipayqr://platformapi/startapp?saId=20000123"};
    public static final int[] SHORTCUT_ICONS = new int[]{R.mipmap.wx_scan, R.mipmap.wx_payment_code, R.mipmap.wx_collect_money_code, R.mipmap.alipay_scan, R.mipmap.alipay_payment_code, R.mipmap.alipay_collect_money_code};
    public static final String[] SHORTCUT_IDS = new String[]{"wx_scan", "wx_payment_code", "wx_collect_money_code", "alipay_scan", "alipay_payment_code", "alipay_collect_money_code"};
    public static final int[] SHORTCUT_TITLES = new int[]{R.string.label_scan, R.string.label_payment_code, R.string.label_collect_money_code, R.string.label_scan, R.string.label_payment_code, R.string.label_collect_money_code};
    public static final String START_WX_ACTIVITY = "am start -n com.tencent.mm/";
    public static final int WX_FUKUAN = 1;
    public static final int WX_SCAN = 0;
    public static final int WX_SHOUKUAN = 2;
    private static RootPerformer rootPerformer;

    public static void openWxPayment(int paymentId) {
        if (rootPerformer == null) {
            rootPerformer = RootPerformer.getInstance(null);
        }
        switch (paymentId) {
            case 0:
                rootPerformer.execCommand(START_WX_ACTIVITY + PAYMENT_URL[0]);
                return;
            case 1:
                rootPerformer.execCommand(START_WX_ACTIVITY + PAYMENT_URL[1]);
                return;
            case 2:
                rootPerformer.execCommand(START_WX_ACTIVITY + PAYMENT_URL[2]);
                return;
            default:
                return;
        }
    }

    public static void openAlipayPayment(int paymentId) {
        switch (paymentId) {
            case 3:
                startAliPayShortcutPay(AppData.getApplicationContext(), PAYMENT_URL[3]);
                return;
            case 4:
                startAliPayShortcutPay(AppData.getApplicationContext(), PAYMENT_URL[4]);
                return;
            case 5:
                startAliPayShortcutPay(AppData.getApplicationContext(), PAYMENT_URL[5]);
                return;
            default:
                return;
        }
    }

    private static void startAliPayShortcutPay(Context context, String parameter) {
        try {
            context.startActivity(Intent.parseUri(parameter, Intent.URI_INTENT_SCHEME));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.toast_no_inset_alipay, Toast.LENGTH_SHORT).show();
        } catch (URISyntaxException e2) {
        }
    }
}

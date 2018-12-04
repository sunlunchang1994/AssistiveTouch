package com.slc.assistivetouch.ui.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.widget.TextView;

import com.slc.assistivetouch.R;
import com.slc.assistivetouch.model.load_app.po.PackInfoItem;
import com.slc.code.ui.baseadapter.abslistview.CommonAdapter;
import com.slc.code.ui.baseadapter.abslistview.ViewHolder;
import com.slc.code.ui.utils.DimenUtil;

import java.util.List;

public class PackInfoAdapter extends CommonAdapter<PackInfoItem> {
    private int compoundDrawablePadding;

    public PackInfoAdapter(Context context, List<PackInfoItem> datas) {
        super(context, R.layout.item_fast_payment, datas);
        this.compoundDrawablePadding = DimenUtil.dip2px(context, 4.0f);
    }

    protected void convert(ViewHolder holder, PackInfoItem item, int position) {
        holder.setText(R.id.tv_name, item.getAppName());
        holder.setImageDrawable(R.id.iv_icon, item.getAppIcon());
    }
}

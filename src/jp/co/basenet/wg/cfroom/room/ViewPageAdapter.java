package jp.co.basenet.wg.cfroom.room;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Map;

import jp.co.basenet.wg.cfroom.R;
import jp.co.basenet.wg.cfroom.thread.ThreadSend;

public class ViewPageAdapter extends PagerAdapter {
    private static int itemSize = 5;
    private ThreadSend ts;
    private LayoutInflater inflater = null;
    private ArrayList<Map<String, Object>> items;

    public ViewPageAdapter(Context context, ThreadSend ts) {
        super();
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.ts = ts;
    }

    public void setListData(ArrayList<Map<String, Object>> data) {
        this.items = data;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LinearLayout layout = (LinearLayout)this.inflater.inflate(R.layout.view_page, null);

        int brt = 0;
        layout.setBackgroundColor(Color.rgb(brt, brt, brt));
        CanvasView img = (CanvasView)layout.findViewById(R.id.img_scroll);
        ProgressBar pb = (ProgressBar)layout.findViewById(R.id.progressBarForDownloadImage);
        pb.setVisibility(View.GONE);

        img.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                String message = String.format("A%dX%fY%f", event.getAction(), event.getX(), event.getY());
                Message msg = new Message();
                Bundle b = new Bundle();
                b.putString("sendbody", message);
                msg.setData(b);
                ts.myHandler.sendMessage(msg);
                return false;
            }
        });

        container.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager)container).removeView((View)object);
    }

    @Override
    public int getCount() {
        return itemSize;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    public static int N() {
        return itemSize;
    }

}

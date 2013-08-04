package jp.co.basenet.wg.cfroom.room;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

import jp.co.basenet.wg.cfroom.R;
import jp.co.basenet.wg.cfroom.login.LoginActivity;
import jp.co.basenet.wg.cfroom.thread.ThreadReveive;
import jp.co.basenet.wg.cfroom.thread.ThreadSend;

public class RoomActivity extends Activity {

    public MainHandler myHandler;
    public static int roomId;
    protected ThreadSend ts;
    protected ThreadReveive tr;
	public static SocketChannel sc;
    protected ViewPageAdapter vpa;
	/*
	public void setSocket(SocketChannel sc) {
		this.sc = sc;
	}*/

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        fileListRefresh();

        /*
        Button btn1 = (Button)findViewById(R.id.btnExit);
        btn1.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v){
        		try {
					sc.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					Intent intent = new Intent(RoomActivity.this, LoginActivity.class);
                    RoomActivity.this.startActivity(intent);
				}
            }
        });*/



    }
	
	public class MainHandler extends Handler {
		public MainHandler() {
		}
		
		public MainHandler(Looper L) {
			super(L);
		}
		
		@Override
		public void handleMessage(Message msg) {
			Log.d("MyHandler", "handleMessage....");
			super.handleMessage(msg);
			switch(msg.getData().getInt("statues")) {
			case 3:
                /*
				String message = msg.getData().getString("recvbody");
				int action = Integer.parseInt(message.substring(1, 2));
				float x = Float.parseFloat(message.substring(message.indexOf("X") + 1, message.indexOf("Y") - message.indexOf("X") + 2));
				float y = Float.parseFloat(message.substring(message.indexOf("Y") + 1));
				CanvasView cv = (CanvasView)findViewById(R.id.canvasView1);
				cv.drawFromSocket(action, x, y);*/
				break;
			default:
				break;
			}
		}
	}

    private void fileListRefresh() {
        new FileListRefreshAsyncTask(RoomActivity.this, roomId).execute();
    }
}


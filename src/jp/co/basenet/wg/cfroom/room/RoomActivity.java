package jp.co.basenet.wg.cfroom.room;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
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
	private ThreadSend ts;
	private ThreadReveive tr;
	public static SocketChannel sc;
	/*
	public void setSocket(SocketChannel sc) {
		this.sc = sc;
	}*/

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        
    	ts = new ThreadSend(sc);
    	ts.start();
    	tr = new ThreadReveive(sc, RoomActivity.this);
    	tr.start();
        
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
        });
        
		CanvasView cv = (CanvasView)findViewById(R.id.canvasView1);
		
		cv.setOnTouchListener(new OnTouchListener() {
			
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
        myHandler = new MainHandler();
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
				String message = msg.getData().getString("recvbody");
				int action = Integer.parseInt(message.substring(1, 2));
				float x = Float.parseFloat(message.substring(message.indexOf("X") + 1, message.indexOf("Y") - message.indexOf("X") + 2));
				float y = Float.parseFloat(message.substring(message.indexOf("Y") + 1));
				CanvasView cv = (CanvasView)findViewById(R.id.canvasView1);
				cv.drawFromSocket(action, x, y);
				break;
			default:
				break;
			}
		}
	}
}


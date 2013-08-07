package jp.co.basenet.wg.cfroom.thread;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class ThreadSend extends Thread{
	private SocketChannel sc;
	public ChildHandler myHandler;
    private boolean halt_;
	
	public ThreadSend(SocketChannel sc) {
		this.sc = sc;
	}

    public void halt() {
        halt_ = true;
        interrupt();
    }
	
	@Override
	public void run() {
		Looper.prepare();
        halt_ = false;
		myHandler = new ChildHandler();
		Looper.loop();
	}
	
	private void sendObj(Message msg) {
		Log.d("ThreadSend", "Start...");
		
		ByteBuffer buffer = null;
		try {
			byte[] strBody = msg.getData().getString("sendbody").getBytes("UTF-8");
			int status = 3;
			int length = strBody.length;
			
			buffer = ByteBuffer.allocate(8 + length);
			buffer.putInt(status);
			buffer.putInt(length);
			buffer.put(strBody);
			buffer.flip();
			
			sc.write(buffer);

		}
		catch (Exception e) {
			Log.d("ThreadSend", "socket.close");
		} 
	}
	
	public class ChildHandler extends Handler {
		public ChildHandler() {
			
		}
		
		public ChildHandler(Looper L) {
			super(L);
		}
		
		@Override
		public void handleMessage(Message msg) {
			Log.d("ChildHandler", "handleMessage....");
			super.handleMessage(msg);
			Log.d("ChildHandler_Output", msg.getData().getString("sendbody"));
			sendObj(msg);
		}
	}
}


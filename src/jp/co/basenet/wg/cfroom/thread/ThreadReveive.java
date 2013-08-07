package jp.co.basenet.wg.cfroom.thread;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import jp.co.basenet.wg.cfroom.room.RoomActivity;

public class ThreadReveive extends Thread{
	private SocketChannel sc;
	private RoomActivity mainThread;
    private boolean halt_;
	
	public ThreadReveive(SocketChannel sc,RoomActivity mainThread) {
		this.sc = sc;
		this.mainThread = mainThread;
	}
	
	@Override
	public void run() {
        halt_ = false;
		receiveObj(sc);
	}

    public void halt() {
        halt_ = true;
        interrupt();
    }
	
	private void receiveObj(SocketChannel sc) {
		Log.d("ThreadReceive", "Start...");
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try {
			//is = new BufferedInputStream(socket.getInputStream());
			buffer.clear();
			int numBytesRead;
			while(!halt_ && ((numBytesRead = sc.read(buffer)) != -1)) {
				if(numBytesRead == 0 ) {
					//
					continue;
				}
				buffer.flip();
				while(buffer.remaining() > 0) {
					int statue = buffer.getInt();
					int length = buffer.getInt();
					byte[] bytes = new byte[length];
					buffer.get(bytes);
					String body = new String(bytes, "UTF-8");
	            	Bundle b = new Bundle();
	            	b.putInt("statues", statue);
	            	b.putInt("length", length);
	            	b.putString("recvbody", body);
	            	Message msg = new Message();
	            	msg.setData(b);
	            	mainThread.myHandler.sendMessage(msg);
				}
            	buffer.clear();
			}
		} catch (Exception e) {
			Log.d("ThreadReceive", "socket.close");
            try {
                if(null != sc) {
                    sc.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
		}
	}
}

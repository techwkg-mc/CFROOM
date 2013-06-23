package jp.co.basenet.wg.cfroom.login;

import java.nio.channels.SocketChannel;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import jp.co.basenet.wg.cfroom.R;
import jp.co.basenet.wg.cfroom.thread.ThreadReveive;
import jp.co.basenet.wg.cfroom.thread.ThreadSend;

public class LoginActivity extends Activity {
	
	private ThreadSend ts;
	private ThreadReveive tr;
	private SocketChannel sc;
	
	public void setSocket(SocketChannel sc) {
		this.sc = sc;
	}
	
	public SocketChannel getSocket() {
		return this.sc;
	}
	
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_login);

	        Button btn1 = (Button)findViewById(R.id.btnLogin);
	        btn1.setOnClickListener(new View.OnClickListener() {
	        	@Override
	            public void onClick(View v){ 
	        		//socket
	            	new LoginAsyncTask(LoginActivity.this).execute();
	            }
	        });
	 }
	
}

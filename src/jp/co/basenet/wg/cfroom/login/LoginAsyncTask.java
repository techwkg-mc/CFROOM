package jp.co.basenet.wg.cfroom.login;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import jp.co.basenet.wg.cfroom.lobby.LobbyActivity;
import jp.co.basenet.wg.cfroom.R;

public class LoginAsyncTask extends AsyncTask<Integer, Integer, Integer >{
	 private LoginActivity loginThread;

	 public LoginAsyncTask(LoginActivity loginThread) {
		 this.loginThread = loginThread;
	 }

	 @Override
	 protected void onPostExecute(Integer result){
		 if(result == 0) {
			 //失敗
			 Log.d("LOGIN", "FAILURE!!");
			 Toast.makeText(loginThread, "ログイン失敗!!", Toast.LENGTH_SHORT).show();
		 } else {
			 //成功
			Log.d("LOGIN", "SUCCESS!!");
			Toast.makeText(loginThread, "ログイン成功!!", Toast.LENGTH_SHORT).show();
			
			Intent intent = new Intent(loginThread, LobbyActivity.class);
            LobbyActivity.sc = loginThread.getSocket();
			loginThread.startActivity(intent);
		 }
	 }

	 @Override
	 protected Integer doInBackground(Integer... arg0) {
		 String address =
             ((EditText) loginThread.findViewById(R.id.txtIPAdress)).getText().toString();
         String strPort =
             ((EditText) loginThread.findViewById(R.id.txtPort)).getText().toString();
         int port = Integer.parseInt(strPort);

         SocketChannel sc = null;

         try {
        	 sc = SocketChannel.open();
        	 sc.configureBlocking(false);
        	 sc.connect(new InetSocketAddress(address, port));
        	 
        	 while(!sc.finishConnect()) {}

        	 //ログイン
        	 byte[] userName = ((EditText) loginThread.findViewById(R.id.txtUserName)).getText().toString().getBytes(Charset.forName("UTF-8"));
        	 byte[] password = ((EditText) loginThread.findViewById(R.id.txtPassword)).getText().toString().getBytes(Charset.forName("UTF-8"));
        	 int status = 0000;
        	 int allLength = 8 + userName.length + password.length;
        	 int userNameLength = userName.length;
        	 int passwordLength = password.length;
        	 
        	 ByteBuffer buffer = ByteBuffer.allocate(16 + userNameLength + passwordLength);
        	 buffer.putInt(status);
        	 buffer.putInt(allLength);
        	 buffer.putInt(userNameLength);
        	 buffer.put(userName);
        	 buffer.putInt(passwordLength);
        	 buffer.put(password);
        	 buffer.flip();
        	 
        	 //送信
        	 sc.write(buffer);
        	 
        	 //結果取得
        	 buffer = ByteBuffer.allocate(1024);
        	 int numBytesRead;
        	 while((numBytesRead = sc.read(buffer)) != -1) {
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
					if(statue == 0 && "SUCCESS".equals(body)) {
						loginThread.setSocket(sc);
						return(1);
					} else {
						sc.close();
						loginThread.setSocket(null);
						return(0);
					}
				}
        	 }
        	 return(0);
         } catch (Exception e) {
         	Log.d("LoginAsyncTask", e.getMessage());
         	if(sc != null) {
         		try {
					sc.close();
				} catch (IOException ioex) {
					ioex.printStackTrace();
				}catch (Exception ex) {
					ex.printStackTrace();
				}
         	}
         	return(0);
         }      
	 }
}
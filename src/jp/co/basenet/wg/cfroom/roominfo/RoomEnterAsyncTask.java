package jp.co.basenet.wg.cfroom.roominfo;

import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import jp.co.basenet.wg.cfroom.room.RoomActivity;

public class RoomEnterAsyncTask extends AsyncTask<Integer, Integer, Integer > {
    RoomInfoActivity mainThread;
    private int roomId;

    public RoomEnterAsyncTask(RoomInfoActivity mainThread, int roomId) {
        this.mainThread = mainThread;
        this.roomId = roomId;
    }

    @Override
    protected void onPostExecute(Integer result){
        if(result == 0) {
            //入室できない
            Toast.makeText(mainThread, "入室できません!!", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(mainThread, RoomActivity.class);
            RoomActivity.sc = mainThread.sc;
            RoomActivity.roomId = roomId;
            mainThread.startActivity(intent);
        }
    }

    @Override
    protected Integer doInBackground(Integer... arg0) {
        int status = 1103;
        byte[] message = String.valueOf(this.roomId).getBytes(Charset.forName("UTF-8"));

        int allLength = message.length;

        try {
            ByteBuffer buffer = ByteBuffer.allocate(8 + allLength);
            buffer.putInt(status);
            buffer.putInt(allLength);
            buffer.put(message);
            buffer.flip();

            //送信
            mainThread.sc.write(buffer);

            //結果取得
            //結果取得
            buffer = ByteBuffer.allocate(1024*10);
            int numBytesRead;
            while((numBytesRead = mainThread.sc.read(buffer)) != -1) {
                if(numBytesRead == 0 ) {
                    continue;
                }
                buffer.flip();
                while(buffer.remaining() > 0) {
                    status = buffer.getInt();
                    if(status != 1203) {
                        //TODO
                    }
                    int length = buffer.getInt();
                    byte[] bytes = new byte[length];
                    buffer.get(bytes);
                    String result = new String(bytes, "UTF-8");
                    if("SUCCESS".equals(result)) {
                        return(1);
                    } else {
                        //TODO
                        //パスワード？
                        return(0);
                    }
                }
            }
            return(0);
        } catch (Exception e) {
            if(mainThread.sc != null) {
                try {
                    mainThread.sc.close();
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

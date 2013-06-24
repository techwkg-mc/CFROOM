package jp.co.basenet.wg.cfroom.lobby;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import jp.co.basenet.wg.cfroom.beans.RoomDetailInfo;
import jp.co.basenet.wg.cfroom.roominfo.RoomInfoActivity;

public class RoomKnockAsyncTask extends AsyncTask<Integer, Integer, Integer > {
    private LobbyActivity mainThread;
    private RoomDetailInfo roomDetailInfo;
    private int roomId;

    public RoomKnockAsyncTask(LobbyActivity mainThread, int roomId) {
        this.mainThread = mainThread;
        this.roomId = roomId;
    }

    @Override
    protected void onPostExecute(Integer result){
        Intent intent = new Intent(mainThread, RoomInfoActivity.class);
        RoomInfoActivity.sc = mainThread.sc;
        RoomInfoActivity.roomDetailInfo = roomDetailInfo;
        mainThread.startActivity(intent);
    }

    @Override
    protected Integer doInBackground(Integer... arg0) {

        int status = 1102;
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
            buffer = ByteBuffer.allocate(1024*10);
            int numBytesRead;
            while((numBytesRead = mainThread.sc.read(buffer)) != -1) {
                if(numBytesRead == 0 ) {
                    continue;
                }
                buffer.flip();
                while(buffer.remaining() > 0) {
                    int statue = buffer.getInt();
                    if(status != 1202) {
                        //TODO
                    }
                    int length = buffer.getInt();
                    byte[] bytes = new byte[length];
                    buffer.get(bytes);
                    String roomDetailInfoJson = new String(bytes, "UTF-8");
                    Type roomDetailInfoType = new TypeToken<RoomDetailInfo>(){}.getType();
                    roomDetailInfo = new Gson().fromJson(roomDetailInfoJson, roomDetailInfoType);
                    return(1);
                }
            }
            return(0);
        } catch (Exception e) {
            Log.d("RoomKnockAsyncTask", e.getMessage());
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

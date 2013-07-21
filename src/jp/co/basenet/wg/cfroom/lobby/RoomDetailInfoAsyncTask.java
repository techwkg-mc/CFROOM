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
import java.util.HashMap;

import jp.co.basenet.wg.cfroom.beans.RoomDetailInfo;
import jp.co.basenet.wg.cfroom.detailinfo.RoomInfoActivity;

public class RoomDetailInfoAsyncTask extends AsyncTask<Integer, Integer, Integer > {
    private LobbyActivity mainThread;
    private RoomDetailInfo roomDetailInfo;
    private int roomId;

    public RoomDetailInfoAsyncTask(LobbyActivity mainThread, int roomId) {
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

        //TODO
        //パスワード？？追加予定
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
            boolean firstRecord = true;
            int seqNo = 0;
            int recordCount = 0;
            byte[] tempRecord = null;
            byte[] tempResult = null;
            HashMap<Integer, byte[]> resultMap = new HashMap<Integer, byte[]>();
            while(mainThread.sc.read(buffer) != -1) {
                if(buffer.position() < 532) {
                    continue;
                }
                buffer.flip();
                while(buffer.remaining() >= 532) {
                    status = buffer.getInt();
                    buffer.getInt();//currentSize
                    buffer.getInt();//fullSize
                    seqNo = buffer.getInt();
                    recordCount = buffer.getInt();
                    tempRecord = new byte[512];
                    buffer.get(tempRecord);
                    if(status != 1202) {
                        continue;
                    }
                    if(firstRecord) {
                        //一つ目のレコード
                        tempResult = new byte[512 * recordCount];
                        firstRecord = false;
                    }
                    resultMap.put(seqNo, tempRecord);
                }

                buffer.compact();
                if(resultMap.size() == recordCount) {
                    break;
                }
            }
            if(resultMap.size() > 0) {
                for(int i = 0; i < resultMap.size(); i++ ) {
                    System.arraycopy(resultMap.get(i + 1), 0, tempResult, 512 * i, 512);
                }

                String roomDetailInfoJson = new String(tempResult, Charset.forName("UTF-8")).trim();
                Type roomDetailInfoType = new TypeToken<RoomDetailInfo>(){}.getType();
                roomDetailInfo = new Gson().fromJson(roomDetailInfoJson, roomDetailInfoType);
                return(1);
            }
        } catch (Exception e) {
            Log.d("RoomDetailInfoAsyncTask", e.getMessage());
            if(mainThread.sc != null) {
                try {
                    mainThread.sc.close();
                } catch (IOException ioex) {
                    ioex.printStackTrace();
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return(0);
    }
}

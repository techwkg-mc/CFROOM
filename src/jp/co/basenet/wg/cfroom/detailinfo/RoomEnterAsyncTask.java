package jp.co.basenet.wg.cfroom.detailinfo;

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
            buffer = ByteBuffer.allocate(1064);
            while(mainThread.sc.read(buffer) != -1) {
                if(buffer.position() < 532) {
                    continue;
                }
                buffer.flip();
                status = buffer.getInt();
                buffer.getInt();//currentSize
                buffer.getInt();//fullSize
                buffer.getInt();//seqNo;
                buffer.getInt();//recordCount
                byte[] tempRecord = new byte[512];
                buffer.get(tempRecord);
                buffer.compact();
                if(status != 1203) {
                    continue;
                }
                String result = new String(tempRecord, Charset.forName("UTF-8")).trim();
                if("SUCCESS".equals(result)) {
                    return(1);
                } else {
                    break;
                }
            }
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
        }
        return(0);
    }
}

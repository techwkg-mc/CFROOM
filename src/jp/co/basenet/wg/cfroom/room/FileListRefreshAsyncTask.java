package jp.co.basenet.wg.cfroom.room;

import android.os.AsyncTask;
import android.provider.CallLog;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

import jp.co.basenet.wg.cfroom.beans.FileDetailInfo;

public class FileListRefreshAsyncTask extends AsyncTask<Integer, Integer, Integer> {
    RoomActivity mainThread;
    private int roomId;
    private ArrayList<FileDetailInfo> fileDetailInfoList;

    public FileListRefreshAsyncTask(RoomActivity mainThread, int roomId) {
        this.mainThread = mainThread;
        this.roomId = roomId;
    }


    @Override
    protected void onPostExecute(Integer result){

    }

    @Override
    protected Integer doInBackground(Integer... arg0) {

        int status;
        byte[] message;
        int allLength;
        int numBytesRead;
        Gson gson = new Gson();
        int length;

        try {
            //2101 -- ファイル一覧取得リクエスト
            status = 2101;
            message = String.valueOf(this.roomId).getBytes(Charset.forName("UTF-8"));
            allLength = message.length;

            ByteBuffer buffer = ByteBuffer.allocate(8 + allLength);
            buffer.putInt(status);
            buffer.putInt(allLength);
            buffer.put(message);
            buffer.flip();

            //送信
            mainThread.sc.write(buffer);

            //結果取得
            buffer = ByteBuffer.allocate(1024*10);
            while((numBytesRead = mainThread.sc.read(buffer)) != -1) {
                if(numBytesRead == 0 ) {
                    continue;
                }
                buffer.flip();
                status = buffer.getInt();
                if(status != 2201) {
                    //TODO
                }
                length = buffer.getInt();
                byte[] bytes = new byte[length];
                buffer.get(bytes);
                String fileDetailInfoJson = new String(bytes, "UTF-8");
                Type listType = new TypeToken<ArrayList<FileDetailInfo>>(){}.getType();
                fileDetailInfoList = new Gson().fromJson(fileDetailInfoJson, listType);
                break;
            }

            //2102 -- ファイル転送リクエスト
            for(int i = 0; i < fileDetailInfoList.size(); i++ ) {
                status = 2102;
                message = gson.toJson(fileDetailInfoList.get(i)).getBytes(Charset.forName("UTF-8"));
                allLength = message.length;
                buffer = ByteBuffer.allocate(8 + allLength);
                buffer.putInt(status);
                buffer.putInt(allLength);
                buffer.put(message);
                buffer.flip();

                //送信
                mainThread.sc.write(buffer);

                //結果取得
                //TODO データベースを使うべし
                buffer = ByteBuffer.allocate(1024*100);
                while((numBytesRead = mainThread.sc.read(buffer)) != -1) {
                    if(numBytesRead == 0 ) {
                        continue;
                    }
                    buffer.flip();
                    status = buffer.getInt();
                    if(status != 2202) {
                        //TODO
                    }
                    length = buffer.getInt();
                    byte[] bytes = new byte[length];
                    buffer.get(bytes);
                    String xxx = new String(bytes);
                    Log.d("00000","00000");
                }
            }

            return (0);
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

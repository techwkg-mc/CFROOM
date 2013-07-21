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
import java.util.HashMap;

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
            buffer = ByteBuffer.allocate(532*10);
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
                    if(status != 2201) {
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
                //取得したファイルリストを元にファイルを取得
                for(int i = 0; i < resultMap.size(); i++ ) {
                    System.arraycopy(resultMap.get(i + 1), 0, tempResult, 512 * i, 512);
                }

                String roomButtonListJson = new String(tempResult, Charset.forName("UTF-8")).trim();
                Type listType = new TypeToken<ArrayList<FileDetailInfo>>(){}.getType();
                fileDetailInfoList = new Gson().fromJson(roomButtonListJson, listType);


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
                    firstRecord = true;
                    byte[] bytes = null;
                    int currentSize = 0;
                    int recordSize = 0;
                    buffer = ByteBuffer.allocate(1024);
                    while((numBytesRead = mainThread.sc.read(buffer)) != -1) {
                        if(numBytesRead == 0 ) {
                            continue;
                        }
                        buffer.flip();
                        if(firstRecord) {
                            status = buffer.getInt();
                            if(status != 2202) {
                                //TODO
                            }
                            length = buffer.getInt();
                            bytes = new byte[length];
                            firstRecord = false;
                        }
                        currentSize = buffer.remaining();




                        // length alllength 追加必要



                        buffer.get(bytes, recordSize, currentSize);
                        //buffer.get(bytes);
                        recordSize += currentSize;
                        buffer.clear();
                    }
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

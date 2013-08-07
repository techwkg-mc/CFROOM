package jp.co.basenet.wg.cfroom.room;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import jp.co.basenet.wg.cfroom.R;
import jp.co.basenet.wg.cfroom.beans.FileDetailInfo;
import jp.co.basenet.wg.cfroom.thread.ThreadReveive;
import jp.co.basenet.wg.cfroom.thread.ThreadSend;

public class FileListRefreshAsyncTask extends AsyncTask<Integer, Integer, Integer> {
    RoomActivity mainThread;
    private int roomId;
    private ArrayList<FileDetailInfo> fileDetailInfoList;

    //todo
    ArrayList<Bitmap> bitmapList = new ArrayList<Bitmap>();

    public FileListRefreshAsyncTask(RoomActivity mainThread, int roomId) {
        this.mainThread = mainThread;
        this.roomId = roomId;
    }


    @Override
    protected void onPostExecute(Integer result){
        mainThread.ts = new ThreadSend(mainThread.sc);
        mainThread.ts.start();
        mainThread.tr = new ThreadReveive(mainThread.sc, mainThread);
        mainThread.tr.start();

        CustomViewPager vp = (CustomViewPager)mainThread.findViewById(R.id.canvasView1);
        mainThread.vpa = new ViewPageAdapter(mainThread, mainThread.ts);

        for(Bitmap bitmap : bitmapList ) {
            mainThread.vpa.add(bitmap);
            mainThread.vpa.notifyDataSetChanged();
        }

        vp.setAdapter(mainThread.vpa);
        int currentPosition = 0;
        vp.setCurrentItem(currentPosition);
        vp.setScanScroll(true);
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
                    buffer = ByteBuffer.allocate(1024*10);
                    firstRecord = true;
                    int currentSize = 0;
                    int fullSize = 0;
                    seqNo = 0;
                    recordCount = 0;
                    tempRecord = null;
                    tempResult = null;
                    resultMap = new HashMap<Integer, byte[]>();
                    while(mainThread.sc.read(buffer) != -1) {
                        if(buffer.position() < 532) {
                            continue;
                        }
                        buffer.flip();
                        while(buffer.remaining() >= 532) {
                            status = buffer.getInt();
                            currentSize = buffer.getInt();//currentSize
                            fullSize = buffer.getInt();//fullSize
                            seqNo = buffer.getInt();
                            recordCount = buffer.getInt();
                            tempRecord = new byte[512];
                            buffer.get(tempRecord);
                            if(status != 2202) {
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
                        for(int j = 0; j < resultMap.size(); j++ ) {
                            System.arraycopy(resultMap.get(j + 1), 0, tempResult, 512 * j, 512);
                        }
                        //ここは修正必要
                        //TODO
                        BitmapFactory.Options bmfOptions = new BitmapFactory.Options();
                        bmfOptions.inPurgeable = true;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(tempResult, 0, tempResult.length,bmfOptions);
                        bitmapList.add(bitmap);
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

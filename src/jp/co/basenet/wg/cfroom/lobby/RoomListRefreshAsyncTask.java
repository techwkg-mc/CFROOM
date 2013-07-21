package jp.co.basenet.wg.cfroom.lobby;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import jp.co.basenet.wg.cfroom.R;
import jp.co.basenet.wg.cfroom.beans.RoomButtonInfo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class RoomListRefreshAsyncTask extends AsyncTask<Integer, Integer, Integer > {
    private LobbyActivity mainThread;
    private ArrayList<RoomButtonInfo> roomButtonList;

    public RoomListRefreshAsyncTask(LobbyActivity mainThread) {
        this.mainThread = mainThread;
    }

    @Override
    protected void onPostExecute(Integer result){
        if(result == 0) {
            //失敗
            //TODO

        } else {
            ScrollView sv = (ScrollView)mainThread.findViewById(R.id.scRoomList);
            sv.removeAllViews();
            //TEST
            LinearLayout linearLayout = new LinearLayout(mainThread);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            for (int i = 0; i < roomButtonList.size(); i++) {
                Button button = new Button(mainThread);
                button.setText("Room:" + roomButtonList.get(i).getRoomName() + "Status:" + roomButtonList.get(i).getStatus());
                final int roomId = roomButtonList.get(i).getId();
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        new RoomDetailInfoAsyncTask(mainThread, roomId).execute();
                    }
                });

                linearLayout.addView(button);
            }

            // ScrollView に View を追加
            sv.addView(linearLayout);
            sv.fullScroll(View.FOCUS_UP);
        }
    }

    @Override
    protected Integer doInBackground(Integer... arg0) {

        int status = 1101;
        byte[] message = "ROOM_LIST_REFRESH".getBytes(Charset.forName("UTF-8"));

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
                    if(status != 1201) {
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

                String roomButtonListJson = new String(tempResult, Charset.forName("UTF-8")).trim();
                Type listType = new TypeToken<ArrayList<RoomButtonInfo>>(){}.getType();
                roomButtonList = new Gson().fromJson(roomButtonListJson, listType);
                return(1);
            }
        } catch (Exception e) {
            Log.d("RoomListRefreshAsyncTask", e.getMessage());
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

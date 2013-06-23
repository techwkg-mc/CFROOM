package jp.co.basenet.wg.cfroom.lobby;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

import jp.co.basenet.wg.cfroom.R;
import jp.co.basenet.wg.cfroom.beans.RoomButtonInfo;

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

            //TEST
            LinearLayout linearLayout = new LinearLayout(mainThread);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            for (int i = 0; i < roomButtonList.size(); i++) {
                Button button = new Button(mainThread);
                button.setText("Room:" + roomButtonList.get(i).getRoomName() + "Status:" + roomButtonList.get(i).getStatus() );
                linearLayout.addView(button);
            }

            // ScrollView に View を追加
            sv.addView(linearLayout);
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
            buffer = ByteBuffer.allocate(1024*10);
            int numBytesRead;
            while((numBytesRead = mainThread.sc.read(buffer)) != -1) {
                if(numBytesRead == 0 ) {
                    continue;
                }
                buffer.flip();
                while(buffer.remaining() > 0) {
                    int statue = buffer.getInt();
                    if(status != 1201) {
                        //TODO
                    }
                    int length = buffer.getInt();
                    byte[] bytes = new byte[length];
                    buffer.get(bytes);
                    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    roomButtonList = (ArrayList<RoomButtonInfo>)ois.readObject();
                    return(1);
                }
            }
            return(0);
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
            return(0);
        }

/*
        roomButtonList = new ArrayList<RoomButtonInfo>();
        for(int i = 0; i < 10; i++) {
            RoomButtonInfo rbi = new RoomButtonInfo();
            rbi.setId(i);
            rbi.setRoomName("ダミルーム" + i);
            rbi.setStatus("進行中");
            roomButtonList.add(rbi);
        }*/
    }
}

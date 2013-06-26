package jp.co.basenet.wg.cfroom.roominfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.nio.channels.SocketChannel;

import jp.co.basenet.wg.cfroom.R;
import jp.co.basenet.wg.cfroom.beans.RoomDetailInfo;
import jp.co.basenet.wg.cfroom.lobby.LobbyActivity;

public class RoomInfoActivity extends Activity {
    public static SocketChannel sc;
    //TODO
    //よくない
    public static RoomDetailInfo roomDetailInfo;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roominfo);

        //画面設定
        TextView textview;
        textview = (TextView)findViewById(R.id.meetingName);
        textview.setText("会議名称：　" + roomDetailInfo.getMeetingName());
        textview = (TextView)findViewById(R.id.locate);
        textview.setText("開催場所：　" + roomDetailInfo.getLocate());
        textview = (TextView)findViewById(R.id.startTime);
        textview.setText("開催時刻：　" + roomDetailInfo.getStartTime());
        textview = (TextView)findViewById(R.id.endTime);
        textview.setText("終了時刻：　" + roomDetailInfo.getEndTime());
        textview = (TextView)findViewById(R.id.chairManName);
        textview.setText("主催者：　　" + roomDetailInfo.getChairManName());

        //ロビーに戻る
        Button btnBackToLobby = (Button)findViewById(R.id.btnBackToLobby);
        btnBackToLobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(RoomInfoActivity.this, LobbyActivity.class);
                LobbyActivity.sc = RoomInfoActivity.sc;
                RoomInfoActivity.this.startActivity(intent);
            }
        });


        //入室
        //TODORoomEnterAsyncTask
        Button btnEnterRoom = (Button)findViewById(R.id.btnEnterRoom);
        btnEnterRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                new RoomEnterAsyncTask(RoomInfoActivity.this, roomDetailInfo.getId()).execute();
            }
        });

    }
}

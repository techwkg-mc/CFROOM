package jp.co.basenet.wg.cfroom.lobby;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import jp.co.basenet.wg.cfroom.R;
import jp.co.basenet.wg.cfroom.login.LoginActivity;

public class LobbyActivity extends Activity{
    public static SocketChannel sc;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        //画面リフレッシュ
        refresh();

        //ログアウトボタン
        Button btnLogout = (Button)findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                try {
                    sc.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    Intent intent = new Intent(LobbyActivity.this, LoginActivity.class);
                    LobbyActivity.this.startActivity(intent);
                }
            }
        });

        //フレッシュボタン
        Button btnFresh = (Button)findViewById(R.id.btnFresh);
        btnFresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //TODO

            }
        });
    }

    private void refresh() {
        new RoomListRefreshAsyncTask(LobbyActivity.this).execute();
    }
}

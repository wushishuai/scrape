package com.germey.andservertest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;

import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private TextView textView;
    private Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.toggle_server);
        textView = findViewById(R.id.server_status);
        server = AndServer.webServer(getApplicationContext())
                .port(8080)
                .timeout(10, TimeUnit.SECONDS)
                .listener(new Server.ServerListener() {
                    @Override
                    public void onStarted() {
                        button.setText(R.string.stop_server);
                        textView.setText(R.string.server_started);
                    }

                    @Override
                    public void onStopped() {
                        button.setText(R.string.start_server);
                        textView.setText(R.string.server_stoped);

                    }

                    @Override
                    public void onException(Exception e) {
                        Log.d("AndServer",e.toString());

                    }
                })
                .build();
        button.setText(R.string.start_server);
        textView.setText(R.string.server_stoped);
    }
    public void toggleServer(View view){
        if (!server.isRunning()){
            server.startup();
        }else {
            server.shutdown();
        }
    }
}
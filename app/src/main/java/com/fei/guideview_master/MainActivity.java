package com.fei.guideview_master;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.fei.guideview.GuideView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.guideView)
    GuideView guideView;
    @BindView(R.id.btn_get_address)
    Button btn_get_address;
    @BindView(R.id.btn_post_friend_circle)
    Button btn_post_friend_circle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        guideView.setTargetView(btn_get_address,btn_post_friend_circle);
    }
}

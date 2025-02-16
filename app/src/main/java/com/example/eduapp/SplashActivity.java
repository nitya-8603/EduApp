package com.example.eduapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eduapp.R;

public class SplashActivity extends AppCompatActivity {

    Animation top_anim,bottom_anim;
    ImageView image;
    TextView logo,slogan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        image=findViewById(R.id.imageView);
        logo=findViewById(R.id.textView);
        slogan=findViewById(R.id.textView2);
        top_anim= AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottom_anim=AnimationUtils.loadAnimation(this,R.anim.bottom_animation);
        image.setAnimation(top_anim);
        logo.setAnimation(bottom_anim);
        slogan.setAnimation(bottom_anim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(SplashActivity.this,Register.class);
                startActivity(intent);
                finish();
            }
        },3000);

    }
}
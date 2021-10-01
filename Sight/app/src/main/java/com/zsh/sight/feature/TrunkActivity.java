package com.zsh.sight.feature;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zsh.sight.R;

public class TrunkActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    NavController navController;
    Context context;
    boolean user_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if(intent != null){
            int type = intent.getIntExtra("type", 0);
            user_type = type == 0;
        }
        setContentView(user_type ? R.layout.activity_trunk: R.layout.activity_trunk2);
        context = this;
        initView();
    }

    private void initView(){
        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.inflateMenu(user_type?R.menu.bottom_nav_menu:R.menu.bottom_nav_menu2);
        navController = Navigation.findNavController(this, R.id.fragment_container_view);
        //建立绑定关系
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }
}
package com.zsh.sight.feature;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zsh.sight.R;

public class TrunkActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    NavController navController;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trunk);
        context = this;
        initView();
    }

    private void initView(){
        bottomNavigationView = findViewById(R.id.bottom_nav);
        navController = Navigation.findNavController(this, R.id.fragment_container_view);
        //建立绑定关系
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }
}
package com.zsh.sight.feature;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.idlestar.ratingstar.RatingStarView;
import com.zsh.sight.MyApplication;
import com.zsh.sight.R;
import com.zsh.sight.Utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class ScoringActivity extends AppCompatActivity {

    private RatingStarView rsv_rating;
    private MaterialButton materialButton;
    private MyApplication myApplication;
    private String username;
    private double during;
    private String other = null;
    private String url1 = "http://121.5.169.147:8000/updateScore";
    private String url2 = "http://121.5.169.147:8000/updateBTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoring);
        myApplication = (MyApplication) getApplication();
        username = myApplication.getUsername();
        Intent intent = getIntent();
        if(intent != null){
            during = intent.getDoubleExtra("during", 0);
            other = intent.getStringExtra("other");
        }
        init();
    }

    private void init(){
        rsv_rating = (RatingStarView) findViewById(R.id.rating_view);
        materialButton = (MaterialButton) findViewById(R.id.confirm_score);
        materialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(other != null && !other.equals("")){
                    JSONObject jsonObject1 = new JSONObject();
                    JSONObject jsonObject2 = new JSONObject();
                    try {
                        jsonObject1.put("username", other);
                        jsonObject1.put("tscore", rsv_rating.getRating());
                        String data = HttpUtils.getJsonData(jsonObject1, url1);
                        Log.e("####", "in Scoring return " + data);
                        jsonObject2.put("username", username);
                        jsonObject2.put("tscore", during);
                        data = HttpUtils.getJsonData(jsonObject2, url2);
                        Log.e("####", "in Blind +" + data);
                        Toast.makeText(ScoringActivity.this, "评分成功", Toast.LENGTH_LONG).show();
                    } catch (JSONException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                finish();
            }
        });
    }
}
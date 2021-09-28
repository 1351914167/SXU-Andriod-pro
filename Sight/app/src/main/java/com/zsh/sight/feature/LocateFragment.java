package com.zsh.sight.feature;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.zsh.sight.R;
import com.zsh.sight.Utils.NavigationIconClickListener;


public class LocateFragment extends Fragment {
    private View view;
    private TrunkActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_locate, container, false);
        mActivity = (TrunkActivity)getActivity();
        setUpToolbar(view);
        return view;
    }
    //导航栏控制
    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.app_bar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }

        toolbar.setNavigationOnClickListener(new NavigationIconClickListener(
                getContext(),
                view.findViewById(R.id.map_view),
                new AccelerateDecelerateInterpolator(),
                getContext().getResources().getDrawable(R.drawable.shr_menu),
                getContext().getResources().getDrawable(R.drawable.ic_close)));
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.shr_toolbar_menu, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }


}

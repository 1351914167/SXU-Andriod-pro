package com.example.test.feature;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;


import com.example.test.MainActivity;
import com.example.test.R;
import com.example.test.adapter.Link;
import com.example.test.adapter.LinkAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LinkFragment extends Fragment implements AdapterView.OnItemClickListener, LinkAdapter.InnerItemOnclickListener {
    private View view;
    private List<Link> linkList = new ArrayList<>();
    private ListView linkListView;

    private LinkAdapter linkAdapter;
    private MainActivity mActivity;
    private ImageView back;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_link, container, false);
        mActivity = (MainActivity)getActivity();
        initView();
        refreshLinks();
        linkAdapter.notifyDataSetChanged();
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        Log.e("### ", linkList.get(pos).getName());
    }

    @Override
    public void itemClick(View v) {

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        // 如果重新在最前端显示，执行刷新操作
        if (!hidden){
            refreshLinks();
        }
    }

    private void initView(){
        linkListView = (ListView) view.findViewById(R.id.link_list);
        linkAdapter = new LinkAdapter(linkList, mActivity);
        linkAdapter.setOnInnerItemOnClickListener(this);
        linkListView.setAdapter(linkAdapter);
        linkListView.setOnItemClickListener(this);

        back = view.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Toast.makeText(mActivity, "link", Toast.LENGTH_SHORT).show();
                mActivity.showFragment("EquipFragment");
            }
        });
    }

    /* 应该从服务器获得社区数据，现在是通过数据库模拟 ！！！ */
    private void refreshLinks(){
        linkList.clear();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null) {
            mBluetoothAdapter.enable();
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if(pairedDevices.size() > 0){
                for(BluetoothDevice device:pairedDevices){
                    // 把名字和地址取出来添加到列表中
                    linkList.add(new Link(device.getName(), device.getAddress()));
                }
            }
        }
        linkAdapter.notifyDataSetChanged();
    }
}

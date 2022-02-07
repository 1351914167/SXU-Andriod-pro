package com.zsh.sight.recruit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zsh.sight.R;

import java.util.List;

public class JobAdapter extends android.widget.BaseAdapter {
    private LayoutInflater inflater;
    private List<Job> jobList;
    private TextView tv_position, tv_salary, tv_number;
    private TextView tv_company, tv_address, tv_contact, tv_require;

    public JobAdapter(Context mContext, List<Job> diaryList) {
        this.jobList = diaryList;
        inflater = LayoutInflater.from(mContext);
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = inflater.inflate(R.layout.job_item, parent,false);

        // 绑定组件
        tv_position = (TextView) convertView.findViewById(R.id.position);
        tv_salary = (TextView) convertView.findViewById(R.id.salary);
        tv_number = (TextView) convertView.findViewById(R.id.number);
        tv_company = (TextView) convertView.findViewById(R.id.company);
        tv_address = (TextView) convertView.findViewById(R.id.address);
        tv_contact = (TextView) convertView.findViewById(R.id.contact);
        tv_require = (TextView) convertView.findViewById(R.id.require);

        // 设置文本属性
        tv_position.setText(jobList.get(position).getPosition());
        tv_salary.setText("￥" + jobList.get(position).getSalary() + "/人");
        tv_number.setText("需要" + jobList.get(position).getNumber() + "人");
        tv_company.setText("公司：" + jobList.get(position).getCompany());
        tv_address.setText("工作地址：" + jobList.get(position).getAddress());
        tv_contact.setText("联系方式：" + jobList.get(position).getContact());
        tv_require.setText("岗位描述：" + jobList.get(position).getRequire());

        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return jobList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return jobList.size();
    }
}

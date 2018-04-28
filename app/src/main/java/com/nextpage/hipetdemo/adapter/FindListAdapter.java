package com.nextpage.hipetdemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nextpage.hipetdemo.R;
import com.nextpage.hipetdemo.model.Report;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by jacobsFactory on 2017-12-01.
 */

public class FindListAdapter extends RecyclerView.Adapter<FindListAdapter.FindViewHolder> {

    private List<Report> reports;
    private Context context;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public FindListAdapter(Context context, List<Report> reports) {
        this.reports = reports;
        this.context = context;
    }

    @Override
    public FindViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_find_list, null);
        FindViewHolder findViewHolder = new FindViewHolder(layoutView);
        return findViewHolder;
    }

    @Override
    public void onBindViewHolder(FindViewHolder holder, int position) {
        Report report = reports.get(position);
        holder.petTextView.setText(report.getPetName());
        holder.reportDateTextView.setText(context.getString(R.string.report_date) + simpleDateFormat.format(report.getReportDate()));
        Glide.with(context)
                .load(report.getPetImageUrl())
                .into(holder.petImageView);
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public static class FindViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView petImageView;
        public TextView petTextView;
        public TextView reportDateTextView;

        public FindViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            petImageView = itemView.findViewById(R.id.petImageView);
            petTextView = itemView.findViewById(R.id.petTextView);
            reportDateTextView = itemView.findViewById(R.id.reportDateTextView);
        }

        @Override
        public void onClick(View view) {
        }
    }
}
package com.codesample.mymemo.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codesample.mymemo.R;
import com.codesample.mymemo.data.Memo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.ItemViewHolder> {
    private List<Memo> data;
    private OnItemClickListener listener;

    public MemoAdapter(OnItemClickListener listener){
        this.listener = listener;
    }

    public void updateData(List<Memo> data){
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_memo, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Memo memo = data.get(position);

        holder.textViewTitle.setText(memo.title);
        LocalDateTime time = Instant.ofEpochMilli(memo.savedTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
        String timeStr = time.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.KOREA));
        holder.textViewTime.setText(timeStr);

        holder.itemView.setOnClickListener(v-> listener.onItemClick(position, memo));
    }

    @Override
    public int getItemCount() {
        return data==null? 0:data.size();
    }

    public Memo getItem(int position){
        if(data==null) return null;
        if(position <0 || position >= data.size()) return null;
        return data.get(position);
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView textViewTitle;
        TextView textViewTime;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewTime = itemView.findViewById(R.id.textViewTime);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position, Memo memo);
    }
}

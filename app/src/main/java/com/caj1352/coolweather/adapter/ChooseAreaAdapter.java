package com.caj1352.coolweather.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.caj1352.coolweather.databinding.AreaItemBinding;

import java.util.List;

public class ChooseAreaAdapter extends RecyclerView.Adapter<ChooseAreaAdapter.ViewHoder> {

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    private List<String> list;

    public ChooseAreaAdapter(List<String> list) {
        this.list = list;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AreaItemBinding viewBinding = AreaItemBinding.inflate(LayoutInflater
                .from(parent.getContext()), parent, false);
        ViewHoder holder = new ViewHoder(viewBinding);
        viewBinding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, holder.getAdapterPosition());
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHoder holder, int position) {
        holder.viewBinding.textView.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHoder extends RecyclerView.ViewHolder {

        AreaItemBinding viewBinding;

        public ViewHoder(@NonNull AreaItemBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
        }
    }
}

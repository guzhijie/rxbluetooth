package com.jack.test;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inuker.bluetooth.library.search.SearchResult;
import com.jack.bluetooth.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/10/25 0025
 */
public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    public final List<SearchResult> m_searchResults = new ArrayList<>();
    private OnItemClickListener m_onItemClickListener;
    private OnItemLongClickListener m_onItemLongClickListener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ble_item_adapter_view, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        TextView textView = viewHolder.item;
        SearchResult searchResult = m_searchResults.get(i);
        String addr = searchResult.device.getAddress();
        String name = searchResult.device.getName();
        int rssi = searchResult.rssi;
        textView.setText(String.format("蓝牙地址:%s, 名字:%s, 信号强度:%d", addr, name, rssi));
    }

    @Override
    public int getItemCount() {
        return m_searchResults.size();
    }

    public Adapter setOnItemClickListener(OnItemClickListener l) {
        this.m_onItemClickListener = l;
        return this;
    }

    public Adapter setOnItemLongClickListener(OnItemLongClickListener l) {
        this.m_onItemLongClickListener = l;
        return this;
    }

    interface OnItemClickListener {
        void OnItemClick(View view, int pos, Object o);
    }


    interface OnItemLongClickListener {
        boolean OnItemLongClick(View view, int pos, Object o);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item)
        TextView item;
        private Adapter m_adapter;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            m_adapter = Adapter.this;
            item.setOnClickListener(v -> {
                if (m_adapter.m_onItemClickListener != null) {
                    int pos = getAdapterPosition();
                    m_adapter.m_onItemClickListener.OnItemClick(v, pos, m_adapter.m_searchResults.get(pos));
                }
            });
            item.setOnLongClickListener(v -> {
                if (m_adapter.m_onItemLongClickListener != null) {
                    int pos = getAdapterPosition();
                    return m_adapter.m_onItemLongClickListener.OnItemLongClick(item, pos, m_adapter.m_searchResults.get(pos));
                }
                return true;
            });
        }

    }
}
package com.jack.test;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.utils.StringUtils;
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
    private final List<SearchResult> m_searchResults = new ArrayList<>();
    private OnItemClickListener m_onItemClickListener;
    private OnItemLongClickListener m_onItemLongClickListener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ble_item_adapter_view, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @SuppressLint({"DefaultLocale", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        SearchResult searchResult = m_searchResults.get(i);
        String addr = searchResult.device.getAddress();
        String name = searchResult.device.getName();
        int rssi = searchResult.rssi;
        viewHolder.m_Name.setText(StringUtils.isBlank(name) ? "未知" : name);
        viewHolder.m_Address.setText(addr);
        viewHolder.m_Signal.setText(String.valueOf(rssi));
        viewHolder.m_Item.setBackgroundResource(i % 2 == 0 ? R.color.lightsteelblue : R.color.skyblue);
    }

    @Override
    public int getItemCount() {
        return m_searchResults.size();
    }

    public Adapter addItem(int pos, SearchResult searchResult) {
        this.m_searchResults.add(pos, searchResult);
        return this;
    }

    public Adapter setOnItemClickListener(OnItemClickListener l) {
        this.m_onItemClickListener = l;
        return this;
    }

    public Adapter setOnItemLongClickListener(OnItemLongClickListener l) {
        this.m_onItemLongClickListener = l;
        return this;
    }

    public void removeAllItem() {
        this.m_searchResults.clear();
        notifyDataSetChanged();
    }

    interface OnItemClickListener {
        void OnItemClick(View view, int pos, Object o);
    }


    interface OnItemLongClickListener {
        boolean OnItemLongClick(View view, int pos, Object o);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name)
        TextView m_Name;
        @BindView(R.id.address)
        TextView m_Address;
        @BindView(R.id.signal)
        TextView m_Signal;
        @BindView(R.id.item)
        LinearLayout m_Item;
        private Adapter m_adapter;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            m_adapter = Adapter.this;
            m_Item.setOnClickListener(v -> {
                if (m_adapter.m_onItemClickListener != null) {
                    int pos = getAdapterPosition();
                    m_adapter.m_onItemClickListener.OnItemClick(v, pos, m_adapter.m_searchResults.get(pos));
                }
            });
            m_Item.setOnLongClickListener(v -> {
                if (m_adapter.m_onItemLongClickListener != null) {
                    int pos = getAdapterPosition();
                    return m_adapter.m_onItemLongClickListener.OnItemLongClick(v, pos, m_adapter.m_searchResults.get(pos));
                }
                return true;
            });
        }

    }
}
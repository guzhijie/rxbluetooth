package com.jack.test;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.inuker.bluetooth.library.search.SearchResult;
import com.jack.bluetooth.R;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2020/7/3
 */
public class DeviceInfoActivity extends RxAppCompatActivity {
    private Unbinder m_unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_unbinder.unbind();
    }
}

package com.wangdh.mengm.ui.activity;


import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wangdh.mengm.R;
import com.wangdh.mengm.base.BaseActivity;
import com.wangdh.mengm.bean.WeChatListData;
import com.wangdh.mengm.component.AppComponent;
import com.wangdh.mengm.component.DaggerActivityComponent;
import com.wangdh.mengm.ui.Presenter.WechatListPresenter;
import com.wangdh.mengm.ui.adapter.WechatListAdapter;
import com.wangdh.mengm.ui.contract.WechatListContract;
import com.wangdh.mengm.utils.NetworkUtil;
import com.wangdh.mengm.utils.RecyclerViewUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;


/**
 * 微信文章列表
 */

public class WeChatListActivity extends BaseActivity implements WechatListContract.View, BaseQuickAdapter.RequestLoadMoreListener {
    @BindView(R.id.recycler)
    RecyclerView recycler;
    @BindView(R.id.swipe)
    SwipeRefreshLayout mSwipe;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    private String type;
    @Inject
    WechatListPresenter mPresenter;
    private List<WeChatListData.ShowapiResBodyBean.PagebeanBean.ContentlistBean> itemdata = new ArrayList<>();
    private WechatListAdapter adapter;
    private int i = 1;

    @Override
    protected void setupActivityComponent(AppComponent appComponent) {
        DaggerActivityComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this);
    }

    @Override
    protected int setLayoutResourceID() {
        return R.layout.activity_wechatlist;
    }

    @Override
    protected void initView() {
        mSwipe.setColorSchemeResources(R.color.colorPrimaryDark2, R.color.btn_blue, R.color.ywlogin_colorPrimaryDark);//设置进度动画的颜色
        mSwipe.setProgressViewOffset(true, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        mSwipe.setOnRefreshListener(() -> {
            itemdata.clear();
            i = 1;
            mPresenter.getWechatlistDta(type, i);
        });
        adapter = new WechatListAdapter(itemdata);
        adapter.setOnLoadMoreListener(this, recycler);
        adapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
        RecyclerViewUtil.StaggeredGridinit(recycler, adapter);
        adapter.setOnItemChildClickListener((adapter1, view, position) -> {
            String url = itemdata.get(position).getUrl();
            Intent intent = new Intent(WeChatListActivity.this, WebViewDetailsActivity.class);
            intent.putExtra("wechaturl", url);
            startActivity(intent);
        });

        mFab.setOnClickListener(v -> recycler.scrollToPosition(0));
    }

    @Override
    protected void initData() {
        showDialog();
        type = getIntent().getStringExtra("wechattype");
        mPresenter.attachView(this);
        mPresenter.getWechatlistDta(type, i);
    }

    @Override
    public void showError(String s) {
        hideDialog();
        mSwipe.setRefreshing(false);
        adapter.loadMoreEnd();
        toast(s);
    }

    @Override
    public void complete() {
        mSwipe.setRefreshing(false);
        adapter.loadMoreComplete();
        hideDialog();
    }

    @Override
    public void showWechatlistDta(WeChatListData data) {
        itemdata.addAll(data.getShowapi_res_body().getPagebean().getContentlist());
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if ((mPresenter != null)) {
            mPresenter.detachView();
        }
    }

    @Override
    public void onLoadMoreRequested() {
        if (itemdata.size() >= 20) {
            recycler.postDelayed(() -> {
                if (NetworkUtil.isAvailable(recycler.getContext())) {
                    i=i+1;
                    mPresenter.getWechatlistDta(type, i);
                } else {
                    //获取更多数据失败
                    adapter.loadMoreFail();
                }
            }, 1000);
        } else {
            adapter.loadMoreEnd();
        }
    }
}

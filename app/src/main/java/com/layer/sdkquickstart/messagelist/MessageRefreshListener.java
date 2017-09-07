package com.layer.sdkquickstart.messagelist;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;

import com.layer.sdk.LayerClient;
import com.layer.sdk.LayerDataObserver;
import com.layer.sdk.LayerDataRequest;
import com.layer.sdk.changes.LayerChangeEvent;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.LayerObject;

public class MessageRefreshListener implements SwipeRefreshLayout.OnRefreshListener,
        LayerDataObserver {
    private static final int MESSAGE_SYNC_AMOUNT = 25;

    private Conversation mConversation;
    private SwipeRefreshLayout mRefreshLayout;


    public MessageRefreshListener(Conversation conversation, SwipeRefreshLayout refreshLayout) {
        mConversation = conversation;
        mRefreshLayout = refreshLayout;
    }

    @Override
    public void onRefresh() {
        if (mConversation != null &&
                mConversation.getHistoricSyncStatus() == Conversation.HistoricSyncStatus.MORE_AVAILABLE) {
            mConversation.syncMoreHistoricMessages(MESSAGE_SYNC_AMOUNT);
        } else {
            disableAndStopRefreshing();
        }
    }

    @Override
    public void onDataChanged(LayerChangeEvent layerChangeEvent) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mConversation == null) {
                    disableAndStopRefreshing();
                    return;
                }
                Conversation.HistoricSyncStatus status = mConversation.getHistoricSyncStatus();
                mRefreshLayout.setEnabled(status == Conversation.HistoricSyncStatus.MORE_AVAILABLE);
                mRefreshLayout.setRefreshing(status == Conversation.HistoricSyncStatus.SYNC_PENDING);
            }
        });
    }

    @Override
    public void onDataRequestCompleted(LayerDataRequest request, LayerObject object) {
    }

    public void registerLayerListener(LayerClient layerClient) {
        layerClient.registerDataObserver(this);
    }

    public void unregisterLayerListener(LayerClient layerClient) {
        layerClient.unregisterDataObserver(this);
    }

    private void disableAndStopRefreshing() {
        mRefreshLayout.setEnabled(false);
        mRefreshLayout.setRefreshing(false);
    }
}

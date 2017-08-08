package com.layer.sdkquickstart.messagelist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.layer.sdk.LayerClient;
import com.layer.sdk.LayerDataObserver;
import com.layer.sdk.LayerDataRequest;
import com.layer.sdk.LayerObjectRequest;
import com.layer.sdk.LayerQueryRequest;
import com.layer.sdk.listeners.LayerTypingIndicatorListener;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.ConversationOptions;
import com.layer.sdk.messaging.ConversationType;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.LayerObject;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessageOptions;
import com.layer.sdk.messaging.MessagePart;
import com.layer.sdk.messaging.PushNotificationPayload;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.Query;
import com.layer.sdkquickstart.App;
import com.layer.sdkquickstart.BaseActivity;
import com.layer.sdkquickstart.ConversationSettingsActivity;
import com.layer.sdkquickstart.PushNotificationReceiver;
import com.layer.sdkquickstart.R;
import com.layer.sdkquickstart.util.ConversationUtils;
import com.layer.sdkquickstart.util.Log;

import java.util.ArrayList;
import java.util.HashSet;

public class MessagesListActivity extends BaseActivity {
    public static final String EXTRA_KEY_PARTICIPANT_IDS = "participantIds";

    private static final int MAX_NOTIFICATION_LENGTH = 200;

    private RecyclerView mMessagesList;
    private LinearLayoutManager mMessagesListLayoutManager;
    private MessagesRecyclerAdapter mMessagesAdapter;
    private TypingIndicatorListener mTypingIndicatorListener;
    private EditText mMessageEntry;
    private Button mSendButton;
    private SwipeRefreshLayout mMessagesRefreshLayout;

    private MessageRefreshListener mMessagesRefreshListener;
    private Conversation mConversation;

    private LayerObjectRequest<Identity> mAuthenticatedUserRequest;
    private LayerQueryRequest<Conversation> mConversationRequest;
    private LayerQueryRequest<Identity> mIdentitiesRequest;
    private Identity mAuthenticatedUser;
    private LayerDataObserver.Abstract mDataObserver;

    public MessagesListActivity() {
        super(R.layout.activity_messages_list, R.menu.menu_messages_list, R.string.title_select_conversation, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (((App) getApplication()).routeLogin(this)) {
            if (!isFinishing()) finish();
            return;
        }

        mDataObserver = new LayerDataObserver.Abstract() {
            @Override
            public void onDataRequestCompleted(LayerDataRequest request, LayerObject object) {
                if (request == mAuthenticatedUserRequest) {
                    // TODO handle failure?
                    mAuthenticatedUser = mAuthenticatedUserRequest.getObject();
                    initializeUi();
                    attemptInit();
                } else if (request == mConversationRequest) {
                    // TODO handle failures
                    mConversation = mConversationRequest.getResults().get(0);
                    attemptInit();
                } else if (request == mIdentitiesRequest) {
                    ConversationOptions options = new ConversationOptions().type(ConversationType.DIRECT_MESSAGE_CONVERSATION);
                    HashSet<Identity> identities = new HashSet<>(mIdentitiesRequest.getResults());
                    mConversation = getLayerClient().newConversation(options, identities);
                    attemptInit();
                }
            }
        };

        loadConversationFromIntent();
        getLayerClient().registerDataObserver(mDataObserver);
        mAuthenticatedUserRequest = getLayerClient().getAuthenticatedUser();
    }

    @Override
    protected void onResume() {
        // Clear any notifications for this conversation
        // TODO FCM support
//        PushNotificationReceiver.getNotifications(this).clear(mConversation);
        super.onResume();
        // TODO typing indicator support
//        getLayerClient().registerTypingIndicator(mTypingIndicatorListener);
        if (mMessagesRefreshListener != null) {
            mMessagesRefreshListener.registerLayerListener(getLayerClient());
        }
    }

    @Override
    protected void onPause() {
        // Update the notification position to the latest seen
        // TODO FCM support
//        PushNotificationReceiver.getNotifications(this).clear(mConversation);
        super.onPause();
        // TODO typing indicator support
//        getLayerClient().unregisterTypingIndicator(mTypingIndicatorListener);
        if (mMessagesRefreshListener != null) {
            mMessagesRefreshListener.unregisterLayerListener(getLayerClient());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getLayerClient().unregisterDataObserver(mDataObserver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_details:
                if (mConversation == null) return true;
                Intent intent = new Intent(this, ConversationSettingsActivity.class);
                // TODO FCM support
//                intent.putExtra(PushNotificationReceiver.LAYER_CONVERSATION_KEY, mConversation.getId());
                startActivity(intent);
                return true;

            case R.id.action_sendlogs:
                LayerClient.sendLogs(getLayerClient(), this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void attemptInit() {
        if (mConversation != null && mAuthenticatedUser != null) {
            setTitle(true);
            createAndSetRefreshListener();
            queryMessages();
        }
    }

    private void initializeUi() {
        initializeMessagesAdapter();
        initializeMessagesList();
        initializeTypingIndicator();
        initializeMessageEntry();
        initializeSendButton();
    }

    private void initializeMessagesAdapter() {
        mMessagesAdapter = new MessagesRecyclerAdapter(this, getLayerClient(), mAuthenticatedUser);
        mMessagesAdapter.setMessageAppendedListener(new ScrollOnMessageAppendedListener());

        mMessagesRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
    }

    private void initializeMessagesList() {
        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        mMessagesListLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mMessagesListLayoutManager.setStackFromEnd(true);
        mMessagesList.setLayoutManager(mMessagesListLayoutManager);
        mMessagesList.setAdapter(mMessagesAdapter);
    }

    private void initializeTypingIndicator() {
        TextView typingIndicatorView = (TextView) findViewById(R.id.typing_indicator);
        // TODO typing indicator support
//        mTypingIndicatorListener = new TypingIndicatorListener(typingIndicatorView);
    }

    private void initializeMessageEntry() {
        mMessageEntry = (EditText) findViewById(R.id.message_entry);
        mMessageEntry.addTextChangedListener(new MessageTextWatcher());
    }

    private void initializeSendButton() {
        mSendButton = (Button) findViewById(R.id.send_button);
    }

    private void loadConversationFromIntent() {
        Intent intent = getIntent();
        // TODO FCM support
        if (intent.hasExtra(PushNotificationReceiver.LAYER_CONVERSATION_KEY)) {
            Uri conversationId = intent.getParcelableExtra(PushNotificationReceiver.LAYER_CONVERSATION_KEY);

            //noinspection unchecked
            mConversationRequest = (LayerQueryRequest<Conversation>) getLayerClient().executeQueryForObjects(Query.builder(Conversation.class)
                            .predicate(new Predicate(Conversation.Property.ID,
                                    Predicate.Operator.EQUAL_TO, conversationId))
                            .build());
        } else if (intent.hasExtra(EXTRA_KEY_PARTICIPANT_IDS)) {
            ArrayList<String> participantUris = intent.getStringArrayListExtra(
                    EXTRA_KEY_PARTICIPANT_IDS);

            //noinspection unchecked
            mIdentitiesRequest = (LayerQueryRequest<Identity>) getLayerClient().executeQueryForObjects(Query.builder(Identity.class)
                    .predicate(new Predicate(Identity.Property.ID,
                            Predicate.Operator.IN, participantUris))
                    .build());
        }
    }

    private void createAndSetRefreshListener() {
        mMessagesRefreshListener = new MessageRefreshListener(mConversation, mMessagesRefreshLayout);
        mMessagesRefreshLayout.setOnRefreshListener(mMessagesRefreshListener);
        mMessagesRefreshListener.registerLayerListener(getLayerClient());
    }

    public void setTitle(boolean useConversation) {
        if (!useConversation) {
            setTitle(R.string.title_select_conversation);
        } else {
            setTitle(ConversationUtils.getConversationTitle(mAuthenticatedUser, mConversation));
        }
    }

    private void queryMessages() {
        mMessagesAdapter.queryMessages(mConversation);
    }

    public void onSendClicked(View v) {
        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Sending text message");
        }
        String text = mMessageEntry.getText().toString();

        // Send message
        String notificationString = createMessageNotificationString(text);
        PushNotificationPayload payload = new PushNotificationPayload.Builder()
                .text(notificationString)
                .build();
        MessageOptions messageOptions = new MessageOptions().defaultPushNotificationPayload(payload);
        sendMessage(text, messageOptions);

        // Clear text
        mMessageEntry.setText(null);
    }

    private String createMessageNotificationString(String text) {
        // TODO authenticated user support
//        Identity me = getLayerClient().getAuthenticatedUser();
//        String myName = me == null ? "" : IdentityUtils.getDisplayName(me);
        String myName = "TODO";
        String pushMessage = (text.length() < MAX_NOTIFICATION_LENGTH) ? text : (text.substring(0, MAX_NOTIFICATION_LENGTH) + "…");
        return String.format("%s: %s", myName, pushMessage);
    }

    private void sendMessage(String text, MessageOptions messageOptions) {
        MessagePart part = getLayerClient().newMessagePart(text);
        Message message = getLayerClient().newMessage(messageOptions, part);
        mConversation.send(message);
    }

    private class MessageTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mConversation == null || mConversation.isDeleted()) return;

            if (s.length() > 0 && !s.toString().trim().isEmpty()) {
                mSendButton.setEnabled(true);
                mConversation.send(LayerTypingIndicatorListener.TypingIndicator.STARTED);
            } else {
                mSendButton.setEnabled(false);
                mConversation.send(LayerTypingIndicatorListener.TypingIndicator.FINISHED);
            }
        }
    }

    private class ScrollOnMessageAppendedListener implements MessagesRecyclerAdapter.OnMessageAppendedListener {
        @Override
        public void onMessageAppended() {
            scrollOnNewMessage();
        }

        private void scrollOnNewMessage() {
            int end = mMessagesAdapter.getItemCount() - 1;
            if (end <= 0) return;
            int visible = mMessagesListLayoutManager.findLastVisibleItemPosition();
            // -3 because -1 seems too finicky
            if (visible >= (end - 3)) mMessagesList.scrollToPosition(end);
        }
    }
}

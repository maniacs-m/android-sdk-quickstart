package com.layer.sdkquickstart.messagelist;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.RecyclerViewController;
import com.layer.sdk.query.SortDescriptor;
import com.layer.sdkquickstart.R;
import com.layer.sdkquickstart.util.IdentityUtils;
import com.layer.sdkquickstart.util.MessageUtils;

import java.util.Date;
import java.util.Map;

public class MessagesRecyclerAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private RecyclerViewController<Message> mQueryController;
    private Context mContext;
    private OnMessageAppendedListener mMessageAppendedListener;

    private Identity mAuthenticatedUser;

    public MessagesRecyclerAdapter(Context context, LayerClient layerClient, Identity authenticatedUser) {
        mContext = context;
        mAuthenticatedUser = authenticatedUser;
        mQueryController = layerClient.newRecyclerViewController(null, null,
                new NotifyChangesCallback());
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = mQueryController.getItem(position);

        Identity sender = message.getSender();
        boolean isSelf = mAuthenticatedUser.equals(sender);
        holder.setIsUsersMessage(isSelf);

        if (isSelf) {
            holder.setParticipantName(null);
            holder.setStatusText(getDateWithStatusText(message));
        } else {
            // TODO receipt support
//            message.markAsRead();
            holder.setParticipantName(IdentityUtils.getDisplayName(sender));
            holder.setStatusText(getDateText(message));
        }

        holder.setMessage(MessageUtils.getMessageText(message));
    }

    @Override
    public int getItemCount() {
        return mQueryController.getItemCount();
    }

    public void queryMessages(Conversation conversation) {
        Query<Message> messageQuery = Query.builder(Message.class)
                .predicate(new Predicate(Message.Property.CONVERSATION, Predicate.Operator.EQUAL_TO, conversation))
                .sortDescriptor(new SortDescriptor(Message.Property.POSITION, SortDescriptor.Order.ASCENDING))
                .build();
        mQueryController.setQuery(messageQuery);
        mQueryController.execute();
    }

    public void setMessageAppendedListener(OnMessageAppendedListener listener) {
        mMessageAppendedListener = listener;
    }

    @Nullable
    private String getDateWithStatusText(Message message) {
        CharSequence formattedTime = getDateText(message);
        String status = getMessageStatus(message);

        if (formattedTime != null && status != null) {
            return formattedTime + " - " + status;
        } else if (formattedTime != null) {
            return formattedTime.toString();
        } else if (status != null) {
            return status;
        } else {
            return null;
        }
    }

    @Nullable
    private String getDateText(Message message) {
        Date sentDate = message.getSentAt();
        String formattedTime = null;
        if (sentDate != null) {
            int flags = DateUtils.FORMAT_SHOW_TIME;
            if (!DateUtils.isToday(sentDate.getTime())) {
                flags |= DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
            }
            formattedTime = DateUtils.formatDateTime(mContext, sentDate.getTime(), flags);
        }
        return formattedTime;
    }

    @Nullable
    private String getMessageStatus(Message message) {
        String status = null;
        boolean sent = false;
        boolean delivered = false;
        Map<Identity, Message.RecipientStatus> recipientStatuses = message.getRecipientStatus();
        for (Map.Entry<Identity, Message.RecipientStatus> entry : recipientStatuses.entrySet()) {
            if (entry.getKey().equals(mAuthenticatedUser)) {
                continue;
            }
            if (entry.getValue() == Message.RecipientStatus.READ) {
                status = mContext.getString(R.string.message_status_read);
                break;
            }
            switch (entry.getValue()) {
                case PENDING:
                    if (!sent && !delivered) {
                        status = mContext.getString(R.string.message_status_pending);
                    }
                    break;
                case SENT:
                    if (!delivered) {
                        status = mContext.getString(R.string.message_status_sent);
                    }
                    sent = true;
                    break;
                case DELIVERED:
                    status = mContext.getString(R.string.message_status_delivered);
                    delivered = true;
                    break;
            }

        }
        return status;
    }

    private class NotifyChangesCallback implements RecyclerViewController.Callback {
        @Override
        public void onQueryDataSetChanged(RecyclerViewController controller) {
            notifyDataSetChanged();
        }

        @Override
        public void onQueryItemChanged(RecyclerViewController controller, int position) {
            notifyItemChanged(position);
        }

        @Override
        public void onQueryItemRangeChanged(RecyclerViewController controller, int positionStart, int itemCount) {
            notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onQueryItemInserted(RecyclerViewController controller, int position) {
            notifyItemInserted(position);
            if (mMessageAppendedListener != null && (position + 1) == getItemCount()) {
                mMessageAppendedListener.onMessageAppended();
            }
        }

        @Override
        public void onQueryItemRangeInserted(RecyclerViewController controller, int positionStart, int itemCount) {
            notifyItemRangeInserted(positionStart, itemCount);
            int positionEnd = positionStart + itemCount;
            if (mMessageAppendedListener != null && (positionEnd + 1) == getItemCount()) {
                mMessageAppendedListener.onMessageAppended();
            }
        }

        @Override
        public void onQueryItemRemoved(RecyclerViewController controller, int position) {
            notifyItemRemoved(position);
        }

        @Override
        public void onQueryItemRangeRemoved(RecyclerViewController controller, int positionStart, int itemCount) {
            notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onQueryItemMoved(RecyclerViewController controller, int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }
    }

    public interface OnMessageAppendedListener {
        void onMessageAppended();
    }
}

package com.layer.sdkquickstart.conversationlist;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

public class ConversationRecyclerAdapter extends RecyclerView.Adapter<ConversationViewHolder> {

    // TODO requires query support

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ConversationViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

//    private RecyclerViewController<Conversation> mQueryController;
//    private Identity mAuthenticatedUser;
//
//    public ConversationRecyclerAdapter(LayerClient layerClient) {
//        mAuthenticatedUser = layerClient.getAuthenticatedUser();
//        setHasStableIds(false);
//
//        buildAndExecuteQuery(layerClient);
//    }
//
//    @Override
//    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_item, parent, false);
//        return new ConversationViewHolder(v);
//    }
//
//    @Override
//    public void onBindViewHolder(ConversationViewHolder holder, int position) {
//        mQueryController.updateBoundPosition(position);
//        final Conversation conversation = mQueryController.getItem(position);
//        holder.setOnClickListener(new ItemClickListener(conversation));
//
//        Set<Identity> participants = conversation.getParticipants();
//        setTitle(holder, participants);
//
//        Message lastMessage = conversation.getLastMessage();
//        setMessage(holder, lastMessage);
//        setMessageDate(holder, lastMessage);
//    }
//
//    @Override
//    public int getItemCount() {
//        return mQueryController.getItemCount();
//    }
//
//    private void buildAndExecuteQuery(LayerClient layerClient) {
//        Query<Conversation> query = Query.builder(Conversation.class)
//                /* Only show conversations we're still a member of */
//                .predicate(new Predicate(Conversation.Property.PARTICIPANT_COUNT, Predicate.Operator.GREATER_THAN, 1))
//
//                /* Sort by the last Message's sentAt time */
//                .sortDescriptor(new SortDescriptor(Conversation.Property.LAST_MESSAGE_SENT_AT, SortDescriptor.Order.DESCENDING))
//                .build();
//
//        mQueryController = layerClient.newRecyclerViewController(query, null, new NotifyChangesCallback());
//        mQueryController.execute();
//    }
//
//    private void setTitle(ConversationViewHolder holder, Set<Identity> participants) {
//        StringBuilder sb = new StringBuilder();
//        for (Identity participant : participants) {
//            if (mAuthenticatedUser.equals(participant)) {
//                continue;
//            }
//            if (sb.length() > 0) {
//                sb.append(", ");
//            }
//            sb.append(IdentityUtils.getDisplayName(participant));
//        }
//
//        holder.setName(sb.toString());
//    }
//
//    private void setMessage(ConversationViewHolder holder, Message lastMessage) {
//        holder.setMessage(MessageUtils.getMessageText(lastMessage));
//    }
//
//    private void setMessageDate(ConversationViewHolder holder, Message lastMessage) {
//        Date sentDate = lastMessage.getSentAt();
//        if (sentDate != null) {
//            CharSequence formattedTime = DateUtils.formatSameDayTime(sentDate.getTime(), System.currentTimeMillis(), DateFormat.DEFAULT, DateFormat.SHORT);
//            holder.setLastMessageTime(formattedTime);
//        } else {
//            holder.setLastMessageTime(null);
//        }
//    }
//
//    private static class ItemClickListener implements View.OnClickListener {
//        private final Conversation conversation;
//
//        public ItemClickListener(Conversation conversation) {
//            this.conversation = conversation;
//        }
//
//        @Override
//        public void onClick(View v) {
//            Intent intent = new Intent(v.getContext(), MessagesListActivity.class);
//            intent.putExtra(PushNotificationReceiver.LAYER_CONVERSATION_KEY, conversation.getId());
//            v.getContext().startActivity(intent);
//        }
//    }
//
//    private class NotifyChangesCallback implements RecyclerViewController.Callback {
//        @Override
//        public void onQueryDataSetChanged(RecyclerViewController controller) {
//            notifyDataSetChanged();
//        }
//
//        @Override
//        public void onQueryItemChanged(RecyclerViewController controller, int position) {
//            notifyItemChanged(position);
//        }
//
//        @Override
//        public void onQueryItemRangeChanged(RecyclerViewController controller, int positionStart, int itemCount) {
//            notifyItemRangeChanged(positionStart, itemCount);
//        }
//
//        @Override
//        public void onQueryItemInserted(RecyclerViewController controller, int position) {
//            notifyItemInserted(position);
//        }
//
//        @Override
//        public void onQueryItemRangeInserted(RecyclerViewController controller, int positionStart, int itemCount) {
//            notifyItemRangeInserted(positionStart, itemCount);
//        }
//
//        @Override
//        public void onQueryItemRemoved(RecyclerViewController controller, int position) {
//            notifyItemRemoved(position);
//        }
//
//        @Override
//        public void onQueryItemRangeRemoved(RecyclerViewController controller, int positionStart, int itemCount) {
//            notifyItemRangeRemoved(positionStart, itemCount);
//        }
//
//        @Override
//        public void onQueryItemMoved(RecyclerViewController controller, int fromPosition, int toPosition) {
//            notifyItemMoved(fromPosition, toPosition);
//        }
//    }
}

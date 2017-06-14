package com.layer.sdkquickstart.messagelist;

public class TypingIndicatorListener {
    // TODO requires typing indicator support
}
//public class TypingIndicatorListener implements LayerTypingIndicatorListener {
//    private final List<Identity> mActiveTypists = new ArrayList<>();
//    private final TextView mIndicatorView;
//
//
//    public TypingIndicatorListener(TextView indicatorView) {
//        mIndicatorView = indicatorView;
//    }
//
//    @Override
//    public void onTypingIndicator(LayerClient layerClient, Conversation conversation, Identity user, TypingIndicator typingIndicator) {
//        if (typingIndicator == TypingIndicator.FINISHED) {
//            mActiveTypists.remove(user);
//        } else if (!mActiveTypists.contains(user)){
//            mActiveTypists.add(user);
//        }
//        refreshView();
//    }
//
//    private void refreshView() {
//        String indicatorText = createTypistsString();
//        mIndicatorView.setText(indicatorText);
//        if (TextUtils.isEmpty(indicatorText)) {
//            mIndicatorView.setVisibility(View.GONE);
//        } else {
//            mIndicatorView.setVisibility(View.VISIBLE);
//        }
//    }
//
//    @NonNull
//    private String createTypistsString() {
//        StringBuilder sb = new StringBuilder();
//        Context context = mIndicatorView.getContext();
//        for (Identity typist : mActiveTypists) {
//            if (sb.length() > 0) {
//                sb.append("\n");
//            }
//            sb.append(context.getString(R.string.typing_indicator_format, IdentityUtils.getDisplayName(typist)));
//        }
//        return sb.toString();
//    }
//}

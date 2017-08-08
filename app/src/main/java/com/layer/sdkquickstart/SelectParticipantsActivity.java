package com.layer.sdkquickstart;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.layer.sdk.LayerClient;
import com.layer.sdk.LayerDataObserver;
import com.layer.sdk.LayerDataRequest;
import com.layer.sdk.LayerObjectRequest;
import com.layer.sdk.LayerQueryRequest;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.LayerObject;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.SortDescriptor;
import com.layer.sdkquickstart.messagelist.MessagesListActivity;
import com.layer.sdkquickstart.util.IdentityUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectParticipantsActivity extends BaseActivity {
    private static final String EXTRA_KEY_CHECKED_PARTICIPANT_IDS = "checkedParticipantIds";

    private boolean mHasCheckedParticipants;
    private Set<String> mCheckedParticipants;
    private ListView mParticipantList;
    private ParticipantAdapter mParticipantAdapter;

    public SelectParticipantsActivity() {
        super(R.layout.activity_new_conversation, R.menu.menu_select_participants, R.string.title_select_participants, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            ArrayList<String> participantIdsArray = savedInstanceState.getStringArrayList(EXTRA_KEY_CHECKED_PARTICIPANT_IDS);
            if (participantIdsArray != null && !participantIdsArray.isEmpty()) {
                mHasCheckedParticipants = true;
                mCheckedParticipants = new HashSet<>(participantIdsArray);
            }
        }

        // Fetch identities from database
        IdentityFetcher identityFetcher = new IdentityFetcher(getLayerClient());
        identityFetcher.fetchIdentities(new IdentitiesFetchedCallback());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        MenuItem doneButton = menu.findItem(R.id.action_done);
        doneButton.setVisible(mHasCheckedParticipants);

        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_done) {
            startConversationActivity();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(EXTRA_KEY_CHECKED_PARTICIPANT_IDS, getSelectedParticipantIds());
    }

    private void setUpParticipantAdapter(List<Identity> identities) {
        mParticipantAdapter = new ParticipantAdapter(this);
        mParticipantAdapter.addAll(identities);
    }

    private void setUpParticipantList() {
        mParticipantList = (ListView) findViewById(R.id.participant_list);
        // Clear choices since we are handling restoration manually
        mParticipantList.clearChoices();
        mParticipantList.setAdapter(mParticipantAdapter);

        mParticipantList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mHasCheckedParticipants = mParticipantList.getCheckedItemCount() > 0;
                invalidateOptionsMenu();
            }
        });
    }

    private void restoreCheckedParticipants(List<Identity> sortedIdentities) {
        if (mCheckedParticipants != null) {
            for (int i = 0; i < sortedIdentities.size(); i++) {
                Identity identity = sortedIdentities.get(i);
                if (mCheckedParticipants.contains(identity.getUserId())) {
                    mParticipantList.setItemChecked(i, true);
                }
            }
        }
    }

    private void startConversationActivity() {
        Intent intent = new Intent(this, MessagesListActivity.class);
        intent.putStringArrayListExtra(MessagesListActivity.EXTRA_KEY_PARTICIPANT_IDS, getSelectedParticipantIds());
        startActivity(intent);
    }

    private ArrayList<String> getSelectedParticipantIds() {
        SparseBooleanArray positions = mParticipantList.getCheckedItemPositions();
        ArrayList<String> participantIds = new ArrayList<>(positions.size());

        for (int i = 0; i < positions.size(); i++) {
            if (!positions.valueAt(i)) {
                // Participant is not checked
                continue;
            }
            int checkedPosition = positions.keyAt(i);
            Identity participant = mParticipantAdapter.getItem(checkedPosition);
            if (participant != null) {
                participantIds.add(participant.getId().toString());
            }
        }
        return participantIds;
    }

    private class IdentitiesFetchedCallback implements IdentityFetcher.IdentityFetcherCallback {
        @Override
        public void identitiesFetched(List<Identity> identities) {
            setUpParticipantAdapter(identities);
            setUpParticipantList();
            restoreCheckedParticipants(identities);
        }
    }

    /**
     * Helper class that handles loading identities from the database via a {@link Query}.
     */
    private static class IdentityFetcher implements LayerDataObserver {
        private final LayerClient mLayerClient;
        private LayerObjectRequest<Identity> mAuthenticatedUserRequest;
        private LayerQueryRequest<Identity> mQueryRequest;
        private IdentityFetcherCallback mCallback;

        IdentityFetcher(LayerClient client) {
            mLayerClient = client;
            mLayerClient.registerDataObserver(this);
        }

        private void fetchIdentities(final IdentityFetcherCallback callback) {
            mCallback = callback;
            mAuthenticatedUserRequest = mLayerClient.getAuthenticatedUser();
        }

        @Override
        public void onDataChanged(LayerObject object) {
            // Ignored
        }

        @Override
        public void onDataRequestCompleted(LayerDataRequest request, LayerObject object) {
            if (request.equals(mAuthenticatedUserRequest)) {
                Identity currentUser = mAuthenticatedUserRequest.getObject();
                Query.Builder<Identity> builder = Query.builder(Identity.class);
                if (currentUser != null) {
                    builder.predicate(new Predicate(Identity.Property.USER_ID,
                            Predicate.Operator.NOT_EQUAL_TO, currentUser.getUserId()));
                }
                builder.sortDescriptor(new SortDescriptor(Identity.Property.DISPLAY_NAME, SortDescriptor.Order.ASCENDING));
                final Query<Identity> identitiesQuery = builder.build();
                mQueryRequest = (LayerQueryRequest<Identity>) mLayerClient.executeQueryForObjects(
                        identitiesQuery);
            } else if (request.equals(mQueryRequest)) {
                mCallback.identitiesFetched(mQueryRequest.getResults());
            }
        }

        interface IdentityFetcherCallback {
            void identitiesFetched(List<Identity> identities);
        }
    }

    private static class ParticipantAdapter extends ArrayAdapter<Identity> {

        private ParticipantAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_multiple_choice);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            CheckedTextView textView = (CheckedTextView) v;
            textView.setText(IdentityUtils.getDisplayName(getItem(position)));
            return v;
        }
    }
}

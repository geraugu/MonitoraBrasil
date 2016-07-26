package com.gamfig.monitorabrasil.views.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.gamfig.monitorabrasil.R;
import com.twitter.sdk.android.core.AppSession;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.CompactTweetView;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TwitterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TwitterFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mtwitter;
    private String mParam2;

    private ProgressBar pb;


    public TwitterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TwitterFragment newInstance(String param1, String param2) {
        TwitterFragment fragment = new TwitterFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mtwitter = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_list_twitter, container, false);
        pb = (ProgressBar)rootView.findViewById(R.id.progressBar6);

        if (mtwitter != null) {
            TwitterCore.getInstance().logInGuest(new Callback() {
                @Override
                public void success(Result result) {
                    AppSession guestAppSession = (AppSession) result.data;
                    TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient(guestAppSession);

                    StatusesService statusesService = twitterApiClient.getStatusesService();
                    statusesService.userTimeline(null, mtwitter, 20, null, null, null, null, null, null, new Callback<List<Tweet>>() {

                        @Override
                        public void success(Result<List<Tweet>> listResult) {
                            pb.setVisibility(View.GONE);
                            for (Tweet tweet : listResult.data) {
                                LinearLayout ln = (LinearLayout) rootView.findViewById(R.id.timeline);
                                ln.addView(
                                        new CompactTweetView(getContext(), tweet));
                            }
                        }

                        @Override
                        public void failure(TwitterException e) {
                            Log.e("Error", "Error");
                        }
                    });
                }

                @Override
                public void failure(TwitterException exception) {
                    // unable to get an AppSession with guest auth
                }
            });


        }

        return rootView;
    }

}

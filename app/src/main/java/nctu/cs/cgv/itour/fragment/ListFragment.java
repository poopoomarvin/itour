package nctu.cs.cgv.itour.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import nctu.cs.cgv.itour.R;
import nctu.cs.cgv.itour.activity.MainActivity;
import nctu.cs.cgv.itour.custom.CheckinItemAdapter;
import nctu.cs.cgv.itour.custom.ItemClickSupport;
import nctu.cs.cgv.itour.object.Checkin;
import nctu.cs.cgv.itour.object.SpotList;

import static nctu.cs.cgv.itour.MyApplication.VERSION_ALL_FEATURE;
import static nctu.cs.cgv.itour.MyApplication.VERSION_ONLY_GOOGLE_COMMENT;
import static nctu.cs.cgv.itour.MyApplication.VERSION_OPTION;
import static nctu.cs.cgv.itour.MyApplication.dirPath;
import static nctu.cs.cgv.itour.MyApplication.mapTag;
import static nctu.cs.cgv.itour.MyApplication.spotList;
import static nctu.cs.cgv.itour.Utility.actionLog;
import static nctu.cs.cgv.itour.Utility.dpToPx;
import static nctu.cs.cgv.itour.Utility.gpsToImgPx;
import static nctu.cs.cgv.itour.activity.MainActivity.checkinMap;

/**
 * Created by lobst3rd on 2017/8/18.
 */

/**
 * For community version:
 *  Shows a list of all checkins of the community
 *  Click any checkin to view checkin content with CheckinDialogFragment
 * For googleComment version:
 *  Shows a list of all spots
 *  Click any spot to view spot content with SpotDescriptionDialogFragment
 */
public class ListFragment extends Fragment {

    private static final String TAG = "ListFragment";
    public CheckinItemAdapter checkinItemAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ActionBar actionBar;
    private final int ORDER_TIME = 0;
    private final int ORDER_POPULAR = 1;
    private int orderFlag = ORDER_TIME;

    public static ListFragment newInstance() {
        return new ListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        checkinItemAdapter = new CheckinItemAdapter(getActivity(), new ArrayList<Checkin>(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recycle_view_swipe_refresh, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.gps_marker_color);

        RecyclerView recyclerView = view.findViewById(R.id.recycle_view);
        recyclerView.setAdapter(checkinItemAdapter);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Checkin checkin = checkinItemAdapter.getItem(position);
                        if (VERSION_OPTION == VERSION_ALL_FEATURE) {
                            CheckinDialogFragment checkinDialogFragment = CheckinDialogFragment.newInstance(checkin.key, TAG);
                            checkinDialogFragment.show(Objects.requireNonNull(getFragmentManager()), "fragment_checkin_dialog");
                        } else if (VERSION_OPTION == VERSION_ONLY_GOOGLE_COMMENT) {
                            SpotDescritionDialogFragment spotDescritionDialogFragment = SpotDescritionDialogFragment.newInstance(checkin.location, TAG);
                            spotDescritionDialogFragment.show(getFragmentManager(), "SpotDescritionDialogFragment");
                        }
                    }
                }
        );
    }

    public void addCheckins() {
        checkinItemAdapter.addAll(checkinMap.values());
    }

    public void refresh() {
        if (VERSION_OPTION == VERSION_ALL_FEATURE) {
            checkinItemAdapter.clear();

            switch (orderFlag) {
                case ORDER_TIME:
                    for (final Checkin checkin : checkinMap.values()) {
                        checkinItemAdapter.insert(checkin, 0);
                    }
                    break;
                case ORDER_POPULAR:
                    ArrayList<Checkin> checkinValues = new ArrayList<>(checkinMap.values());

                    final String uid;
                    if (FirebaseAuth.getInstance().getCurrentUser() != null)
                        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    else
                        uid = "";

                    Collections.sort(checkinValues, new Comparator<Checkin>() {
                        @Override
                        public int compare(Checkin checkin1, Checkin checkin2) {
                            boolean checkin1Popular = false;
                            boolean checkin2Popular = false;
                            if (checkin1.popularTargetUid.get("all") || (!uid.equals("") && checkin1.popularTargetUid.containsKey(uid) && checkin1.popularTargetUid.get(uid)))
                                checkin1Popular = true;
                            if (checkin2.popularTargetUid.get("all") || (!uid.equals("") && checkin2.popularTargetUid.containsKey(uid) && checkin2.popularTargetUid.get(uid)))
                                checkin2Popular = true;

                            if (checkin1Popular == checkin2Popular) {
                                return (checkin1.likeNum + checkin1.like.size()) - (checkin2.likeNum + checkin2.like.size());
                            }
                            if (checkin1Popular) {
                                return 1;
                            }
                            if (checkin2Popular) {
                                return -1;
                            }
                            return (checkin1.likeNum + checkin1.like.size()) - (checkin2.likeNum + checkin2.like.size());
                        }
                    });

                    for (final Checkin checkin : checkinValues) {
                        checkinItemAdapter.insert(checkin, 0);
                    }
                    break;
            }
        } else if (VERSION_OPTION == VERSION_ONLY_GOOGLE_COMMENT) {
            checkinItemAdapter.setSpotChecking();
        }


    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            if (actionBar != null) {
                actionBar.setElevation(0);
                actionBar.setSubtitle(getString(R.string.subtitle_list));
            }
            refresh();
        } else {
            if (actionBar != null) {
                actionBar.setElevation(dpToPx(getContext(), 4));
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (VERSION_OPTION == VERSION_ALL_FEATURE) inflater.inflate(R.menu.checkin_filter_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.popular:
                orderFlag = ORDER_POPULAR;
                refresh();
                return true;
            case R.id.time:
                orderFlag = ORDER_TIME;
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

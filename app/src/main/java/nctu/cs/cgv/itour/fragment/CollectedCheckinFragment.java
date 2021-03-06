package nctu.cs.cgv.itour.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import nctu.cs.cgv.itour.R;
import nctu.cs.cgv.itour.custom.CheckinItemAdapter;
import nctu.cs.cgv.itour.custom.ItemClickSupport;
import nctu.cs.cgv.itour.object.Checkin;

import static nctu.cs.cgv.itour.activity.MainActivity.checkinMap;
import static nctu.cs.cgv.itour.activity.MainActivity.collectedCheckinKey;

/**
 * For community version:
 *  Shows the list of collected/starred checkins 
 */
public class CollectedCheckinFragment extends Fragment {

    private static final String TAG = "CollectedCheckinFragment";
    private SwipeRefreshLayout swipeRefreshLayout;
    public CheckinItemAdapter checkinItemAdapter;

    public static CollectedCheckinFragment newInstance() {
        return new CollectedCheckinFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkinItemAdapter == null) checkinItemAdapter = new CheckinItemAdapter(getActivity(), new ArrayList<Checkin>(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recycle_view_swipe_refresh, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

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
                        CheckinDialogFragment checkinDialogFragment = CheckinDialogFragment.newInstance(checkin.key, TAG);
                        checkinDialogFragment.show(Objects.requireNonNull(getFragmentManager()), "fragment_checkin_dialog");
                    }
                }
        );
    }

    public void refresh() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (checkinItemAdapter == null) checkinItemAdapter = new CheckinItemAdapter(getActivity(), new ArrayList<Checkin>(), this);
            checkinItemAdapter.clear();
            for (final Map.Entry<String, Boolean> entry : collectedCheckinKey.entrySet()) {
                if (entry.getValue()) {
                    String postId = entry.getKey();
                    Checkin checkin = checkinMap.get(postId);
                    if (checkin != null) {
                        checkinItemAdapter.insert(checkin, 0);
                    } else {
                        // TODO delete this postId
                    }
                }
            }
        }
    }
}

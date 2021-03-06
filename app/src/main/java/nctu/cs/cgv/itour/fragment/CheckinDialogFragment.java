package nctu.cs.cgv.itour.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import nctu.cs.cgv.itour.R;
import nctu.cs.cgv.itour.Utility;
import nctu.cs.cgv.itour.activity.MainActivity;
import nctu.cs.cgv.itour.custom.CheckinCommentItemAdapter;
import nctu.cs.cgv.itour.object.Checkin;
import nctu.cs.cgv.itour.object.CheckinComment;
import nctu.cs.cgv.itour.object.CollectNotification;
import nctu.cs.cgv.itour.object.CommentNotification;
import nctu.cs.cgv.itour.object.FirebaseLogData;
import nctu.cs.cgv.itour.object.LikeNotification;

import static nctu.cs.cgv.itour.MyApplication.VERSION_ALL_FEATURE;
import static nctu.cs.cgv.itour.MyApplication.VERSION_OPTION;
import static nctu.cs.cgv.itour.MyApplication.fileDownloadURL;
import static nctu.cs.cgv.itour.MyApplication.latitude;
import static nctu.cs.cgv.itour.MyApplication.longitude;
import static nctu.cs.cgv.itour.MyApplication.mapTag;
import static nctu.cs.cgv.itour.Utility.actionLog;
import static nctu.cs.cgv.itour.activity.MainActivity.checkinMap;
import static nctu.cs.cgv.itour.activity.MainActivity.collectedCheckinKey;
import static nctu.cs.cgv.itour.activity.MainActivity.firebaseLogManager;
import static nctu.cs.cgv.itour.object.FirebaseLogData.LOG_CHECKIN_CLOSE;
import static nctu.cs.cgv.itour.object.FirebaseLogData.LOG_CHECKIN_COLLECT_ADD;
import static nctu.cs.cgv.itour.object.FirebaseLogData.LOG_CHECKIN_COLLECT_REMOVE;
import static nctu.cs.cgv.itour.object.FirebaseLogData.LOG_CHECKIN_COMMENT;
import static nctu.cs.cgv.itour.object.FirebaseLogData.LOG_CHECKIN_LIKE;
import static nctu.cs.cgv.itour.object.FirebaseLogData.LOG_CHECKIN_LOCATE;
import static nctu.cs.cgv.itour.object.FirebaseLogData.LOG_CHECKIN_OPEN;
import static nctu.cs.cgv.itour.object.FirebaseLogData.LOG_CHECKIN_UNLIKE;
import static nctu.cs.cgv.itour.object.FirebaseLogData.LOG_NOTE_IS_COLLECTED_CHECKIN;
import static nctu.cs.cgv.itour.object.FirebaseLogData.LOG_NOTE_IS_NOT_SELF_CHECKIN;
import static nctu.cs.cgv.itour.object.FirebaseLogData.LOG_NOTE_IS_OTHER_CHECKIN;
import static nctu.cs.cgv.itour.object.FirebaseLogData.LOG_NOTE_IS_SELF_CHECKIN;
import static nctu.cs.cgv.itour.object.FirebaseLogData.LOG_NOTIFICATION_CLICKED_LIKE;


/**
 * For community version:
 *  Shows the targeting checkin content
 *
 */
public class CheckinDialogFragment extends DialogFragment {

    private static final String TAG = "CheckinDialogFragment";
    private Query postReference;
    public Checkin notificationCheckin;

    private String postId;
    private String fromPath;
    private Query query;
    private ChildEventListener childEventListener;

    public static CheckinDialogFragment newInstance(String postId, String fromPath) {
        CheckinDialogFragment checkinDialogFragment = new CheckinDialogFragment();
        Bundle args = new Bundle();
        Log.d("NotificationNewIntent", "new Instance" + postId);
        args.putString("postId", postId);
        args.putString("fromPath", fromPath);
        checkinDialogFragment.setArguments(args);
        return checkinDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString("postId");
            fromPath = getArguments().getString("fromPath");
        }
        Log.d("NotificationNewIntent", "onCreate" + postId);
        firebaseLogManager.log(LOG_CHECKIN_OPEN, postId, fromPath);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkin_dialog, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        Log.d(TAG, "onViewCreated :" + postId);
//        Log.d("NIVRAM", "postId:" + postId);
        Log.d("NotificationNewIntent", "onViewCreated" + postId);
        Checkin checkin = checkinMap.get(postId);
//        Log.d("NIVRAM", "checkinMap count:" + checkinMap.size());
        if (checkin != null) {
//            Log.d("NIVRAM", "checkin fine");
        } else {
//            Log.d("FATTTT", "NULL!");

            SharedPreferences sharedPreferences = getContext().getSharedPreferences("data", Context.MODE_PRIVATE);

//            getCheckinFromFirebase(postId);
            checkin = notificationCheckin;
            Log.d("NotificationNewIntent", "get data" + postId);

        }


        TextView username = view.findViewById(R.id.tv_username);
        TextView location = view.findViewById(R.id.tv_location);
        TextView like = view.findViewById(R.id.tv_like);
        TextView collect = view.findViewById(R.id.tv_collect);
        TextView description = view.findViewById(R.id.tv_description);
        TextView distance = view.findViewById(R.id.tv_distance);

        if (checkin != null) {
            actionLog("browse checkin", checkin.location, checkin.key);
            username.setText(checkin.username);
            location.setText(checkin.location);
            description.setText(checkin.description);

            float dist = Utility.gpsToMeter(latitude, longitude, Float.valueOf(checkin.lat), Float.valueOf(checkin.lng));
            distance.setText(String.valueOf((int)dist) + getString(R.string.meter));

            int likeNum = checkin.likeNum;
            if (checkin.like != null && checkin.like.size() > 0) {
                likeNum += checkin.like.size();
            }

            String likeStr = likeNum > 0 ? String.valueOf(likeNum) + Objects.requireNonNull(getContext()).getString(R.string.checkin_card_like_num) : "";
            like.setText(likeStr);

            int collectNum = checkin.collectNum;

            String collectStr = collectNum > 0 ? String.valueOf(collectNum) + "人收藏" : "";
            collect.setText(collectStr);

            setPhoto(view, checkin.photo);
            setActionBtn(view, checkin);
            setComment(view, checkin);
        } else {
            username.setText("");
            location.setText("");
            description.setText(getString(R.string.tv_checkin_remove));
            like.setText("");
            distance.setText("");

            setPhoto(view, "");
        }
    }

    private void setPhoto(final View view, final String filename) {

        final ImageView photo = view.findViewById(R.id.photo);

        if (filename.equals("")) {
            photo.setVisibility(View.GONE);
            return;
        }

        Glide.with(Objects.requireNonNull(getContext()))
                .load(fileDownloadURL + "?filename=" + filename)
                .apply(new RequestOptions().placeholder(R.drawable.ic_broken_image_black_48dp))
                .into(photo);
    }

    private void setActionBtn(final View view, final Checkin checkin) {
        final ImageView likeBtn = view.findViewById(R.id.btn_like);
        final ImageView saveBtn = view.findViewById(R.id.btn_save);
        final LinearLayout locateBtn = view.findViewById(R.id.btn_locate);
        final TextView like = view.findViewById(R.id.tv_like);
        final TextView collect = view.findViewById(R.id.tv_collect);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), getString(R.string.toast_guest_function), Toast.LENGTH_SHORT).show();
                }
            });

            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), getString(R.string.toast_guest_function), Toast.LENGTH_SHORT).show();
                }
            });
        } else {

            // set like
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    if (checkin.like.containsKey(uid) && checkin.like.get(uid)) {
                        //unlike
                        likeBtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                                R.drawable.ic_favorite_border_black_24dp, null));
                        String likeStr = "";
                        if (checkin.like != null && checkin.like.size() > 0) {
                            likeStr = String.valueOf(checkin.likeNum + checkin.like.size() - 1) + getContext().getString(R.string.checkin_card_like_num);
                        }
                        like.setText(likeStr);
                        databaseReference.child("checkin").child(mapTag).child(checkin.key).child("like").child(uid).removeValue();
                        checkin.like.remove(uid);
                        checkinMap.get(checkin.key).like.remove(uid);
                        actionLog("cancel like checkin", checkin.location, checkin.key);

                        String logNote = "";
                        if (uid.equals(checkin.uid)) {
                            logNote = LOG_NOTE_IS_SELF_CHECKIN;
                        } else if (collectedCheckinKey != null && collectedCheckinKey.containsKey(checkin.key) && collectedCheckinKey.get(checkin.key)) {
                            logNote = LOG_NOTE_IS_COLLECTED_CHECKIN;
                        } else {
                            logNote = LOG_NOTE_IS_OTHER_CHECKIN;
                        }
                        firebaseLogManager.log(LOG_CHECKIN_UNLIKE, checkin.key, logNote);
                    } else {
                        // set like
                        if (!checkin.uid.equals(uid))
                            sendLikeNotification(checkin);
                        likeBtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                                R.drawable.ic_favorite_red_500_24dp, null));
                        String likeStr;
                        if (checkin.like != null && checkin.like.size() > 0) {
                            likeStr = String.valueOf(checkin.likeNum + checkin.like.size() + 1) + getContext().getString(R.string.checkin_card_like_num);
                        } else {
                            likeStr = String.valueOf(checkin.likeNum + 1) + getContext().getString(R.string.checkin_card_like_num);
                        }
                        like.setText(likeStr);
                        databaseReference.child("checkin").child(mapTag).child(checkin.key).child("like").child(uid).setValue(true);
                        checkin.like.put(uid, true);
                        checkinMap.get(checkin.key).like.put(uid, true);
                        actionLog("like checkin", checkin.location, checkin.key);

                        String logNote = "";
                        if (uid.equals(checkin.uid)) {
                            logNote = LOG_NOTE_IS_SELF_CHECKIN;
                        } else if (collectedCheckinKey != null && collectedCheckinKey.containsKey(checkin.key) && collectedCheckinKey.get(checkin.key)) {
                            logNote = LOG_NOTE_IS_COLLECTED_CHECKIN;
                        } else {
                            logNote = LOG_NOTE_IS_OTHER_CHECKIN;
                        }
                        firebaseLogManager.log(LOG_CHECKIN_LIKE, checkin.key, logNote);
                    }
                }
            });

            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    if (collectedCheckinKey.containsKey(checkin.key) && collectedCheckinKey.get(checkin.key)) {
                        saveBtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                                R.drawable.icon_star_gray_32, null));
                        databaseReference.child("users").child(uid).child("saved").child(mapTag).child(checkin.key).removeValue();
                        collectedCheckinKey.remove(checkin.key);
                        actionLog("cancel save checkin", checkin.location, checkin.key);

                        String logNote = "";
                        if (uid.equals(checkin.uid)) logNote = LOG_NOTE_IS_SELF_CHECKIN;
                        else logNote = LOG_NOTE_IS_NOT_SELF_CHECKIN;
                        firebaseLogManager.log(LOG_CHECKIN_COLLECT_REMOVE, checkin.key, logNote);
                        checkin.collectNum = checkin.collectNum > 1 ? checkin.collectNum - 1 : 0;
                        databaseReference.child("checkin").child(mapTag).child(checkin.key).child("collectnum").setValue(checkin.collectNum);
                        collect.setText(String.valueOf(checkin.collectNum) + "人收藏");
                    } else {
                        if (!checkin.uid.equals(uid))
                            sendCollectNotification(checkin);
                        saveBtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                                R.drawable.icon_star_32, null));
                        databaseReference.child("users").child(uid).child("saved").child(mapTag).child(checkin.key).setValue(true);
                        collectedCheckinKey.put(checkin.key, true);
                        actionLog("save checkin", checkin.location, checkin.key);

                        String logNote = "";
                        if (uid.equals(checkin.uid)) logNote = LOG_NOTE_IS_SELF_CHECKIN;
                        else logNote = LOG_NOTE_IS_NOT_SELF_CHECKIN;
                        firebaseLogManager.log(LOG_CHECKIN_COLLECT_ADD, checkin.key, logNote);
                        checkin.collectNum += 1;
                        databaseReference.child("checkin").child(mapTag).child(checkin.key).child("collectnum").setValue(checkin.collectNum);
                        collect.setText(String.valueOf(checkin.collectNum) + "人收藏");
                    }
                }
            });

            if (checkin.like.containsKey(uid) && checkin.like.get(uid)) {
                likeBtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_favorite_red_500_24dp, null));
            }

            if (collectedCheckinKey.containsKey(checkin.key) && collectedCheckinKey.get(checkin.key)) {
                saveBtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.icon_star_32, null));
            }
        }

        locateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String logNote = "";
                if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(checkin.uid)) {
                    logNote = LOG_NOTE_IS_SELF_CHECKIN;
                } else if (collectedCheckinKey != null && collectedCheckinKey.containsKey(checkin.key) && collectedCheckinKey.get(checkin.key)) {
                    logNote = LOG_NOTE_IS_COLLECTED_CHECKIN;
                } else {
                    logNote = LOG_NOTE_IS_OTHER_CHECKIN;
                }
                firebaseLogManager.log(LOG_CHECKIN_LOCATE, checkin.key, logNote);
                if ((fromPath.equals("PersonalMapFragment") && VERSION_OPTION == VERSION_ALL_FEATURE) || (fromPath.equals("CollectedCheckinFragment") && logNote.equals(LOG_NOTE_IS_COLLECTED_CHECKIN))) {
                    ((MainActivity)getActivity()).personalFragment.viewPager.setCurrentItem(0);
                    ((MainActivity)getActivity()).personalFragment.personalMapFragment.onLocateCheckinClick(checkin);
                } else {
                    ((MainActivity) Objects.requireNonNull(getActivity())).onLocateCheckinClick(checkin.key);
                }
                Fragment fragment = Objects.requireNonNull(getFragmentManager()).findFragmentByTag("fragment_checkin_dialog");
                Objects.requireNonNull(getFragmentManager()).beginTransaction().remove(fragment).commitAllowingStateLoss();
                actionLog("locate checkin", checkin.location, checkin.key);
            }
        });
    }

    private void setComment(final View view, final Checkin checkin) {
        final View commentDivider = view.findViewById(R.id.comment_divider);
        final RecyclerView commentList = view.findViewById(R.id.lv_comment);
        final RelativeLayout commentEdit = view.findViewById(R.id.comment_edit);
        final TextView commentUsername = view.findViewById(R.id.tv_comment_username);
        final EditText commentMsg = view.findViewById(R.id.et_comment_msg);
        final ImageView sendBtn = view.findViewById(R.id.btn_comment_send);

        final CheckinCommentItemAdapter checkinCommentItemAdapter = new CheckinCommentItemAdapter(getContext(), new ArrayList<CheckinComment>());
        commentList.setAdapter(checkinCommentItemAdapter);
        commentList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        commentList.scrollToPosition(checkinCommentItemAdapter.getItemCount() - 1);

        query = FirebaseDatabase.getInstance().getReference()
                .child("checkin").child(mapTag).child(checkin.key).child("checkinComment");
        childEventListener = query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                CheckinComment checkinComment = dataSnapshot.getValue(CheckinComment.class);
                if (checkinComment != null) {
                    checkinCommentItemAdapter.add(checkinComment);
                    commentDivider.setVisibility(View.VISIBLE);
                    commentList.setVisibility(View.VISIBLE);
                    commentList.scrollToPosition(checkinCommentItemAdapter.getItemCount() - 1);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // set comment edit
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            commentEdit.setVisibility(View.VISIBLE);
            commentUsername.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // send checkinComment
                    String msg = commentMsg.getText().toString().trim();
                    if (msg.equals("")) return;
                    //send CommentNotification
                    if(!checkin.uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        sendCommentNotification(checkin);

                    CheckinComment checkinComment = new CheckinComment(msg,
                            FirebaseAuth.getInstance().getCurrentUser().getUid(),
                            FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                            System.currentTimeMillis() / 1000);
                    String pushKey = FirebaseDatabase.getInstance().getReference()
                            .child("checkin").child(mapTag).child(checkin.key).child("checkinComment")
                            .push().getKey();

                    Map<String, Object> commentValue = checkinComment.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/checkin/" + mapTag + "/" + checkin.key + "/checkinComment/" + pushKey, commentValue);
                    FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates,
                            new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, final DatabaseReference databaseReference) {
                                    commentMsg.setText("");
                                }
                            });

                    String logNote = "";
                    if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(checkin.uid)) {
                        logNote = LOG_NOTE_IS_SELF_CHECKIN;
                    } else if (collectedCheckinKey != null && collectedCheckinKey.containsKey(checkin.key) && collectedCheckinKey.get(checkin.key)) {
                        logNote = LOG_NOTE_IS_COLLECTED_CHECKIN;
                    } else {
                        logNote = LOG_NOTE_IS_OTHER_CHECKIN;
                    }
                    firebaseLogManager.log(LOG_CHECKIN_COMMENT, checkin.key, msg + "," + logNote);
                }
            });

        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // set dialog layout
        Objects.requireNonNull(getDialog().getWindow())
                .setLayout(WindowManager.LayoutParams.MATCH_PARENT,     // width
                        WindowManager.LayoutParams.WRAP_CONTENT);    // height
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(query != null && childEventListener != null) query.removeEventListener(childEventListener);
        firebaseLogManager.log(LOG_CHECKIN_CLOSE, postId);
    }
    /**
     * Send a comment notification if add comment
    **/
    private void sendCommentNotification(Checkin checkin) {

        if (checkin.uid == FirebaseAuth.getInstance().getCurrentUser().getUid()) {
            // user comment self checkin
            return;
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final String pushKey = databaseReference.child("comment_notification").child(mapTag).push().getKey();
        final String commentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String commentUserName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        final String commentedUid = checkin.uid;
        final String commentedCheckinId = checkin.key;
        final String commentedCheckinDescription = checkin.description;
        long timestamp = System.currentTimeMillis() / 1000;
        CommentNotification commentNotification = new CommentNotification(commentUid, commentUserName, commentedUid, commentedCheckinId, commentedCheckinDescription, timestamp);
        Map<String, Object> commentNotificationValue = commentNotification.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/comment_notification/" + mapTag + "/" + pushKey, commentNotificationValue);
        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates,
                new DatabaseReference.CompletionListener() {
                    @Override                    public void onComplete(DatabaseError databaseError, final DatabaseReference databaseReference) {
                        //commentMsg.setText("");
                        Log.e("NIVRAM", "ERROR GG");
                    }
                });
    }
    /**
     * Send a collect notification if add collect
     **/
    private void sendCollectNotification(Checkin checkin) {

        if (checkin.uid == FirebaseAuth.getInstance().getCurrentUser().getUid()) {
            // user collect self checkin
            return;
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final String pushKey = databaseReference.child("collect_notification").child(mapTag).push().getKey();
        final String collectUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String collectUserName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        final String collectedUid = checkin.uid;
        final String collectedCheckinId = checkin.key;
        final String collectedCheckinDescription = checkin.description;
        long timestamp = System.currentTimeMillis() / 1000;
        CollectNotification collectNotification = new CollectNotification(collectUid, collectUserName, collectedUid, collectedCheckinId, collectedCheckinDescription, timestamp);
        Map<String, Object> collectNotificationValue = collectNotification.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/collect_notification/" + mapTag + "/" + pushKey, collectNotificationValue);
        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates,
                new DatabaseReference.CompletionListener() {
                    @Override                    public void onComplete(DatabaseError databaseError, final DatabaseReference databaseReference) {
                        //collectMsg.setText("");
                        Log.e("NIVRAM", "ERROR GG");
                    }
                });
    }

    /**
     * Send a like notification if add like
     **/
    private void sendLikeNotification(Checkin checkin) {

        if (checkin.uid == FirebaseAuth.getInstance().getCurrentUser().getUid()) {
            // user comment self checkin
            return;
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final String pushKey = databaseReference.child("like_notification").child(mapTag).push().getKey();
        final String likeUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String likeUserName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        final String likedUid = checkin.uid;
        final String likedCheckinId = checkin.key;
        final String likedCheckinDescription = checkin.description;
        long timestamp = System.currentTimeMillis() / 1000;
        LikeNotification likeNotification = new LikeNotification(likeUid, likeUserName, likedUid, likedCheckinId, likedCheckinDescription, timestamp);
        Map<String, Object> likeNotificationValue = likeNotification.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/like_notification/" + mapTag + "/" + pushKey, likeNotificationValue);
        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates,
                new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, final DatabaseReference databaseReference) {
                        //likeMsg.setText("");
                        Log.e("NIVRAM", "ERROR GG");
                    }
                });
    }
    public void getCheckinFromFirebase(String id, final FragmentManager fragmentManager, final String tag) {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                notificationCheckin = dataSnapshot.getValue(Checkin.class);
                notificationCheckin.key = dataSnapshot.getKey();
                show(fragmentManager, tag);
                Log.d("NotificationNewIntent", "notificationCheckin " + notificationCheckin.key);
                Log.d("NotificationNewIntent", "dataSnapshot " + dataSnapshot.getKey());
                Log.d("NotificationNewIntent", "notificationCheckin " + notificationCheckin.key);
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        postReference = FirebaseDatabase.getInstance().getReference().child("checkin").child(mapTag).child(id);
        postReference.addListenerForSingleValueEvent(postListener);

    }
    @Override
    public void show(FragmentManager manager, String tag) {
//        mDismissed = false;
//        mShownByMe = true;
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }

}

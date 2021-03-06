package nctu.cs.cgv.itour.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import nctu.cs.cgv.itour.R;
import nctu.cs.cgv.itour.activity.LoginActivity;
import nctu.cs.cgv.itour.activity.RegisterActivity;
import nctu.cs.cgv.itour.object.InitDataManager;
import nctu.cs.cgv.itour.service.AudioFeedbackService;
import nctu.cs.cgv.itour.service.ScreenShotService;

import static android.content.Context.MODE_PRIVATE;
import static nctu.cs.cgv.itour.MyApplication.VERSION_ONLY_GOOGLE_COMMENT;
import static nctu.cs.cgv.itour.MyApplication.VERSION_OPTION;

public class SettingsFragment extends PreferenceFragmentCompat {

    private OnFogListener onFogListener;
    private OnDistanceIndicatorListener onDistanceIndicatorListener;
    private OnCheckinIconListener onCheckinIconListener;
    private OnSpotIonListener onSpotIonListener;
    private ActionBar actionBar;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference btnSignOut = getPreferenceManager().findPreference("logout");
        Preference btnRegister = getPreferenceManager().findPreference("register");
        Preference btnUid = getPreferenceManager().findPreference("uid");
        Log.d("SETUSERID", "?????");
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            PreferenceCategory accountCategory = (PreferenceCategory) findPreference("category_account");
            accountCategory.removePreference(btnSignOut);

            btnRegister.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    startActivity(new Intent(getContext(), RegisterActivity.class));
                    return true;
                }
            });
            Log.d("SETUSERID", "!!!!!");
        } else {
            PreferenceCategory accountCategory = (PreferenceCategory) findPreference("category_account");
            accountCategory.removePreference(btnRegister);
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("data", MODE_PRIVATE);
            String userId = sharedPreferences.getString("givenUserId", "");
            btnUid.setSummary(userId);
            Log.d("SETUSERID", " get userId :" + userId);

            btnSignOut.setSummary(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString());
            btnSignOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    FirebaseAuth.getInstance().signOut();

                    getActivity().finish();
                    getContext().stopService(new Intent(getContext(), ScreenShotService.class));
                    getContext().stopService(new Intent(getContext(), AudioFeedbackService.class));
//                    getContext().stopService(new Intent(getContext(), SystemNotificationService.class));
                    startActivity(new Intent(getContext(), LoginActivity.class));
                    return true;
                }
            });
        }
        Preference checkinSwitch = getPreferenceManager().findPreference("checkin");
        Preference spotSwitch = getPreferenceManager().findPreference("spot");

        if (VERSION_OPTION == VERSION_ONLY_GOOGLE_COMMENT) {

            PreferenceCategory switchCategory = (PreferenceCategory) findPreference("switch");
            switchCategory.removePreference(checkinSwitch);
            switchCategory.removePreference(spotSwitch);
        }
        checkinSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                onCheckinIconListener.onCheckinIconSwitched((Boolean) newValue);
                return true;
            }
        });

        spotSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                onSpotIonListener.onSpotIconSwitched((Boolean) newValue);
                return true;
            }
        });



//        Preference distanceIndicatorSwitch = getPreferenceManager().findPreference("distance_indicator");
//        distanceIndicatorSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                onDistanceIndicatorListener.onDistanceIndicatorSwitched((Boolean) newValue);
//                return true;
//            }
//        });

//        Preference fogSwitch = getPreferenceManager().findPreference("fog");
//        fogSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                onFogListener.onFogSwitched((Boolean) newValue);
//                return true;
//            }
//        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
//            onFogListener = (OnFogListener) context;
//            onDistanceIndicatorListener = (OnDistanceIndicatorListener) context;
            onCheckinIconListener = (OnCheckinIconListener) context;
            onSpotIonListener = (OnSpotIonListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement interfaces");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setDivider(null);
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();


    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (actionBar != null) {
            if (getUserVisibleHint()) {
                actionBar.setSubtitle(getString(R.string.subtitle_setting));
                Preference btnUid = getPreferenceManager().findPreference("uid");
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("data", MODE_PRIVATE);
                String userId = sharedPreferences.getString("givenUserId", "");
                btnUid.setSummary(userId);
            }
        }
    }

    public interface OnFogListener {
        void onFogSwitched(boolean flag);
    }

    public interface OnDistanceIndicatorListener {
        void onDistanceIndicatorSwitched(boolean flag);
    }

    public interface OnCheckinIconListener {
        void onCheckinIconSwitched(boolean flag);
    }

    public interface OnSpotIonListener {
        void onSpotIconSwitched(boolean flag);
    }
}
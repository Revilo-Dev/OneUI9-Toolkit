package dev.oneuiproject.oneuiexample.fragment;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.SeslMenuItem;
import androidx.appcompat.widget.SeslProgressBar;
import androidx.appcompat.widget.SwitchCompat;
import androidx.apppickerview.widget.AppPickerView;

import com.sec.sesl.tester.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.oneuiproject.oneui.widget.Toast;
import dev.oneuiproject.oneuiexample.base.BaseFragment;

public class AppPickerFragment extends BaseFragment
        implements AppPickerView.OnBindListener {
    private static final String[] CATEGORIES = {"List", "List, Action Button",
            "List, CheckBox", "List, CheckBox, All apps", "List, RadioButton",
            "List, Switch", "List, Switch, All apps", "Grid", "Grid, CheckBox"};
    private boolean mListInitialized = false;
    private int mListType = AppPickerView.TYPE_LIST;
    private boolean mShowSystemApps = false;

    private final List<Boolean> mItems = new ArrayList<>();
    private boolean mIsAllAppsSelected = false;
    private int mCheckedPosition = 0;

    private AppPickerView mAppPickerView;
    private SeslProgressBar mProgress;
    private final ArrayList<String> mVisiblePackages = new ArrayList<>();
    private ArrayList<String> mCachedPackages;
    private int mLoadGeneration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgress = view.findViewById(R.id.apppicker_progress);
        mAppPickerView = view.findViewById(R.id.apppicker_list);
        mAppPickerView.setItemAnimator(null);
        mAppPickerView.seslSetSmoothScrollEnabled(true);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && !mListInitialized) {
            fillListView();
            mListInitialized = true;
        }
    }

    @Override public void onDestroyView() {
        ++mLoadGeneration;
        mAppPickerView = null;
        mProgress = null;
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem systemAppsItem = menu.findItem(R.id.menu_apppicker_system);
        systemAppsItem.setVisible(true);
        if (mShowSystemApps) {
            systemAppsItem.setTitle("Hide system apps");
        } else {
            systemAppsItem.setTitle("Show system apps");
        }
        ((SeslMenuItem) systemAppsItem)
                .setBadgeText(getString(R.string.oui_new_badge_text));
        MenuItem modeItem = menu.findItem(R.id.menu_apppicker_mode);
        modeItem.setVisible(true);
        if (modeItem.getSubMenu().size() == 0) {
            for (int i = 0; i < CATEGORIES.length; i++)
                modeItem.getSubMenu().add(R.id.menu_apppicker_mode, i, i, CATEGORIES[i]);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getGroupId() == R.id.menu_apppicker_mode) {
            mListType = item.getItemId();
            fillListView();
            return true;
        }
        if (item.getItemId() == R.id.menu_apppicker_system) {
            ((SeslMenuItem) item)
                    .setBadgeText(null);

            mShowSystemApps = !mShowSystemApps;
            mCachedPackages = null;
            if (mShowSystemApps) {
                item.setTitle("Hide system apps");
            } else {
                item.setTitle("Show system apps");
            }

            refreshListView();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.sample3_fragment_apppicker;
    }

    @Override
    public int getIconResId() {
        return R.drawable.ic_oui_all_apps;
    }

    @Override
    public CharSequence getTitle() {
        return "AppPickerView";
    }

    private void fillListView() {
        loadPackages();
    }

    private void refreshListView() {
        loadPackages();
    }

    private void loadPackages() {
        int generation = ++mLoadGeneration;
        int type = mListType;
        boolean includeSystem = mShowSystemApps;
        Context context = requireContext().getApplicationContext();
        SeslProgressBar progress = mProgress;
        showProgressCircle(true);
        if (mCachedPackages != null) {
            startBatches(new ArrayList<>(mCachedPackages), generation, type);
            return;
        }
        new Thread(() -> {
            ArrayList<String> packages = new ArrayList<>();
            for (ApplicationInfo app : context.getPackageManager()
                    .getInstalledApplications(PackageManager.GET_META_DATA)) {
                if (!includeSystem && (app.flags & (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
                        | ApplicationInfo.FLAG_SYSTEM)) != 0) continue;
                packages.add(app.packageName);
            }
            Collections.sort(packages);
            progress.post(() -> {
                if (generation != mLoadGeneration || mAppPickerView == null) return;
                mCachedPackages = packages;
                startBatches(packages, generation, type);
            });
        }, "app-picker-packages").start();
    }

    private void startBatches(ArrayList<String> packages, int generation, int type) {
        mVisiblePackages.clear();
        mItems.clear();
        mIsAllAppsSelected = false;
        mCheckedPosition = 0;
        if (type == AppPickerView.TYPE_LIST_CHECKBOX_WITH_ALL_APPS
                || type == AppPickerView.TYPE_LIST_SWITCH_WITH_ALL_APPS)
            mItems.add(Boolean.FALSE);
        appendBatch(packages, generation, type, 0);
    }

    private void appendBatch(ArrayList<String> packages, int generation, int type, int start) {
        if (generation != mLoadGeneration || mAppPickerView == null) return;
        int end = Math.min(start + 40, packages.size());
        for (int i = start; i < end; i++) {
            mVisiblePackages.add(packages.get(i));
            mItems.add(Boolean.FALSE);
        }
        if (start == 0) {
            while (mAppPickerView.getItemDecorationCount() > 0)
                mAppPickerView.removeItemDecorationAt(0);
            mAppPickerView.setOnBindListener(this);
            mAppPickerView.setAppPickerView(type, new ArrayList<>(mVisiblePackages),
                    AppPickerView.ORDER_ASCENDING_IGNORE_CASE);
        } else {
            mAppPickerView.resetPackages(new ArrayList<>(mVisiblePackages));
        }
        if (end < packages.size()) {
            mAppPickerView.postDelayed(() -> appendBatch(packages, generation, type, end), 16);
        } else {
            showProgressCircle(false);
        }
    }

    @Override
    public void onBindViewHolder(AppPickerView.ViewHolder holder,
                                 int position, String packageName) {
        switch (mListType) {
            case AppPickerView.TYPE_LIST: {
                holder.getItem().setOnClickListener(view -> { });
            } break;

            case AppPickerView.TYPE_LIST_ACTION_BUTTON: {
                holder.getActionButton().setOnClickListener(view
                        -> Toast.makeText(mContext, "onClick", Toast.LENGTH_SHORT).show());
            } break;

            case AppPickerView.TYPE_LIST_CHECKBOX: {
                CheckBox checkBox = holder.getCheckBox();
                checkBox.setChecked(mItems.get(position));
                checkBox.setOnCheckedChangeListener((buttonView, isChecked)
                        -> mItems.set(position, isChecked));
            } break;

            case AppPickerView.TYPE_LIST_CHECKBOX_WITH_ALL_APPS: {
                CheckBox checkBox = holder.getCheckBox();
                if (position == 0) {
                    holder.getAppLabel().setText("All apps");
                    checkBox.setChecked(mIsAllAppsSelected);
                    checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (mIsAllAppsSelected != isChecked) {
                            mIsAllAppsSelected = isChecked;
                            for (int i = 0; i < mItems.size(); i++){
                                mItems.set(i, mIsAllAppsSelected);
                            }
                            mAppPickerView.refreshUI();
                        }
                    });
                } else {
                    checkBox.setChecked(mItems.get(position));
                    checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        mItems.set(position, isChecked);
                        checkAllAppsToggle();
                    });
                }
            } break;

            case AppPickerView.TYPE_LIST_RADIOBUTTON: {
                RadioButton radioButton = holder.getRadioButton();
                radioButton.setChecked(mItems.get(position));
                holder.getRadioButton().setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        if (mCheckedPosition != position) {
                            mItems.set(mCheckedPosition, false);
                            mAppPickerView.refreshUI(mCheckedPosition);
                        };
                        mItems.set(position, true);
                        mCheckedPosition = position;
                    }
                });
            } break;

            case AppPickerView.TYPE_LIST_SWITCH: {
                SwitchCompat switchWidget = holder.getSwitch();
                switchWidget.setChecked(mItems.get(position));
                switchWidget.setOnCheckedChangeListener((buttonView, isChecked)
                        -> mItems.set(position, isChecked));
            } break;

            case AppPickerView.TYPE_LIST_SWITCH_WITH_ALL_APPS: {
                SwitchCompat switchWidget = holder.getSwitch();
                if (position == 0) {
                    holder.getAppLabel().setText("All apps");
                    switchWidget.setChecked(mIsAllAppsSelected);
                    switchWidget.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (mIsAllAppsSelected != isChecked) {
                            mIsAllAppsSelected = isChecked;
                            for (int i = 0; i < mItems.size(); i++){
                                mItems.set(i, mIsAllAppsSelected);
                            }
                            mAppPickerView.refreshUI();
                        }
                    });
                } else {
                    switchWidget.setChecked(mItems.get(position));
                    switchWidget.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        mItems.set(position, isChecked);
                        checkAllAppsToggle();
                    });
                }
            } break;

            case AppPickerView.TYPE_GRID: {
                holder.getItem().setOnClickListener(view -> { });
            } break;

            case AppPickerView.TYPE_GRID_CHECKBOX: {
                CheckBox checkBox = holder.getCheckBox();
                checkBox.setChecked(mItems.get(position));
                checkBox.setOnCheckedChangeListener((buttonView, isChecked)
                        -> mItems.set(position, isChecked));
                holder.getItem().setOnClickListener(view
                        -> checkBox.setChecked(!checkBox.isChecked()));
            } break;
        }
    }

    private void checkAllAppsToggle() {
        mIsAllAppsSelected = true;
        for (boolean selected : mItems) {
            if (!selected) {
                mIsAllAppsSelected = false;
                break;
            }
        }
        mAppPickerView.refreshUI(0);
    }

    private void showProgressCircle(boolean show) {
        mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        mAppPickerView.setVisibility(View.VISIBLE);
    }
}

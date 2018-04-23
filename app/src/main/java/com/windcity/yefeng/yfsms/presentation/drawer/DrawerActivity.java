package com.windcity.yefeng.yfsms.presentation.drawer;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.bugly.beta.Beta;
import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.base.BaseActivity;
import com.windcity.yefeng.yfsms.domain.service.TaskService;
import com.windcity.yefeng.yfsms.domain.usecase.reward.RewardTask;
import com.windcity.yefeng.yfsms.domain.usecase.share.ShareTask;
import com.windcity.yefeng.yfsms.presentation.about.AboutActivity;
import com.windcity.yefeng.yfsms.presentation.classify.ClassifyFragment;
import com.windcity.yefeng.yfsms.presentation.sendsms.SendSmsActivity;
import com.windcity.yefeng.yfsms.presentation.setting.SettingActivity;
import com.windcity.yefeng.yfsms.presentation.star.StarConversationListFragment;
import com.yefeng.support.base.AppInfo;
import com.yefeng.support.util.SharedPreferenceUtil;
import com.yefeng.support.util.SimCardUtil;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DrawerActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, DrawerContract.View {

    private static final String CURRENT_ITEM_TAG = "CURRENT_ITEM_TAG";
    private static final int REQUEST_DEFAULT_SMS = 1000;

    @BindView(R.id.toolbar)
    Toolbar mToolBar;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @BindView(R.id.nav_view)
    NavigationView mNavigation;
    @Inject
    DrawerPresenter mPresenter;
    @BindView(R.id.tabs)
    TabLayout mTabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        ButterKnife.bind(this);
        DaggerDrawerComponent.builder()
                .drawerModule(new DrawerModule(this))
                .build()
                .inject(this);
        //require permission
        requirePermission();
    }

    private void requirePermission() {
        new RxPermissions(this)
                .request(
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS)
                .subscribe(granted -> {
                    if (granted) { // Always true pre-M
                        checkSimCard();
                    } else {
                        new MaterialDialog.Builder(DrawerActivity.this)
                                .content(R.string.no_sms_permission)
                                .positiveText(R.string.confirm)
                                .dismissListener(dialog -> finish())
                                .show();
                    }
                });
    }

    private void checkSimCard() {
        if (!SimCardUtil.hasSimCard(this)) {
            new MaterialDialog.Builder(DrawerActivity.this)
                    .content(R.string.insert_sim_card)
                    .positiveText(R.string.confirm)
                    .dismissListener(dialog -> finish())
                    .show();
            return;
        }
        setDefaultSmsApp(true);
    }

    private void setDefaultSmsApp(boolean req) {
        String currentPn = getPackageName();//获取当前程序包名
        String defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);//获取手机当前设置的默认短信应用的包名
        if (null != currentPn && currentPn.equals(defaultSmsApp)) {
            init();
        } else {
            if (req) {
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, currentPn);
                startActivityForResult(intent, REQUEST_DEFAULT_SMS);
            } else {
                new MaterialDialog.Builder(this)
                        .content(R.string.prompt_set_default_sms)
                        .positiveText(R.string.confirm)
                        .dismissListener(dialog -> finish())
                        .show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DEFAULT_SMS) {
            setDefaultSmsApp(false);
        }
    }

    private void init() {
        TaskService.startSyncSms(this);
        TaskService.syncContactName(this, null);
        //clear notification
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
        initView();
        RewardTask.promptReward(this);
    }

    private void initView() {
        setSupportActionBar(mToolBar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        mNavigation.setNavigationItemSelectedListener(this);

        //choose item
        setCurrentItemTag(null);
        mNavigation.setCheckedItem(R.id.nav_classify);
        selectNavItem(R.id.nav_classify);
        mTabLayout.setSelectedTabIndicatorHeight((int) (AppInfo.sDensity * 4));
        Beta.checkUpgrade(false, false);
    }

    private String getCurrentItemTag() {
        return SharedPreferenceUtil.getString(this, CURRENT_ITEM_TAG, null);
    }

    private void setCurrentItemTag(String tag) {
        SharedPreferenceUtil.putString(this, CURRENT_ITEM_TAG, String.valueOf(tag));
    }

    @OnClick(R.id.fab)
    void sendSms() {
        SendSmsActivity.startMe(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.subscribe();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.unSubscribe();
    }

    @Override
    protected void onDestroy() {
        //clear notification
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
        super.onDestroy();
        mPresenter = null;
    }

    @Override
    public void onBackPressed() {
        if (null != mDrawer) {
            if (mDrawer.isDrawerOpen(GravityCompat.START)) {
                mDrawer.closeDrawer(GravityCompat.START);
                return;
            }
            if (!String.valueOf(R.id.nav_classify).equals(getCurrentItemTag())) {
                mNavigation.setCheckedItem(R.id.nav_classify);
                selectNavItem(R.id.nav_classify);
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            SettingActivity.startMe(this);
            return true;
        } else if (id == R.id.action_share) {
            ShareTask.shareApp(this);
            return true;
        } else if (id == R.id.action_about) {
            AboutActivity.startMe(this);
            return true;
        } else if (id == R.id.action_reward) {
            RewardTask.start(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_classify || id == R.id.nav_star) {
            selectNavItem(id);
        } else if (id == R.id.nav_setting) {
            SettingActivity.startMe(this);
        } else if (id == R.id.nav_about) {
            AboutActivity.startMe(this);
        } else if (id == R.id.nav_share) {
            ShareTask.shareApp(this);
            if (null != mDrawer) {
                mDrawer.closeDrawer(GravityCompat.START);
            }
        } else if (id == R.id.nav_reward) {
            RewardTask.start(this);
            if (null != mDrawer) {
                mDrawer.closeDrawer(GravityCompat.START);
            }
        }
        return true;
    }

    private void selectNavItem(int id) {
        String selectItemTag = String.valueOf(id);
        String currentItemTag = getCurrentItemTag();
        if (selectItemTag.equals(currentItemTag)) {
            mDrawer.closeDrawer(GravityCompat.START);
            return;
        }
        if (null != mToolBar && null != mToolBar.getLayoutParams()) {
            AppBarLayout.LayoutParams params =
                    (AppBarLayout.LayoutParams) mToolBar.getLayoutParams();
            if (id == R.id.nav_classify) {
                params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                        | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
            } else {
                params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                        | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
            }
            mToolBar.setLayoutParams(params);
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(currentItemTag);
        if (null != currentFragment) {
            ft.detach(currentFragment);
        }
        currentItemTag = selectItemTag;
        Fragment targetFragment = getSupportFragmentManager().findFragmentByTag(currentItemTag);
        if (null == targetFragment) {
            if (id == R.id.nav_classify) {
                targetFragment = ClassifyFragment.newInstance();
            } else if (id == R.id.nav_star) {
                targetFragment = StarConversationListFragment.newInstance();
            }
            if (null != targetFragment) {
                ft.add(R.id.fl_content, targetFragment, currentItemTag);
            }
        } else {
            ft.attach(targetFragment);
        }
        ft.commitAllowingStateLoss();
        setCurrentItemTag(currentItemTag);
        getSupportFragmentManager().executePendingTransactions();
        if (id == R.id.nav_classify) {
            setTitle(R.string.classified_sms);
        } else if (id == R.id.nav_star) {
            setTitle(R.string.stared_sms);
        }
        mDrawer.closeDrawer(GravityCompat.START);
    }

    public void updateTabs(ViewPager vp, int visible) {
        mTabLayout.setVisibility(visible);
        mTabLayout.setupWithViewPager(vp);
    }

    public TabLayout getTabLayout() {
        return mTabLayout;
    }

}

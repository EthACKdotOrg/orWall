package org.ethack.orwall;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.ethack.orwall.adapter.RowAdapter;
import org.ethack.orwall.iptables.InitializeIptables;
import org.ethack.orwall.lib.Constants;
import org.ethack.orwall.lib.InstallScripts;
import org.ethack.orwall.lib.NatRules;
import org.ethack.orwall.lib.PackageComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import info.guardianproject.onionkit.ui.OrbotHelper;

public class MainActivity extends Activity {

    private InitializeIptables initializeIptables;
    private PackageManager packageManager;
    private List<PackageInfo> finalList;
    private CountDownTimer timer;

    private ListView listview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Intent checkInit = new Intent(this, DialogActivity.class);
        int requestCode = 1;
        this.startActivityForResult(checkInit, requestCode);

        initializeIptables = new InitializeIptables(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 1) {
            initDisplay();
        } else if (resultCode == 2) {
            noIptables();
        } else if (resultCode == 3) {
            noRoot();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        OrbotHelper oh = new OrbotHelper(this);

        if (oh.isOrbotInstalled() && !oh.isOrbotRunning()) {
            oh.requestOrbotStart(this);
        }
    }

    @Override
    public boolean onMenuItemSelected(final int featureID, final MenuItem item) {

        switch (item.getItemId()) {
            case R.id.enable_tethering:
            case R.id.disable_tethering:
                final boolean enabled = (item.getItemId() == R.id.enable_tethering);
                Intent bgpProcess = new Intent(this, BackgroundProcess.class);
                bgpProcess.putExtra(Constants.PARAM_TETHER_STATUS, enabled);
                bgpProcess.putExtra(Constants.ACTION, Constants.ACTION_TETHER);
                this.startService(bgpProcess);
                getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).edit().putBoolean(Constants.PREF_KEY_IS_TETHER_ENAVLED, enabled).commit();

                return true;
            case R.id.authorize_browser:
            case R.id.disable_browser:
                final Long browser_uid = Long.valueOf(getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).getString(Constants.PREF_KEY_SPEC_BROWSER, null));
                final Context context = this;

                initializeIptables.manageCaptiveBrowser((item.getItemId() == R.id.authorize_browser), browser_uid);
                getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).edit().putBoolean(Constants.PREF_KEY_BROWSER_ENABLED, (item.getItemId() == R.id.authorize_browser)).commit();

                if (item.getItemId() == R.id.authorize_browser) {

                    long gracetime = Long.valueOf(getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).getString(Constants.PREF_KEY_BROWSER_GRACETIME, String.valueOf(Constants.BROWSER_GRACETIME)));

                    this.timer = new CountDownTimer(TimeUnit.MINUTES.toMillis(gracetime), TimeUnit.SECONDS.toMillis(30)) {
                        public void onTick(long untilFinished) {

                            final long minutes = TimeUnit.MILLISECONDS.toMinutes(untilFinished);
                            final long seconds = TimeUnit.MILLISECONDS.toSeconds(untilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(untilFinished));

                            CharSequence text = String.format(getResources().getString(R.string.main_counter), minutes, seconds);
                            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
                        }

                        public void onFinish() {
                            initializeIptables.manageCaptiveBrowser(false, browser_uid);
                            getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).edit().putBoolean(Constants.PREF_KEY_BROWSER_ENABLED, false).commit();
                            CharSequence text = getResources().getString(R.string.main_end_of_browser);
                            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
                        }
                    }.start();
                } else {
                    this.timer.cancel();
                }
                return true;

            case R.id.enable_sip:
            case R.id.disable_sip:
                final Long sip_uid = Long.valueOf(getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).getString(Constants.PREF_KEY_SIP_APP, null));
                initializeIptables.manageSip((item.getItemId() == R.id.enable_sip), sip_uid);
                getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).edit().putBoolean(Constants.PREF_KEY_SIP_ENABLED, (item.getItemId() == R.id.enable_sip)).commit();
                return true;

            case R.id.action_settings:
                showPreferences();
                return true;

            case R.id.action_about:
                LayoutInflater li = LayoutInflater.from(this);
                View view = li.inflate(R.layout.about, null);
                String versionName = "";
                try {
                    versionName = packageManager.getPackageInfo(this.getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {

                }
                TextView version = (TextView) view.findViewById(R.id.about_version);
                version.setText(versionName);
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.menu_action_about))
                        .setView(view)
                        .show();
                return true;

            case R.id.action_search:
                TextWatcher filterTextWatcher = new TextWatcher() {

                    public void afterTextChanged(Editable s) {
                        showApplications(s.toString(), false);
                    }

                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                        showApplications(s.toString(), false);
                    }

                };

                item.setActionView(R.layout.searchbar);

                final EditText filterText = (EditText) item.getActionView().findViewById(R.id.searchApps);

                filterText.addTextChangedListener(filterTextWatcher);
                filterText.setEllipsize(TextUtils.TruncateAt.END);
                filterText.setSingleLine();

                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        // Do something when collapsed
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        filterText.post(new Runnable() {
                            @Override
                            public void run() {
                                filterText.requestFocus();
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.showSoftInput(filterText, InputMethodManager.SHOW_IMPLICIT);
                            }
                        });
                        return true; // Return true to expand action view
                    }
                });
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final String sip_app = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).getString(Constants.PREF_KEY_SIP_APP, null);
        final boolean sip_enabled = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).getBoolean(Constants.PREF_KEY_SIP_ENABLED, false);

        final String browser_app = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).getString(Constants.PREF_KEY_SPEC_BROWSER, null);
        final boolean browser_enabled = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).getBoolean(Constants.PREF_KEY_BROWSER_ENABLED, false);

        final boolean tethering_enabled = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).getBoolean(Constants.PREF_KEY_TETHER_ENABLED, false);
        final boolean is_tether_enabled = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).getBoolean(Constants.PREF_KEY_IS_TETHER_ENAVLED, false);

        if (sip_app != null && !sip_app.equals("0")) {
            MenuItem item = menu.getItem(3);
            item.setEnabled(true);
            if (sip_enabled) {
                item.setVisible(false);
                menu.getItem(4).setVisible(true);
            } else {
                item.setVisible(true);
                menu.getItem(4).setVisible(false);
            }
        }
        if (browser_app != null && !browser_app.equals("0")) {
            MenuItem item = menu.getItem(1);
            item.setEnabled(true);
            if (browser_enabled) {
                item.setVisible(false);
                menu.getItem(2).setVisible(true);
            } else {
                item.setVisible(true);
                menu.getItem(2).setVisible(false);
            }
        }
        MenuItem tether_enable = menu.getItem(5);
        MenuItem tether_disable = menu.getItem(6);
        if (tethering_enabled) {
            tether_enable.setEnabled(true);

            if (is_tether_enabled) {
                tether_disable.setVisible(true);
                tether_enable.setVisible(false);
            } else {
                tether_enable.setVisible(true);
                tether_disable.setVisible(false);
            }
        } else {
            tether_enable.setEnabled(false);
            tether_disable.setVisible(false);
        }
        return true;
    }

    private void showPreferences() {
        Intent intent = new Intent(this, PreferencesActivity.class);
        startActivityForResult(intent, 1);
    }

    private void showApplications(final String searchStr, boolean showAll) {
        boolean isMatchFound = false;
        List<PackageInfo> searchApp = new ArrayList<PackageInfo>();

        if (searchStr != null && searchStr.length() > 0) {
            for (PackageInfo pkg : finalList) {
                String[] names = {
                        pkg.packageName,
                        packageManager.getApplicationLabel(pkg.applicationInfo).toString()
                };
                for (String name : names) {
                    if ((name.contains(searchStr.toLowerCase()) ||
                            name.toLowerCase().contains(searchStr.toLowerCase())) &&
                            !searchApp.contains(pkg)
                            ) {
                        searchApp.add(pkg);
                        isMatchFound = true;
                    }
                }
            }
        }

        List<PackageInfo> apps2;
        if (showAll || (searchStr != null && searchStr.equals(""))) {
            apps2 = finalList;
        } else if (isMatchFound || searchApp.size() > 0) {
            apps2 = searchApp;
        } else {
            apps2 = new ArrayList<PackageInfo>();
        }

        Collections.sort(apps2, new PackageComparator(packageManager));

        this.listview.setAdapter(new RowAdapter(this, apps2, packageManager));
    }

    private void initDisplay(){
        ApplicationInfo orbot_id = null;
        packageManager = getPackageManager();

        try {
            orbot_id = packageManager.getApplicationInfo("org.torproject.android", PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(BootBroadcast.class.getName(), "Unable to get Orbot APK info - is it installed?");
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage(R.string.main_no_orbot);
            alert.setNeutralButton(R.string.main_dismiss, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
            alert.show();
        }

        if (orbot_id != null) {

            InstallScripts installScripts = new InstallScripts(this);
            installScripts.run();
            // install the initscript — there is a check in the function in order to avoid useless writes.;
            boolean enforceInit = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).getBoolean(Constants.PREF_KEY_ENFOCE_INIT, true);
            boolean disableInit = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).getBoolean(Constants.PREF_KEY_DISABLE_INIT, false);

            if (enforceInit) {
                Log.d("Main", "Enforcing or installing init-script");
                initializeIptables.installInitScript(this);
            }
            if (disableInit && !enforceInit) {
                Log.d("Main", "Disabling init-script");
                initializeIptables.removeIniScript();
            }

            if (enforceInit && !initializeIptables.isInitialized()) {
                Log.d("INIT", "IPTables was NOT initialized as expected!");
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setMessage(R.string.main_reboot_required);
                alert.setNeutralButton(R.string.main_dismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                });
                alert.setPositiveButton(R.string.main_start_iptables, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        initializeIptables.boot();
                    }
                });

                alert.show();
            }

            NatRules natRules = new NatRules(this);
            Set oldRules = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).getStringSet("nat_rules", null);
            if (natRules.getRuleCount() == 0 && oldRules != null) {
                natRules.importFromSharedPrefs(oldRules);
                getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).edit().remove("nat_rules").apply();
            }

            List<PackageInfo> packageList = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
            finalList = new ArrayList<PackageInfo>();

            for (PackageInfo applicationInfo : packageList) {
                String[] permissions = applicationInfo.requestedPermissions;
                if (!applicationInfo.packageName.equals(Constants.I2P_APP_NAME) &&
                        !applicationInfo.packageName.equals(Constants.ORBOT_APP_NAME) &&
                        !applicationInfo.packageName.equals(this.getPackageName()) &&
                        permissions != null) {
                    for (String perm : permissions) {
                        if (perm.equals("android.permission.INTERNET")) {
                            finalList.add(applicationInfo);
                            break;
                        }
                    }
                }
            }

            Collections.sort(finalList, new PackageComparator(packageManager));

            listview = (ListView) findViewById(R.id.applist);
            listview.setAdapter(new RowAdapter(this, finalList, packageManager));
        }
    }

    private void noIptables() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(R.string.main_no_iptables);
        alert.setNeutralButton(R.string.main_dismiss, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        alert.show();
    }

    private void noRoot() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(R.string.main_no_root);
        alert.setNeutralButton(R.string.main_dismiss, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        alert.show();
    }
}

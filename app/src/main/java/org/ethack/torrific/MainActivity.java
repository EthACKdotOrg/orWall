package org.ethack.torrific;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import org.ethack.torrific.adapter.RowAdapter;
import org.ethack.torrific.iptables.InitializeIptables;
import org.ethack.torrific.lib.InstallScripts;
import org.ethack.torrific.lib.PackageComparator;
import org.ethack.torrific.lib.Shell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    public final static String PREFERENCE = "org.ethack.torrific_preferences";
    public final static String PREF_KEY_SIP_APP = "sip_app";
    public final static String PREF_KEY_SIP_ENABLED = "sip_enabled";
    public final static String PREF_KEY_SPEC_BROWSER = "browser_app";
    public final static String PREF_KEY_BROWSER_ENABLED = "browser_enabled";
    private PackageManager packageManager;

    private List<PackageInfo> finalList;
    private final InitializeIptables initializeIptables = new InitializeIptables();

    private ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Shell shell = new Shell();

        if (!shell.checkSu()) {
            Log.e(MainActivity.class.getName(), "Unable to get root shell, exiting.");
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("Seems you do not have root access on this device");
            alert.setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
            alert.show();
        } else {

            ApplicationInfo orbot_id = null;
            packageManager = getPackageManager();

            try {
                orbot_id = packageManager.getApplicationInfo("org.torproject.android", PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(BootBroadcast.class.getName(), "Unable to get Orbot APK info - is it installed?");
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setMessage("You must have Orbot installed!");
                alert.setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
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
                boolean enforceInit = getSharedPreferences(PREFERENCE, MODE_PRIVATE).getBoolean("enforce_init_script", true);
                boolean disableInit = getSharedPreferences(PREFERENCE, MODE_PRIVATE).getBoolean("deactivate_init_script", false);

                if (enforceInit) {
                    Log.d("Main", "Enforcing or installing init-script");
                    initializeIptables.installInitScript(this);
                }
                if (disableInit && !enforceInit) {
                    Log.d("Main", "Disabling init-script");
                    initializeIptables.removeIniScript();
                }

                List<PackageInfo> packageList = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
                finalList = new ArrayList<PackageInfo>();

                for (PackageInfo applicationInfo : packageList) {
                    String[] permissions = applicationInfo.requestedPermissions;
                    if (permissions != null) {
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


    }

    @Override
    public boolean onMenuItemSelected(int featureID, MenuItem item) {
        Long uid;
        switch (item.getItemId()) {
            case R.id.authorize_browser:
                // TODO: implement browser bypass
            case R.id.disable_browser:
                // TODO: disable browser
                Log.d("Menu Action", "TODO :)");
                return true;

            case R.id.enable_sip:
            case R.id.disable_sip:
                uid = Long.valueOf(getSharedPreferences(PREFERENCE, MODE_PRIVATE).getString(PREF_KEY_SIP_APP, null));
                initializeIptables.manageSip((item.getItemId() == R.id.enable_sip), uid);
                getSharedPreferences(PREFERENCE, MODE_PRIVATE).edit().putBoolean(PREF_KEY_SIP_ENABLED, (item.getItemId() == R.id.enable_sip)).commit();
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
                TextView version = (TextView)view.findViewById(R.id.about_version);
                version.setText(versionName);
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.action_about))
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
        String sip_app = getSharedPreferences(PREFERENCE, MODE_PRIVATE).getString(PREF_KEY_SIP_APP, null);
        boolean sip_enabled = getSharedPreferences(PREFERENCE, MODE_PRIVATE).getBoolean(PREF_KEY_SIP_ENABLED, false);

        String browser_app = getSharedPreferences(PREFERENCE, MODE_PRIVATE).getString(PREF_KEY_SPEC_BROWSER, null);
        boolean browser_enabled = getSharedPreferences(PREFERENCE, MODE_PRIVATE).getBoolean(PREF_KEY_BROWSER_ENABLED, false);

        if(sip_app != null) {
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
        if (browser_app != null) {
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
}

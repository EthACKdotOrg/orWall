package org.ethack.torrific;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.ethack.torrific.adapter.RowAdapter;
import org.ethack.torrific.iptables.InitializeIptables;
import org.ethack.torrific.lib.InstallScripts;
import org.ethack.torrific.lib.NATLiteSource;
import org.ethack.torrific.lib.PackageComparator;
import org.ethack.torrific.lib.Shell;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    public final static String TAG = "Torrific";
    private PackageManager packageManager;
    private NATLiteSource natLiteSource;
    private boolean newIntent;
    private List<PackageInfo> finalList;

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
            natLiteSource = new NATLiteSource(this);
            try {
                natLiteSource.open();
            } catch (SQLException e) {
                Log.e(MainActivity.class.getName(), "Unable to open database");
                android.os.Process.killProcess(android.os.Process.myPid());
            }

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

                InitializeIptables initializeIptables = new InitializeIptables(natLiteSource);

                InstallScripts installScripts = new InstallScripts(this);
                installScripts.run();
                // install the initscript — there is a check in the function in order to avoid useless writes.;
                boolean enforceInit = getSharedPreferences("org.ethack.torrific_preferences", MODE_PRIVATE).getBoolean("enforce_init_script", true);
                boolean disableInit = getSharedPreferences("org.ethack.torrific_preferences", MODE_PRIVATE).getBoolean("deactivate_init_script", false);
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
                listview.setAdapter(new RowAdapter(this, finalList, packageManager, natLiteSource));
            }
        }


    }

    @Override
    public boolean onMenuItemSelected(int featureID, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                newIntent = true;
                showPreferences();
                return true;
            case R.id.action_about:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("About Torrific");
                String versionName = null;
                try {
                    versionName = packageManager.getPackageInfo(this.getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {

                }
                String about = "Torrific %s — GPLv2\n\n" +
                        "Provided by EthACK, the Swiss Privacy Basecamp\n\n" +
                        "http://torrific.ethack.org/ - https://www.ethack.org/\n\n\n" +
                        "This product is produced independently from the Tor® anonymity software " +
                        "and carries no guarantee from The Tor Project about quality, suitability or anything else.";
                alert.setMessage(String.format(about, versionName));
                alert.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing
                    }
                });
                alert.show();
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
    protected void onResume() {
        try {
            natLiteSource.open();
        } catch (SQLException e) {
            Log.e(MainActivity.class.getName(), "Unable to open database");
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        // prevent App crash when root permission is requested…
        if (!newIntent) {
            natLiteSource.close();
        }
        super.onResume();
    }

    private void showPreferences() {
        Intent intent = new Intent(this, PreferencesActivity.class);
        startActivityForResult(intent, 1);
        newIntent = false;
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

        this.listview.setAdapter(new RowAdapter(this, apps2, packageManager, natLiteSource));
    }
}
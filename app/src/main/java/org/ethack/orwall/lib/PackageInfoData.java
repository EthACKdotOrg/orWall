package org.ethack.orwall.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Small structure holding special application info
 * Taken from AFWall+:
 * https://github.com/ukanth/afwallplus/blob/fd4843824e6e6678c8b50204b68a01f6fb8ed3d0/src/dev/ukanth/ufirewall/Api.java#L1569
 */
public class PackageInfoData {
    // Linux user ID
    private long uid;
    // Application name related to UID
    private String name;
    private String pkgName;

    public PackageInfoData() {}

    public PackageInfoData(long uid, String name, String pkgName) {
        this.uid = uid;
        this.name = name;
        this.pkgName = pkgName;
    }

    public PackageInfoData(String user, String name, String pkgName) {
        this(android.os.Process.getUidForName(user), name, pkgName);
    }

    public void setUid(long uid) {
        this.uid = uid;
    }
    public long getUid() {
        return this.uid;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }
    public String getPkgName() {
        return this.pkgName;
    }

    /**
     * It seems some system things don't show up in packagemanager.
     * This code comes from AFWall+, as they already hit this small problem:
     * https://github.com/ukanth/afwallplus/blob/fd4843824e6e6678c8b50204b68a01f6fb8ed3d0/src/dev/ukanth/ufirewall/Api.java#L2313-L2327
     */
    public static Map<String, PackageInfoData> specialApps() {
        String prefix = Constants.SPECIAL_APPS_PREFIX;
        Map<String, PackageInfoData> specialApps = new HashMap<String, PackageInfoData>();
        specialApps.put(prefix+"media", new PackageInfoData("media", "Media Server", prefix+"media"));
        specialApps.put(prefix+"vpn", new PackageInfoData("vpn", "VPN Service", prefix+"vpn"));
        specialApps.put(prefix+"shell", new PackageInfoData("shell", "Linux Shell", prefix+"shell"));
        specialApps.put(prefix+"adb", new PackageInfoData("adb", "Android Debug Bridge (ADB)", prefix+"adb"));

        return specialApps;
    }
}

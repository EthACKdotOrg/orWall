package org.ethack.torrific.lib;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Database helper class
 *
 * @see SQLiteOpenHelper
 */
public class NATDBLite extends SQLiteOpenHelper {
    public static final String TABLE_NAT = "nat";
    public static final String COLUMN_APPUID = "appUID";
    public static final String COLUMN_APPNAME = "appName";
    private static final String DATABASE_CREATE =
            String.format("create table %s (%s integer primary key, %s varchar not null)",
                    TABLE_NAT, COLUMN_APPUID, COLUMN_APPNAME);
    public static final String DATABASE_NAME = "firewall.db";
    public static final int DATABASE_VERSION = 1;

    public NATDBLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Override onCreate SQLiteOpenHelper
     *
     * @param database
     */
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    /**
     * Override onUpgrade SQLiteOpenHelper
     * Maybe a bit harsh as it doesn't migrate data to new model for now.
     *
     * @param database
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(NATDBLite.class.getName(), String.format("Upgrading from %i to %i", oldVersion, newVersion));
        database.execSQL(String.format("drop table if exists %s", TABLE_NAT));
        onCreate(database);
    }

}

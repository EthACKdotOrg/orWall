package org.ethack.orwall.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.ethack.orwall.lib.Constants;

/**
 * Simple DB helper in order to manage SQLite for NAT rules.
 * This also prepare the way for more features.
 */
public class natDBHelper extends SQLiteOpenHelper {

    public static final String NAT_TABLE_NAME = "rules";
    public static final String COLUMN_APPUID = "appUID";
    public static final String COLUMN_APPNAME = "appName";
    public static final String COLUMN_ONIONTYPE = "onionType";
    public static final String COLUMN_LOCALHOST = "localhost";
    public static final String COLUMN_LOCALNETWORK = "localnetwork";

    private static final String NAT_TABLE_CREATE =
            String.format(
                    "CREATE TABLE %s (" +
                            "%s INTEGER PRIMARY KEY," +
                            "%s TEXT NOT NULL," +
                            "%s TEXT," +
                            "%s INTEGER," +
                            "%s INTEGER)",
                    NAT_TABLE_NAME,
                    COLUMN_APPUID,
                    COLUMN_APPNAME,
                    COLUMN_ONIONTYPE,
                    COLUMN_LOCALHOST,
                    COLUMN_LOCALNETWORK
            );
    private static final int DATABASE_VERSION = 1;
    private static final String DB_NAME = "nat.s3db";

    public natDBHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NAT_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        return;
    }
}

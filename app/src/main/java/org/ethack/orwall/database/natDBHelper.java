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
    public static final String COLUMN_ONIONPORT = "onionPort";
    public static final String COLUMN_PORTTYPE = "portType";
    private static final String NAT_TABLE_CREATE =
            String.format(
                    "CREATE TABLE %s (" +
                            "%s INTEGER PRIMARY KEY," +
                            "%s TEXT NOT NULL," +
                            "%s TEXT NOT NULL DEFAULT \"%s\"," +
                            "%s INTEGER NOT NULL DEFAULT '%d'," +
                            "%s TEXT NOT NULL DEFAULT \"TransProxy\")",
                    NAT_TABLE_NAME,
                    COLUMN_APPUID,
                    COLUMN_APPNAME,
                    COLUMN_ONIONTYPE, Constants.DB_ONION_TYPE_TOR,
                    COLUMN_ONIONPORT, Constants.ORBOT_TRANSPROXY,
                    COLUMN_PORTTYPE
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

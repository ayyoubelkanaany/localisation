package ma.ayyou.googlepos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class sqliteDbHelper extends SQLiteOpenHelper {
    private Context context;
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "zones.db";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + Contrat.testcontrat.TABLE_NAME + " (" + Contrat.testcontrat._ID + " INTEGER PRIMARY KEY," + Contrat.testcontrat.COLUMN_NAME_ZONE + " TEXT, "+ Contrat.testcontrat.COLUMN_NAME_ALTTUDE + " TEXT," + Contrat.testcontrat.COLUMN_NAME_LONGITUDE + " TEXT)";
    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + Contrat.testcontrat.TABLE_NAME;

    public sqliteDbHelper(Context context) {
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
       // Toast.makeText(context, "hello1", Toast.LENGTH_SHORT).show();
        this.context=context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
      ///  Toast.makeText(context, "hello2", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TABLE);
        onCreate(db);
      //  Toast.makeText(context, "hello3", Toast.LENGTH_SHORT).show();

    }
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
        onUpgrade(db, oldVersion, newVersion);
      //  Toast.makeText(context, "hello4", Toast.LENGTH_SHORT).show();

    }
    @Override
    public SQLiteDatabase getReadableDatabase() {
       // Toast.makeText(context, "hello5", Toast.LENGTH_SHORT).show();

        return super.getReadableDatabase();
    }
    @Override
    public SQLiteDatabase getWritableDatabase()
    {
       // Toast.makeText(context, "hello6", Toast.LENGTH_SHORT).show();

        return super.getWritableDatabase();
    }
}

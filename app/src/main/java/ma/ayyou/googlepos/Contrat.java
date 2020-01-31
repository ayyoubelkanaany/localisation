package ma.ayyou.googlepos;

import android.provider.BaseColumns;

public final class Contrat {

    private Contrat() {}


    public static class testcontrat implements BaseColumns {
        public static final String TABLE_NAME = "zone";
        public static final String COLUMN_NAME_ZONE ="zone";
        public static final String COLUMN_NAME_ALTTUDE ="altitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";

    }
}
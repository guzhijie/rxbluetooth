package com.jack.test.dbflow;

import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/12/13
 */
@Database(version = 1)
public final class BasicData {
    @Migration(version = 2, database = BasicData.class)
    public static final class MyBaseMigration extends BaseMigration {

        @Override
        public void migrate(@NonNull DatabaseWrapper database) {
        }
    }
}

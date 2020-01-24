package com.jack.test.dbflow;

import android.database.sqlite.SQLiteDatabase;

import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.structure.database.AndroidDatabase;
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperDelegate;
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperListener;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.OpenHelper;

import javax.annotation.Nonnull;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/12/13
 */
public class MyOpenHelperCreator implements DatabaseConfig.OpenHelperCreator {
    private final String m_databaseFileName;

    public MyOpenHelperCreator(String databaseFileName) {
        m_databaseFileName = databaseFileName;
    }

    @Override
    public OpenHelper createHelper(DatabaseDefinition databaseDefinition, DatabaseHelperListener helperListener) {
        return new OpenHelper() {
            private final AndroidDatabase m_androidDatabase = AndroidDatabase.from(SQLiteDatabase.openOrCreateDatabase(m_databaseFileName, null));
            private final DatabaseHelperDelegate m_helperDelegate = new DatabaseHelperDelegate(helperListener, databaseDefinition, this);

            @Override
            public void performRestoreFromBackup() {
                m_helperDelegate.performRestoreFromBackup();
            }

            @Override
            @Nonnull
            public DatabaseWrapper getDatabase() {
                return m_androidDatabase;
            }

            @Override
            public DatabaseHelperDelegate getDelegate() {
                return m_helperDelegate;
            }

            @Override
            public boolean isDatabaseIntegrityOk() {
                return m_helperDelegate.isDatabaseIntegrityOk();
            }

            @Override
            public void backupDB() {
                m_helperDelegate.backupDB();
            }

            @Override
            public void setDatabaseListener(DatabaseHelperListener helperListener) {
                m_helperDelegate.setDatabaseHelperListener(helperListener);
            }

            @Override
            public void closeDB() {
                m_androidDatabase.getDatabase().close();
            }
        };
    }
}

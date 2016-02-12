/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Sven Dubbeld
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.svendubbeld.car.backup;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;

import nl.svendubbeld.car.database.DatabaseHandler;

public class BackupAgent extends BackupAgentHelper {

    private static final String PREFS_BACKUP_KEY = "prefs";
    private static final String DB_BACKUP_KEY = "db";

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferencesBackupHelper preferencesBackupHelper = new SharedPreferencesBackupHelper(this, "nl.svendubbeld.car_preferences");
        addHelper(PREFS_BACKUP_KEY, preferencesBackupHelper);

        FileBackupHelper databaseBackupHelper = new FileBackupHelper(this, "../databases/" + DatabaseHandler.DATABASE_NAME);
        addHelper(DB_BACKUP_KEY, databaseBackupHelper);
    }
}
/*
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */

package com.google.code.geobeagle.xmlimport;

import com.google.code.geobeagle.activity.cachelist.presenter.CacheListRefresh;
import com.google.code.geobeagle.bcaching.BCachingModule;
import com.google.code.geobeagle.bcaching.ImportBCachingWorker;
import com.google.inject.Inject;
import com.google.inject.Provider;

import roboguice.inject.ContextScoped;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GpxImporterDI {
    // Too hard to test this class due to final methods in base.
    @ContextScoped
    public static class MessageHandler extends Handler implements MessageHandlerInterface {
        public static final String GEOBEAGLE = "GeoBeagle";
        static final int MSG_DONE = 1;
        static final int MSG_PROGRESS = 0;
        private static final int MSG_BCACHING_IMPORT = 2;

        private int mCacheCount;
        private boolean mLoadAborted;
        private CacheListRefresh mMenuActionRefresh;
        private final ProgressDialogWrapper mProgressDialogWrapper;
        private String mSource;
        private String mStatus;
        private String mWaypointId;
        private final Provider<ImportBCachingWorker> mImportBCachingWorkerProvider;
        private final SharedPreferences mSharedPreferences;

        @Inject
        public MessageHandler(ProgressDialogWrapper progressDialogWrapper,
                Provider<ImportBCachingWorker> importBCachingWorkerProvider,
                SharedPreferences sharedPreferences) {
            mProgressDialogWrapper = progressDialogWrapper;
            mImportBCachingWorkerProvider = importBCachingWorkerProvider;
            mSharedPreferences = sharedPreferences;
        }

        @Override
        public void abortLoad() {
            mLoadAborted = true;
            mProgressDialogWrapper.dismiss();
        }

        @Override
        public void handleMessage(Message msg) {
            // Log.d(GEOBEAGLE, "received msg: " + msg.what);
            switch (msg.what) {
                case MessageHandler.MSG_PROGRESS:
                    mProgressDialogWrapper.setMessage(mStatus);
                    break;
                case MessageHandler.MSG_DONE:
                    if (!mLoadAborted) {
                        mProgressDialogWrapper.dismiss();
                        mMenuActionRefresh.forceRefresh();
                    }
                    break;
                case MessageHandler.MSG_BCACHING_IMPORT:
                    if (mLoadAborted) {
                        return;
                    }

                    if (mSharedPreferences.getBoolean(BCachingModule.BCACHING_ENABLED, false)) {
                        ImportBCachingWorker importBCachingWorker = mImportBCachingWorkerProvider
                                .get();
                        importBCachingWorker.start();
                        while (!importBCachingWorker.inProgress()) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Log.d("GeoBeagle",
                                        "InterruptedException while waiting for bcaching thread to die: "
                                                + e);
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void loadComplete() {
            sendEmptyMessage(MessageHandler.MSG_DONE);
        }

        @Override
        public void start(CacheListRefresh cacheListRefresh) {
            mCacheCount = 0;
            mLoadAborted = false;
            mMenuActionRefresh = cacheListRefresh;
            // TODO: move text into resource.
            mProgressDialogWrapper.show("Sync from sdcard", "Please wait...");
        }

        @Override
        public void updateName(String name) {
            mStatus = mCacheCount++ + ": " + mSource + " - " + mWaypointId + " - " + name;
            if (!hasMessages(MessageHandler.MSG_PROGRESS))
                sendEmptyMessage(MessageHandler.MSG_PROGRESS);
        }

        @Override
        public void updateSource(String text) {
            mSource = text;
            mStatus = "Opening: " + mSource + "...";
            if (!hasMessages(MessageHandler.MSG_PROGRESS))
                sendEmptyMessage(MessageHandler.MSG_PROGRESS);
        }

        @Override
        public void updateWaypointId(String wpt) {
            mWaypointId = wpt;
        }

        @Override
        public void updateStatus(String status) {
            mStatus = status;
            if (!hasMessages(MessageHandler.MSG_PROGRESS))
                sendEmptyMessage(MessageHandler.MSG_PROGRESS);
        }

        @Override
        public void deletingCacheFiles() {
            mStatus = "Deleting old cache files....";
            if (!hasMessages(MessageHandler.MSG_PROGRESS))
                sendEmptyMessage(MessageHandler.MSG_PROGRESS);
        }

        @Override
        public void startBCachingImport() {
            sendEmptyMessage(MessageHandler.MSG_BCACHING_IMPORT);
        }

    }

    // Wrapper so that containers can follow the "constructors do no work" rule.
    public static class ProgressDialogWrapper {
        private final Provider<Context> mContextProvider;
        private ProgressDialog mProgressDialog;

        @Inject
        public ProgressDialogWrapper(Provider<Context> context) {
            mContextProvider = context;
        }

        public void dismiss() {
            if (mProgressDialog != null)
                mProgressDialog.dismiss();
        }

        public void setMessage(CharSequence message) {
            mProgressDialog.setMessage(message);
        }

        public void show(String title, String msg) {
            mProgressDialog = ProgressDialog.show(mContextProvider.get(), title, msg);
//            mProgressDialog.setCancelable(true);
        }
    }
}

package com.byd.library;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import com.byd.accountservice.ISaveAndReadFileInterface;
import java.util.concurrent.LinkedBlockingQueue;

public class SaveReadManager {
    private static final long RETRY_TIME = 5000;
    private static final String TAG = "SaveReadManager";
    private static final String THREAD_NAME = "bindServiceThread";
    private static volatile SaveReadManager sInstance;
    private Context mContext;
    private final Handler mHandlerThread;
    private ISaveAndReadFileInterface mInterface;
    private final Handler mMainThread;
    private IServiceConnectListener mServiceListener;
    private final LinkedBlockingQueue<Runnable> mTaskQueue = new LinkedBlockingQueue<>();
    private boolean isServiceConnected = false;
    private final Runnable mBindServiceTask = new Runnable() { // from class: com.byd.library.SaveReadManager$$ExternalSyntheticLambda0
        @Override // java.lang.Runnable
        public final void run() {
            SaveReadManager.this.bindService();
        }
    };
    private final ServiceConnection mServiceConnection = new ServiceConnection() { // from class: com.byd.library.SaveReadManager.1
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(SaveReadManager.TAG, "[onServiceConnected]");
            SaveReadManager.this.mInterface = ISaveAndReadFileInterface.Stub.asInterface(service);
            try {
                service.linkToDeath(SaveReadManager.this.mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (SaveReadManager.this.mServiceListener != null) {
                SaveReadManager.this.mServiceListener.onServiceConnected();
            }
            SaveReadManager.this.isServiceConnected = true;
            SaveReadManager.this.handleTask();
            SaveReadManager.this.mHandlerThread.removeCallbacks(SaveReadManager.this.mBindServiceTask);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            Log.e(SaveReadManager.TAG, "[onServiceDisconnected]");
            SaveReadManager.this.mInterface = null;
            if (SaveReadManager.this.mServiceListener != null) {
                SaveReadManager.this.mServiceListener.onServiceDisconnected();
            }
            SaveReadManager.this.isServiceConnected = false;
        }
    };
    private final IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() { // from class: com.byd.library.SaveReadManager.2
        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Log.e(SaveReadManager.TAG, "[binderDied]");
            if (SaveReadManager.this.mServiceListener != null) {
                SaveReadManager.this.mServiceListener.onBinderDied();
            }
            SaveReadManager.this.isServiceConnected = false;
            if (SaveReadManager.this.mInterface != null) {
                SaveReadManager.this.mInterface.asBinder().unlinkToDeath(SaveReadManager.this.mDeathRecipient, 0);
                SaveReadManager.this.mInterface = null;
            }
            SaveReadManager.this.attemptToRebindService();
        }
    };

    public interface IServiceConnectListener {
        void onBinderDied();

        void onServiceConnected();

        void onServiceDisconnected();
    }

    private long getRetryBindTimeMill() {
        return 5000L;
    }

    private SaveReadManager() {
        HandlerThread handlerThread = new HandlerThread(THREAD_NAME, 6);
        handlerThread.start();
        this.mHandlerThread = new Handler(handlerThread.getLooper());
        this.mMainThread = new Handler(Looper.getMainLooper());
    }

    public static SaveReadManager getInstance() {
        if (sInstance == null) {
            synchronized (SaveReadManager.class) {
                if (sInstance == null) {
                    sInstance = new SaveReadManager();
                }
            }
        }
        return sInstance;
    }

    public void init(Context context, IServiceConnectListener listener) {
        if (context != null) {
            this.mContext = context.getApplicationContext();
        }
        Log.d(TAG, "SDKVersion= " + getSDKVersion());
        this.mServiceListener = listener;
        bindService();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void bindService() {
        if (this.mInterface == null) {
            if (this.mContext == null) {
                Log.e(TAG, "Context is null, return!");
                return;
            }
            Log.e(TAG, "[bindService] start");
            Intent intent = new Intent();
            intent.setAction("byd.intent.action.SAVE_READ_FILE");
            intent.setPackage("com.byd.accountservice");
            boolean bindService = this.mContext.bindService(intent, this.mServiceConnection, 1);
            Log.w(TAG, "[bindService] result: " + bindService);
            if (bindService) {
                return;
            }
            attemptToRebindService();
            return;
        }
        Log.w(TAG, "[bindService] not need");
    }

    public void saveFile(String filePath, String fileName, String content) {
        if (isServiceConnected(true)) {
            try {
                this.mInterface.saveFile(filePath, fileName, content);
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.e(TAG, "saveFile Catch an Exception, ", e);
            }
        }
    }

    public String readFile(String strFilePath) {
        if (isServiceConnected(true)) {
            try {
                return this.mInterface.readFile(strFilePath);
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.e(TAG, "readFile Catch an Exception, ", e);
            }
        }
        return "";
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void attemptToRebindService() {
        Log.w(TAG, "[attemptToRebindService] version: " + getSDKVersion());
        this.mHandlerThread.postDelayed(this.mBindServiceTask, getRetryBindTimeMill());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleTask() {
        while (true) {
            Runnable poll = this.mTaskQueue.poll();
            if (poll == null) {
                return;
            }
            Log.w(TAG, "[handleTask] poll task form task queue");
            this.mHandlerThread.post(poll);
        }
    }

    public boolean isServiceConnected() {
        return isServiceConnected(false);
    }

    private boolean isServiceConnected(boolean tryConnect) {
        Log.w(TAG, "[isServiceConnected] tryConnect " + tryConnect + ";isConnected " + (this.mInterface != null));
        if (this.mInterface == null && tryConnect) {
            attemptToRebindService();
        }
        return this.mInterface != null || this.isServiceConnected;
    }

    public void release() {
        Log.w(TAG, "[release]");
        if (isServiceConnected()) {
            ISaveAndReadFileInterface iSaveAndReadFileInterface = this.mInterface;
            if (iSaveAndReadFileInterface != null) {
                iSaveAndReadFileInterface.asBinder().unlinkToDeath(this.mDeathRecipient, 0);
                this.mInterface = null;
            }
            try {
                this.mContext.unbindService(this.mServiceConnection);
            } catch (Exception e) {
                Log.e(TAG, "release Service: " + e.getMessage(), e);
            }
        }
        this.mServiceListener = null;
    }

    private LinkedBlockingQueue<Runnable> getTaskQueue() {
        return this.mTaskQueue;
    }

    private Handler getMainHandler() {
        return this.mMainThread;
    }

    private String getSDKVersion() {
        try {
            return this.mContext.getPackageManager().getPackageInfo(this.mContext.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "1.0.000.20230308";
        }
    }
}
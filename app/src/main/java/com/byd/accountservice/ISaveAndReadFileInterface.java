package com.byd.accountservice;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISaveAndReadFileInterface extends IInterface {

    public static class Default implements ISaveAndReadFileInterface {
        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }

        @Override // com.byd.accountservice.ISaveAndReadFileInterface
        public String readFile(String strFilePath) throws RemoteException {
            return null;
        }

        @Override // com.byd.accountservice.ISaveAndReadFileInterface
        public void saveFile(String filePath, String fileName, String content) throws RemoteException {
        }
    }

    String readFile(String strFilePath) throws RemoteException;

    void saveFile(String filePath, String fileName, String content) throws RemoteException;

    public static abstract class Stub extends Binder implements ISaveAndReadFileInterface {
        private static final String DESCRIPTOR = "com.byd.accountservice.ISaveAndReadFileInterface";
        static final int TRANSACTION_readFile = 2;
        static final int TRANSACTION_saveFile = 1;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISaveAndReadFileInterface asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface queryLocalInterface = obj.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface != null && (queryLocalInterface instanceof ISaveAndReadFileInterface)) {
                return (ISaveAndReadFileInterface) queryLocalInterface;
            }
            return new Proxy(obj);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                saveFile(data.readString(), data.readString(), data.readString());
                reply.writeNoException();
                return true;
            }
            if (code != 2) {
                if (code == 1598968902) {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                return super.onTransact(code, data, reply, flags);
            }
            data.enforceInterface(DESCRIPTOR);
            String readFile = readFile(data.readString());
            reply.writeNoException();
            reply.writeString(readFile);
            return true;
        }

        private static class Proxy implements ISaveAndReadFileInterface {
            public static ISaveAndReadFileInterface sDefaultImpl;
            private IBinder mRemote;

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // com.byd.accountservice.ISaveAndReadFileInterface
            public void saveFile(String filePath, String fileName, String content) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(filePath);
                    obtain.writeString(fileName);
                    obtain.writeString(content);
                    if (!this.mRemote.transact(1, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        Stub.getDefaultImpl().saveFile(filePath, fileName, content);
                    } else {
                        obtain2.readException();
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.byd.accountservice.ISaveAndReadFileInterface
            public String readFile(String strFilePath) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(strFilePath);
                    if (!this.mRemote.transact(2, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().readFile(strFilePath);
                    }
                    obtain2.readException();
                    return obtain2.readString();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISaveAndReadFileInterface impl) {
            if (Proxy.sDefaultImpl != null) {
                throw new IllegalStateException("setDefaultImpl() called twice");
            }
            if (impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISaveAndReadFileInterface getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
package com.peihua.touchmonitor;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import com.fz.common.file.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InstallerActivity extends ComponentActivity {


    public Uri f2864OooOOo;

    public Uri f2867OooOo;

    public int mType;

    public Uri mTreeUri;

    public Uri f2873OooOoO0;


    public final Uri OooOOO(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        InputStream inputStream = null;
        FileOutputStream fos = null;
        try {
            inputStream = contentResolver.openInputStream(uri);
            File r2 = getExternalFilesDir("share_apk");
            String r7 = uri.getLastPathSegment();
            int r3 = r7.length();
            if (r3 < 3) {
                r7 = "temp";
            }
            File tempFile = java.io.File.createTempFile(r7, ".apk", r2);
            Log.e("xxxxx", tempFile.getAbsolutePath());
            fos = new FileOutputStream(tempFile);
            byte[] byteArray = new byte[8192];
            while ((r3 = inputStream.read(byteArray)) != -1) {
                fos.write(byteArray, 0, r3);
            }
            inputStream.close();
            fos.close();
            return FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID+".provider", tempFile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public final void OooOOOO(Uri uri) {
        String scheme = uri.getScheme();
        if ("content".equals(scheme)) {
            Log.e("paramUri_pre", uri.toString());
            Uri r7 = OooOOO(uri);
            Log.e("paramUri", r7 + "");
            File parentFile = getExternalFilesDir("");
            StringBuilder builder = new StringBuilder();
            builder.append(parentFile);
            if (r7 == null) {
                Toast.makeText(this, "解析包出问题1", Toast.LENGTH_LONG).show();
                Log.e("解析包出问题", "1");
                return;
            }
            builder.append(r7.getPath());
            Log.e("str2", builder.toString());
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageArchiveInfo(builder.toString(), 1);
            if (packageInfo == null) {
                Toast.makeText(this, "解析包出问题2", Toast.LENGTH_LONG).show();
                Log.e("解析包出问题", "2");
                return;
            }
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            applicationInfo.sourceDir = builder.toString();
            applicationInfo.publicSourceDir = builder.toString();
            f2864OooOOo = r7;
        }
    }

    public final Uri OooOOOo(Uri uri) {
        DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
        if (documentFile == null) {
            return null;
        }
        if (!documentFile.exists()) {
            return null;
        }
        File file = getExternalFilesDir("share_apk");
        DocumentFile r1 = DocumentFile.fromFile(file);
        DocumentFile r0 = r1.createFile("", documentFile.getName());
        if (r0 == null) {
            return null;
        }
        ContentResolver contentResolver = getContentResolver();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = contentResolver.openInputStream(uri);
            outputStream = contentResolver.openOutputStream(r0.getUri()); 
            byte[] bytes = new byte[8192]; 
            int length;
            while ((length = inputStream.read(bytes)) > 0) {
                outputStream.write(bytes, 0, length);
            }
            return r0.getUri();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public final java.io.File OooOOo(Uri uri) {
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        if (scheme == null) {
            return null;
        }
        if ("content".equals(scheme)) {
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor == null) {
                return null;
            }
            cursor.moveToFirst();
            String columnName = "_data";
            int columnIndex = cursor.getColumnIndexOrThrow(columnName);
            String columnValue = cursor.getString(columnIndex);
            cursor.close();
            if (columnValue == null) {
                return null;
            }
            return new File(columnValue);
        } else {
            if ("file".equals(scheme)) {
                return new File(uri.getPath());
            }
        }
        return null;
    }

    public final void OooOOo0(Uri uri, Uri uri1) {
        if (uri == null) {
            return;
        }
        Log.e("treeUri", uri.toString());
        DocumentFile documentFile = DocumentFile.fromTreeUri(this, uri);
        if (documentFile == null) {
            return;
        }
        Log.e("pickedDir", documentFile.toString());
        if (!documentFile.exists()) {
            return;
        }
        if (!documentFile.isDirectory()) {
            return;
        }
        DocumentFile[] docFiles = documentFile.listFiles();
        int length = docFiles.length;
        int index = 0;
        if (index == length) return;
        DocumentFile documentFile1 = null;
        while (length > index) {
            documentFile1 = docFiles[index];
            Log.e("DocumentFile", documentFile1.getName());
            Uri docFileUri = documentFile1.getUri();
            String r6 = docFileUri.getPath();
            String r7 = uri1.getPath();
            if (documentFile1.isFile() && r6.equals(r7)) {
                break;
            }
            index = index + 1;
        }
        if (!documentFile1.exists()) return;
        if (!documentFile1.canRead()) return;
        android.content.ContentResolver contentResolver = getContentResolver(); 
        Uri r0 = documentFile1.getUri();
        File uri1File = OooOOoo(uri1);
        try {
            InputStream inputStream = contentResolver.openInputStream(r0);
            FileOutputStream fos = new FileOutputStream(uri1File);
            byte[] bytes = new byte[8192];
            while ((length = inputStream.read(bytes)) > 0) {
                fos.write(bytes, 0, length);
            }

            fos.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Uri r9 = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", uri1File);
        if (r9 == null) return;
        StringBuilder builder = new StringBuilder();
        java.io.File externalFilesDir = getExternalFilesDir("");
        builder.append(externalFilesDir);
        builder.append(r9.getPath());
        String r10 = uri1.toString();
        PackageManager packageManager = getPackageManager();
        android.content.pm.PackageInfo packageInfo = packageManager.getPackageArchiveInfo(r10, 1);
        if (packageInfo == null) {
            Toast.makeText(this, "解析包出问题", Toast.LENGTH_LONG).show();
            return;
        }
        android.content.pm.ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        applicationInfo.sourceDir = r10;
        applicationInfo.publicSourceDir = r10;
        DocumentFile documentFile2 = DocumentFile.fromSingleUri(this, r9);
        if (documentFile2 == null) return;
        if (!documentFile2.exists()) return;
        f2867OooOo = OooOOOo(r9);
        Log.e("newUri", f2867OooOo.getPath());
    }

    public final java.io.File OooOOoo(Uri uri) {
        try {
            File r0 = getExternalFilesDir("share_apk");
            String r3 = uri.getLastPathSegment();
            String r1 = ".apk";
            return File.createTempFile(r3, r1, r0);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public final void OooOo(Uri r5) {
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT < 30) {
            intent.setData(r5);
            startActivity(intent);
        } else {
            File file = OooOOo(r5);
            if (file == null) return;
            intent.setData(FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file));
            startActivity(intent);
        }
    }

    public final void OooOo0o(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, "解析包出问题4", Toast.LENGTH_LONG).show();
            Log.e("解析包出问题", "4");
            return;
        }
        try {
            String r4 = "application/vnd.android.package-archive";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT < 29) {
                intent.setDataAndType(uri, r4);
            } else {
                intent.setDataAndType(uri, r4);
            }

            startActivity(intent);
            finish();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public final void OooOoO(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, "解析包出问题7", Toast.LENGTH_LONG).show();
            Log.e("解析包出问题", "7");
            return;
        }
        String scheme = uri.getScheme();
        if (scheme == null) {
            Toast.makeText(this, "解析包出问题7", Toast.LENGTH_LONG).show();
            Log.e("解析包出问题", "7");
            return;
        }
        if ("file".equals(scheme)) {
            if (Build.VERSION.SDK_INT >= 30) {
                f2873OooOoO0 = uri;
                Uri r0 = mTreeUri;
                OooOOo0(r0, uri);
                return;
            }
        }
        StringBuilder builder = new StringBuilder();
        File externalFilesDir = getExternalFilesDir("");
        builder.append(externalFilesDir);
        builder.append(uri.getPath());
        String path = builder.toString();
        PackageManager packageManager = getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(path, 1);
        if (packageInfo == null) {
            Toast.makeText(this, "解析包出问题8", Toast.LENGTH_LONG).show();
            Log.e("解析包出问题", "8");
            return;
        }
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        applicationInfo.sourceDir = path;
        applicationInfo.publicSourceDir = path;
        DocumentFile r0 = DocumentFile.fromSingleUri(this, uri);
        if (r0 == null) return;
        if (!r0.exists()) return;
        f2867OooOo = OooOOOo(uri);
    }

    public final void OooOoO0(Uri uri) {
        Log.e("installer", uri.toString());
        Uri r5 = OooOOO(uri);
        File externalFilesDir = getExternalFilesDir("");
        StringBuilder builder = new StringBuilder();
        builder.append(externalFilesDir);
        builder.append(r5.getPath());
        String path = builder.toString();
        PackageManager packageManager = getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(path, 1);
        if (packageInfo == null) {
            Toast.makeText(this, "解析包出问题3", Toast.LENGTH_LONG).show();
            Log.e("解析包出问题", "3");
            return;
        }
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        applicationInfo.sourceDir = path;
        applicationInfo.publicSourceDir = path;
    }



    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("qwer", "onCreate");
        java.io.File r6 = getExternalFilesDir("share_apk");
        FileUtil.deleteFileOrDir(r6);
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri == null) {
            finish();
            return;
        }
        mType = intent.getIntExtra("type", 0);
        if (mType == 1) {
            if (Build.VERSION.SDK_INT < 30) {
                OooOoO0(uri);
                installApk();
                return;
            } else {
                OooOoO(uri);
                installApk();
                return;
            }
        }
        Log.e("detailDocumentByUri", uri.getPath());
        OooOOOO(uri);
        installApk();
        finish();
    }
    public void installApk() {
        if (Build.VERSION.SDK_INT >= 30){
            if (mType == 1){
                if (f2867OooOo!=null) {
                    OooOo(f2867OooOo);
                    return;
                }
            }
        }
        if (f2864OooOOo!=null) {
            OooOo0o(f2864OooOOo);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("qwer", "onDestroy");
    }

    @Override
    public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        Log.e("qwer", "onNewIntent");
        Uri uri = intent.getData();
        if (uri == null) return;
        OooOOOO(uri);
        installApk();
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("qwer", "onResume");
        if (getPackageName().equals(BuildConfig.APPLICATION_ID)) return;
        Log.d("error", "tuichu: ");
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("qwer", "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
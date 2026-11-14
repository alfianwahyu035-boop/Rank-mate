package com.rankmate.dashboard;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ClipData;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Full native Android host for the offline RankMate V5.4 dashboard.
 *
 * Features: local trusted HTTPS-like origin, persistent LocalStorage,
 * native Excel source picker, native Downloads for generated PNG/XLSX/CSV/JSON,
 * Android share sheet for generated PNG, and hardware back navigation.
 * No Capacitor runtime or external Android library required.
 */
public final class MainActivity extends Activity {
    private static final String TAG = "RankMate";
    private static final int FILE_PICK_REQUEST = 27421;
    private static final String HOST = "appassets.androidplatform.net";
    private static final String HOME = "https://" + HOST + "/index.html";
    private WebView webView;
    private ValueCallback<Uri[]> pendingFileCallback;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        webView = new WebView(this);
        setContentView(webView);
        webView.setBackgroundColor(0xFFFFFEFA);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportMultipleWindows(false);
        settings.setDefaultTextEncodingName("UTF-8");
        webView.addJavascriptInterface(new NativeExportBridge(), "AndroidRankMate");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view,
                ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (pendingFileCallback != null) pendingFileCallback.onReceiveValue(null);
                pendingFileCallback = callback;
                try {
                    Intent intent = params.createIntent();
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, FILE_PICK_REQUEST);
                    return true;
                } catch (ActivityNotFoundException | SecurityException error) {
                    pendingFileCallback = null;
                    Toast.makeText(MainActivity.this,"Pemilih file tidak tersedia",Toast.LENGTH_LONG).show();
                    callback.onReceiveValue(null);
                    return false;
                }
            }
            @Override public boolean onConsoleMessage(ConsoleMessage message) {
                if (message.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                    Log.e(TAG, message.message() + " @ " + message.lineNumber());
                }
                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (!HOST.equalsIgnoreCase(uri.getHost())) return super.shouldInterceptRequest(view,request);
                String path = uri.getPath();
                if (path == null || path.equals("/")) path = "/index.html";
                if (!path.equals("/index.html") && !path.equals("/native-bridge.js")) return notFound();
                try {
                    InputStream stream = getAssets().open(path.substring(1));
                    String mime = path.endsWith(".html") ? "text/html" : "application/javascript";
                    return new WebResourceResponse(mime,"UTF-8",stream);
                } catch (IOException error) {
                    Log.e(TAG, "Asset load failed: " + path, error);
                    return notFound();
                }
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (HOST.equalsIgnoreCase(uri.getHost())) return false;
                if (request.isForMainFrame()) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW,uri)); }
                    catch (ActivityNotFoundException error) { showToast("Tautan tidak bisa dibuka"); }
                }
                return true;
            }
            @Override public void onPageFinished(WebView view, String url) {
                if (!url.startsWith("https://" + HOST + "/")) return;
                try (InputStream in = getAssets().open("native-bridge.js")) {
                    java.io.ByteArrayOutputStream output=new java.io.ByteArrayOutputStream();
                    byte[] buffer=new byte[8192]; int count;
                    while((count=in.read(buffer))!=-1)output.write(buffer,0,count);
                    view.evaluateJavascript(new String(output.toByteArray(), StandardCharsets.UTF_8), null);
                } catch (IOException error) { Log.e(TAG,"Native JS bridge init failed",error); }
            }
        });
        webView.loadUrl(HOME);
    }

    private WebResourceResponse notFound() {
        return new WebResourceResponse("text/plain", "UTF-8", 404,"Not Found",
                java.util.Collections.emptyMap(), new ByteArrayInputStream(new byte[0]));
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent result) {
        super.onActivityResult(requestCode,resultCode,result);
        if (requestCode != FILE_PICK_REQUEST) return;
        ValueCallback<Uri[]> callback = pendingFileCallback;
        pendingFileCallback = null;
        if (callback == null) return;
        Uri[] uris = null;
        if (resultCode == RESULT_OK && result != null) {
            ClipData selected = result.getClipData();
            if (selected != null && selected.getItemCount()>0) {
                uris = new Uri[selected.getItemCount()];
                for (int i=0;i<selected.getItemCount();i++) uris[i]=selected.getItemAt(i).getUri();
            } else if (result.getData()!=null) {
                uris = new Uri[]{result.getData()};
            }
        }
        callback.onReceiveValue(uris);
    }

    private void showToast(String value) {
        mainHandler.post(() -> Toast.makeText(this,value,Toast.LENGTH_LONG).show());
    }

    /** Store app exports in public Downloads/RankMate on Android 10+. */
    private Uri publishToDownloads(File temporary, String name, String mime) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        values.put(MediaStore.MediaColumns.MIME_TYPE,mime);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS+"/RankMate");
        values.put(MediaStore.MediaColumns.IS_PENDING,1);
        Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if(uri==null) throw new IOException("Tidak bisa membuat file Downloads");
        try (InputStream src = new FileInputStream(temporary);
             OutputStream dest = getContentResolver().openOutputStream(uri)) {
            if (dest==null) throw new IOException("Gagal membuka berkas tujuan");
            byte[] buffer = new byte[64*1024]; int n;
            while ((n=src.read(buffer))!=-1) dest.write(buffer,0,n);
        } catch(IOException error) {
            getContentResolver().delete(uri,null,null);
            throw error;
        }
        values.clear(); values.put(MediaStore.MediaColumns.IS_PENDING,0);
        getContentResolver().update(uri,values,null,null);
        return uri;
    }

    private void shareUri(Uri uri, String mime) {
        mainHandler.post(() -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType(mime);
            share.putExtra(Intent.EXTRA_STREAM,uri);
            share.setClipData(ClipData.newUri(getContentResolver(),"RankMate",uri));
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try { startActivity(Intent.createChooser(share,"Bagikan ranking RankMate")); }
            catch (ActivityNotFoundException error) { showToast("Aplikasi berbagi tidak tersedia"); }
        });
    }

    @Override public void onBackPressed() {
        if (webView!=null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override public void onDestroy() {
        if (pendingFileCallback!=null) {
            pendingFileCallback.onReceiveValue(null);
            pendingFileCallback=null;
        }
        if (webView!=null) {
            webView.removeJavascriptInterface("AndroidRankMate");
            webView.destroy();
        }
        super.onDestroy();
    }

    private final class NativeExportBridge {
        private OutputStream currentStream;
        private File currentFile;
        private String currentName;
        private String currentMime;
        private boolean currentShare;

        @JavascriptInterface public synchronized void beginExport(String filename,String mime, boolean share) {
            closeCurrent();
            try {
                currentName = filename == null ? "rankmate-export" : filename.replaceAll("[^a-zA-Z0-9._-]","_");
                if (currentName.isEmpty() || currentName.equals(".") || currentName.equals("..")) currentName="rankmate-export";
                currentMime = mime == null || mime.isEmpty() ? "application/octet-stream" : mime;
                currentShare=share;
                currentFile = File.createTempFile("rankmate-",".download",getCacheDir());
                currentStream=new FileOutputStream(currentFile);
            } catch(IOException error) {
                Log.e(TAG,"beginExport failed",error);closeCurrent();showToast("Tidak bisa menyiapkan ekspor");
            }
        }
        @JavascriptInterface public synchronized void appendExport(String base64Chunk) {
            if (currentStream==null) return;
            try { currentStream.write(Base64.decode(base64Chunk,Base64.DEFAULT)); }
            catch(Exception error) {
                Log.e(TAG,"appendExport failed",error);closeCurrent();showToast("Gagal menerima berkas ekspor");
            }
        }
        @JavascriptInterface public synchronized void finishExport() {
            if(currentStream==null||currentFile==null) return;
            try {currentStream.close();}catch(IOException ignored){}
            final File file=currentFile;final String filename=currentName, mime=currentMime;
            final boolean share=currentShare;
            currentStream=null;currentFile=null;currentName=null;currentMime=null;
            new Thread(() -> {
                try {
                    Uri uri=publishToDownloads(file,filename,mime);
                    showToast("Disimpan: Downloads/RankMate/"+filename);
                    if(share)shareUri(uri,mime);
                } catch(Exception error) {
                    Log.e(TAG,"Saving to Downloads failed",error);
                    showToast("Gagal menyimpan file: "+error.getMessage());
                } finally { if (!file.delete()) Log.w(TAG,"Cache file retained"); }
            },"RankMateSave").start();
        }
        private synchronized void closeCurrent() {
            if(currentStream!=null) try {currentStream.close();}catch(IOException ignored){}
            if(currentFile!=null && currentFile.exists()) currentFile.delete();
            currentStream=null;currentFile=null;
        }
    }
}

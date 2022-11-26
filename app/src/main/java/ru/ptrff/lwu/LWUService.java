package ru.ptrff.lwu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class LWUService extends WallpaperService {

    static int nextEngineId = 1;


    @Override
    public WallpaperService.Engine onCreateEngine(){
        return new LWUEngine(this);
    }

    public class LWUEngine extends WallpaperService.Engine {
        private Context myContext;
        private WebView myWebView;
        private SurfaceHolder myHolder;
        private int myId;
        private LWUJSInterface myJSInterface;
        private BroadcastReceiver myMessageReceiver;

        public LWUEngine(Context context) {
            myId = nextEngineId;
            nextEngineId++;
            myContext = context;
            myWebView = null;
            myMessageReceiver = null;
            log("Engine created.");
        }

        private void log(String message) {
            Log.d("LWUEngine " + myId, message);
        }

        private void logError(String message) {
            Log.e("LWUEngine " + myId, message);
        }

        public void incomingMessage() {
            Intent intent = new Intent("draw-frame");
            LocalBroadcastManager.getInstance(myContext).sendBroadcast(intent);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            log("On Create");
            super.onCreate(surfaceHolder);
        }

        @Override
        public void onDestroy() {
            log("On Destroy");
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            log("On Visibility Changed " + String.valueOf(visible));
            super.onVisibilityChanged(visible);

            if (myWebView == null) {
                return;
            }


            if (visible) {
                //myWebView.loadUrl("javascript:resumeWallpaper()");
            } else {
                //myWebView.loadUrl("javascript:pauseWallpaper()");
            }
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            log("On Surface Create");
            super.onSurfaceCreated(holder);

            myHolder = holder;

            // Create WebView
            if (myWebView != null) {
                myWebView.destroy();
            }

            WebViewClient client = new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return false;
                }
            };

            myWebView = new WebView(myContext);

            myWebView.setWebViewClient(client);
            WebView.setWebContentsDebuggingEnabled(true);
            WebSettings webSettings = myWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);

            myJSInterface = new LWUJSInterface(this);
            myWebView.addJavascriptInterface(myJSInterface, "androidWallpaperInterface");
            myWebView.loadUrl("file:///android_asset/wallpaper.html");


            // Create message receiver
            myMessageReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    drawFrame();
                }
            };

            LocalBroadcastManager.getInstance(myContext).registerReceiver(myMessageReceiver,
                    new IntentFilter("draw-frame")
            );
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            myWebView.dispatchTouchEvent(event);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            log("On Surface Destroy");

            if (myMessageReceiver != null) {
                LocalBroadcastManager.getInstance(myContext).unregisterReceiver(myMessageReceiver);
                myMessageReceiver = null;
            }

            if (myWebView != null) {
                myWebView.destroy();
                myWebView = null;
            }

            super.onSurfaceDestroyed(holder);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            log("On Surface Changed " + String.valueOf(format) + ", " + String.valueOf(width) + ", " + String.valueOf(height));
            super.onSurfaceChanged(holder, format, width, height);

            if (myWebView != null) {
                int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
                int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
                myWebView.measure(widthSpec, heightSpec);
                myWebView.layout(0, 0, width, height);
            }
        }

        public void drawFrame() {
            if (myWebView != null) {
                try {
                    Canvas canvas = myHolder.lockCanvas();
                    if (canvas == null) {
                        logError("Can't lock canvas");
                    } else {
                        myWebView.draw(canvas);
                        myHolder.unlockCanvasAndPost(canvas);
                    }
                } catch (Exception e) {
                    logError("drawing exception " + e.toString());
                }
            }
        }
    }


    // JS интерфейс позволяет получать сообщения от вебвью
    public class LWUJSInterface {
        private LWUEngine activity;
        public LWUJSInterface  (LWUEngine activity) {
            this.activity = activity;
        }

        @JavascriptInterface
        public void drawFrame(){
            this.activity.incomingMessage();
        }
    }
}
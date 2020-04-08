/*
 * This program is the client app for Team Unununium's VR Robot Explorer found at <https://github.com/team-unununium>
 * Copyright (C) 2020 Team Unununium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/> .
 */

package com.unununium.vrclient.functions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.unununium.vrclient.BuildConfig;

import org.jetbrains.annotations.NotNull;

/** Functions that alters the UI of the app. **/
public class UIFunctions {
    /** Inserts a HTML text into a TextView. **/
    public static void setHtml(TextView view, String htmlText) {
        Spanned output;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            output = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY);
        } else {
            output = Html.fromHtml(htmlText);
        }
        view.setText(output);
        view.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /** Initializes a WebView to play the specified video from the Twitch user. **/
    @SuppressLint("SetJavaScriptEnabled")
    public static void initWebView(@NotNull WebView webView) {
        String currentUrl = "https://player.twitch.tv/?channel=" + BuildConfig.CHANNEL_ID;
        webView.setWebViewClient(new WebViewClient() {
            // WebView doesn't work on emulators because reasons
            // Remove element for ads (#amznidpxl) & overlay (.player-ui)
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // element.remove() does not work on WebView for some reason
                webView.loadUrl(
                        "javascript:(function() {" +
                                "console.log('I1');" +
                                "document.getElementsByClassName('player-ui')[0].style.display='none';" +
                                "})()");
                webView.loadUrl("javascript:(function() {" +
                        "console.log('I2');" +
                        "document.getElementById('#amaznidpxl').style.display='none';" +
                        "})()");
                webView.loadUrl("javascript:(function() {console.log(document.body.outerHTML)})()");
            }
        });
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        /* webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(webView.getContext().getFilesDir().getAbsolutePath() + "/cache");
        webSettings.setDatabaseEnabled(true); */
        webView.loadUrl(currentUrl);
    }

    /** Clears a specific WebView. **/
    public static void clearWebView(@NotNull WebView webView) {
        webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(false);
        webSettings.setLoadWithOverviewMode(false);
        webSettings.setDomStorageEnabled(false);
        webSettings.setAppCacheEnabled(false);
        webSettings.setDatabaseEnabled(false);
        webView.loadUrl("about:blank");
    }

    /** Set the top bar of the screen to be hidden. **/
    public static void setImmersiveSticky(@NotNull Activity activity) {
        activity.getWindow()
                .getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

}

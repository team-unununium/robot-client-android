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

package com.unununium.vrclient.update;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.HashMap;
import java.util.Map;

/** Request format used by Volley to download a file. **/
public class VolleyFileDownloadRequest extends Request<byte[]> {
    private final Response.Listener<byte[]> requestResponse;
    private final Map<String, String> dlParams;

    /** Default constructor. **/
    VolleyFileDownloadRequest(int method, String mUrl, Response.Listener<byte[]> listener,
                              Response.ErrorListener errorListener, HashMap<String, String> params) {
        super(method, mUrl, errorListener);
        setShouldCache(false);
        requestResponse = listener;
        dlParams = params;
    }

    /** Returns the params. Nothing to see here. **/
    @Override
    protected Map<String, String> getParams() {
        return dlParams;
    }

    /** Delivers the response, nothing to see here. **/
    @Override
    protected void deliverResponse(byte[] response) {
        requestResponse.onResponse(response);
    }

    /** Pass on the response data. Nothing to see here. **/
    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        return Response.success( response.data, HttpHeaderParser.parseCacheHeaders(response));
    }
}

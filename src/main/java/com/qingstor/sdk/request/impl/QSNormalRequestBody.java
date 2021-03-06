/*
 * Copyright (C) 2020 Yunify, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this work except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file, or at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qingstor.sdk.request.impl;

import com.qingstor.sdk.constants.QSConstant;
import com.qingstor.sdk.exception.QSException;
import com.qingstor.sdk.request.QSRequestBody;
import com.qingstor.sdk.utils.QSStringUtil;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.http.HttpMethod;

/** @author karooli */
@Slf4j
public class QSNormalRequestBody implements QSRequestBody {

    @Override
    public RequestBody getRequestBody(
            String contentType,
            long contentLength,
            String method,
            Map<String, Object> bodyParams,
            Map<String, Object> queryParams)
            throws QSException {
        log.debug("----QSNormalRequestBody----");
        MediaType mediaType = MediaType.parse(contentType);
        if (bodyParams != null && bodyParams.size() > 0) {

            RequestBody body = null;
            Object bodyObj = getBodyContent(bodyParams);
            if (bodyObj instanceof String) {
                body = RequestBody.create(mediaType, bodyObj.toString());
            } else if (bodyObj instanceof File) {
                body = RequestBody.create(mediaType, (File) bodyObj);
            } else if (bodyObj instanceof InputStream) {
                body = new InputStreamUploadBody(contentType, (InputStream) bodyObj, contentLength);
            }
            return body;
            // connection.getOutputStream().write(bodyContent.getBytes());
        } else {
            if (HttpMethod.permitsRequestBody(method)) {
                return new EmptyRequestBody(contentType);
            }
        }
        return null;
    }

    public static Object getBodyContent(Map bodyContent) throws QSException {
        Iterator iterator = bodyContent.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            Object bodyObj = bodyContent.get(key);
            if (QSConstant.PARAM_TYPE_BODYINPUTFILE.equals(key)
                    || QSConstant.PARAM_TYPE_BODYINPUTSTREAM.equals(key)
                    || QSConstant.PARAM_TYPE_BODYINPUTSTRING.equals(key)) {
                return bodyObj;
            }
        }
        return QSStringUtil.getMapToJson(bodyContent).toString();
    }
}

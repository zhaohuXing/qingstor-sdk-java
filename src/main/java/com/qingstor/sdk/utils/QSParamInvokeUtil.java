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
package com.qingstor.sdk.utils;

import com.qingstor.sdk.annotation.ParamAnnotation;
import com.qingstor.sdk.constants.QSConstant;
import com.qingstor.sdk.exception.QSException;
import com.qingstor.sdk.model.OutputModel;
import com.qingstor.sdk.request.ResponseCallBack;
import java.lang.reflect.*;
import java.util.*;

public class QSParamInvokeUtil {

    public static Map getRequestParams(Object model, String paramType) {
        Map retParametersMap = new HashMap();
        if (model != null) {
            try {
                Class tmpClass = model.getClass();
                while (tmpClass != Object.class) {
                    initParameterMap(tmpClass, model, retParametersMap, paramType);
                    tmpClass = tmpClass.getSuperclass();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (QSConstant.PARAM_TYPE_HEADER.equals(paramType)) {
            if (!retParametersMap.containsKey(QSConstant.HEADER_PARAM_KEY_DATE)) {
                retParametersMap.put(
                        QSConstant.HEADER_PARAM_KEY_DATE,
                        QSSignatureUtil.formatGmtDate(new Date()));
            }
            /*if(!retParametersMap.containsKey(SDKConstant.HEADER_PARAM_KEY_CONTENTTYPE)){
                retParametersMap.put(SDKConstant.HEADER_PARAM_KEY_CONTENTTYPE, SDKConstant.CONTENT_TYPE_TEXT);
            }*/
        }
        return retParametersMap;
    }

    @SuppressWarnings("PARAMETER")
    private static void initParameterMap(
            Class objClass, Object source, Map retParametersMap, String paramType)
            throws QSException {
        Field[] declaredField = objClass.getDeclaredFields();
        for (Field field : declaredField) {
            String methodName = "get" + QSStringUtil.capitalize(field.getName());
            String fieldName = field.getName();
            Method[] methods = objClass.getDeclaredMethods();
            for (Method m : methods) {
                if (m.getName().equalsIgnoreCase(methodName)) {
                    ParamAnnotation annotation = m.getAnnotation(ParamAnnotation.class);
                    if (annotation == null) {
                        continue;
                    }
                    if (!"".equals(annotation.paramName())) {
                        fieldName = annotation.paramName();
                    }
                    if (paramType.equals(annotation.paramType())) {
                        setParameterToMap(m, source, retParametersMap, fieldName);
                    } else if (paramType.equals(annotation.paramType())) {
                        setParameterToMap(m, source, retParametersMap, fieldName);
                    } else if (paramType.equals(annotation.paramType())) {
                        setParameterToMap(m, source, retParametersMap, fieldName);
                    } else if ("".equals(paramType)) {
                        setParameterToMap(m, source, retParametersMap, fieldName);
                    }
                }
            }
        }
    }

    private static void setParameterToMap(
            Method m, Object source, Map targetParametersMap, String paramKey) throws QSException {
        Object[] invokeParams = null;
        try {
            Object objValue = m.invoke(source, invokeParams);
            if (objValue != null) {
                targetParametersMap.put(paramKey, objValue);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new QSException("IllegalAccessException", e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new QSException("InvocationTargetException", e);
        }
    }

    @SuppressWarnings("PARAMETER")
    public static void invokeObject2Map(Class sourceClass, Object source, Map targetParametersMap)
            throws QSException {
        Field[] declaredField = sourceClass.getDeclaredFields();
        for (Field field : declaredField) {
            String methodName = "get" + QSStringUtil.capitalize(field.getName());
            String fieldName = field.getName();
            Method[] methods = sourceClass.getDeclaredMethods();
            for (Method m : methods) {
                if (m.getName().equalsIgnoreCase(methodName)) {
                    ParamAnnotation annotation = m.getAnnotation(ParamAnnotation.class);
                    if (annotation != null && !QSStringUtil.isEmpty(annotation.paramName())) {
                        fieldName = annotation.paramName();
                    }
                    setParameterToMap(m, source, targetParametersMap, fieldName);
                }
            }
        }
    }

    public static Object getOutputModel(Class className) throws QSException {
        try {
            return className.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new QSException(e.getMessage());
        }
    }

    public static Map serializeParams(Map parameters) {
        Map result = new HashMap();
        for (Object o : parameters.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String key = (String) entry.getKey();
            Object value = (Object) entry.getValue();
            if (value instanceof List) {
                for (int i = 0, cnt = ((List) value).size(); i < cnt; i++) {
                    Object v2 = ((List) value).get(i);
                    if (v2 instanceof Map) {
                        for (Object key2 : ((Map) v2).keySet()) {
                            result.put(key + "." + (i + 1) + "." + key2, ((Map) v2).get(key2));
                        }
                    } else {
                        result.put(key + "." + (i + 1), v2);
                    }
                }
            } else if (value instanceof Integer
                    || value instanceof Long
                    || value instanceof Float
                    || value instanceof Double
                    || value instanceof Boolean) {
                result.put(key, String.valueOf(value));
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    public static OutputModel getOutputModel(ResponseCallBack o) throws QSException {
        Type[] typeClass = o.getClass().getGenericInterfaces();

        try {
            if (typeClass[0] instanceof ParameterizedType) {
                Class actualType =
                        (Class) ((ParameterizedType) typeClass[0]).getActualTypeArguments()[0];

                return (OutputModel) actualType.newInstance();
            } else {
                return OutputModel.class.newInstance();
            }
        } catch (InstantiationException e) {
            throw new QSException("InstantiationException", e);
        } catch (IllegalAccessException e) {
            throw new QSException("IllegalAccessException", e);
        }
    }

    public static String metadataIsValid(Map<String, String> metadata) {
        final String metaPrefix = "x-qs-meta-";
        boolean valid = true;
        String errK = "", errV = "";
        int kLen = 0;
        int vLen = 0;

        for (Map.Entry<String, String> e : metadata.entrySet()) {
            String k = e.getKey().toLowerCase();
            String v = e.getValue();
            if (!k.startsWith(metaPrefix)) {
                errK = k;
                errV = v;
                valid = false;
            }
            kLen += k.length() - 9; // not include `metaPrefix`
            vLen += v.length();
            for (char ch : k.toCharArray()) { // check header field name
                if (!(ch >= 65 && ch <= 90
                        || ch >= 97 && ch <= 122
                        || ch <= 57 && ch >= 48
                        || ch == 45
                        || ch == 46)) {
                    errK = k;
                    errV = v;
                    valid = false;
                    break;
                }
            }
            // is Ascii Printable
            for (char ch : v.toCharArray()) {
                if (!(ch >= 32 && ch < 127)) {
                    errK = k;
                    errV = v;
                    valid = false;
                    break;
                }
            }
            if (kLen > 512 || vLen > 2048) {
                errK = k;
                errV = v;
                valid = false;
            }
        }
        if (!valid) {
            return QSStringUtil.getParameterValueNotAllowedError(
                    errK, errV, new String[] {"x-qs-meta-*"});
        }
        return null;
    }
}

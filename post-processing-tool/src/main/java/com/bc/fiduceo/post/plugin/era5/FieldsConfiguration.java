package com.bc.fiduceo.post.plugin.era5;

import org.esa.snap.core.util.StringUtils;

class FieldsConfiguration {

    protected String expand(String variableName, String key, String replacement) {
        if (StringUtils.isNullOrEmpty(replacement)) {
            return variableName;
        } else {
            final int idx = variableName.indexOf(key);
            if (idx < 0) {
                return variableName;
            }

            final StringBuilder stringBuilder = new StringBuilder();
            if (idx > 0) {
                stringBuilder.append(variableName, 0, idx);
            }
            stringBuilder.append(replacement);
            stringBuilder.append(variableName.substring(idx + key.length()));
            return stringBuilder.toString();
        }
    }
}

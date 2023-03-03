package com.bc.fiduceo.util;

import java.util.regex.Pattern;

public class MMDUtil {

    public static Pattern getMMDFileNamePattern() {
        return Pattern.compile("\\w*\\d{1,2}.*_.*_.*_\\d{4}-\\d{3}_\\d{4}-\\d{3}.nc");
    }
}

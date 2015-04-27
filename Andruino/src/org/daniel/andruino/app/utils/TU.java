package org.daniel.andruino.app.utils;

import android.util.Log;

/**
 * Created by jiaoyang on 4/26/15.
 */
public class TU {
    public static void j(Object... objects) {
        if (objects == null || objects.length == 0) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (Object obj : objects) {
            builder.append(obj.toString());
            builder.append(", ");
        }
        Log.i("jy", builder.toString());
    }
}

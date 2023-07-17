package org.calyxos.ripple;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Set;

import info.guardianproject.panic.PanicTrigger;

public class RippleContentProvider extends ContentProvider {

    static final String GET_ENABLED_RESPONDERS = "GET_ENABLED_RESPONDERS";
    static final String ENABLED_RESPONDERS = "ENABLED_RESPONDERS";

    private final String TAG = RippleContentProvider.class.getSimpleName();
    private Set<String> enabledResponders;

    @Override
    public boolean onCreate() {
        if (getContext() != null) {
            enabledResponders = PanicTrigger.getEnabledResponders(getContext());
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        Bundle bundle = new Bundle();
        Log.d(TAG, "Method: " + method);
        if (method.equals(GET_ENABLED_RESPONDERS)) {
            bundle.putStringArray(ENABLED_RESPONDERS, enabledResponders.toArray(new String[0]));
        }
        return bundle;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}

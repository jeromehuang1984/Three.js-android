package edu.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public final class RawShaderLoader {

    @SuppressLint("UseSparseArrays")
    private static final HashMap<Integer, String> mRawMaterials = new HashMap<Integer, String>();

    // Prevent memory leaks as referencing the context can be dangerous.
    public static WeakReference<Context> mContext;

    /**
     * Read a material from the raw resources folder. Subsequent calls will return from memory.
     *
     * @param resID
     * @return
     */
    public static final String fetch(final int resID) {
        if (mRawMaterials.containsKey(resID))
            return mRawMaterials.get(resID);

        final StringBuilder sb = new StringBuilder();

        try {
            final Resources res = mContext.get().getResources();
            final InputStreamReader isr = new InputStreamReader(res.openRawResource(resID));
            final BufferedReader br = new BufferedReader(isr);

            String line;
            while ((line = br.readLine()) != null)
                sb.append(line).append("\n");

            mRawMaterials.put(resID, sb.toString());

            isr.close();
            br.close();
        } catch (Exception e) {
            Log.e("RawShaderLoader", "Failed to read material: " + e.getMessage());
            e.printStackTrace();
        }

        return mRawMaterials.get(resID);
    }
}


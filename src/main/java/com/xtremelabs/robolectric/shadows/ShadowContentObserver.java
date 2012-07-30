/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xtremelabs.robolectric.shadows;

import android.database.ContentObserver;
import android.os.Handler;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

/**
 * Receives call backs for changes to content. Must be implemented by objects which are added
 * to a {@link android.database.ContentObservable}.
 */
@Implements(ContentObserver.class)
public class ShadowContentObserver {

    @RealObject
    private ContentObserver realObject;

    // Protects mTransport
    private Object lock = new Object();

    /* package */ Handler mHandler;

    private final class NotificationRunnable implements Runnable {

        private boolean mSelf;

        public NotificationRunnable(boolean self) {
            mSelf = self;
        }

        public void run() {
            ShadowContentObserver.this.realObject.onChange(mSelf);
        }
    }

    /**
     * onChange() will happen on the provider Handler.
     *
     * @param handler The handler to run {@link #onChange} on.
     */
    public void __constructor__(Handler handler) {
        mHandler = handler;
    }

    /**
     * Returns true if this observer is interested in notifications for changes
     * made through the cursor the observer is registered with.
     */
    @Implementation
    public boolean deliverSelfNotifications() {
        return false;
    }

    /**
     * This method is called when a change occurs to the cursor that
     * is being observed.
     *
     * @param selfChange true if the update was caused by a call to <code>commit</code> on the
     *  cursor that is being observed.
     */
    @Implementation
    public void onChange(boolean selfChange) {}

    @Implementation
    public final void dispatchChange(boolean selfChange) {
        if (mHandler == null) {
            realObject.onChange(selfChange);
        } else {
            mHandler.post(new NotificationRunnable(selfChange));
        }
    }
}

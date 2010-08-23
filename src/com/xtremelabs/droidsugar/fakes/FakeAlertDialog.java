package com.xtremelabs.droidsugar.fakes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import com.xtremelabs.droidsugar.ProxyDelegatingHandler;
import com.xtremelabs.droidsugar.util.Implements;

import java.lang.reflect.Constructor;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AlertDialog.class)
public class FakeAlertDialog extends FakeDialog {
    public static FakeAlertDialog latestAlertDialog;

    public CharSequence[] items;
    public String title;
    public String message;
    private DialogInterface.OnClickListener clickListener;
    private AlertDialog realDialog;
    private boolean isMultiItem;
    private DialogInterface.OnMultiChoiceClickListener multiChoiceClickListener;
    private boolean[] checkedItems;

    public FakeAlertDialog(AlertDialog dialog) {
        super(dialog);
    }

    @Override
    public View findViewById(int viewId) {
        return null;
    }

    public void clickOnItem(int index) {
        if (isMultiItem) {
            checkedItems[index] = !checkedItems[index];
            multiChoiceClickListener.onClick(realDialog, index, checkedItems[index]);
        } else {
            clickListener.onClick(realDialog, index);
        }
    }

    @Implements(AlertDialog.Builder.class)
    public static class FakeBuilder {
        private CharSequence[] items;
        private DialogInterface.OnClickListener clickListener;
        private String title;
        private String message;
        private AlertDialog.Builder realBuilder;
        private Context context;
        private boolean isMultiItem;
        private DialogInterface.OnMultiChoiceClickListener multiChoiceClickListener;
        private boolean[] checkedItems;

        public FakeBuilder(AlertDialog.Builder realBuilder) {
            this.realBuilder = realBuilder;
        }

        public void __constructor__(Context context) {
            this.context = context;
        }

        public AlertDialog.Builder setItems(CharSequence[] items, final DialogInterface.OnClickListener listener) {
            this.isMultiItem = false;

            this.items = items;
            this.clickListener = listener;
            return realBuilder;
        }

        public AlertDialog.Builder setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems,
                final DialogInterface.OnMultiChoiceClickListener listener) {
            this.isMultiItem = true;

            this.items = items;
            this.multiChoiceClickListener = listener;

            if (checkedItems == null) {
                checkedItems = new boolean[items.length];
            } else if (checkedItems.length != items.length) {
                throw new IllegalArgumentException("checkedItems must be the same length as items, or pass null to specify checked items");
            }
            this.checkedItems = checkedItems;

            return realBuilder;
        }

        public AlertDialog.Builder setTitle(CharSequence title) {
            this.title = title.toString();
            return realBuilder;
        }

        public AlertDialog.Builder setTitle(int titleId) {
            this.title = context.getResources().getString(titleId);
            return realBuilder;
        }

        public AlertDialog.Builder setMessage(CharSequence message) {
            this.message = message.toString();
            return realBuilder;
        }

        public AlertDialog create() {
            AlertDialog realDialog;
            try {
                Constructor<AlertDialog> c = AlertDialog.class.getDeclaredConstructor(Context.class);
                c.setAccessible(true);
                realDialog = c.newInstance((Context) null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            FakeAlertDialog latestAlertDialog = proxyFor(realDialog);
            latestAlertDialog.context = context;
            latestAlertDialog.realDialog = realDialog;
            latestAlertDialog.items = items;
            latestAlertDialog.title = title;
            latestAlertDialog.message = message;
            latestAlertDialog.clickListener = clickListener;
            latestAlertDialog.isMultiItem = isMultiItem;
            latestAlertDialog.multiChoiceClickListener = multiChoiceClickListener;
            latestAlertDialog.checkedItems = checkedItems;

            FakeAlertDialog.latestAlertDialog = latestAlertDialog;

            return realDialog;
        }
    }

    private static FakeAlertDialog proxyFor(AlertDialog realDialog) {
        return (FakeAlertDialog) ProxyDelegatingHandler.getInstance().proxyFor(realDialog);
    }
}

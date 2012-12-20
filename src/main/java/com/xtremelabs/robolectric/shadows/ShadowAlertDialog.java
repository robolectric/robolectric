package com.xtremelabs.robolectric.shadows;

import android.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.lang.reflect.Constructor;

import static com.xtremelabs.robolectric.Robolectric.getShadowApplication;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AlertDialog.class)
public class ShadowAlertDialog extends ShadowDialog {
    @RealObject
    private AlertDialog realAlertDialog;

    private CharSequence[] items;
    private String message;
    private DialogInterface.OnClickListener clickListener;
    private boolean isMultiItem;
    private boolean isSingleItem;
    private DialogInterface.OnMultiChoiceClickListener multiChoiceClickListener;
    private boolean[] checkedItems;
    private int checkedItemIndex;
    private Button positiveButton;
    private Button negativeButton;
    private Button neutralButton;
    private View view;
    private View customTitleView;
    private ListAdapter adapter;
    private ListView listView;

    /**
     * Non-Android accessor.
     *
     * @return the most recently created {@code AlertDialog}, or null if none has been created during this test run
     */
    public static AlertDialog getLatestAlertDialog() {
        ShadowAlertDialog dialog = Robolectric.getShadowApplication().getLatestAlertDialog();
        return dialog == null ? null : dialog.realAlertDialog;
    }

    @Override
    @Implementation
    public View findViewById(int viewId) {
        if(view == null) {
            return super.findViewById(viewId);
        }

        return view.findViewById(viewId);
    }

    @Implementation
    public void setView(View view) {
        this.view = view;
    }

    /**
     * Resets the tracking of the most recently created {@code AlertDialog}
     */
    public static void reset() {
        getShadowApplication().setLatestAlertDialog(null);
    }

    /**
     * Simulates a click on the {@code Dialog} item indicated by {@code index}. Handles both multi- and single-choice dialogs, tracks which items are currently
     * checked and calls listeners appropriately.
     *
     * @param index the index of the item to click on
     */
    public void clickOnItem(int index) {
        shadowOf(realAlertDialog.getListView()).performItemClick(index);
    }

    @Implementation
    public Button getButton(int whichButton) {
        switch (whichButton) {
            case AlertDialog.BUTTON_POSITIVE:
                return positiveButton;
            case AlertDialog.BUTTON_NEGATIVE:
                return negativeButton;
            case AlertDialog.BUTTON_NEUTRAL:
                return neutralButton;
        }
        throw new RuntimeException("Only positive, negative, or neutral button choices are recognized");
    }
    
    @Implementation
    public void setButton(int whichButton, CharSequence text, DialogInterface.OnClickListener listener) {
        switch (whichButton) {
            case AlertDialog.BUTTON_POSITIVE:
                positiveButton = createButton(context, realAlertDialog, whichButton, text, listener);
                return;
            case AlertDialog.BUTTON_NEGATIVE:
                negativeButton = createButton(context, realAlertDialog, whichButton, text, listener);
                return;
            case AlertDialog.BUTTON_NEUTRAL:
                neutralButton = createButton(context, realAlertDialog, whichButton, text, listener);
                return;
        }
        throw new RuntimeException("Only positive, negative, or neutral button choices are recognized");
    }

    private static Button createButton(final Context context, final DialogInterface dialog, final int which, CharSequence text, final DialogInterface.OnClickListener listener) {
        if (text == null && listener == null) {
            return null;
        }
        Button button = new Button(context);
        Robolectric.shadowOf(button).setText(text); // use shadow to skip
                                                    // i18n-strict checking
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(dialog, which);
                }
                dialog.dismiss();
            }
        });
        return button;
    }

    @Implementation
    public ListView getListView() {
        if (listView == null) {
            listView = new ListView(context);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (isMultiItem) {
                        checkedItems[position] = !checkedItems[position];
                        multiChoiceClickListener.onClick(realAlertDialog, position, checkedItems[position]);
                    } else {
                        if (isSingleItem) {
                            checkedItemIndex = position;
                        }
                        clickListener.onClick(realAlertDialog, position);
                    }
                }
            });
        }
        return listView;
    }

    /**
     * Non-Android accessor.
     *
     * @return the items that are available to be clicked on
     */
    public CharSequence[] getItems() {
        return items;
    }

    public Adapter getAdapter() {
        return adapter;
    }

    /**
     * Non-Android accessor.
     *
     * @return the message displayed in the dialog
     */
    public String getMessage() {
        return message;
    }

    @Implementation
    public void setMessage(CharSequence message) {
        this.message = (message == null ? null : message.toString());
    }

    /**
     * Non-Android accessor.
     *
     * @return an array indicating which items are and are not clicked on a multi-choice dialog
     */
    public boolean[] getCheckedItems() {
        return checkedItems;
    }

    /**
     * Non-Android accessor.
     *
     * @return return the index of the checked item clicked on a single-choice dialog
     */
    public int getCheckedItemIndex() {
        return checkedItemIndex;
    }

    @Implementation
    public void show() {
        super.show();
        if (items != null) {
            adapter = new ArrayAdapter<CharSequence>(context, R.layout.simple_list_item_checked, R.id.text1, items);
        }

        if (adapter != null) {
            getListView().setAdapter(adapter);
        }


        getShadowApplication().setLatestAlertDialog(this);
    }

    /**
     * Non-Android accessor.
     *
     * @return return the view set with {@link ShadowAlertDialog.ShadowBuilder#setView(View)}
     */
    public View getView() {
        return view;
    }

    /**
     * Non-Android accessor.
     *
     * @return return the view set with {@link ShadowAlertDialog.ShadowBuilder#setCustomTitle(View)}
     */
    public View getCustomTitleView() {
        return customTitleView;
    }

    /**
     * Shadows the {@code android.app.AlertDialog.Builder} class.
     */
    @Implements(AlertDialog.Builder.class)
    public static class ShadowBuilder {
        @RealObject
        private AlertDialog.Builder realBuilder;

        private CharSequence[] items;
        private ListAdapter adapter;
        private DialogInterface.OnClickListener clickListener;
        private DialogInterface.OnCancelListener cancelListener;
        private String title;
        private String message;
        private Context context;
        private boolean isMultiItem;
        private DialogInterface.OnMultiChoiceClickListener multiChoiceClickListener;
        private boolean[] checkedItems;
        private CharSequence positiveText;
        private DialogInterface.OnClickListener positiveListener;
        private CharSequence negativeText;
        private DialogInterface.OnClickListener negativeListener;
        private CharSequence neutralText;
        private DialogInterface.OnClickListener neutralListener;
        private boolean isCancelable;
        private boolean isSingleItem;
        private int checkedItem;
        private View view;
        private View customTitleView;

        /**
         * just stashes the context for later use
         *
         * @param context the context
         */
        public void __constructor__(Context context) {
            this.context = context;
        }

        /**
         * Set a list of items to be displayed in the dialog as the content, you will be notified of the selected item via the supplied listener. This should be
         * an array type i.e. R.array.foo
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        @Implementation
        public AlertDialog.Builder setItems(int itemsId, final DialogInterface.OnClickListener listener) {
            this.isMultiItem = false;

            this.items = context.getResources().getTextArray(itemsId);
            this.clickListener = listener;
            return realBuilder;
        }

        @Implementation(i18nSafe=false)
        public AlertDialog.Builder setItems(CharSequence[] items, final DialogInterface.OnClickListener listener) {
            this.isMultiItem = false;

            this.items = items;
            this.clickListener = listener;
            return realBuilder;
        }

        @Implementation(i18nSafe=false)
        public AlertDialog.Builder setSingleChoiceItems(CharSequence[] items, int checkedItem, final DialogInterface.OnClickListener listener) {
            this.isSingleItem = true;
            this.checkedItem = checkedItem;
            this.items = items;
            this.clickListener = listener;
            return realBuilder;
        }

        @Implementation(i18nSafe=false)
        public AlertDialog.Builder setSingleChoiceItems(ListAdapter adapter, int checkedItem, final DialogInterface.OnClickListener listener) {
            this.isSingleItem = true;
            this.checkedItem = checkedItem;
            this.items = null;
            this.adapter = adapter;
            this.clickListener = listener;
            return realBuilder;
        }

        @Implementation(i18nSafe=false)
        public AlertDialog.Builder setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems, final DialogInterface.OnMultiChoiceClickListener listener) {
            this.isMultiItem = true;

            this.items = items;
            this.multiChoiceClickListener = listener;

            if (checkedItems == null) {
                checkedItems = new boolean[items.length];
            } else if (checkedItems.length != items.length) {
                throw new IllegalArgumentException("checkedItems must be the same length as items, or pass null to specify no checked items");
            }
            this.checkedItems = checkedItems;

            return realBuilder;
        }

        @Implementation(i18nSafe=false)
        public AlertDialog.Builder setTitle(CharSequence title) {
            this.title = title.toString();
            return realBuilder;
        }


        @Implementation
        public AlertDialog.Builder setCustomTitle(android.view.View customTitleView) {
            this.customTitleView = customTitleView;
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setTitle(int titleId) {
            return setTitle(context.getResources().getString(titleId));
        }

        @Implementation(i18nSafe=false)
        public AlertDialog.Builder setMessage(CharSequence message) {
            this.message = message.toString();
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setMessage(int messageId) {
            setMessage(context.getResources().getString(messageId));
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setIcon(int iconId) {
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setView(View view) {
            this.view = view;
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setAdapter (ListAdapter adapter, DialogInterface.OnClickListener listener){
        	this.adapter = adapter;
        	this.clickListener = listener;
        	return realBuilder;
        }
        
        @Implementation(i18nSafe=false)
        public AlertDialog.Builder setPositiveButton(CharSequence text, final DialogInterface.OnClickListener listener) {
            this.positiveText = text;
            this.positiveListener = listener;
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setPositiveButton(int positiveTextId, final DialogInterface.OnClickListener listener) {
            return setPositiveButton(context.getResources().getText(positiveTextId), listener);
        }

        @Implementation(i18nSafe=false)
        public AlertDialog.Builder setNegativeButton(CharSequence text, final DialogInterface.OnClickListener listener) {
            this.negativeText = text;
            this.negativeListener = listener;
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setNegativeButton(int negativeTextId, final DialogInterface.OnClickListener listener) {
            return setNegativeButton(context.getResources().getString(negativeTextId), listener);
        }

        @Implementation(i18nSafe=false)
        public AlertDialog.Builder setNeutralButton(CharSequence text, final DialogInterface.OnClickListener listener) {
            this.neutralText = text;
            this.neutralListener = listener;
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setNeutralButton(int neutralTextId, final DialogInterface.OnClickListener listener) {
            return setNeutralButton(context.getResources().getText(neutralTextId), listener);
        }


        @Implementation
        public AlertDialog.Builder setCancelable(boolean cancelable) {
            this.isCancelable = cancelable;
            return realBuilder;
        }

        @Implementation
        public AlertDialog.Builder setOnCancelListener(DialogInterface.OnCancelListener listener) {
            this.cancelListener = listener;
            return realBuilder;
        }

        @Implementation
        public AlertDialog create() {
            AlertDialog realDialog;
            try {
                Constructor<AlertDialog> c = AlertDialog.class.getDeclaredConstructor(Context.class);
                c.setAccessible(true);
                realDialog = c.newInstance((Context) null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            ShadowAlertDialog latestAlertDialog = shadowOf(realDialog);
            latestAlertDialog.context = context;
            latestAlertDialog.items = items;
            latestAlertDialog.adapter = adapter;
            latestAlertDialog.setTitle(title);
            latestAlertDialog.message = message;
            latestAlertDialog.clickListener = clickListener;
            latestAlertDialog.setOnCancelListener(cancelListener);
            latestAlertDialog.isMultiItem = isMultiItem;
            latestAlertDialog.isSingleItem = isSingleItem;
            latestAlertDialog.checkedItemIndex = checkedItem;
            latestAlertDialog.multiChoiceClickListener = multiChoiceClickListener;
            latestAlertDialog.checkedItems = checkedItems;
            latestAlertDialog.setView(view);
            latestAlertDialog.positiveButton = createButton(context, realDialog, AlertDialog.BUTTON_POSITIVE, positiveText, positiveListener);
            latestAlertDialog.negativeButton = createButton(context, realDialog, AlertDialog.BUTTON_NEGATIVE, negativeText, negativeListener);
            latestAlertDialog.neutralButton = createButton(context, realDialog, AlertDialog.BUTTON_NEUTRAL, neutralText, neutralListener);
            latestAlertDialog.setCancelable(isCancelable);
            latestAlertDialog.customTitleView = customTitleView;
            return realDialog;
        }

        @Implementation
        public AlertDialog show() {
            AlertDialog dialog = realBuilder.create();
            dialog.show();
            return dialog;
        }

        @Implementation
        public Context getContext() {
            return context;
        }
    }
}

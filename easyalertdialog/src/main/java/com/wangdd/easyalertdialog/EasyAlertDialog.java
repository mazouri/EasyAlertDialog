package com.wangdd.easyalertdialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by wangdongdong on 16-1-6.
 */
public class EasyAlertDialog extends Dialog implements DialogInterface{

    private CharSequence mMessage;
    private CharSequence mTitle;
    private int mMessageGravity;
    private View mView;
    private int mListLayout;
    private int mCheckedItem = -1;
    private int mSingleChoiceItemLayout;
    private int mMultiChoiceItemLayout;
    private int mListItemLayout;
    private ListAdapter mAdapter;
    private ListView mListView;
    private ScrollView mScrollView;
    private LinearLayout mContentPanel;

    private Button mButtonPositive;
    private CharSequence mButtonPositiveText;
    private Message mButtonPositiveMessage;

    private Button mButtonNegative;
    private CharSequence mButtonNegativeText;
    private Message mButtonNegativeMessage;

    private Button mButtonNeutral;
    private CharSequence mButtonNeutralText;
    private Message mButtonNeutralMessage;

    private TextView mTitleView;
    private TextView mMessageView;
    private View mCustomPanel;
    private View mCustomView;
//    private View mButtonContainer1;
//    private View mButtonContainer2;

    private Handler mHandler;

    private EasyDialogDismissListener mEasyDialogDismissListener;

    private final View.OnClickListener mButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Message m;
            if (v == mButtonPositive && mButtonPositiveMessage != null) {
                m = Message.obtain(mButtonPositiveMessage);
            } else if (v == mButtonNegative && mButtonNegativeMessage != null) {
                m = Message.obtain(mButtonNegativeMessage);
            } else if (v == mButtonNeutral && mButtonNeutralMessage != null) {
                m = Message.obtain(mButtonNeutralMessage);
            } else {
                m = null;
            }

            if (m != null) {
                m.sendToTarget();
            }

            // Post a message so we dismiss after the above handlers are executed
            mHandler.obtainMessage(ButtonHandler.MSG_DISMISS_DIALOG, EasyAlertDialog.this).sendToTarget();
        }
    };

    private static final class ButtonHandler extends Handler {
        // Button clicks have Message.what as the BUTTON{1,2,3} constant
        private static final int MSG_DISMISS_DIALOG = 1;

        private WeakReference<DialogInterface> mDialog;

        public ButtonHandler(DialogInterface dialog) {
            mDialog = new WeakReference<>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case DialogInterface.BUTTON_POSITIVE:
                case DialogInterface.BUTTON_NEGATIVE:
                case DialogInterface.BUTTON_NEUTRAL:
                    ((OnClickListener) msg.obj).onClick(mDialog.get(), msg.what);
                    break;

                case MSG_DISMISS_DIALOG:
                    ((DialogInterface) msg.obj).dismiss();
            }
        }
    }

    public EasyAlertDialog(Context context) {
        super(context, R.style.Theme_Easy_Light_Dialog);
        mHandler = new ButtonHandler(this);
        mListLayout = R.layout.select_dialog;
        mListItemLayout = R.layout.select_dialog_item;
        mSingleChoiceItemLayout = R.layout.select_dialog_singlechoice;
        mMultiChoiceItemLayout = R.layout.select_dialog_multichoice;
    }

    /**
     * Constructor
     * @param context
     * @param dialogTheme
     */
    public EasyAlertDialog(Context context, int dialogTheme) {
        super(context, dialogTheme);
        mHandler = new ButtonHandler(this);
        mListLayout = R.layout.select_dialog;
        mListItemLayout = R.layout.select_dialog_item;
        mSingleChoiceItemLayout = R.layout.select_dialog_singlechoice;
        mMultiChoiceItemLayout = R.layout.select_dialog_multichoice;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (mCustomView == null || !canTextInput(mCustomView)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }
        super.onCreate(savedInstanceState);

        if(getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mView = LayoutInflater.from(getContext()).inflate(R.layout.easy_alert_dialog, null);
        } else if(getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mView = LayoutInflater.from(getContext()).inflate(R.layout.easy_alert_dialog, null);
        }
        setContentView(mView);
        bindViews(mView);
        setupContent();
        setupButtons();
    }

    private void bindViews(View view) {
        mTitleView = (TextView) view.findViewById(R.id.alertTitle);
        mMessageView = (TextView) view.findViewById(android.R.id.message);
        mCustomPanel = view.findViewById(R.id.customPanel);
        mButtonNegative = (Button) view.findViewById(R.id.button1);
        mButtonPositive = (Button) view.findViewById(R.id.button2);
        mButtonNeutral = (Button) view.findViewById(R.id.button3);
        mScrollView = (ScrollView) view.findViewById(R.id.scrollView);
        mContentPanel = (LinearLayout) view.findViewById(R.id.contentPanel);
    }

    private boolean setupButtons() {
        mButtonNegative.setOnClickListener(mButtonHandler);
        mButtonNegative.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mButtonNegative.setSelected(v.hasFocus());
            }
        });

        if (TextUtils.isEmpty(mButtonNegativeText)) {
            mButtonNegative.setVisibility(View.GONE);
        } else {
            mButtonNegative.setText(mButtonNegativeText);
            mButtonNegative.setVisibility(View.VISIBLE);
            mButtonNegative.requestFocus();
        }

        mButtonPositive.setOnClickListener(mButtonHandler);
        mButtonPositive.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mButtonPositive.setSelected(v.hasFocus());
            }
        });

        if (TextUtils.isEmpty(mButtonPositiveText)) {
            mButtonPositive.setVisibility(View.GONE);
        } else {
            mButtonPositive.setText(mButtonPositiveText);
            mButtonPositive.setVisibility(View.VISIBLE);
            mButtonPositive.requestFocus();
        }

        mButtonNeutral.setOnClickListener(mButtonHandler);
        mButtonNeutral.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mButtonNeutral.setSelected(v.hasFocus());
            }
        });

        if (TextUtils.isEmpty(mButtonNeutralText)) {
            mButtonNeutral.setVisibility(View.GONE);
        } else {
            mButtonNeutral.setText(mButtonNeutralText);
            mButtonNeutral.setVisibility(View.VISIBLE);
            mButtonNeutral.requestFocus();
        }

        return true;
    }

    public static int getSuitableTheme(Context context, int theme) {
        if (theme != 0) {
            return theme;
        }
        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            return R.style./*Theme_Easy_Light_Dialog*/ThemeEasyAlertDialog;
        } else if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return R.style./*Theme_Easy_Light_Dialog_Landscape*/ThemeEasyAlertDialog;
        }
        return R.style.Theme_Easy_Light_Dialog;
    }

    static boolean canTextInput(View v) {
        if (v.onCheckIsTextEditor()) {
            return true;
        }

        if (!(v instanceof ViewGroup)) {
            return false;
        }

        ViewGroup vg = (ViewGroup)v;
        int i = vg.getChildCount();
        while (i > 0) {
            i--;
            v = vg.getChildAt(i);
            if (canTextInput(v)) {
                return true;
            }
        }

        return false;
    }

    public void setTitle(CharSequence title) {
        mTitle = title;
    }

    public void setMessage(CharSequence message) {
        mMessage = message;
    }

    public void setMessageGravity(int gravity) {
        mMessageGravity = gravity;
    }

    public void setView(View customView) {
        mCustomView = customView;
    }

    private void setupContent() {
        final boolean hasTextTitle = !TextUtils.isEmpty(mTitle);
        if (hasTextTitle) {
            if (mTitleView != null) {
                mTitleView.setText(mTitle);
            }
        } else {
            View titleTemplate = mView.findViewById(R.id.topPanel);
            titleTemplate.setVisibility(View.GONE);
        }
        if (mMessage != null) {
            mMessageView.setText(mMessage);
        } else {
            mMessageView.setVisibility(View.GONE);
            mScrollView.removeView(mMessageView);

            if (mListView != null) {
                mContentPanel.removeView(mView.findViewById(R.id.scrollView));
                mContentPanel.addView(mListView,
                        new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
                mContentPanel.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, 0, 1.0f));
            } else {
                mContentPanel.setVisibility(View.GONE);
            }
        }
        if (mMessageGravity != 0) {
            mMessageView.setGravity(mMessageGravity);
        }
        if ((mListView != null) && (mAdapter != null)) {
            mListView.setAdapter(mAdapter);
            if (mCheckedItem > -1) {
                mListView.setItemChecked(mCheckedItem, true);
                mListView.setSelection(mCheckedItem);
            }
        }
        if (mCustomView != null) {
            FrameLayout custom = (FrameLayout) mView.findViewById(R.id.custom);
            custom.addView(mCustomView, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
            if (mListView != null) {
                ((LinearLayout.LayoutParams) mCustomPanel.getLayoutParams()).weight = 0;
            }
        } else {
            mCustomPanel.setVisibility(View.GONE);
        }
    }

    public void setButton(int whichButton, CharSequence text,
                          OnClickListener listener, Message msg) {

        if (msg == null && listener != null) {
            msg = mHandler.obtainMessage(whichButton, listener);
        }

        switch (whichButton) {

            case DialogInterface.BUTTON_POSITIVE:
                mButtonPositiveText = text;
                mButtonPositiveMessage = msg;
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                mButtonNegativeText = text;
                mButtonNegativeMessage = msg;
                break;

            case DialogInterface.BUTTON_NEUTRAL:
                mButtonNeutralText = text;
                mButtonNeutralMessage = msg;
                break;

            default:
                throw new IllegalArgumentException("Button does not exist");
        }
    }

    public Button getButton(int whichButton) {
        switch (whichButton) {
            case DialogInterface.BUTTON_POSITIVE:
                return mButtonPositive;
            case DialogInterface.BUTTON_NEGATIVE:
                return mButtonNegative;
            case DialogInterface.BUTTON_NEUTRAL:
                return mButtonNeutral;
            default:
                return null;
        }
    }

    public ListView getListView() {
        return mListView;
    }

    public void show() {
        final Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();

        if(getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.CENTER;

            onWindowAttributesChanged(lp);
            super.show();

            lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        } else if(getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.CENTER;

            onWindowAttributesChanged(lp);
            super.show();

            lp = window.getAttributes();
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }
        window.setAttributes(lp);
    }

    private static class AlertParams {
        private Context mContext;
        public CharSequence mTitle;
        public CharSequence mMessage;
        public int mMessageGravity;
        public CharSequence mPositiveButtonText;
        public CharSequence mNegativeButtonText;
        public CharSequence mNeutralButtonText;
        public int mCheckedItem = -1;
        public CharSequence[] mItems;
        public boolean[] mCheckedItems;
        public boolean mIsMultiChoice;
        public boolean mIsSingleChoice;
        public Cursor mCursor;
        public ListAdapter mAdapter;
        public String mLabelColumn;
        public String mIsCheckedColumn;
        public final LayoutInflater mInflater;
        public OnClickListener mOnClickListener;
        public OnMultiChoiceClickListener mOnCheckboxClickListener;
        public AdapterView.OnItemSelectedListener mOnItemSelectedListener;
        public OnClickListener mNegativeButtonListener;
        public OnClickListener mPositiveButtonListener;
        public OnClickListener mNeutralButtonListener;

        public OnCancelListener mOnCancelListener;
        public OnDismissListener mOnDismissListener;
        public OnKeyListener mOnKeyListener;

        private Boolean mCancelable;
        public View mCustomView;

        public AlertParams(Context context) {
            mContext = context;
            mCancelable = true;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void apply(EasyAlertDialog dialog) {
            if (mTitle != null) {
                dialog.setTitle(mTitle);
            }
            if (mPositiveButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, mPositiveButtonText,
                        mPositiveButtonListener, null);
            }

            if (mNegativeButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, mNegativeButtonText,
                        mNegativeButtonListener, null);
            }
            if (mNeutralButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_NEUTRAL, mNeutralButtonText,
                        mNeutralButtonListener, null);
            }
            if (mMessage != null) {
                dialog.setMessage(mMessage);
            } else if ((mItems != null) || (mCursor != null) || (mAdapter != null)) {
                createListView(dialog);
            }
            if (mMessageGravity != 0) {
                dialog.setMessageGravity(mMessageGravity);
            }
            if (mCustomView != null) {
                dialog.setView(mCustomView);
            }
        }

        private void createListView(final EasyAlertDialog dialog) {
            final ListView listView = (ListView)
                    mInflater.inflate(dialog.mListLayout, null);
            ListAdapter adapter;

            if (mIsMultiChoice) {
                if (mCursor == null) {
                    adapter = new ArrayAdapter<CharSequence>(
                            mContext, dialog.mMultiChoiceItemLayout, android.R.id.text1, mItems) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            if (mCheckedItems != null) {
                                boolean isItemChecked = mCheckedItems[position];
                                if (isItemChecked) {
                                    listView.setItemChecked(position, true);
                                }
                            }
                            return view;
                        }
                    };
                } else {
                    adapter = new CursorAdapter(mContext, mCursor, false) {
                        private final int mLabelIndex;
                        private final int mIsCheckedIndex;

                        {
                            final Cursor cursor = getCursor();
                            mLabelIndex = cursor.getColumnIndexOrThrow(mLabelColumn);
                            mIsCheckedIndex = cursor.getColumnIndexOrThrow(mIsCheckedColumn);
                        }

                        @Override
                        public void bindView(View view, Context context, Cursor cursor) {
                            CheckedTextView text = (CheckedTextView) view.findViewById(android.R.id.text1);
                            text.setText(cursor.getString(mLabelIndex));
                            listView.setItemChecked(cursor.getPosition(),
                                    cursor.getInt(mIsCheckedIndex) == 1);
                        }

                        @Override
                        public View newView(Context context, Cursor cursor, ViewGroup parent) {
                            return mInflater.inflate(dialog.mMultiChoiceItemLayout,
                                    parent, false);
                        }

                    };
                }
            } else {
                int layout = mIsSingleChoice
                        ? dialog.mSingleChoiceItemLayout : dialog.mListItemLayout;
                if (mCursor == null) {
                    adapter = (mAdapter != null) ? mAdapter
                            : new ArrayAdapter<CharSequence>(mContext, layout, android.R.id.text1, mItems);
                } else {
                    adapter = new SimpleCursorAdapter(mContext, layout,
                            mCursor, new String[]{mLabelColumn}, new int[]{android.R.id.text1},
                            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
                }
            }

            dialog.mAdapter = adapter;
            dialog.mCheckedItem = mCheckedItem;

            if (mOnClickListener != null) {
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView parent, View v, int position, long id) {
                        mOnClickListener.onClick(dialog, position);
                        if (!mIsSingleChoice) {
                            dialog.dismiss();
                        }
                    }
                });
            } else if (mOnCheckboxClickListener != null) {
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView parent, View v, int position, long id) {
                        if (mCheckedItems != null) {
                            mCheckedItems[position] = listView.isItemChecked(position);
                        }
                        mOnCheckboxClickListener.onClick(
                                dialog, position, listView.isItemChecked(position));
                    }
                });
            }

            if (mOnItemSelectedListener != null) {
                listView.setOnItemSelectedListener(mOnItemSelectedListener);
            }

            if (mIsSingleChoice) {
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            } else if (mIsMultiChoice) {
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
            dialog.mListView = listView;
        }

    }

    public static class Builder {
        private final AlertParams P;
        private int mTheme;

        public Builder(Context context) {
            P = new AlertParams(context);
        }

        public Builder(Context context, int theme) {
            P = new AlertParams(context);
            mTheme = theme;
        }

        public Builder setTitle(int titleId) {
            P.mTitle = P.mContext.getText(titleId);;
            return this;
        }

        public Builder setTitle(CharSequence title) {
            P.mTitle = title;
            return this;
        }

        public Builder setMessage(int messageId) {
            P.mMessage = P.mContext.getText(messageId);
            return this;
        }

        public Builder setMessage(CharSequence message) {
            P.mMessage = message;
            return this;
        }

        public Builder setMessageGravity(int gravity) {
            P.mMessageGravity = gravity;
            return this;
        }

        public Builder setView(View view) {
            P.mCustomView = view;
            return this;
        }

        public Builder setPositiveButton(int textId, final OnClickListener listener) {
            P.mPositiveButtonText = P.mContext.getText(textId);
            P.mPositiveButtonListener = listener;
            return this;
        }

        public Builder setPositiveButton(CharSequence text, final OnClickListener listener) {
            P.mPositiveButtonText = text;
            P.mPositiveButtonListener = listener;
            return this;
        }


        public Builder setNegativeButton(int textId, final OnClickListener listener) {
            P.mNegativeButtonText = P.mContext.getText(textId);
            P.mNegativeButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(CharSequence text, final OnClickListener listener) {
            P.mNegativeButtonText = text;
            P.mNegativeButtonListener = listener;
            return this;
        }

        public Builder setNeutralButton(int textId, final OnClickListener listener) {
            P.mNeutralButtonText = P.mContext.getText(textId);
            P.mNeutralButtonListener = listener;
            return this;
        }

        public Builder setNeutralButton(CharSequence text, final OnClickListener listener) {
            P.mNeutralButtonText = text;
            P.mNeutralButtonListener = listener;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            P.mCancelable = cancelable;
            return this;
        }

        public Builder setOnCancelListener(OnCancelListener onCancelListener) {
            P.mOnCancelListener = onCancelListener;
            return this;
        }

        public Builder setOnDismissListener(OnDismissListener onDismissListener) {
            P.mOnDismissListener = onDismissListener;
            return this;
        }

        public Builder setOnKeyListener(OnKeyListener onKeyListener) {
            P.mOnKeyListener = onKeyListener;
            return this;
        }

        public Builder setItems(int itemsId, final OnClickListener listener) {
            P.mItems = P.mContext.getResources().getTextArray(itemsId);
            P.mOnClickListener = listener;
            return this;
        }

        public Builder setItems(CharSequence[] items, final OnClickListener listener) {
            P.mItems = items;
            P.mOnClickListener = listener;
            return this;
        }

        public Builder setAdapter(final ListAdapter adapter, final OnClickListener listener) {
            P.mAdapter = adapter;
            P.mOnClickListener = listener;
            return this;
        }

        public Builder setCursor(final Cursor cursor, final OnClickListener listener,
                                 String labelColumn) {
            P.mCursor = cursor;
            P.mLabelColumn = labelColumn;
            P.mOnClickListener = listener;
            return this;
        }

        public Builder setMultiChoiceItems(int itemsId, boolean[] checkedItems,
                                           final OnMultiChoiceClickListener listener) {
            P.mItems = P.mContext.getResources().getTextArray(itemsId);
            P.mOnCheckboxClickListener = listener;
            P.mCheckedItems = checkedItems;
            P.mIsMultiChoice = true;
            return this;
        }

        public Builder setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems,
                                           final OnMultiChoiceClickListener listener) {
            P.mItems = items;
            P.mOnCheckboxClickListener = listener;
            P.mCheckedItems = checkedItems;
            P.mIsMultiChoice = true;
            return this;
        }

        public Builder setMultiChoiceItems(Cursor cursor, String isCheckedColumn, String labelColumn,
                                           final OnMultiChoiceClickListener listener) {
            P.mCursor = cursor;
            P.mOnCheckboxClickListener = listener;
            P.mIsCheckedColumn = isCheckedColumn;
            P.mLabelColumn = labelColumn;
            P.mIsMultiChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(int itemsId, int checkedItem,
                                            final OnClickListener listener) {
            P.mItems = P.mContext.getResources().getTextArray(itemsId);
            P.mOnClickListener = listener;
            P.mCheckedItem = checkedItem;
            P.mIsSingleChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(Cursor cursor, int checkedItem, String labelColumn,
                                            final OnClickListener listener) {
            P.mCursor = cursor;
            P.mOnClickListener = listener;
            P.mCheckedItem = checkedItem;
            P.mLabelColumn = labelColumn;
            P.mIsSingleChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(CharSequence[] items, int checkedItem, final OnClickListener listener) {
            P.mItems = items;
            P.mOnClickListener = listener;
            P.mCheckedItem = checkedItem;
            P.mIsSingleChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(ListAdapter adapter, int checkedItem, final OnClickListener listener) {
            P.mAdapter = adapter;
            P.mOnClickListener = listener;
            P.mCheckedItem = checkedItem;
            P.mIsSingleChoice = true;
            return this;
        }

        public Builder setOnItemSelectedListener(final AdapterView.OnItemSelectedListener listener) {
            P.mOnItemSelectedListener = listener;
            return this;
        }

        public EasyAlertDialog create() {
            final EasyAlertDialog dialog = new EasyAlertDialog(P.mContext, getSuitableTheme(P.mContext, mTheme));
            P.apply(dialog);
            dialog.setCancelable(P.mCancelable);
            if (P.mCancelable) {
                dialog.setCanceledOnTouchOutside(true);
            }

            dialog.setOnCancelListener(P.mOnCancelListener);
            dialog.setOnDismissListener(P.mOnDismissListener);
            if (P.mOnKeyListener != null) {
                dialog.setOnKeyListener(P.mOnKeyListener);
            }
            return dialog;
        }

        public EasyAlertDialog show() {
            EasyAlertDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }

    public void setEasyDialogDismissListener(EasyDialogDismissListener mEasyDialogDismissListener) {
        this.mEasyDialogDismissListener = mEasyDialogDismissListener;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mEasyDialogDismissListener != null) {
            mEasyDialogDismissListener.onEasyDialogDismiss();
        }
    }

    public interface EasyDialogDismissListener {
        void onEasyDialogDismiss();
    }
}

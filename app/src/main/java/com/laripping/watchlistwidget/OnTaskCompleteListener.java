package com.laripping.watchlistwidget;

/**
 * The Interface that MainActivity will implement, to pass back a signal from the dialog
 */
public interface OnTaskCompleteListener {
    public abstract void onComplete(boolean success);

}

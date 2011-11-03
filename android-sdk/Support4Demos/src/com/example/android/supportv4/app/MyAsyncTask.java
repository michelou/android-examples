package com.example.android.supportv4.app;

import android.os.AsyncTask;

/**
 * Temporary workaround to solve a Scala compiler issue which shows up
 * at runtime with the error message
 * "java.lang.AbstractMethodError: abstract method not implemented"
 * for the missing method LookupTask.doInBackground(Params... args).
 *
 * Our solution: the Java method doInBackground(Params... args) forwards
 * the call to the Scala method doInBackground1(Params[] args).
 */
public abstract class MyAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    protected abstract Result doInBackground1(Params[] args);

    @Override
    protected Result doInBackground(Params... args) {
        return doInBackground1(args);
    }

}


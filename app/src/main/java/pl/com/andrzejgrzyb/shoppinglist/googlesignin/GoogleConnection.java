package pl.com.andrzejgrzyb.shoppinglist.googlesignin;

/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.lang.ref.WeakReference;
import java.util.Observable;

import pl.com.andrzejgrzyb.shoppinglist.R;

public class GoogleConnection extends Observable
        implements ConnectionCallbacks, OnConnectionFailedListener {

    public static final int REQUEST_CODE = 1234;
    public String TAG = "GoogleConnection";
   // private static GoogleConnection sGoogleConnection;

    private WeakReference<Activity> activityWeakReference;
    private static Activity mActivity;
    private GoogleApiClient.Builder googleApiClientBuilder;
    private GoogleApiClient googleApiClient;
    private ConnectionResult connectionResult;
    private GoogleSignInAccount mGoogleSignInAccount;
    private State currentState;
    private ProgressDialog mProgressDialog;
    private static final int RC_SIGN_IN = 9001;


    public void connect() {
        Log.d(TAG, "connect");
       // currentState.connect(this);
        onSignIn();
        if(!isSignedIn()) {
            onSignUp();
        }
    }
    public void connectSilently() {
        Log.d(TAG, "connectSilently");
        onSignIn();
    }

    public void disconnect() {
        Log.d(TAG, "disconnect");
       // currentState.disconnect(this);
        onSignOut();
    }

    public void revokeAccessAndDisconnect() {
        Log.d(TAG, "revoke");
       // currentState.revokeAccessAndDisconnect(this);
        onRevokeAccessAndDisconnect();
    }

//    public static GoogleConnection getInstance(Activity activity) {
//        mActivity = activity;
//        Log.d(TAG, "getInstance, activity = " + mActivity.getClass().getSimpleName());
//        if (null == sGoogleConnection) {
//            Log.d(TAG, "sGoogleConnection == null");
//            sGoogleConnection = new GoogleConnection(activity);
//        }
//
//        return sGoogleConnection;
//    }

    @Override
    public void onConnected(Bundle hint) {
        Log.d(TAG, "onConnected");
        //changeState(State.OPENED);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        // We call connect() to attempt to re-establish the connection or get a
        // ConnectionResult that we can attempt to resolve.
        Log.d(TAG, "onConnectionSuspended");
        changeState(State.CLOSED);
        connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
        if (currentState.equals(State.CLOSED) && connectionResult.hasResolution()) {
            changeState(State.CREATED);
            this.connectionResult = connectionResult;
        } else {
            connect();
        }
    }

//    public void onActivityResult(int result) {
//        Log.d(TAG, "onActivityResult");
//        if (result == Activity.RESULT_OK) {
//            Log.d(TAG, "Activity.RESULT_OK");
//            // If the error resolution was successful we should continue
//            // processing errors.
//            changeState(State.CREATED);
//        } else {
//            Log.d(TAG, "NOT Activity.RESULT_OK");
//            // If the error resolution was not successful or the user canceled,
//            // we should stop processing errors.
//            changeState(State.CLOSED);
//        }
//
//        // If Google Play services resolved the issue with a dialog then
//        // onStart is not called so we need to re-attempt connection here.
//        onSignIn();
//    }

    public String getEmail() {
        if (mGoogleSignInAccount != null) {
            return mGoogleSignInAccount.getEmail();
        } else {
            return null;
        }
    }
    public String getId() {
        if (mGoogleSignInAccount != null) {
            return mGoogleSignInAccount.getId();
        } else {
            return null;
        }
    }
    public String getIdToken() {
        if (mGoogleSignInAccount != null) {
            return mGoogleSignInAccount.getIdToken();
        } else {
            return null;
        }
    }
    public String getName() {
        if (mGoogleSignInAccount != null) {
            return mGoogleSignInAccount.getDisplayName();
        } else {
            return null;
        }
    }
    public String getPhotoUrlString() {
        if (mGoogleSignInAccount != null) {
            return mGoogleSignInAccount.getPhotoUrl().toString();
        } else {
            return null;
        }
    }
    public boolean isSignedIn() {
        Log.d(TAG, "isSignedIn: " + (mGoogleSignInAccount != null));
        return(mGoogleSignInAccount != null);
    }

    protected void onSignIn() {
        Log.d(TAG, "onSignIn");
//        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
//            googleApiClient.connect();
//        }
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            Log.d(TAG, "Expired sign-in");
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    protected void onSignOut() {
        Log.d(TAG, "onSignOut");
//        if (googleApiClient.isConnected()) {
//            // We clear the default account on sign out so that Google Play
//            // services will not return an onConnected callback without user
//            // interaction.
//            Plus.AccountApi.clearDefaultAccount(googleApiClient);
//            googleApiClient.disconnect();
//            googleApiClient.connect();
//            changeState(State.CLOSED);
//        }
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        changeState(State.CLOSED);
                        // [END_EXCLUDE]
                    }
                });
        mGoogleSignInAccount = null;
    }

    protected void onSignUp() {
        // We have an intent which will allow our user to sign in or
        // resolve an error.  For example if the user needs to
        // select an account to sign in with, or if they need to consent
        // to the permissions your app is requesting.

//        try {
//            // Send the pending intent that we stored on the most recent
//            // OnConnectionFailed callback.  This will allow the user to
//            // resolve the error currently preventing our connection to
//            // Google Play services.
//            changeState(State.OPENING);
//            connectionResult.startResolutionForResult(activityWeakReference.get(), REQUEST_CODE);
//        } catch (IntentSender.SendIntentException e) {
//            // The intent was canceled before it was sent.  Attempt to connect to
//            // get an updated ConnectionResult.
//            changeState(State.CREATED);
//            googleApiClient.connect();
//        }



        Log.d(TAG, "onSignUp");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        mActivity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setMessage(mActivity.getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    public void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            mGoogleSignInAccount = result.getSignInAccount();
            changeState(State.OPENED);

            Log.d(TAG, "DisplayName = " + mGoogleSignInAccount.getDisplayName());
            Log.d(TAG, "Id = " + mGoogleSignInAccount.getId());
            Log.d(TAG, "Email = " + mGoogleSignInAccount.getEmail());
            //goToMainActivity();
        } else {
            changeState(State.CLOSED);
            Log.d(TAG, "result.getStatus().toString(): " + result.getStatus().toString());
            // Signed out, show unauthenticated UI.
        }
    }

    protected void onRevokeAccessAndDisconnect() {
        Log.d(TAG, "onRevokeAccessAndDisconnect");
//        // After we revoke permissions for the user with a GoogleApiClient
//        // instance, we must discard it and create a new one.
//        Plus.AccountApi.clearDefaultAccount(googleApiClient);
//
//        // Our sample has caches no user data from Google+, however we
//        // would normally register a callback on revokeAccessAndDisconnect
//        // to delete user data so that we comply with Google developer
//        // policies.
//        Plus.AccountApi.revokeAccessAndDisconnect(googleApiClient);
//        googleApiClient = googleApiClientBuilder.build();
//        googleApiClient.connect();
//        changeState(State.CLOSED);
        Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        changeState(State.CLOSED);
                        // [END_EXCLUDE]
                    }
                });

    }

    public GoogleConnection(FragmentActivity fragmentActivity) {
       // activityWeakReference = new WeakReference<>(activity);
        mActivity = fragmentActivity;
        TAG = TAG + "(" + mActivity.getClass().getSimpleName() + ")";

//        googleApiClientBuilder =
//                new GoogleApiClient.Builder(activityWeakReference.get().getApplicationContext())
//                        .addConnectionCallbacks(this)
//                        .addOnConnectionFailedListener(this)
//                        .addApi(Plus.API, Plus.PlusOptions.builder().build())
//                        .addScope(new Scope("email"));
//
//        googleApiClient = googleApiClientBuilder.build();

        // [START configure_signin]
        // Request only the user's ID token, which can be used to identify the
        // user securely to your backend. This will contain the user's basic
        // profile (name, profile picture URL, etc) so you should not need to
        // make an additional call to personalize your application.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(fragmentActivity.getString(R.string.server_client_id))
                .requestEmail()
                .build();
        // [END configure_signin]

        // Build GoogleAPIClient with the Google Sign-In API and the above options.
        googleApiClient = new GoogleApiClient.Builder(fragmentActivity)
                .enableAutoManage(fragmentActivity /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        changeState(State.CREATED);


    }

    private void changeState(State state) {
        currentState = state;
        setChanged();
        notifyObservers(state);
        Log.d(TAG, "changeState("+ state.toString() +")");
    }


}
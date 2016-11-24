package pl.grzyb.andrzej.shoppinglist;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;

import java.util.Observable;
import java.util.Observer;

import pl.grzyb.andrzej.shoppinglist.googlesignin.GoogleConnection;
import pl.grzyb.andrzej.shoppinglist.googlesignin.State;

public class LoginActivity extends AppCompatActivity  implements Observer, View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private TextView mStatusTextView;
    private SignInButton mGoogleSignInButton;
    private Button mNoSignInButton;

    private GoogleConnection googleConnection;
    AlertDialog dialog;
    private static final int RC_SIGN_IN = 9001;

    @Override
    public void update(Observable observable, Object data) {
        if (observable != googleConnection) {
            return;
        }

        switch ((State) data) {
            case CREATED:
                dialog.dismiss();
                onSignedOutUI();
                break;
            case OPENING:
                dialog.show();
                break;
            case OPENED:
                dialog.dismiss();
                // Update the user interface to reflect that the user is signed in.
                onSignedInUI();

                // We are signed in!
                // Retrieve some profile information to personalize our app for the user.

                break;
            case CLOSED:
                dialog.dismiss();
                onSignedOutUI();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //  Views
        mStatusTextView = (TextView) findViewById(R.id.status);

        mGoogleSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mNoSignInButton = (Button) findViewById(R.id.continue_without_signin_button);

        setGooglePlusButtonText(mGoogleSignInButton, getResources().getString(R.string.signin_with_google));

        // Button click listeners
        mGoogleSignInButton.setOnClickListener(this);
        mNoSignInButton.setOnClickListener(this);

        googleConnection = GoogleConnection.getInstance(this);
        googleConnection.addObserver(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.dismiss();

    }

    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.

        Log.d(TAG, "onStart");
       // googleConnection.connect();

//        // OLD STUFF ///////////////////////////////////////////////////////////
//        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
//        if (opr.isDone()) {
//            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
//            // and the GoogleSignInResult will be available instantly.
//            Log.d(TAG, "Got cached sign-in");
//            GoogleSignInResult result = opr.get();
//            handleSignInResult(result);
//        } else {
//            // If the user has not previously signed in on this device or the sign-in has expired,
//            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
//            // single sign-on will occur in this branch.
//            Log.d(TAG, "Expired sign-in");
//            showProgressDialog();
//            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
//                @Override
//                public void onResult(GoogleSignInResult googleSignInResult) {
//                    hideProgressDialog();
//                    handleSignInResult(googleSignInResult);
//                }
//            });
//        }
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        AppIndex.AppIndexApi.start(mGoogleApiClient, getIndexApiAction());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
        googleConnection.deleteObserver(this);
        googleConnection.disconnect();
    }
//    @Override
//    protected void onPause() {
//        super.onPause();
//
////        if(mProgressDialog != null) {
////            mProgressDialog.dismiss();
////            mProgressDialog = null;
////        }
//    }

    protected void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }





    private void onSignedOutUI() {
        // Update the UI to reflect that the user is signed out.
//        mSignInButton.setEnabled(true);
//        mSignOutButton.setEnabled(false);
//        mRevokeButton.setEnabled(false);

        mStatusTextView.setText("Signed out");
    }
    private void onSignedInUI() {
        // Update the UI to reflect that the user is signed in.
       mGoogleSignInButton.setVisibility(View.GONE);
        mNoSignInButton.setText(getString(R.string.continue_as, googleConnection.getName()));
//        mRevokeButton.setEnabled(true);
        Log.d(TAG, "onSignedInUI");
        mStatusTextView.setText(getString(R.string.signed_in_as, googleConnection.getEmail()));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                mStatusTextView.setText(R.string.signing_in);
                googleConnection.connect();
                break;
            case R.id.continue_without_signin_button:
                goToMainActivity();
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //    super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        Log.d(TAG, "onActivityResult");
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            googleConnection.handleSignInResult(result);
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Login Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

//    @Override
//    public void onStop() {
//        super.onStop();
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        AppIndex.AppIndexApi.end(mGoogleApiClient, getIndexApiAction());
//        mGoogleApiClient.disconnect();
//    }
}

package com.example.binarybombtech.emailfromtasker.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.binarybombtech.emailfromtasker.R;
import com.example.binarybombtech.emailfromtasker.TaskerPlugin;
import com.example.binarybombtech.emailfromtasker.bundle.BundleScrubber;
import com.example.binarybombtech.emailfromtasker.bundle.PluginBundleManager;

import java.io.File;

public class MainActivity extends AppCompatActivity implements DialogInterface.OnClickListener{

    /*TODO
        new authenticated gmail
        comment for posterity
        scrollable view UI?
        app icon and name
        Log for debugging
     */

    private boolean mIsCancelled = false;
    AlertDialog mSettings;
    AlertDialog mHelp;
    EditText mHost;
    EditText mPort;
    EditText mSport;
    String host = "smtp.googlemail.com";
    String port = "465";
    String sport = "465";
    String uName;
    String pWord;
    String mTo;
    String mSub;
    String mBody;
    String mAttach;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get any information previously input and restore the fields to their values
        BundleScrubber.scrub(getIntent());

        final Bundle localeBundle = getIntent().getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
        BundleScrubber.scrub(localeBundle);

        if (null == savedInstanceState)
        {
            if (PluginBundleManager.isBundleValid(localeBundle))
            {
                getBundle(localeBundle);
            }
        }
    }

    public void setupDialog(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.dsettings, null);
        mHost = (EditText) layout.findViewById(R.id.host);
        mPort = (EditText) layout.findViewById(R.id.port);
        mSport = (EditText) layout.findViewById(R.id.sport);

        mHost.setText(host);
        mPort.setText(port);
        mSport.setText(sport);

        builder.setView(layout);
        builder.setTitle("Email Settings")
                .setPositiveButton("OK", this)
                .setNegativeButton("Cancel", this);

        mSettings = builder.create();
        mSettings.show();
    }

    public void setupHelpDialog(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.content_help, null);

        builder.setView(layout);
        builder.setTitle("Help")
                .setNegativeButton("Cancel", this);

        mHelp = builder.create();
        mHelp.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int id) {
        if (id == Dialog.BUTTON_POSITIVE) {
            host = mHost.getText().toString();
            port = mPort.getText().toString();
            sport = mSport.getText().toString();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.t_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();

        if (android.R.id.home == id)
        {
            finish();
            return true;
        }
        else if (R.id.twofortyfouram_locale_menu_dontsave == id)
        {
            mIsCancelled = true;
            finish();
            return true;
        }
        else if (R.id.twofortyfouram_locale_menu_save == id)
        {
            finish();
            return true;
        }
        else if (R.id.twofortyfouram_locale_menu_help == id){
            setupHelpDialog();
            return true;
        }
        else if (R.id.menu_settings == id){
            setupDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method takes all the user inputs and puts them in a bundle to be sent back to
     * Tasker. It includes some input error checking.
     */
    @Override
    public void finish()
    {
        if (!mIsCancelled)
        {
            getInputs();
            //error checking -> user must input username, password, and recipient
            if (uName.length()>0 && mTo.length()>0 && pWord.length()>0)
            {
                //if fields were empty, put single character in so Mail.java accepts the input
                if(mSub.length()==0){
                    mSub = " ";
                }
                if(mBody.length()==0){
                    mBody = " ";
                }
                if(mAttach.length()==0){
                    mAttach = " ";
                }//check attachment files to see if they are valid
                else{
                    String [] attachments = mAttach.split(",");

                    for(int i=0;i<attachments.length;i++) {

                        String filecheck = Environment.getExternalStorageDirectory().toString() + "/"
                                + attachments[i];

                        File file = new File(filecheck);
                        if (file.exists() && !file.isDirectory()) {
                            //attachment file exists
                        } else {
                            Toast.makeText(this,"File " + attachments[i] + " does not exist. Will not be attached."
                            ,Toast.LENGTH_LONG).show();
                        }
                    }
                }

                final Intent resultIntent = new Intent();

                /*
                 * This extra is the data to ourselves: either for the Activity or the BroadcastReceiver. Note
                 * that anything placed in this Bundle must be available to Locale's class loader. So storing
                 * String, int, and other standard objects will work just fine. Parcelable objects are not
                 * acceptable, unless they also implement Serializable. Serializable objects must be standard
                 * Android platform objects (A Serializable class private to this plug-in's APK cannot be
                 * stored in the Bundle, as Locale's classloader will not recognize it).
                 */
                Bundle resultBundle =
                        PluginBundleManager.generateBundle(getApplicationContext(), uName, pWord, mTo,
                                                            mSub, mBody, mAttach, host, port, sport);
                //parse for Tasker variables
                if ( TaskerPlugin.Setting.hostSupportsOnFireVariableReplacement( this ) )
                    TaskerPlugin.Setting.setVariableReplaceKeys( resultBundle, new String []
                            { PluginBundleManager.BUNDLE_EXTRA_STRING_UNAME,
                              PluginBundleManager.BUNDLE_EXTRA_STRING_PWORD,
                              PluginBundleManager.BUNDLE_EXTRA_STRING_MTO,
                              PluginBundleManager.BUNDLE_EXTRA_STRING_MSUB,
                              PluginBundleManager.BUNDLE_EXTRA_STRING_MBODY,
                              PluginBundleManager.BUNDLE_EXTRA_STRING_MATTACH} );

                resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, resultBundle);

                /*
                 * The blurb is concise status text to be displayed in the host's UI.
                 */
                resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, uName);

                setResult(RESULT_OK, resultIntent);
            }
            else{
                Toast.makeText(this,"Error: please enter username, password, and recipient",Toast.LENGTH_LONG).show();
            }
        }

        super.finish();
    }

    void getInputs(){
        //retrieve user inputs
        uName = ((EditText) findViewById(R.id.uName)).getText().toString();
        pWord = ((EditText) findViewById(R.id.pWord)).getText().toString();
        mTo = ((EditText) findViewById(R.id.mTo)).getText().toString();
        mSub = ((EditText) findViewById(R.id.mSubject)).getText().toString();
        mBody = ((EditText) findViewById(R.id.mBody)).getText().toString();
        mAttach = null;
        if (((EditText) findViewById(R.id.fAttach)) != null) {
            mAttach = ((EditText) findViewById(R.id.fAttach)).getText().toString();
        }
    }

    void getBundle(Bundle localeBundle){
        final String name =
                localeBundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_UNAME);
        ((EditText) findViewById(R.id.uName)).setText(name);
        final String pass =
                localeBundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_PWORD);
        ((EditText) findViewById(R.id.pWord)).setText(pass);
        String sub =
                localeBundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MSUB);
        ((EditText) findViewById(R.id.mSubject)).setText(sub);
        final String to =
                localeBundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MTO);
        ((EditText) findViewById(R.id.mTo)).setText(to);
        String body =
                localeBundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MBODY);
        ((EditText) findViewById(R.id.mBody)).setText(body);
        String attach =
                localeBundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MATTACH);
        ((EditText) findViewById(R.id.fAttach)).setText(attach);
        host = localeBundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MHOST);
        port = localeBundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MPORT);
        sport = localeBundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MSPORT);
    }
}

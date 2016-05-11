package com.example.binarybombtech.emailfromtasker;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.binarybombtech.emailfromtasker.bundle.BundleScrubber;
import com.example.binarybombtech.emailfromtasker.bundle.PluginBundleManager;

import java.io.File;

public class EmailIntentService extends IntentService {

	public EmailIntentService() {
		super("EmailIntentServiceName");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		//retrieve data from intent
		BundleScrubber.scrub(intent);
		final Bundle bundle = intent.getBundleExtra("SEND SEND");
		BundleScrubber.scrub(bundle);

		final String uName = bundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_UNAME);
		final String pWord = bundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_PWORD);
		final String mTo = bundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MTO);
		final String mSub = bundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MSUB);
		final String mBody = bundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MBODY);
		final String mAttach = bundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MATTACH);
		final String mPort = bundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MPORT);
		final String mHost = bundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MHOST);
		final String mSport = bundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MSPORT);

		//format data for email
		String[] to = mTo.split(",");
		String from = to[0];

		//Send email
		Mail mail = new Mail(uName, pWord, mHost, mPort, mSport);
		try {
			mail.send(to,from,mSub,mBody,mAttach);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

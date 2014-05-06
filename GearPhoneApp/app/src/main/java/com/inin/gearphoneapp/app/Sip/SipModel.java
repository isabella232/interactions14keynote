package com.inin.gearphoneapp.app.Sip;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.net.sip.SipSession;
import android.preference.PreferenceManager;
import android.util.Log;

import com.inin.gearphoneapp.app.MainActivity;
import com.inin.gearphoneapp.app.pref.PreferencesFragment;

import java.text.ParseException;


public class SipModel {

    private static SipModel _instance = null;
    private static final String ACTION_INCOMING_CALL = "android.SipDemo.INCOMING_CALL";

    private MainActivity _mainActivity = null;
    private SipManager _sipManager = null;
    private IncomingCallReceiver _incomingCallReceiver = null;
    private AudioManager _audioManager = null;
    private SipProfile _stationProfile = null;
    private SipAudioCall _audioCall = null;
    private String _prefServer = "";
    private String _prefPort = "";
    private String _prefStationId = "";
    private SipStationState _stationState = SipStationState.Unknown;
    private String _stationMessage = "Tap to register station";



    private SipModel(MainActivity context){
        Log.v(HelperModel.TAG, "SipModel::ctor");

        // Initialize persistent things
        _mainActivity = context;
        _audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        _sipManager = SipManager.newInstance(context);
    }



    // The context parameter is expected to be the MainActivity reference (use "this" when calling GetInstance)
    public static SipModel GetInstance(MainActivity context){
        if (_instance == null && context != null) {
            _instance = new SipModel(context);
        }

        // Update MainActivity context
        if (_instance != null && context != null && _instance._mainActivity != context){
            _instance._mainActivity = context;
        }

        return _instance;
    }

    public void playDtmf(int digit){
        try{
            if (_audioCall == null){
                Log.w(HelperModel.TAG_CALLS, "Call was null, cannot send DTMF!");
            } else {
                Log.v(HelperModel.TAG_CALLS, "Sending DTMF: " + digit);
                _audioCall.sendDtmf(digit);
            }
        } catch (Exception e){
            Log.e(HelperModel.TAG_CALLS, "Error executing DTMF request.", e);
        }
    }

    public void makeCall(String dialstring){
        try {
            // Register station first if not already registered
            if (_stationProfile == null) registerSipProfile(false);

            Log.v(HelperModel.TAG_SIP, "Raw dialstring: " + dialstring);

            //TODO: Remove this line, it is a shortcut for debug purposes
            if (dialstring.equals("") || dialstring == null) dialstring = "5000";

            /*
            Because this is an actual SIP call, not a CIC dial request, we must address it to a
            server. Typical CIC dialplans will send non-local SIP user-addresses (the part before
            the @ symbol) out an external line. In this way, a SIP call to 3172222222@server.com
            will be send to the CIC server and the CIC server will dial 3172222222 on an outbound
            line to place an actual call.
             */
            if (!dialstring.contains("@")) dialstring += "@" + _prefServer;

            SipProfile.Builder builder = new SipProfile.Builder(dialstring);
            SipProfile remote = builder.build();

            Log.v(HelperModel.TAG_SIP, "Dialing " + remote.getUriString());
            _audioCall = _sipManager.makeAudioCall(_stationProfile, remote, getSipAudioCallListener(), 30);

        } catch (ParseException e) {
            Log.e(HelperModel.TAG_SIP, "Unable to set up remote SipProfile.", e);
//        } catch (SipException e) {
//            Log.e(HelperModel.TAG_SIP, "SipAudioCall error.", e);
        } catch (Exception e){
            Log.e(HelperModel.TAG_SIP, "General error.", e);
        }
    }

    public void registerSipProfile(Boolean forceDeregister){
        try {
            // Deregister
            if (_stationProfile != null || forceDeregister){
                Log.v(HelperModel.TAG_SIP, "Processing deregistration request...");
                if (_sipManager.isOpened(_stationProfile.getUriString())){
                    Log.v(HelperModel.TAG_SIP, "Attempting to close: " + _stationProfile.getUriString());
                    _sipManager.close(_stationProfile.getUriString());
                }
                if (_sipManager.isRegistered(_stationProfile.getUriString())){
                    Log.v(HelperModel.TAG_SIP, "Attempting to unregister: " + _stationProfile.getUriString());
                    _sipManager.unregister(_stationProfile, getSipRegistrationListener());
                }
                Log.v(HelperModel.TAG_SIP, "Local profile has been deregistered.");
                setStationState(SipStationState.Unregistered, "");
                _stationProfile = null;
                return;
            }

            Log.v(HelperModel.TAG_CALLS, "VOIP Supported: " + SipManager.isVoipSupported(_mainActivity));
            Log.v(HelperModel.TAG_CALLS, "SIP API Supported: " + SipManager.isApiSupported(_mainActivity));

            Log.v(HelperModel.TAG_SIP, "Getting registration settings...");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_mainActivity);
            _prefServer = prefs.getString(PreferencesFragment.KEY_PREF_SERVER, "");
            _prefPort = prefs.getString(PreferencesFragment.KEY_PREF_SERVER_PORT, "");
            _prefStationId = prefs.getString(PreferencesFragment.KEY_PREF_STATION_ID, "");

            // Create SIP URI from config parts
            String address = "sip:" + _prefStationId + "@" + _prefServer + ":" + _prefPort;

            //SipProfile.Builder builder = new SipProfile.Builder("sip:GalaxyS3@172.19.33.19:8060"); // IP for tim-cic4su5.dev2000.com
            SipProfile.Builder builder = new SipProfile.Builder(address);
            builder.setAutoRegistration(true);
            _stationProfile = builder.build();

            Log.v(HelperModel.TAG_SIP, "Attempting to open profile: " + _stationProfile.getUriString());
            Intent i = new Intent();
            i.setAction(ACTION_INCOMING_CALL);
            PendingIntent pi = PendingIntent.getBroadcast(_mainActivity, 1234, i, Intent.FILL_IN_DATA);
            _sipManager.open(_stationProfile, pi, null);

            Log.v(HelperModel.TAG_SIP, "Attempting to set registration listener...");
            _sipManager.setRegistrationListener(_stationProfile.getUriString(), getSipRegistrationListener());

            Log.v(HelperModel.TAG_SIP, "Registration initiated successfully.");
        } catch (ParseException e) {
            Log.e(HelperModel.TAG_SIP, "Unable to set up local SipProfile.", e);
        } catch (SipException e) {
            Log.e(HelperModel.TAG_SIP, "Unable to open local SipProfile.", e);
        } catch (Exception e) {
            Log.e(HelperModel.TAG_SIP, "General error.", e);
        }
    }

    public void callToggleMute(){
        try{
            if (_audioCall == null) return;
            _audioCall.toggleMute();
            _mainActivity.updateCallStatus(_audioCall);
        } catch (Exception e){
            Log.e(HelperModel.TAG_CALLS, "Error on call mute.", e);
        }
    }

    public void callToggleHold(){
        try{
            if (_audioCall == null) return;

            Log.v(HelperModel.TAG_CALLS, "held -> " + _audioCall.isOnHold());

            if (!_audioCall.isOnHold())
                _audioCall.holdCall(0);
            else
                _audioCall.continueCall(0);
            _mainActivity.updateCallStatus(_audioCall);
        } catch (Exception e){
            Log.e(HelperModel.TAG_CALLS, "Error on call hold.", e);
        }
    }

    public void callToggleSpeaker(){
        if (_audioCall == null) return;
        _audioCall.setSpeakerMode(!_audioManager.isSpeakerphoneOn());
        _mainActivity.updateCallStatus(_audioCall);
    }

    public void callDisconnect() {
        if (_audioCall == null) return;
        try{
            _audioCall.endCall();

            _mainActivity.updateCallStatus(_audioCall);
        } catch (Exception e){
            Log.e(HelperModel.TAG_CALLS, "Error on call disconnect.", e);
        }
    }

    public void callAnswer(){
        try {
            if (_audioCall.isInCall()) return;

            _audioCall.answerCall(30);
            _audioCall.startAudio();

            //TODO: Setting for default speakerphone and mute value
            _audioCall.setSpeakerMode(true);
            if(_audioCall.isMuted()) {
                _audioCall.toggleMute();
            }

            _mainActivity.updateCallStatus(_audioCall);
        } catch (Exception e) {
            Log.e(HelperModel.TAG_SIP, "Error taking call.", e);
        }
    }

    public void receiveCall(Intent intent){
        try{
            Log.d(HelperModel.TAG_CALLS, "receiveCall");

            Log.v(HelperModel.TAG_CALLS, "getCallId: " + SipManager.getCallId(intent));
            Log.v(HelperModel.TAG_CALLS, "getOfferSessionDescription: " + SipManager.getOfferSessionDescription(intent));
            Log.v(HelperModel.TAG_CALLS, "isIncomingCallIntent: " + SipManager.isIncomingCallIntent(intent));
            _audioCall = _sipManager.takeAudioCall(intent, getSipAudioCallListener());

            // TODO: Make useful trace with uri
            Log.d(HelperModel.TAG_CALLS, "call is now: " + _audioCall);

            // Auto-answer all incoming calls
            //callAnswer();

            _mainActivity.updateCallStatus(_audioCall);
        } catch (Exception e){
            Log.e(HelperModel.TAG_CALLS, "Error receiving call", e);
        }
    }

    public boolean isSpeakerphoneOn(){
        return _audioManager.isSpeakerphoneOn();
    }

    public SipAudioCall getCall(){
        return _audioCall;
    }

    public SipStationState getStationState(){ return _stationState; }

    public String getStationMessage() { return _stationMessage; }

    public void registerCallReceiver(){
        // Set up the intent filter.  This will be used to fire an
        // IncomingCallReceiver when someone calls the SIP address used by this
        // application.
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_INCOMING_CALL);
        _incomingCallReceiver = new IncomingCallReceiver();
        _mainActivity.registerReceiver(_incomingCallReceiver, filter);
    }

    public void unregisterCallReceiver(){
        _mainActivity.unregisterReceiver(_incomingCallReceiver);
    }



    private SipRegistrationListener getSipRegistrationListener(){
        return new SipRegistrationListener() {

            public void onRegistering(String localProfileUri) {
                Log.v(HelperModel.TAG_REGISTRATION_LISTENER, "Registering profile with SIP Server. URI: " + localProfileUri);
                setStationState(SipStationState.Attempting, "");
            }

            public void onRegistrationDone(String localProfileUri, long expiryTime) {
                Log.v(HelperModel.TAG_REGISTRATION_LISTENER, "Registered profile with SIP Server. URI: " + localProfileUri + " expiry time: " + expiryTime);
                setStationState(SipStationState.Registered, localProfileUri);
            }

            public void onRegistrationFailed(String localProfileUri, int errorCode, String errorMessage) {
                Log.e(HelperModel.TAG_REGISTRATION_LISTENER, "Registration failed for " + localProfileUri + " --  Error: (" + Integer.toString(errorCode) + ") " + errorMessage);
                setStationState(SipStationState.Error, errorMessage);
            }
        };
    }

    private SipSession.Listener getSipSessionListener(){
        return new SipSession.Listener() {
            public void onCallBusy(SipSession session){
                // Called when the peer is busy during session initialization.
                Log.v(HelperModel.TAG_SESSION_LISTENER, "onCallBusy");
            }

            public void onCallChangeFailed(SipSession session, int errorCode, String errorMessage){
                // Called when an error occurs during session modification negotiation.
                Log.e(HelperModel.TAG_SESSION_LISTENER, "SIP call change failed: (" + Integer.toString(errorCode) + ") " + errorMessage);
            }

            public void onCallEnded(SipSession session){
                // Called when the session is terminated.
                Log.v(HelperModel.TAG_SESSION_LISTENER, "onCallEnded");
            }

            public void onCallEstablished(SipSession session, String sessionDescription){
                // Called when the session is established.
                Log.v(HelperModel.TAG_SESSION_LISTENER, "onCallEstablished - " + sessionDescription);
            }

            public void onCalling(SipSession session){
                // Called when an INVITE request is sent to initiate a new call.
                Log.v(HelperModel.TAG_SESSION_LISTENER, "onCalling");
            }

            public void onError(SipSession session, int errorCode, String errorMessage){
                // Called when an error occurs during session initialization and termination.
                Log.e(HelperModel.TAG_SESSION_LISTENER, "SIP session error: (" + Integer.toString(errorCode) + ") " + errorMessage);
            }

            public void onRegistering(SipSession session){
                // Called when a registration request is sent.
                Log.v(HelperModel.TAG_SESSION_LISTENER, "onRegistering");
            }

            public void onRegistrationDone(SipSession session, int duration){
                // Called when registration is successfully done.
                Log.v(HelperModel.TAG_SESSION_LISTENER, "onRegistrationDone - duration: " + duration);
            }

            public void onRegistrationFailed(SipSession session, int errorCode, String errorMessage){
                // Called when the registration fails.
                Log.e(HelperModel.TAG_SESSION_LISTENER, "SIP registration failed: (" + Integer.toString(errorCode) + ") " + errorMessage);
            }

            public void onRegistrationTimeout(SipSession session){
                // Called when the registration gets timed out.
                Log.v(HelperModel.TAG_SESSION_LISTENER, "onRegistrationTimeout");
            }

            public void onRinging(SipSession session, SipProfile caller, String sessionDescription){
                // Called when an INVITE request is received.
                Log.v(HelperModel.TAG_SESSION_LISTENER, "onRinging - " + caller.getUriString() + " (" + sessionDescription + ")");
            }

            public void onRingingBack(SipSession session){
                // Called when a RINGING response is received for the INVITE request sent
                Log.v(HelperModel.TAG_SESSION_LISTENER, "onRingingBack");
            }
        };
    }

    private SipAudioCall.Listener getSipAudioCallListener(){
        return new SipAudioCall.Listener() {
            @Override
            public void onCallBusy(SipAudioCall call) {
                // Called when the peer is busy during session initialization.
                Log.d(HelperModel.TAG_SIP, "onCallBusy");
                _mainActivity.updateCallStatus(call);
            }

            @Override
            public void onCallEnded(SipAudioCall call) {
                // Called when the session is terminated.
                Log.d(HelperModel.TAG_SIP, "onCallEnded");
                _audioCall = null;
                _mainActivity.updateCallStatus(null);

                //TODO: clean up UI when this happens -- call is always over now. The UI doesn't always get reliable information from call.isInCall() at this point.
            }

            @Override
            public void onCallEstablished(SipAudioCall call) {
                // Called when the session is established.
                    /*
                    NOTE: For some reason, this is also called when a remote disconnect happens.
                    onCallEnded is called several seconds after the call is disconnected on the remote end.
                     */
                Log.d(HelperModel.TAG_SIP, "onCallEstablished");

                call.startAudio();
                call.setSpeakerMode(true);

                _mainActivity.updateCallStatus(call);
            }

            @Override
            public void onCallHeld(SipAudioCall call) {
                // Called when the call is on hold.
                Log.d(HelperModel.TAG_SIP, "onCallHeld");
                _mainActivity.updateCallStatus(call);
            }

            @Override
            public void onCalling(SipAudioCall call) {
                // Called when a request is sent out to initiate a new call.
                Log.d(HelperModel.TAG_SIP, "onCalling");
                _mainActivity.updateCallStatus(call);
            }

            @Override
            public void onChanged(SipAudioCall call) {
                // Called when an event occurs and the corresponding callback is not overridden.
                Log.d(HelperModel.TAG_SIP, "onChanged");
                _mainActivity.updateCallStatus(call);
            }

            @Override
            public void onError(SipAudioCall call, int errorCode, String errorMessage) {
                // Called when an error occurs.
                    /*
                    NOTE: This throws an exception randomly for no obvious reason. It appears that
                    DNS resolution fails for some reason when dialing. Trying again immediately,
                    without changing anything, will typically work. The error is:
                    SIP Call Error: (-4) libcore.io.GaiException: getaddrinfo failed: EAI_NODATA (No address associated with hostname)
                     */
                Log.e(HelperModel.TAG_SIP, "SIP Call Error: (" + Integer.toString(errorCode) + ") " + errorMessage);
                //TODO: notify user of failure
                _audioCall = null;
                _mainActivity.updateCallStatus(null);
            }

            @Override
            public void onReadyToCall(SipAudioCall call) {
                // Called when the call object is ready to make another call.
                Log.d(HelperModel.TAG_SIP, "onReadyToCall");
                _mainActivity.updateCallStatus(call);
            }

            @Override
            public void onRinging(SipAudioCall call, SipProfile caller) {
                // Called when a new call comes in.
                Log.d(HelperModel.TAG_SIP, "onRinging");
                _mainActivity.updateCallStatus(call);
            }

            @Override
            public void onRingingBack(SipAudioCall call) {
                // Called when a RINGING response is received for the INVITE request sent.
                Log.d(HelperModel.TAG_SIP, "onRingingBack");
                _mainActivity.updateCallStatus(call);
            }
        };
    }

    private void setStationState(SipStationState state, String message){
        _stationState = state;
        _stationMessage = message;
        _mainActivity.updateStationState();
    }
}

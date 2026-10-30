package pro.brickstorm.game;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import com.yandex.mobile.ads.common.*;
import com.yandex.mobile.ads.interstitial.*;
import com.yandex.mobile.ads.rewarded.*;

/** Android-only adapter; the pure model owns all eligibility, timing and reward rules. */
final class YandexAdHost implements AdsHost {
    private final Activity activity;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private RewardedAdLoader rewardedLoader;
    private InterstitialAdLoader interstitialLoader;
    private RewardedAd rewarded;
    private InterstitialAd interstitial;
    private boolean destroyed,showing,rewardLoading,interLoading;
    private int rewardFailures,interFailures;
    private Runnable watchdog;
    YandexAdHost(Activity activity){this.activity=activity;}
    void start(){
        if(BuildConfig.INTERSTITIAL_AD_UNIT.isEmpty()||BuildConfig.REWARDED_AD_UNIT.isEmpty())return;
        // No location permission or assumed consent for personalized advertising.
        YandexAds.setLocationTracking(false);
        YandexAds.setUserConsent(false);
        YandexAds.setAppAdAnalyticsReporting(false);
        YandexAds.enableLogging(BuildConfig.DEBUG);
        YandexAds.initialize(activity.getApplicationContext(),()->{
            if(destroyed)return;
            rewardedLoader=new RewardedAdLoader(activity);
            interstitialLoader=new InterstitialAdLoader(activity);
            loadRewarded();loadInterstitial();
        });
    }
    private boolean alive(){return !destroyed&&!activity.isFinishing()&&!activity.isDestroyed();}
    private void loadRewarded(){
        if(!alive()||rewardedLoader==null||rewardLoading||rewarded!=null)return;
        rewardLoading=true;
        rewardedLoader.loadAd(new AdRequest.Builder(BuildConfig.REWARDED_AD_UNIT).build(),new RewardedAdLoadListener(){
            public void onAdLoaded(RewardedAd ad){rewardLoading=false;if(!alive()){ad.setAdEventListener(null);return;}rewarded=ad;rewardFailures=0;}
            public void onAdFailedToLoad(AdRequestError error){rewardLoading=false;if(++rewardFailures<=3&&alive())handler.postDelayed(()->loadRewarded(),30_000L*rewardFailures);}
        });
    }
    private void loadInterstitial(){
        if(!alive()||interstitialLoader==null||interLoading||interstitial!=null)return;
        interLoading=true;
        interstitialLoader.loadAd(new AdRequest.Builder(BuildConfig.INTERSTITIAL_AD_UNIT).build(),new InterstitialAdLoadListener(){
            public void onAdLoaded(InterstitialAd ad){interLoading=false;if(!alive()){ad.setAdEventListener(null);return;}interstitial=ad;interFailures=0;}
            public void onAdFailedToLoad(AdRequestError error){interLoading=false;if(++interFailures<=3&&alive())handler.postDelayed(()->loadInterstitial(),30_000L*interFailures);}
        });
    }
    public boolean rewardedReady(){return alive()&&!showing&&rewarded!=null;}
    public boolean interstitialReady(){return alive()&&!showing&&interstitial!=null;}
    private void cancelWatchdog(){if(watchdog!=null){handler.removeCallbacks(watchdog);watchdog=null;}}
    public void showRewarded(Events events){
        if(!rewardedReady()){events.failed();return;}
        RewardedAd ad=rewarded;showing=true;
        ad.setAdEventListener(new RewardedAdEventListener(){
            boolean finished;
            private void finish(boolean success){if(finished||rewarded!=ad)return;finished=true;cancelWatchdog();ad.setAdEventListener(null);rewarded=null;showing=false;if(success)events.closed();else events.failed();if(alive())loadRewarded();}
            public void onAdShown(){if(finished||rewarded!=ad||!alive())return;cancelWatchdog();events.shown();}
            public void onRewarded(Reward reward){if(!finished&&rewarded==ad)events.rewarded();}
            public void onAdDismissed(){finish(true);}
            public void onAdFailedToShow(AdError error){finish(false);}
            public void onAdClicked(){}
            public void onAdImpression(ImpressionData data){}
        });
        watchdog=()->{ad.setAdEventListener(null);rewarded=null;showing=false;events.failed();if(alive())loadRewarded();};
        handler.postDelayed(watchdog,30_000);
        try{ad.show(activity);}catch(RuntimeException error){cancelWatchdog();ad.setAdEventListener(null);rewarded=null;showing=false;events.failed();if(alive())loadRewarded();}
    }
    public void showInterstitial(Events events){
        if(!interstitialReady()){events.failed();return;}
        InterstitialAd ad=interstitial;showing=true;
        ad.setAdEventListener(new InterstitialAdEventListener(){
            boolean finished;
            private void finish(boolean success){if(finished||interstitial!=ad)return;finished=true;cancelWatchdog();ad.setAdEventListener(null);interstitial=null;showing=false;if(success)events.closed();else events.failed();if(alive())loadInterstitial();}
            public void onAdShown(){if(finished||interstitial!=ad||!alive())return;cancelWatchdog();events.shown();}
            public void onAdDismissed(){finish(true);}
            public void onAdFailedToShow(AdError error){finish(false);}
            public void onAdClicked(){}
            public void onAdImpression(ImpressionData data){}
        });
        watchdog=()->{ad.setAdEventListener(null);interstitial=null;showing=false;events.failed();if(alive())loadInterstitial();};
        handler.postDelayed(watchdog,30_000);
        try{ad.show(activity);}catch(RuntimeException error){cancelWatchdog();ad.setAdEventListener(null);interstitial=null;showing=false;events.failed();if(alive())loadInterstitial();}
    }
    void close(){
        destroyed=true;handler.removeCallbacksAndMessages(null);
        if(rewardedLoader!=null)rewardedLoader.cancelLoading();if(interstitialLoader!=null)interstitialLoader.cancelLoading();
        if(rewarded!=null)rewarded.setAdEventListener(null);if(interstitial!=null)interstitial.setAdEventListener(null);
        rewarded=null;interstitial=null;
    }
}

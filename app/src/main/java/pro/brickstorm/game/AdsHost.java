package pro.brickstorm.game;

/** SDK boundary. Rewards are confirmed only by the advertising SDK callback. */
public interface AdsHost {
    interface Events { void shown(); void rewarded(); void closed(); void failed(); }
    boolean rewardedReady();
    boolean interstitialReady();
    void showRewarded(Events events);
    void showInterstitial(Events events);
    AdsHost NONE=new AdsHost(){
        public boolean rewardedReady(){return false;}
        public boolean interstitialReady(){return false;}
        public void showRewarded(Events e){e.failed();}
        public void showInterstitial(Events e){e.failed();}
    };
}

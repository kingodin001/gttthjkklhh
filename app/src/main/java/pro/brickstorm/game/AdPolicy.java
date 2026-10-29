package pro.brickstorm.game;

/** Two victories AND 180 real seconds; only the caller's result-screen transition can show. */
public final class AdPolicy {
    public static final long INTERVAL=180;
    private final AppModel.Storage storage;
    private boolean unlocked;
    private int wins;
    private long lastAd,lastSeen;
    AdPolicy(AppModel.Storage storage,boolean levelThreeCompleted){
        this.storage=storage;unlocked=storage.get("ads_unlocked",0)==1||levelThreeCompleted;
        wins=Math.min(2,Math.max(0,storage.get("ads_wins",unlocked?2:0)));
        lastAd=storage.getLong("ads_last",0);lastSeen=storage.getLong("ads_seen",0);
    }
    private long now(){lastSeen=Math.max(lastSeen,Math.max(0,storage.nowSeconds()));return lastSeen;}
    public void victory(int level){
        if(!unlocked){if(level!=3)return;unlocked=true;wins=2;}
        else wins=Math.min(2,wins+1);
        now();persist();
    }
    public boolean eligible(){return unlocked&&wins>=2&&(lastAd==0||now()-lastAd>=INTERVAL);}
    public void shown(boolean interstitial){if(interstitial)wins=0;lastAd=now();persist();}
    public void closed(){lastAd=now();persist();}
    private void persist(){storage.saveAdGate(unlocked,wins,lastAd,lastSeen);}
}

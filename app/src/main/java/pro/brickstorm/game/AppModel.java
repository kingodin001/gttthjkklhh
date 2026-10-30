package pro.brickstorm.game;
public final class AppModel implements Engine.Events {
    public interface Storage {
        int get(String key,int fallback);void put(String key,int value);void sound(int kind);
        default void openPrivacy(){}
        default void openAdPrivacy(){openPrivacy();}
        default long nowSeconds(){return System.currentTimeMillis()/1000L;}
        default long getLong(String key,long fallback){return ((long)get(key+"_hi",(int)(fallback>>>32))<<32)|(get(key+"_lo",(int)fallback)&0xffffffffL);}
        default void putLong(String key,long value){put(key+"_hi",(int)(value>>>32));put(key+"_lo",(int)value);}
        default void saveGiftState(int stock,long lastSeen,long[] claims){put("gift_stock",stock);putLong("gift_last_seen",lastSeen);for(int i=0;i<claims.length;i++)putLong("gift_claim_"+i,claims[i]);}
        default void saveRewardState(int stock,long lastSeen,long[] claims,long[] adClaims){saveGiftState(stock,lastSeen,claims);for(int i=0;i<adClaims.length;i++)putLong("gift_ad_claim_"+i,adClaims[i]);}
        default void saveChargeState(int freeStock,long adStock,long lastSeen,long[] claims){saveGiftState(freeStock,lastSeen,claims);putLong("gift_ad_stock",adStock);}
        default void saveAdGate(boolean unlocked,int wins,long last,long seen){put("ads_unlocked",unlocked?1:0);put("ads_wins",wins);putLong("ads_last",last);putLong("ads_seen",seen);}
    }
    public static final int PAGE_SIZE=16,PAGES=(Levels.COUNT+PAGE_SIZE-1)/PAGE_SIZE;
    public final Storage storage;public final Engine game;public String screen="home";public int page=0,last=1,speed=1;public boolean sound=true,effects=true,hasSession=false;
    public final int[] stars=new int[Levels.COUNT+1],best=new int[Levels.COUNT+1];
    public final RewardBank gifts;public boolean giftUsed=false;
    public final AdPolicy adPolicy;public AdsHost ads=AdsHost.NONE;
    public boolean adBusy=false,reviveUsed=false,shieldAdUsed=false;
    public float adShieldTime=0;public String rewardReturn="home",adMessage="";
    private long adSerial=0,attempt=0;private AdRequest request;
    private static final class AdRequest {long serial,attempt;String kind,navigation;boolean shown,earned,closed,failed;}
    // New campaign has its own score namespace. Existing classic results stay on disk.
    public AppModel(Storage s){
        storage=s;last=Levels.positionOf(s.get("level_last_id",s.get("route4_last",1)));sound=s.get("sound",1)==1;effects=s.get("effects",1)==1;
        for(int i=1;i<=Levels.COUNT;i++){
            int id=Levels.make(i).id();int oldStars=id<=58?s.get("route4_stars_"+id,0):0,oldBest=id<=58?s.get("route4_best_"+id,0):0;
            stars[i]=Math.max(0,Math.min(3,s.get("level_stars_"+id,oldStars)));best[i]=Math.max(0,s.get("level_best_"+id,oldBest));
        }
        // A level selected in the former all-open test build is not earned progress.
        if(!isUnlocked(last)){last=nextCampaignLevel();storage.put("level_last_id",Levels.make(last).id());}
        game=new Engine(this);gifts=new RewardBank(s);adPolicy=new AdPolicy(s,stars[3]>0);
    }
    public boolean isUnlocked(int n){return n>=1&&n<=Levels.COUNT&&(n==1||stars[n]>0||stars[n-1]>0);}
    public int nextCampaignLevel(){int n=1;while(n<Levels.COUNT&&stars[n]>0)n++;return n;}
    public int totalStars(){int sum=0;for(int i:stars)sum+=i;return sum;}
    public int completedCount(){int sum=0;for(int i:stars)if(i>0)sum++;return sum;}
    public void start(int n){if(adBusy||!isUnlocked(n))return;attempt++;last=n;storage.put("level_last_id",Levels.make(n).id());game.load(n);hasSession=true;speed=1;giftUsed=reviveUsed=shieldAdUsed=false;adShieldTime=0;adMessage="";screen="game";}
    public boolean canUseGift(){return screen.equals("game")&&game.state==Engine.State.PLAYING&&gifts.canSpend(!giftUsed)&&!game.balls.isEmpty()&&game.balls.size()<=Engine.MAX_BALLS/3;}
    public boolean useGift(){if(!canUseGift())return false;boolean usingFree=!giftUsed&&gifts.freeCharges()>0;if(!gifts.spend(!giftUsed))return false;game.power(1);if(usingFree)giftUsed=true;return true;}
    public void tick(float dt){if(adBusy)return;game.adShield=adShieldTime>0;for(int i=0;i<speed;i++)game.update(dt);if(game.state==Engine.State.PLAYING)adShieldTime=Math.max(0,adShieldTime-Math.max(0,dt));game.adShield=adShieldTime>0;}
    public void action(String a){
        if(adBusy)return;
        if(a.equals("adinfo")){screen="adinfo";return;}
        if(a.equals("privacy:open")){storage.openPrivacy();return;}
        if(a.equals("privacy:ads")){storage.openAdPrivacy();return;}
        if(a.equals("rewards")){rewardReturn=screen;menu();screen="rewards";return;}
        if(a.equals("rewardback")){screen=rewardReturn;return;}
        if(a.startsWith("ad:")){requestReward(a.substring(3));return;}
        if(screen.equals("game")&&game.state==Engine.State.WON&&(a.equals("next")||a.equals("retry")||a.equals("levels")||a.equals("home"))&&adPolicy.eligible()&&ads.interstitialReady()){
            beginAd("interstitial",a);return;
        }
        executeAction(a);
    }
    private void executeAction(String a){
        if(a.startsWith("level:")){start(Integer.parseInt(a.substring(6)));return;}
        switch(a){
            case "gift:claim":if(screen.equals("home")&&gifts.claim())sound(3);break;
            case "gift:use":useGift();break;
            case "play":if(hasSession&&game.state!=Engine.State.LOST&&game.state!=Engine.State.WON){screen="game";if(game.state==Engine.State.PAUSED)game.togglePause();}else start(last);break;
            case "levels":if(game.state==Engine.State.PLAYING||game.state==Engine.State.READY)game.togglePause();screen="levels";page=(last-1)/PAGE_SIZE;break;
            case "settings":screen="settings";break;
            case "progress":screen="progress";break;
            case "giftinfo":screen="giftinfo";break;
            case "home":menu();break;
            case "pause":game.togglePause();break;
            case "resume":if(game.state==Engine.State.PAUSED)game.togglePause();break;
            case "retry":start(game.level.number);break;
            case "next":if(game.state==Engine.State.WON){if(game.level.number<Levels.COUNT)start(game.level.number+1);else{screen="levels";page=PAGES-1;}}break;
            case "prevpage":page=Math.max(0,page-1);break;
            case "nextpage":page=Math.min(PAGES-1,page+1);break;
            case "sound":sound=!sound;storage.put("sound",sound?1:0);break;
            case "effects":effects=!effects;storage.put("effects",effects?1:0);break;
            case "speed1":speed=1;break;
            case "speed2":speed=2;break;
            case "speed4":speed=4;break;
        }
    }
    public void menu(){if(game.state==Engine.State.PLAYING||game.state==Engine.State.READY)game.togglePause();screen="home";}
    public void back(){if(adBusy)return;if(screen.equals("game")&&game.state==Engine.State.WON){action("home");return;}if(screen.equals("rewards")){screen=rewardReturn;return;}if(screen.equals("game")&&(game.state==Engine.State.PLAYING||game.state==Engine.State.READY))game.togglePause();else menu();}
    @Override public void sound(int kind){if(sound)storage.sound(kind);}
    @Override public void completed(int level,int value,int score){if(level<1||level>Levels.COUNT||value<1)return;hasSession=false;stars[level]=Math.max(stars[level],Math.min(3,value));best[level]=Math.max(best[level],score);storage.put("level_stars_"+Levels.make(level).id(),stars[level]);storage.put("level_best_"+Levels.make(level).id(),best[level]);last=Math.min(Levels.COUNT,level+1);storage.put("level_last_id",Levels.make(last).id());adPolicy.victory(level);}
    public boolean rewardEligible(String kind){
        switch(kind){
            case "charge":return gifts.canClaimAd();
            case "revive":return hasSession&&!reviveUsed&&game.state==Engine.State.LOST;
            case "shield":return hasSession&&!shieldAdUsed&&(game.state==Engine.State.READY||game.state==Engine.State.PLAYING||game.state==Engine.State.PAUSED);
            default:return false;
        }
    }
    public boolean requestReward(String kind){
        if(adBusy||!rewardEligible(kind))return false;
        if(!ads.rewardedReady()){adMessage="Реклама пока недоступна. Попробуй позже.";return false;}
        beginAd(kind,null);return true;
    }
    private void beginAd(String kind,String navigation){
        AdRequest r=new AdRequest();r.serial=++adSerial;r.attempt=attempt;r.kind=kind;r.navigation=navigation;request=r;adBusy=true;adMessage="";
        if(game.state==Engine.State.PLAYING||game.state==Engine.State.READY)game.togglePause();
        AdsHost.Events events=new AdsHost.Events(){
            private boolean current(){return request==r&&r.serial==adSerial&&!r.failed;}
            public void shown(){if(!current()||r.shown||r.closed)return;r.shown=true;adPolicy.shown(kind.equals("interstitial"));}
            public void rewarded(){
                if(!current()||!r.shown||r.earned||kind.equals("interstitial"))return;r.earned=true;
                if(kind.equals("charge")){if(gifts.claimAd())adMessage="Заряд ×3 добавлен в запас.";return;}
                if(r.attempt!=attempt)return;
                if(kind.equals("revive")&&rewardEligible(kind)&&game.revive()){reviveUsed=true;game.notice="Ещё одна жизнь. Коснись поля для запуска.";game.noticeTime=3;adMessage="Ещё одна жизнь. Коснись поля для запуска.";screen="game";}
                else if(kind.equals("shield")&&rewardEligible(kind)){shieldAdUsed=true;adShieldTime=10;game.adShield=true;adMessage="Защитная стена: 10 секунд игры.";}
            }
            public void closed(){if(!current()||r.closed)return;r.closed=true;adBusy=false;if(r.shown)adPolicy.closed();if(navigation!=null)executeAction(navigation);}
            public void failed(){if(!current()||r.closed)return;r.failed=true;r.closed=true;adBusy=false;if(r.shown)adPolicy.closed();if(!r.earned)adMessage="Реклама недоступна. Можно играть дальше.";if(navigation!=null)executeAction(navigation);}
        };
        try{if(kind.equals("interstitial"))ads.showInterstitial(events);else ads.showRewarded(events);}catch(RuntimeException error){events.failed();}
    }
}

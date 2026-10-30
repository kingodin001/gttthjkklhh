package pro.brickstorm.game;
import java.util.*;

/** Real model/engine with a controllable SDK boundary: no ad request or wall-clock sleep. */
public final class AdsTest {
    static int checks;
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    static final class Clock implements AppModel.Storage {
        final Map<String,Integer> data=new HashMap<>();long now=2_200_000_000L;int privacyRequests,adPrivacyRequests;
        public void openPrivacy(){privacyRequests++;}public void openAdPrivacy(){adPrivacyRequests++;}
        public int get(String k,int fallback){return data.getOrDefault(k,fallback);}public void put(String k,int n){data.put(k,n);}public void sound(int k){}public long nowSeconds(){return now;}
    }
    static final class Host implements AdsHost {
        boolean ready=true,fail=false;int rewardRequests,interRequests;Events event;
        public boolean rewardedReady(){return ready;}public boolean interstitialReady(){return ready;}
        public void showRewarded(Events e){rewardRequests++;event=e;if(fail)e.failed();}
        public void showInterstitial(Events e){interRequests++;event=e;if(fail)e.failed();}
        void watch(){event.shown();event.rewarded();event.closed();}
    }
    static void win(AppModel a){a.game.launch();a.game.remaining=0;a.tick(.01f);check(a.game.state==Engine.State.WON,"Fixture reaches the engine completion callback");}
    public static void main(String[] args){
        Clock clock=new Clock();AppModel a=new AppModel(clock);Host host=new Host();a.ads=host;
        for(int n=1;n<=2;n++){a.start(n);win(a);a.action("next");check(host.interRequests==0&&!a.adBusy,"No ordinary ad before third victory");}
        win(a);a.action("next");check(host.interRequests==1&&a.adBusy&&a.game.level.number==3,"First ordinary ad waits at level-three result transition");
        a.action("retry");a.start(1);check(a.game.level.number==3,"Touches cannot change attempts underneath an ad");
        host.event.shown();host.event.rewarded();check(a.gifts.charges()==0,"Interstitial cannot grant a rewarded bonus");host.event.closed();host.event.closed();check(a.game.level.number==4&&!a.adBusy,"Dismissal navigates exactly once");
        win(a);a.action("next");check(host.interRequests==1,"One completed level is not enough");win(a);clock.now+=179;a.action("next");check(host.interRequests==1&&a.game.level.number==6,"Two wins cannot bypass the 180-second condition");
        a.action("speed4");for(int i=0;i<30;i++)a.tick(.01f);check(!a.adPolicy.eligible(),"Speed ×4 never advances real ad time");clock.now++;win(a);check(host.interRequests==1,"Timer expiration never starts a midgame ad");a.action("home");check(host.interRequests==2,"Next natural result transition may show once both conditions hold");host.event.shown();clock.now+=25;host.event.closed();
        AppModel restored=new AppModel(clock);check(!restored.adPolicy.eligible(),"Cooldown and victory count survive process restart");clock.now-=1000;check(!restored.adPolicy.eligible(),"Clock rollback cannot bypass cooldown");clock.now+=1000;
        Clock rc=new Clock();AppModel r=new AppModel(rc);Host rh=new Host();r.ads=rh;
        check(r.requestReward("charge")&&r.adBusy&&r.gifts.charges()==0,"Starting a rewarded ad never grants the bonus");rh.event.shown();rh.event.closed();check(r.gifts.charges()==0,"Closing without SDK reward grants nothing");
        check(r.requestReward("charge"),"Retry a voluntarily requested ad");rh.event.shown();rh.event.rewarded();rh.event.rewarded();rh.event.closed();check(r.gifts.charges()==1&&r.gifts.adCharges()==1,"One confirmed view grants one charge despite duplicate callbacks");
        check(r.gifts.canClaim(),"Ad reward does not consume the free four-hour gift");r.action("gift:claim");check(r.gifts.charges()==2,"Free and ad rewards share the same bank");
        r.requestReward("charge");rh.watch();check(r.gifts.charges()==3&&r.gifts.canClaimAd(),"Inventory of three never blocks an ad reward");
        r.requestReward("charge");rh.watch();check(r.gifts.charges()==4&&r.gifts.adCharges()==3,"Another confirmed video grows inventory beyond three without waiting");
        AppModel bank=new AppModel(rc);check(bank.gifts.charges()==4&&bank.gifts.canClaimAd(),"Unlimited advertising bank persists without a daily quota");
        Clock vc=new Clock();AppModel v=new AppModel(vc);Host vh=new Host();v.ads=vh;v.start(1);v.game.launch();v.game.cells[100]=0;v.game.score=123;v.game.broken=25;v.game.state=Engine.State.LOST;v.game.lives=0;v.giftUsed=true;
        int[] cells=v.game.cells.clone();check(v.requestReward("revive"),"Lost attempt may request one continuation");vh.watch();check(v.game.state==Engine.State.READY&&v.game.lives==1&&v.game.score==123&&Arrays.equals(cells,v.game.cells),"Continuation retains brick damage and score and waits for touch");
        check(v.reviveUsed&&v.giftUsed&&!v.requestReward("revive"),"Revival never resets gift usage or enables another revive");v.game.state=Engine.State.LOST;check(!v.requestReward("revive"),"Second loss cannot buy another continuation in the same attempt");
        v.action("retry");check(!v.reviveUsed&&!v.giftUsed,"Only a new attempt resets usage");v.game.launch();v.action("pause");v.action("rewards");check(v.requestReward("shield"),"Shield can be requested from the paused rewards menu");vh.watch();check(v.adShieldTime==10&&v.shieldAdUsed&&v.game.adShield,"Confirmed video activates exactly one ten-second wall");v.tick(2);check(v.adShieldTime==10,"Reward menu and pause do not spend protection time");v.action("rewardback");v.action("resume");v.action("speed4");v.tick(.25f);check(Math.abs(v.adShieldTime-9.75f)<.001f,"×4 does not shorten the ad shield");
        v.game.balls.clear();Engine.Ball b=new Engine.Ball(200,Engine.SHIELD_Y-8,0,200);v.game.balls.add(b);v.tick(.05f);check(b.vy<0&&v.game.lives==3,"Ad wall physically reflects a ball below the paddle");check(!v.requestReward("shield"),"Only one wall reward per attempt");
        v.tick(20);check(v.adShieldTime==0&&!v.game.adShield,"Protection expires");
        Clock fc=new Clock();AppModel f=new AppModel(fc);Host fh=new Host();f.ads=fh;fh.ready=false;check(!f.requestReward("charge")&&!f.adBusy,"No fill leaves game usable and grants nothing");fh.ready=true;fh.fail=true;f.requestReward("charge");check(!f.adBusy&&f.gifts.charges()==0,"Show failure grants nothing and unlocks input");
        for(int n=1;n<=3;n++){f.start(n);win(f);}f.action("next");check(f.game.level.number==4&&!f.adBusy,"Failed interstitial still transitions without blocking the player");
        fh.fail=false;f.action("home");f.requestReward("charge");EventsHolder old=new EventsHolder(fh.event);fh.event.shown();fh.event.closed();f.requestReward("charge");old.e.rewarded();check(f.gifts.charges()==0,"Old request cannot reward a later request");fh.watch();check(f.gifts.charges()==1,"Current request still rewards normally");
        check(!f.adPolicy.eligible(),"Voluntary video restarts ordinary-ad cooldown");
        System.out.println("PASS: "+checks+" ad flow checks: 3rd victory, 2 wins AND 180 seconds, restart, callback idempotency, unlimited charge rewards, continuation, shield physics and no-fill.");
    }
    static final class EventsHolder {final AdsHost.Events e;EventsHolder(AdsHost.Events e){this.e=e;}}
}

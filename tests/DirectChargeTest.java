package pro.brickstorm.game;
import java.util.Arrays;

/** Exercise real rendered touch targets and SDK lifecycle together, without a device or live ad. */
public final class DirectChargeTest {
    static int checks;
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    static void draw(GameRenderer r,AppModel a,String name)throws Exception{AdsRenderCheck.snapshot(r,a,name);}
    public static void main(String[] args)throws Exception{
        AdsTest.Clock clock=new AdsTest.Clock();AppModel a=new AppModel(clock);AdsTest.Host host=new AdsTest.Host();a.ads=host;GameRenderer r=new GameRenderer();
        draw(r,a,"direct-home-free");check("gift:claim".equals(r.hit(234,692)),"Free gift remains directly accessible");check("ad:charge".equals(r.hit(322,692)),"Home video directly requests a charge, without information/rewards menus");
        a.action(r.hit(234,692));draw(r,a,"direct-home-cooldown");check(a.gifts.charges()==1&&r.hit(234,692)==null&&"ad:charge".equals(r.hit(322,692)),"Free cooldown never hides the video action");
        a.action(r.hit(322,692));check(a.adBusy&&host.rewardRequests==1&&a.screen.equals("home")&&a.gifts.charges()==1,"One home tap starts video, without early reward or navigation");host.watch();check(a.gifts.charges()==2&&a.screen.equals("home"),"Home video credits the shared bank");
        a.start(1);int[] cells=a.game.cells.clone();draw(r,a,"direct-game-ready");check("ad:charge".equals(r.hit(346,786)),"Video is visible before launching");check(r.hit(268,786)==null,"Use waits for a flying ball");
        a.game.launch();draw(r,a,"direct-game-playing");check("gift:use".equals(r.hit(268,786))&&"ad:charge".equals(r.hit(346,786)),"Use and video are separate game buttons");check("speed4".equals(r.hit(210,786))&&"levels".equals(r.hit(56,786)),"Video does not overlap existing controls");
        a.action(r.hit(346,786));check(a.adBusy&&a.game.state==Engine.State.PAUSED&&a.screen.equals("game"),"One game tap pauses safely and starts video in place");host.watch();check(a.gifts.charges()==3&&Arrays.equals(cells,a.game.cells),"Video preserves the level and earns a stored charge");draw(r,a,"direct-game-full-paused");check(r.hit(346,786)==null,"Full stock does not start an unrewardable video");
        a.action("resume");int before=a.game.balls.size();a.action("gift:use");check(a.game.balls.size()==before*3&&a.gifts.charges()==2,"Separate use button multiplies all flying balls");
        a.action("pause");draw(r,a,"direct-game-paused");check("ad:charge".equals(r.hit(346,786)),"Video remains accessible while paused");
        host.ready=false;a.action("resume");draw(r,a,"direct-game-loading");a.action(r.hit(346,786));check(!a.adBusy&&a.game.state==Engine.State.PLAYING&&!a.adMessage.isEmpty()&&host.rewardRequests==2,"No-fill explains unavailability without freezing active play or issuing a request");
        host.ready=true;
        while(a.gifts.adClaimsLeft()>0){a.gifts.spend();a.requestReward("charge");host.watch();}
        a.gifts.spend();a.screen="home";draw(r,a,"direct-home-daily-limit");check(r.hit(322,692)==null&&a.gifts.adWaitSeconds()==RewardBank.DAY,"Daily cap shows exact real-time wait rather than an actionable ad");
        AppModel restored=new AppModel(clock);check(restored.gifts.adCountdown().equals("24:00:00"),"Daily countdown survives restart");clock.now+=61;check(restored.gifts.adCountdown().equals("23:58:59"),"Countdown tracks elapsed real seconds");clock.now+=RewardBank.DAY;check(restored.gifts.adWaitSeconds()==0&&restored.gifts.canClaimAd(),"Daily video becomes available again");
        for(int n=1;n<=Levels.COUNT;n++)a.stars[n]=1;
        for(int n:new int[]{1,8,151,301}){a.start(n);draw(r,a,"direct-level-"+n);check("ad:charge".equals(r.hit(346,786)),"Direct charge works in campaign level "+n);}
        System.out.println("PASS: "+checks+" direct-charge checks: home/free cooldown, real button-to-ad flow, live level preservation, all-ball ×3, pause, no-fill, stock, daily timer, restart and first/middle/final levels.");
    }
}

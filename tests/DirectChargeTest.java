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
        a.action(r.hit(346,786));check(a.adBusy&&a.game.state==Engine.State.PAUSED&&a.screen.equals("game"),"One game tap pauses safely and starts video in place");host.watch();check(a.gifts.charges()==3&&Arrays.equals(cells,a.game.cells),"Video preserves the level and earns a stored charge");draw(r,a,"direct-game-full-paused");check("ad:charge".equals(r.hit(346,786)),"Stock of three must keep the video actionable");
        a.action("resume");int before=a.game.balls.size();a.action("gift:use");check(a.game.balls.size()==before*3&&a.gifts.charges()==2,"Separate use button multiplies all flying balls");
        a.action("pause");draw(r,a,"direct-game-paused");check("ad:charge".equals(r.hit(346,786)),"Video remains accessible while paused");
        host.ready=false;a.action("resume");draw(r,a,"direct-game-loading");a.action(r.hit(346,786));check(!a.adBusy&&a.game.state==Engine.State.PLAYING&&!a.adMessage.isEmpty()&&host.rewardRequests==2,"No-fill explains unavailability without freezing active play or issuing a request");
        host.ready=true;
        long stock=a.gifts.charges();
        for(int i=0;i<1000;i++){check(a.requestReward("charge"),"No daily/stock limit on video "+i);host.watch();}
        a.screen="home";draw(r,a,"direct-home-unlimited");check("ad:charge".equals(r.hit(322,692))&&a.gifts.charges()==stock+1000,"1000 same-day views increase inventory and keep home video available");
        AppModel restored=new AppModel(clock);check(restored.gifts.charges()==a.gifts.charges()&&restored.gifts.canClaimAd(),"Large advertising inventory survives restart without clamping");
        a.screen="game";a.action("resume");
        for(int i=0;i<12;i++){a.game.balls.clear();a.game.balls.add(new Engine.Ball(200,600,0,-200));long beforeStock=a.gifts.charges();check(a.useGift()&&a.game.balls.size()==3&&a.gifts.charges()==beforeStock-1,"Stored advertising charges are usable repeatedly in the same attempt");}
        check(a.giftUsed,"Repeated advertising use does not reset the used free gift flag");
        a.action("pause");draw(r,a,"direct-game-unlimited-stock");check("ad:charge".equals(r.hit(346,786)),"Large inventory keeps paused video available");
        for(int n=1;n<=Levels.COUNT;n++)a.stars[n]=1;
        for(int n:new int[]{1,8,151,301}){a.start(n);draw(r,a,"direct-level-"+n);check("ad:charge".equals(r.hit(346,786)),"Direct charge works in campaign level "+n);}
        System.out.println("PASS: "+checks+" direct-charge checks: home/free cooldown, real button-to-ad flow, live level preservation, all-ball ×3, pause, no-fill, unlimited 1000-view inventory, repeated use, restart and first/middle/final levels.");
    }
}

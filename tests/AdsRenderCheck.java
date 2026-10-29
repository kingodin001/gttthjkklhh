package pro.brickstorm.game;
import javax.imageio.ImageIO;import java.io.File;
public final class AdsRenderCheck {
    static void snapshot(GameRenderer renderer,AppModel a,String name)throws Exception{RenderCheck p=new RenderCheck();renderer.draw(p,a);ImageIO.write(p.image,"png",new File("verification/"+name+".png"));}
    public static void main(String[] args)throws Exception{
        AppModel a=new AppModel(new AdsTest.Clock());AdsTest.Host host=new AdsTest.Host();a.ads=host;GameRenderer r=new GameRenderer();
        a.start(1);a.game.launch();a.action("pause");snapshot(r,a,"ad-pause");if(!"rewards".equals(r.hit(200,610)))throw new AssertionError("Pause reward menu missing");
        a.action("rewards");snapshot(r,a,"ad-rewards");if(!"ad:charge".equals(r.hit(200,275))||!"ad:shield".equals(r.hit(200,490)))throw new AssertionError("Reward cards unavailable");
        a.action("ad:shield");snapshot(r,a,"ad-busy");if(r.hit(200,275)!=null)throw new AssertionError("Ad busy screen must not accept input");host.watch();snapshot(r,a,"ad-shield-used");if(r.hit(200,490)!=null)throw new AssertionError("Used shield remains clickable");
        a.action("rewardback");a.game.state=Engine.State.LOST;snapshot(r,a,"ad-continue");if(!"ad:revive".equals(r.hit(200,365)))throw new AssertionError("Continuation ad button missing");
        host.ready=false;snapshot(r,a,"ad-offline");if(r.hit(200,365)!=null||!"retry".equals(r.hit(200,468)))throw new AssertionError("No-fill must preserve free retry");
        a.action("adinfo");snapshot(r,a,"ad-info");if(!"privacy:open".equals(r.hit(200,575)))throw new AssertionError("Privacy information link missing");
        System.out.println("PASS: reward, continuation, used/disabled/no-fill, privacy and ad input-blocking controls; shared Android layout rendered.");
    }
}

package pro.brickstorm.game;
import java.util.*;

/** Reward rules with an injectable real-world clock, no sleep or network. */
public final class RewardTest {
    static int checks;
    static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    static final class ClockStorage implements AppModel.Storage {
        final Map<String,Integer> data=new HashMap<>();long now=2_200_000_000L;
        public int get(String key,int fallback){return data.getOrDefault(key,fallback);}
        public void put(String key,int value){data.put(key,value);}
        public void sound(int kind){}
        public long nowSeconds(){return now;}
    }
    static AppModel collected(ClockStorage s){AppModel a=new AppModel(s);a.action("gift:claim");return a;}
    public static void main(String[] args){
        ClockStorage s=new ClockStorage();long first=s.now;AppModel a=collected(s);
        check(a.gifts.charges()==1,"First gift is claimable immediately");
        a.action("gift:claim");check(a.gifts.charges()==1,"Double tap cannot claim twice");
        check(a.gifts.waitSeconds()==14400&&a.gifts.countdown().equals("04:00:00"),"Four-hour real-world countdown");
        s.now=first+14399;check(!a.gifts.canClaim(),"One second before cooldown");s.now++;
        a.action("gift:claim");check(a.gifts.charges()==2,"Second claim exactly after four hours");
        a=new AppModel(s);check(a.gifts.charges()==2&&!a.gifts.canClaim(),"Inventory and cooldown survive app restart, including post-2038 timestamps");
        s.now+=14400;a.action("gift:claim");check(a.gifts.charges()==3&&a.gifts.claimsLeft()==0,"Three claims in rolling 24 hours");
        check(!a.gifts.canClaim(),"Full inventory is capped at three");
        a.start(20);check(!a.useGift()&&a.gifts.charges()==3,"Ready ball does not consume a gift");a.game.launch();
        a.game.balls.clear();a.game.balls.add(new Engine.Ball(60,580,100,-180));a.game.balls.add(new Engine.Ball(240,610,-70,210));
        check(a.useGift()&&a.game.balls.size()==6&&a.gifts.charges()==2,"Gift triples EVERY flying ball and consumes one charge");
        for(float x:new float[]{60,240}){int copies=0;for(Engine.Ball b:a.game.balls)if(b.x==x)copies++;check(copies==3,"Gift copies each flying ball at its own position");}
        check(!a.useGift()&&a.gifts.charges()==2,"Only one gift per attempt");
        a.action("home");a.action("play");check(!a.useGift(),"Menu and resume cannot reset the attempt limit");
        a.game.balls.clear();a.game.update(.02f);a.game.launch();check(!a.useGift(),"Losing one life does not reset gift usage");
        int before=a.game.balls.size();a.game.power(1);check(a.game.balls.size()==before*3,"Dropped yellow bonus still works after gift use");
        a.action("retry");a.game.launch();check(a.useGift()&&a.gifts.charges()==1,"New attempt permits one more stored charge");
        a.action("retry");a.game.launch();a.action("pause");check(!a.useGift()&&a.gifts.charges()==1,"Paused gameplay cannot spend charge");a.action("resume");
        a.game.balls.clear();for(int i=0;i<Engine.MAX_BALLS/3+1;i++)a.game.balls.add(new Engine.Ball(200,600,100,-150));
        check(!a.useGift()&&a.gifts.charges()==1,"Do not charge for a multiplication that cannot fit the physical limit");
        a.game.balls.clear();a.game.balls.add(new Engine.Ball(200,600,100,-150));check(a.useGift()&&a.gifts.charges()==0,"Last charge spends normally");
        check(!a.useGift()&&a.gifts.charges()==0,"Inventory never goes negative");
        a.action("home");s.now=first+86400-1;check(!a.gifts.canClaim(),"Three gifts do not reset at midnight or before 24 hours");s.now++;
        check(a.gifts.canClaim()&&a.gifts.claimsLeft()==1,"Oldest claim expires exactly at 24 hours");a.action("gift:claim");check(a.gifts.charges()==1&&a.gifts.claimsLeft()==0,"Rolling-day limit applies after next claim");
        a=new AppModel(s);check(a.gifts.charges()==1&&!a.gifts.canClaim(),"Restart cannot replenish daily allowance");
        s.now-=86400;check(!a.gifts.canClaim()&&a.gifts.waitSeconds()>0,"Clock rollback cannot grant an early gift");
        a.start(20);a.game.launch();a.action("speed4");long wait=a.gifts.waitSeconds();for(int i=0;i<100;i++)a.tick(.01f);check(a.gifts.waitSeconds()==wait,"Speed ×4 never speeds up gift cooldown");
        a.game.state=Engine.State.WON;check(!a.useGift(),"Win screen cannot spend charge");a.game.state=Engine.State.LOST;check(!a.useGift(),"Loss screen cannot spend charge");
        ClockStorage full=new ClockStorage();AppModel f=collected(full);full.now+=14400;f.action("gift:claim");full.now+=14400;f.action("gift:claim");full.now+=86400;
        check(!f.gifts.claim()&&f.gifts.charges()==3,"Full inventory blocks new gifts even when cooldown is over");
        f.start(1);f.game.launch();check(f.useGift()&&f.gifts.charges()==2,"Stored gifts can be used after a day");f.action("home");f.action("gift:claim");check(f.gifts.charges()==3,"One free inventory slot can receive an available gift");
        ClockStorage upgrade=new ClockStorage();upgrade.data.put("level_stars_20200",3);upgrade.data.put("level_best_20200",999);upgrade.data.put("level_last_id",20200);AppModel upgraded=new AppModel(upgrade);
        check(upgraded.stars[200]==3&&upgraded.best[200]==999&&upgraded.last==200,"First 200 scores and resume survive expansion to 300");
        upgraded.start(300);check(upgraded.game.level.number==300,"All new levels are directly available");
        System.out.println("PASS: "+checks+" reward checks: cooldown, rolling daily cap, capacity, restart/upgrade, clocks, pause, all-ball multiplication, attempt limit and no-waste behavior.");
    }
}

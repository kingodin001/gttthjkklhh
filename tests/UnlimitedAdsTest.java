package pro.brickstorm.game;
/** No live impressions: exercise reward callbacks, migration, independent free rules and large balances. */
public final class UnlimitedAdsTest {
    static int checks;static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        AdsTest.Clock clock=new AdsTest.Clock();
        clock.put("gift_stock",3);for(int i=0;i<3;i++)clock.putLong("gift_ad_claim_"+i,clock.now);
        AppModel a=new AppModel(clock);AdsTest.Host host=new AdsTest.Host();a.ads=host;
        check(a.gifts.freeCharges()==3&&a.gifts.charges()==3&&a.gifts.canClaimAd(),"Legacy balance is retained; old full inventory and daily ad history do not block new videos");
        for(int i=0;i<1000;i++){
            check(a.requestReward("charge"),"View allowed without clock advance: "+i);
            check(!a.requestReward("charge"),"One fullscreen request at a time");
            host.event.shown();host.event.rewarded();host.event.rewarded();host.event.closed();
        }
        check(a.gifts.adCharges()==1000&&a.gifts.charges()==1003,"One reward per completed view; no quota or storage cap");
        AppModel restart=new AppModel(clock);check(restart.gifts.adCharges()==1000&&restart.gifts.freeCharges()==3,"Both balances survive restart independently");
        a.start(1);a.game.launch();check(a.useGift()&&a.giftUsed&&a.gifts.freeCharges()==2,"First free charge retains the old one-per-attempt rule");
        a.game.balls.clear();a.game.balls.add(new Engine.Ball(200,600,0,-200));check(a.useGift()&&a.gifts.adCharges()==999&&a.gifts.freeCharges()==2,"A used free gift does not block advertising charges");
        a.game.balls.clear();a.game.balls.add(new Engine.Ball(200,600,0,-200));check(a.useGift()&&a.gifts.adCharges()==998,"Another stored ad charge is usable in the same level");
        a.game.balls.clear();for(int i=0;i<Engine.MAX_BALLS/3+1;i++)a.game.balls.add(new Engine.Ball(200,600,0,-200));long balance=a.gifts.charges();check(!a.useGift()&&a.gifts.charges()==balance,"Original physical ball safety limit does not waste a charge");
        a.screen="home";host.ready=false;check(!a.requestReward("charge")&&a.gifts.charges()==balance,"No-fill gives no reward and spends nothing");host.ready=true;
        a.requestReward("charge");host.event.shown();host.event.closed();check(a.gifts.charges()==balance,"Dismissal without completion earns nothing");
        AdsTest.Clock freeClock=new AdsTest.Clock();AppModel f=new AppModel(freeClock);f.action("gift:claim");check(f.gifts.freeCharges()==1&&f.gifts.waitSeconds()==14400,"Free four-hour cooldown unchanged");
        freeClock.now+=14400;f.action("gift:claim");freeClock.now+=14400;f.action("gift:claim");check(f.gifts.freeCharges()==3&&!f.gifts.canClaim()&&f.gifts.claimsLeft()==0,"Free stock and rolling daily cap stay at three");
        freeClock.putLong("gift_ad_stock",5_000_000_000L);AppModel large=new AppModel(freeClock);check(large.gifts.adCharges()==5_000_000_000L&&large.gifts.charges()==5_000_000_003L,"Large ad stock uses 64-bit storage without wrapping or clamping");large.gifts.claimAd();check(new AppModel(freeClock).gifts.adCharges()==5_000_000_001L,"Another view persists beyond 32-bit limits");
        System.out.println("PASS: "+checks+" unlimited-ad checks: 1000 same-day views, duplicate callbacks, migration, repeat use, independent free gifts, no-fill and 64-bit persistence.");
    }
}

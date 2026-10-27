package pro.brickstorm.game;

/** Production access rules, including migration from the temporary all-open build. */
public final class ProgressionTest {
    static int checks;
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    static void win(AppModel app){app.game.launch();app.game.remaining=0;app.tick(.01f);check(app.game.state==Engine.State.WON,"Engine emits a real win callback");}
    public static void main(String[] args){
        EngineTest.Memory storage=new EngineTest.Memory();storage.put("unlocked",300);storage.put("level_last_id",Levels.make(250).id());
        AppModel fresh=new AppModel(storage);
        check(fresh.last==1&&fresh.isUnlocked(1),"Selected test level and legacy unlock flag cannot unlock the campaign");
        for(int n=2;n<=Levels.COUNT;n++)check(!fresh.isUnlocked(n),"Fresh install locks level "+n);
        check(!fresh.isUnlocked(0)&&!fresh.isUnlocked(301),"Access boundaries");
        fresh.action("level:250");check(fresh.screen.equals("home")&&!fresh.hasSession,"Direct locked-card action is rejected");
        fresh.action("play");check(fresh.game.level.number==1,"Play starts the first earned level");
        fresh.action("next");check(fresh.game.level.number==1&&fresh.game.state==Engine.State.READY,"Next cannot skip a ready level");
        fresh.game.launch();fresh.game.score=99;fresh.action("home");fresh.action("levels");
        check(!fresh.isUnlocked(2),"Playing, score, menu and level picker do not grant access");
        AppModel restart=new AppModel(storage);check(restart.last==1&&!restart.isUnlocked(2),"Restart cannot unlock unfinished levels");
        restart.start(1);restart.game.state=Engine.State.LOST;restart.action("next");check(!restart.isUnlocked(2),"Loss cannot unlock next");
        restart.action("retry");restart.game.launch();restart.action("pause");restart.action("next");check(!restart.isUnlocked(2),"Pause cannot skip a level");
        restart.action("resume");restart.game.remaining=0;restart.tick(.01f);
        check(restart.stars[1]>0&&restart.isUnlocked(2)&&!restart.isUnlocked(3),"Win unlocks exactly one following level");
        restart=new AppModel(storage);check(restart.isUnlocked(1)&&restart.isUnlocked(2)&&!restart.isUnlocked(3)&&restart.last==2,"Win and access survive restart");
        restart.start(2);restart.game.score=321;restart.giftUsed=true;restart.action("level:300");
        check(restart.game.level.number==2&&restart.game.score==321&&restart.giftUsed,"Locked launch cannot reset an ongoing attempt");
        restart.start(1);int previousStars=restart.stars[1];restart.completed(1,1,1);
        check(restart.stars[1]==previousStars&&!restart.isUnlocked(3),"Replay preserves best stars without opening an extra level");
        EngineTest.Memory sparse=new EngineTest.Memory();sparse.put("level_stars_"+Levels.make(107).id(),2);sparse.put("level_best_"+Levels.make(107).id(),777);sparse.put("level_last_id",Levels.make(109).id());sparse.put("gift_stock",2);
        AppModel migrated=new AppModel(sparse);
        check(migrated.stars[107]==2&&migrated.best[107]==777&&migrated.gifts.charges()==2,"Upgrade keeps earned scores and gifts");
        check(migrated.isUnlocked(107)&&migrated.isUnlocked(108)&&!migrated.isUnlocked(106)&&!migrated.isUnlocked(109),"Sparse old wins retain themselves and their next levels without opening gaps");
        check(migrated.last==1,"Old selected locked level returns to the first unfinished campaign level");
        migrated.start(108);migrated=new AppModel(sparse);check(migrated.last==108,"Earned high-level selection remains valid across restart");
        migrated.completed(1,0,10);check(!migrated.isUnlocked(2),"No stars means no completion");
        AppModel campaign=new AppModel(new EngineTest.Memory());
        for(int n=1;n<=Levels.COUNT;n++){
            campaign.start(n);check(campaign.game.level.number==n,"Sequential start "+n);win(campaign);
            check(campaign.isUnlocked(n)&&campaign.stars[n]>0,"Completed level remains replayable "+n);
            if(n<Levels.COUNT){check(campaign.isUnlocked(n+1),"Next is earned "+n);if(n+2<=Levels.COUNT)check(!campaign.isUnlocked(n+2),"No multi-level skip "+n);}
            campaign.action("next");if(n<Levels.COUNT)check(campaign.game.level.number==n+1,"Win next button "+n);
        }
        check(campaign.screen.equals("levels")&&campaign.page==AppModel.PAGES-1&&campaign.last==300,"Final victory returns to the last map page, never level 301");
        AppModel finished=new AppModel(campaign.storage);check(finished.completedCount()==300&&finished.isUnlocked(300),"Entire earned campaign survives restart");
        System.out.println("PASS: "+checks+" progression checks: locked starts, no skip, engine win events, replay, restart, sparse migration, gifts and final level.");
    }
}

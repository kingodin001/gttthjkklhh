package pro.brickstorm.game;

/** An introductory stage is inserted without changing established level behavior. */
public final class IntroTest {
    static int checks;
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    public static void main(String[] args){
        Levels.Layout intro=Levels.make(1);check(Levels.COUNT==301&&intro.id()==20301&&intro.intro(),"New independent introductory ID");
        check(intro.speed<Levels.make(2).speed&&intro.paddleWidth>Levels.make(2).paddleWidth,"Introduction is slower with a wider paddle");
        check(intro.gateWidth==Levels.CELL*2&&intro.portCount()==3,"Two-block inlet and three target windows");
        for(int hp:intro.cells)check(hp<=1,"Introduction has no armored bricks");
        Engine e=new Engine(EngineTest.events);e.launch();check(e.balls.size()==3,"Introduction serves three real balls");
        for(int frame=0;frame<120;frame++)e.update(1f/60);
        check(e.broken>0&&e.lives==3,"Default launch reaches a real target within two seconds without losing a life");
        for(int n=2;n<=Levels.COUNT;n++){
            Levels.Layout l=Levels.make(n);int old=n-1;
            check(l.id()==20000+old&&l.legacyNumber()==old,"Established stable ID and order "+n);
            check(l.speed==190+(old-1)*.6f&&l.paddleWidth==144-(old-1)*.24f,"Established handling unchanged "+n);
            check(l.targetSeconds==240+(old-1)*5&&l.randomSeed()==9137L*old,"Established targets and bonus seed unchanged "+n);
            e.load(n);e.launch();check(e.balls.size()==(old==1?3:1),"Established starter ball count unchanged "+n);
        }
        EngineTest.Memory saved=new EngineTest.Memory();saved.put("level_stars_20001",2);saved.put("level_best_20001",777);saved.put("level_stars_20107",3);saved.put("level_best_20107",987);saved.put("level_last_id",20108);saved.put("gift_stock",2);
        AppModel migrated=new AppModel(saved);
        check(migrated.stars[1]==0&&migrated.stars[2]==2&&migrated.best[2]==777,"Old first score belongs to new second, not the introduction");
        check(migrated.stars[108]==3&&migrated.best[108]==987&&migrated.last==109,"Old selected ID and high scores shift once");
        check(migrated.totalStars()==5&&migrated.completedCount()==2&&migrated.gifts.charges()==2,"No duplicate stars, completed levels or gift reset");
        check(migrated.isUnlocked(1)&&migrated.isUnlocked(2)&&migrated.isUnlocked(3)&&migrated.isUnlocked(109)&&!migrated.isUnlocked(110),"Earned access survives insertion");
        AppModel again=new AppModel(saved);check(again.last==109&&again.stars[108]==3&&again.stars[1]==0,"Repeated restarts never shift results twice");
        EngineTest.Memory selectedOnly=new EngineTest.Memory();selectedOnly.put("level_last_id",20001);AppModel freshUpgrade=new AppModel(selectedOnly);
        check(freshUpgrade.last==1&&!freshUpgrade.isUnlocked(2),"Unfinished old first does not bypass the new introduction");
        freshUpgrade.start(1);freshUpgrade.game.launch();freshUpgrade.game.remaining=0;freshUpgrade.tick(.01f);freshUpgrade.action("next");
        check(freshUpgrade.game.level.number==2&&freshUpgrade.game.level.id()==20001,"Intro victory unlocks and starts the original first stage");
        System.out.println("PASS: "+checks+" introductory level checks: first target, easier handling, 300 preserved difficulty/seeds/serves, save migration and sequential access.");
    }
}

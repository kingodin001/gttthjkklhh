package pro.brickstorm.game;
public final class AppModel implements Engine.Events {
    public interface Storage {int get(String key,int fallback);void put(String key,int value);void sound(int kind);}
    public static final int PAGE_SIZE=16,PAGES=(Levels.COUNT+PAGE_SIZE-1)/PAGE_SIZE;
    public final Storage storage;public final Engine game;public String screen="home";public int page=0,last=1,speed=1;public boolean sound=true,effects=true,hasSession=false;
    public final int[] stars=new int[Levels.COUNT+1],best=new int[Levels.COUNT+1];
    // New campaign has its own score namespace. Existing classic results stay on disk.
    public AppModel(Storage s){storage=s;last=Math.max(1,Math.min(Levels.COUNT,s.get("route3_last",1)));sound=s.get("sound",1)==1;effects=s.get("effects",1)==1;for(int i=1;i<=Levels.COUNT;i++){stars[i]=Math.max(0,Math.min(3,s.get("route3_stars_"+i,0)));best[i]=Math.max(0,s.get("route3_best_"+i,0));}game=new Engine(this);}
    public int totalStars(){int sum=0;for(int i:stars)sum+=i;return sum;}
    public int completedCount(){int sum=0;for(int i:stars)if(i>0)sum++;return sum;}
    public void start(int n){if(n<1||n>Levels.COUNT)return;last=n;storage.put("route3_last",n);game.load(n);hasSession=true;speed=1;screen="game";}
    public void tick(float dt){for(int i=0;i<speed;i++)game.update(dt);}
    public void action(String a){
        if(a.startsWith("level:")){start(Integer.parseInt(a.substring(6)));return;}
        switch(a){
            case "play":if(hasSession&&game.state!=Engine.State.LOST&&game.state!=Engine.State.WON){screen="game";if(game.state==Engine.State.PAUSED)game.togglePause();}else start(last);break;
            case "levels":if(game.state==Engine.State.PLAYING||game.state==Engine.State.READY)game.togglePause();screen="levels";page=(last-1)/PAGE_SIZE;break;
            case "settings":screen="settings";break;
            case "home":menu();break;
            case "pause":game.togglePause();break;
            case "resume":if(game.state==Engine.State.PAUSED)game.togglePause();break;
            case "retry":start(game.level.number);break;
            case "next":if(game.level.number<Levels.COUNT)start(game.level.number+1);else{screen="levels";page=PAGES-1;}break;
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
    public void back(){if(screen.equals("game")&&(game.state==Engine.State.PLAYING||game.state==Engine.State.READY))game.togglePause();else menu();}
    @Override public void sound(int kind){if(sound)storage.sound(kind);}
    @Override public void completed(int level,int value,int score){hasSession=false;stars[level]=Math.max(stars[level],value);best[level]=Math.max(best[level],score);storage.put("route3_stars_"+level,stars[level]);storage.put("route3_best_"+level,best[level]);last=Math.min(Levels.COUNT,level+1);storage.put("route3_last",last);}
}

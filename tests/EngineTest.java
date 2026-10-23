package pro.brickstorm.game;
import java.util.*;
import java.math.BigInteger;

public final class EngineTest {
    static int checks=0,wins=0;
    static void check(boolean b,String message){checks++;if(!b)throw new AssertionError(message);}
    static final Engine.Events events=new Engine.Events(){public void sound(int k){}public void completed(int l,int s,int score){wins++;}};
    static final class Memory implements AppModel.Storage{final Map<String,Integer> map=new HashMap<>();public int get(String k,int f){return map.getOrDefault(k,f);}public void put(String k,int v){map.put(k,v);}public void sound(int k){}}
    public static void main(String[] args){
        Set<Integer> hashes=new HashSet<>();Set<String> names=new HashSet<>();float previous=0,width=160;int min=99999,max=0,art=0;
        check(Levels.COUNT==76,"68 puzzles plus eight art levels");
        for(int n=1;n<=Levels.COUNT;n++){
            Levels.Layout l=Levels.make(n),again=Levels.make(n);check(Arrays.equals(l.cells,again.cells),"Deterministic level "+n);check(l.cells.length==64*68,"Grid size");int count=0;
            for(int v:l.cells){check(v>=-1&&v<=2,"Valid cell");if(v>0)count++;}check(count==l.blocks&&count>=200,"Block count "+n);check(l.speed>previous&&l.paddleWidth<width,"Monotonic handling difficulty "+n);previous=l.speed;width=l.paddleWidth;
            check(hashes.add(Objects.hash(Arrays.hashCode(l.cells),Arrays.hashCode(l.colors))),"Unique layout "+n);check(names.add(l.title()),"Unique title "+n);accessible(l);min=Math.min(min,count);max=Math.max(max,count);if(l.artistic())art++;
            for(int x=0;x<64;x++)check(l.cells[x]==-1,"Closed top "+n);
            for(int y=0;y<68;y++){check(l.cells[y*64]==-1,"Closed left "+n);check(l.cells[y*64+63]==-1,"Closed right "+n);}
            // Each advertised entrance fits the ball, is open, and has a clear landing gallery.
            for(int g=0;g<l.gates.length;g+=2){int a=l.gates[g],b=l.gates[g+1];check(l.gateWidth>Engine.RADIUS*4&&l.gateWidth<=Engine.RADIUS*6+.6f,"Exact two-to-three-ball entry "+n);
                for(int y=64;y<68;y++)for(int x=a;x<b;x++)check(l.cells[y*64+x]==0,"Unobstructed entry "+n);
                for(int x=a;x<b;x++)check(l.cells[65*64+x]==0,"No decorative rail across entrance "+n);
            }
        }
        check(art==8,"Eight distinct mosaics");
        for(int n=1;n<=Levels.COUNT;n++){
            Engine serve=new Engine(events);serve.load(n);check(serve.paddle-serve.width/2>=Levels.LEFT&&serve.paddle+serve.width/2<=Levels.RIGHT,"Initial paddle stays on screen "+n);serve.launch();boolean entered=false;
            for(int frame=0;frame<90;frame++){serve.update(1f/60);for(Engine.Ball ball:serve.balls){if(ball.y<Levels.FLOOR_TOP-Engine.RADIUS)entered=true;noSteelOverlap(serve,ball);}}
            check(entered,"Default launch enters the actual route "+n);
        }

        Engine e=new Engine(events);e.launch();e.togglePause();float elapsed=e.elapsed;e.update(1);check(e.elapsed==elapsed,"Pause freezes simulation");e.togglePause();
        e.balls.clear();e.update(.02f);check(e.lives==2&&e.state==Engine.State.READY,"Life loss resets serve");e.launch();e.balls.clear();e.update(.02f);e.launch();e.balls.clear();e.update(.02f);check(e.state==Engine.State.LOST,"Three lives then loss");
        e.load(10);e.launch();BigInteger count=e.totalBalls();
        for(int i=0;i<40;i++){e.power(1);count=count.multiply(BigInteger.valueOf(3));check(e.totalBalls().equals(count),"Every repeated yellow pickup triples the full swarm, pickup "+i);check(e.balls.size()<=Engine.MAX_BALLS,"Trajectory budget");}
        check(e.hasGroups()&&e.totalBalls().compareTo(BigInteger.valueOf(Long.MAX_VALUE))>0,"Grouped count never overflows a primitive integer");
        BigInteger saturated=e.totalBalls();e.power(0);check(e.totalBalls().equals(saturated.add(BigInteger.valueOf(3))),"Blue +3 also works with a full trajectory pool");
        e.balls.remove(0);BigInteger afterLoss=e.totalBalls();e.power(1);check(e.totalBalls().equals(afterLoss.multiply(BigInteger.valueOf(3))),"Multiply surviving swarm after loss");
        int winsBefore=wins;e.load(1);e.launch();for(int i=0;i<e.cells.length;i++)if(e.cells[i]>0)e.hit(i);e.update(.01f);check(e.state==Engine.State.WON,"Clear board wins");check(wins==winsBefore+1,"One completion event");e.update(.1f);check(wins==winsBefore+1,"No duplicate win");
        // Four faces and four corners of steel at every supported frame size.
        for(int direction=0;direction<8;direction++)for(float frame:new float[]{1f/120,1f/60,.05f,.08f}){
            e.load(58);e.launch();Arrays.fill(e.cells,0);int solid=30*64+32;e.cells[solid]=-1;e.cells[0]=1;e.remaining=1;
            float l=16+32*Levels.CELL,t=146+30*Levels.CELL,r=l+Levels.CELL,bot=t+Levels.CELL;
            float x=(l+r)/2,y=(t+bot)/2,vx=0,vy=0;
            switch(direction){case 0:x=l-Engine.RADIUS-2;vx=e.level.speed;break;case 1:x=r+Engine.RADIUS+2;vx=-e.level.speed;break;case 2:y=t-Engine.RADIUS-2;vy=e.level.speed;break;case 3:y=bot+Engine.RADIUS+2;vy=-e.level.speed;break;
                default:boolean right=(direction==5||direction==7),below=direction>=6;x=right?r+Engine.RADIUS+2:l-Engine.RADIUS-2;y=below?bot+Engine.RADIUS+2:t-Engine.RADIUS-2;vx=(right?-1:1)*e.level.speed*.7071068f;vy=(below?-1:1)*e.level.speed*.7071068f;break;}
            e.balls.clear();Engine.Ball ball=new Engine.Ball(x,y,vx,vy);e.balls.add(ball);e.update(frame);e.update(frame);check(e.cells[solid]==-1,"Wall never breaks");
            if(vx!=0)check(Math.signum(ball.vx)==-Math.signum(vx),"Steel reflects horizontally "+direction+" frame "+frame);
            if(vy!=0)check(Math.signum(ball.vy)==-Math.signum(vy),"Steel reflects vertically "+direction+" frame "+frame);
        }
        // Yellow x3 clones EACH existing ball at its actual position and preserves energy.
        e.load(20);e.launch();e.balls.clear();e.balls.add(new Engine.Ball(60,580,100,-180));e.balls.add(new Engine.Ball(240,610,-70,210));e.balls.add(new Engine.Ball(330,665,210,20));
        e.power(1);check(e.balls.size()==9,"Triples all three flying balls");
        for(int i=0;i<3;i++){Engine.Ball original=e.balls.get(i);int copies=0;for(Engine.Ball ball:e.balls)if(ball.x==original.x&&ball.y==original.y){copies++;check(Math.abs(Math.hypot(ball.vx,ball.vy)-Math.hypot(original.vx,original.vy))<.01,"Clone preserves velocity magnitude");}check(copies==3,"Each flying position has three balls");}
        e.load(1);e.launch();int before=e.balls.size();e.power(0);check(e.balls.size()==before+3,"Blue adds exactly three");for(int i=before;i<e.balls.size();i++)check(e.balls.get(i).y>Levels.BOTTOM,"Blue bonus spawns above paddle");
        e.power(3);check(e.slowTime==12,"Slow bonus");e.power(2);check(e.wideTime==16,"Wide bonus");
        // Temporary floor catches a missed ball BELOW the paddle, then expires honestly.
        e.load(1);e.launch();e.power(4);e.balls.clear();Engine.Ball caught=new Engine.Ball(36,Engine.SHIELD_Y-9,0,250);e.balls.add(caught);e.update(.05f);check(caught.vy<0&&e.lives==3,"Shield reflects missed ball");
        e.shieldTime=.01f;e.balls.clear();e.balls.add(new Engine.Ball(36,Engine.SHIELD_Y-9,0,250));for(int i=0;i<10;i++)e.update(.02f);check(e.lives==2,"Shield expires and life can be lost");
        e.load(1);e.launch();e.power(4);e.togglePause();float shield=e.shieldTime;e.update(.5f);check(e.shieldTime==shield,"Shield timer freezes on pause");e.load(2);check(e.shieldTime==0,"New level clears shield");
        e.load(58);e.launch();boolean[] kinds=new boolean[5];for(int i=0;i<e.cells.length;i++)if(e.cells[i]>0){while(e.cells[i]>0)e.hit(i);for(Engine.Drop d:e.drops){check(d.kind>=0&&d.kind<5,"Valid bonus");kinds[d.kind]=true;}e.drops.clear();}for(boolean kind:kinds)check(kind,"All five bonuses can drop");
        // A grouped trajectory actually breaks reinforced bricks in one collision.
        e.load(25);e.launch();Arrays.fill(e.cells,0);int reinforced=30*64+32;e.cells[reinforced]=2;e.remaining=1;e.balls.clear();Engine.Ball grouped=new Engine.Ball(16+32.5f*Levels.CELL,146+31*Levels.CELL+5,0,-300);grouped.count=BigInteger.valueOf(27);e.balls.add(grouped);e.update(.04f);check(e.cells[reinforced]==0,"Grouped swarm deals stronger damage instead of just inflating HUD");
        // Every entrance passes a real ball. Every floor section reflects from either side.
        for(int n=1;n<=Levels.COUNT;n++){
            Levels.Layout l=Levels.make(n);
            for(int g=0;g<l.gates.length;g+=2){float gapX=l.gateCenter(g);
                e.load(n);e.launch();e.balls.clear();Engine.Ball ball=new Engine.Ball(gapX,Levels.BOTTOM+4,0,-420);e.balls.add(ball);e.update(.05f);check(ball.y<Levels.FLOOR_TOP-Engine.RADIUS&&ball.vy<0,"Real ball passes entry "+n);
            }
            for(boolean below:new boolean[]{true,false}){
                e.load(n);e.launch();e.balls.clear();Arrays.fill(e.cells,0);e.cells[65]=1;e.remaining=1;int wallCol=2;while(l.cells[66*64+wallCol]!=-1)wallCol++;float wallX=Levels.LEFT+(wallCol+.5f)*Levels.CELL,y=below?Levels.BOTTOM+Engine.RADIUS+1:Levels.FLOOR_TOP-Engine.RADIUS-1;
                Engine.Ball ball=new Engine.Ball(wallX,y,0,below?-420:420);e.balls.add(ball);e.update(.02f);check(below?ball.vy>0:ball.vy<0,"Real floor reflects both sides "+n);
            }
        }
        for(int n=1;n<=Levels.COUNT;n++){
            e.load(n);e.launch();e.balls.clear();float x=e.level.gateLeft(0)+Engine.RADIUS-.15f;
            Engine.Ball clipped=new Engine.Ball(x,Levels.BOTTOM+Engine.RADIUS+1,0,-300);e.balls.add(clipped);e.update(.03f);check(clipped.vy>0,"Precise floor cap blocks an off-center ball "+n);
        }
        e.load(3);e.launch();e.balls.clear();Engine.Ball grazing=new Engine.Ball(100,Levels.BOTTOM+Engine.RADIUS+.002f,-300,-1);grazing.quiet=10;e.balls.add(grazing);e.update(.02f);check(grazing.vy>0,"Anti-stall correction cannot turn a grazing reflection back into steel");noSteelOverlap(e,grazing);
        for(int n=1;n<=Levels.COUNT;n++){
            e.load(n);e.launch();e.balls.clear();int capacity=e.level.gateWidth>12?3:2;
            for(int slot=0;slot<capacity;slot++)e.balls.add(new Engine.Ball(e.level.gateCenter(0)+(slot-(capacity-1)/2f)*Engine.RADIUS*2,Levels.BOTTOM+4,0,-300));
            e.update(.05f);check(e.balls.size()==capacity,"Packed entrance retains all balls "+n);for(Engine.Ball ball:e.balls)check(ball.y<Levels.FLOOR_TOP-Engine.RADIUS&&ball.vy<0,"Two or three full ball diameters pass side by side "+n);
        }
        // All 76 levels are open even on an upgrade from a completed classic campaign.
        Memory memory=new Memory();memory.map.put("unlocked",200);memory.map.put("stars_1",3);memory.map.put("last",199);AppModel a=new AppModel(memory);
        for(int n=1;n<=Levels.COUNT;n++){a.start(n);check(a.game.level.number==n&&a.screen.equals("game"),"Free level access "+n);}
        a.completed(Levels.COUNT,2,700);a.completed(Levels.COUNT,1,500);AppModel b=new AppModel(memory);check(b.stars[Levels.COUNT]==2&&b.best[Levels.COUNT]==700&&b.last==Levels.COUNT,"Best result persistence");check(memory.map.get("stars_1")==3,"Old campaign progress is preserved separately");
        b.start(4);b.game.launch();b.game.score=123;b.action("home");check(b.screen.equals("home")&&b.game.state==Engine.State.PAUSED,"Menu pauses");b.action("play");check(b.game.state==Engine.State.PLAYING&&b.game.score==123,"Continue retains session");
        b.action("levels");check(b.game.state==Engine.State.PAUSED&&b.screen.equals("levels"),"Level picker pauses session");b.action("nextpage");b.action("nextpage");b.action("nextpage");b.action("nextpage");check(b.page==AppModel.PAGES-1,"Last page bounded");b.start(Levels.COUNT);b.action("next");check(b.screen.equals("levels"),"Final level next returns to map");
        for(int speed:new int[]{1,2,4}){b.start(1);b.game.launch();b.action("speed"+speed);b.tick(.01f);check(Math.abs(b.game.elapsed-.01f*speed)<.00001,"Simulation speed "+speed);b.action("pause");float time=b.game.elapsed;b.tick(.02f);check(b.game.elapsed==time,"Pause at speed "+speed);}
        Memory upgrade=new Memory();upgrade.map.put("route4_last",5);upgrade.map.put("route4_stars_5",3);upgrade.map.put("route4_best_5",4321);AppModel migrated=new AppModel(upgrade);int moved=Levels.positionOf(5);check(migrated.last==moved&&migrated.stars[moved]==3&&migrated.best[moved]==4321,"Reordered familiar level retains its stars, score and resume position");migrated.start(moved);migrated.completed(moved,2,100);AppModel reload=new AppModel(upgrade);check(reload.stars[moved]==3&&reload.best[moved]==4321,"Stable IDs retain the best result across subsequent saves");
        // No automated campaign playthrough in this release: structural checks and focused regressions only.
        System.out.println("PASS: "+checks+" focused checks; 76 selectable layouts, 68 different route graphs + eight mosaics; bricks "+min+".."+max+"; repeated yellow bonus x40 preserves exact count beyond trajectory budget.");
    }
    static void noSteelOverlap(Engine e,Engine.Ball b){
        int cx=(int)((b.x-Levels.LEFT)/Levels.CELL),cy=(int)((b.y-Levels.TOP)/Levels.CELL);
        for(int f=0;f<e.level.floorSegments.length;f+=2){
            float l=e.level.floorSegments[f],r=e.level.floorSegments[f+1];float px=Engine.clamp(b.x,l,r),py=Engine.clamp(b.y,Levels.FLOOR_TOP,Levels.BOTTOM);
            check((b.x-px)*(b.x-px)+(b.y-py)*(b.y-py)>Engine.RADIUS*Engine.RADIUS-.1f,"No penetration of exact floor aperture "+e.level.number);
        }
        for(int y=Math.max(0,cy-1);y<=Math.min(67,cy+1);y++)for(int x=Math.max(0,cx-1);x<=Math.min(63,cx+1);x++)if(y<67&&e.cells[y*64+x]<0){
            float l=Levels.LEFT+x*Levels.CELL,t=Levels.TOP+y*Levels.CELL;
            float px=Engine.clamp(b.x,l,l+Levels.CELL),py=Engine.clamp(b.y,t,t+Levels.CELL);
            check((b.x-px)*(b.x-px)+(b.y-py)*(b.y-py)>Engine.RADIUS*Engine.RADIUS-.1f,"No steel penetration on "+e.level.number+" at "+x+","+y);
        }
    }
    static void accessible(Levels.Layout l){
        // Cell centers leave 0.375 px of clearance on each side of the actual ball.
        // Four-neighbor traversal cannot pass diagonally through touching metal corners.
        boolean[] seen=new boolean[l.cells.length];ArrayDeque<Integer> q=new ArrayDeque<>();for(int x=0;x<64;x++){int i=67*64+x;if(l.cells[i]!=-1){q.add(i);seen[i]=true;}}
        while(!q.isEmpty()){int i=q.remove(),x=i%64,y=i/64;int[] next={x>0?i-1:-1,x<63?i+1:-1,y>0?i-64:-1,y<67?i+64:-1};for(int k:next)if(k>=0&&!seen[k]&&l.cells[k]!=-1){seen[k]=true;q.add(k);}}
        for(int i=0;i<seen.length;i++)check(l.cells[i]<=0||seen[i],"No sealed destructible pocket on "+l.number+" at "+(i%64)+","+(i/64));
    }
}

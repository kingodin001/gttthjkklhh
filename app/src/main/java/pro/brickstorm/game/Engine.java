package pro.brickstorm.game;
import java.util.ArrayList;
import java.util.Random;

/** Pure Java physics. No Android dependencies; fixed 240 Hz integration and spatial grid. */
public final class Engine {
    public enum State { READY,PLAYING,PAUSED,WON,LOST }
    public interface Events { void sound(int kind); void completed(int level,int stars,int score); }
    public static final int MAX_BALLS=8192;
    public static final float PADDLE_Y=700,FLOOR=750,RADIUS=2.5f,SHIELD_Y=735;
    public static final class Ball { public float x,y,vx,vy,quiet=0; public final float[] tx=new float[5],ty=new float[5]; public Ball(float x,float y,float vx,float vy){this.x=x;this.y=y;this.vx=vx;this.vy=vy;for(int i=0;i<5;i++){tx[i]=x;ty[i]=y;}} }
    public static final class Drop {public float x,y;public int kind;Drop(float x,float y,int k){this.x=x;this.y=y;kind=k;}}
    public final ArrayList<Ball> balls=new ArrayList<>();
    public final ArrayList<Drop> drops=new ArrayList<>();
    public Levels.Layout level; public int[] cells; public State state=State.READY;
    public int remaining,lives=3,score=0,broken=0,combo=0;public float paddle=200,target=200,width=96,elapsed=0,wideTime=0,slowTime=0,shieldTime=0,comboTime=0;
    public String notice=""; public float noticeTime=0;
    public Events events; private State pausedFrom=State.PLAYING; private Random rng; private float accumulator=0;private float lastX=200,lastY=330;
    public Engine(Events e){events=e;load(1);}
    public void load(int n){level=Levels.make(n);cells=level.cells.clone();remaining=level.blocks;balls.clear();drops.clear();rng=new Random(9137L*n);state=State.READY;width=level.paddleWidth;paddle=target=clamp(Levels.LEFT+(level.gateStart+level.gateEnd)*.5f*Levels.CELL,Levels.LEFT+width/2,Levels.RIGHT-width/2);lives=3;score=broken=combo=0;elapsed=wideTime=slowTime=shieldTime=comboTime=noticeTime=accumulator=0;notice="";balls.add(new Ball(serveX(),PADDLE_Y-8,0,0));}
    public void launch(){
        if(state!=State.READY)return;state=State.PLAYING;
        float angle=serveAngle();
        Ball b=balls.get(0);b.vx=(float)Math.sin(angle)*level.speed;b.vy=-(float)Math.cos(angle)*level.speed;
        if(level.number==1&&broken==0){for(float offset:new float[]{-.008f,.008f})balls.add(new Ball(b.x,PADDLE_Y-8,(float)Math.sin(angle+offset)*level.speed,-(float)Math.cos(angle+offset)*level.speed));}
        notice="Войди в проход и выбирай угол отскока";noticeTime=3;events.sound(0);
    }
    private float serveX(){return clamp(level.gateCenter(0),paddle-width/2+RADIUS+1,paddle+width/2-RADIUS-1);}
    public float serveAngle(){Ball ball=balls.get(0);return (float)Math.atan2(level.gateCenter(0)-ball.x,ball.y-(Levels.FLOOR_TOP+Levels.BOTTOM)/2);}
    public void togglePause(){if(state==State.PLAYING||state==State.READY){pausedFrom=state;state=State.PAUSED;}else if(state==State.PAUSED)state=pausedFrom;}
    public void aim(float x){target=clamp(x,16+width/2,384-width/2);if(state==State.READY){paddle=target;balls.get(0).x=serveX();}}
    public void update(float dt){
        if(state!=State.PLAYING&&state!=State.READY)return;
        dt=Math.min(.08f,Math.max(0,dt));noticeTime=Math.max(0,noticeTime-dt);
        if(state==State.READY)return;
        elapsed+=dt;wideTime=Math.max(0,wideTime-dt);slowTime=Math.max(0,slowTime-dt);shieldTime=Math.max(0,shieldTime-dt);comboTime=Math.max(0,comboTime-dt);if(comboTime==0)combo=0;
        width=wideTime>0?Math.min(176,level.paddleWidth*1.55f):level.paddleWidth;
        accumulator+=dt;
        while(accumulator>=1f/240&&state==State.PLAYING){step(1f/240);accumulator-=1f/240;}
    }
    private void step(float dt){
        paddle=clamp(paddle+(target-paddle)*Math.min(1,dt*32),16+width/2,384-width/2);
        float sf=slowTime>0?.72f:1;
        for(int j=balls.size()-1;j>=0;j--){Ball b=balls.get(j);b.quiet+=dt;moveBall(b,dt*sf);
            if(b.y>FLOOR){balls.remove(j);continue;}
            for(int k=4;k>0;k--){b.tx[k]=b.tx[k-1];b.ty[k]=b.ty[k-1];}b.tx[0]=b.x;b.ty[0]=b.y;
        }
        if(state!=State.PLAYING)return;
        for(int i=drops.size()-1;i>=0;i--){Drop d=drops.get(i);d.y+=107*dt;if(d.y>=PADDLE_Y-9&&d.y<PADDLE_Y+18&&Math.abs(d.x-paddle)<width/2+10){power(d.kind);drops.remove(i);}else if(d.y>FLOOR)drops.remove(i);}
        if(remaining==0){win();return;}
        if(balls.isEmpty()){
            lives--;drops.clear();wideTime=slowTime=shieldTime=0;width=level.paddleWidth;accumulator=0;
            if(lives<=0){state=State.LOST;events.sound(4);}else{state=State.READY;balls.add(new Ball(serveX(),PADDLE_Y-8,0,0));notice="Ещё попытка. Коснись поля для запуска";noticeTime=3;events.sound(4);}
        }
    }
    private static final class Contact {float t,nx,ny;int cell;boolean paddle;void reset(){t=1.0001f;nx=ny=0;cell=-1;paddle=false;}}
    private final Contact contact=new Contact();
    /** Swept circle against expanded solid cells: resolve the earliest impact, then the remaining motion. */
    private void moveBall(Ball b,float dt){
        float left=dt;
        for(int bounce=0;bounce<8&&left>0.000001f;bounce++){
            Contact h=contact;h.reset();float dx=b.vx*left,dy=b.vy*left;
            if(dx<0)boundary(h,(16+RADIUS-b.x)/dx,1,0);
            if(dx>0)boundary(h,(384-RADIUS-b.x)/dx,-1,0);
            if(dy<0)boundary(h,(146+RADIUS-b.y)/dy,0,1);
            if(shieldTime>0&&dy>0&&b.y<=SHIELD_Y-RADIUS+.001f)boundary(h,(SHIELD_Y-RADIUS-b.y)/dy,0,-1);
            if(dy>0&&b.y<=PADDLE_Y-RADIUS+.001f){float t=(PADDLE_Y-RADIUS-b.y)/dy;float x=b.x+dx*t;
                if(t>=0&&t<=1&&x>=paddle-width/2-RADIUS&&x<=paddle+width/2+RADIUS&&t<h.t){h.t=t;h.nx=0;h.ny=-1;h.cell=-1;h.paddle=true;}}
            for(int floor=0;floor<level.floorSegments.length;floor+=2){
                float l=level.floorSegments[floor],r=level.floorSegments[floor+1];
                if(r>l)sweep(h,b.x,b.y,dx,dy,l-RADIUS,Levels.FLOOR_TOP-RADIUS,r+RADIUS,Levels.BOTTOM+RADIUS,-1);
            }
            int x0=Math.max(0,(int)Math.floor((Math.min(b.x,b.x+dx)-RADIUS-16)/Levels.CELL));
            int x1=Math.min(63,(int)Math.floor((Math.max(b.x,b.x+dx)+RADIUS-16)/Levels.CELL));
            int y0=Math.max(0,(int)Math.floor((Math.min(b.y,b.y+dy)-RADIUS-146)/Levels.CELL));
            int y1=Math.min(67,(int)Math.floor((Math.max(b.y,b.y+dy)+RADIUS-146)/Levels.CELL));
            for(int y=y0;y<=y1;y++)for(int x=x0;x<=x1;x++){int i=y*64+x;if(y==67||cells[i]==0)continue;
                sweep(h,b.x,b.y,dx,dy,16+x*Levels.CELL-RADIUS,146+y*Levels.CELL-RADIUS,16+(x+1)*Levels.CELL+RADIUS,146+(y+1)*Levels.CELL+RADIUS,i);}
            if(h.t>1){b.x+=dx;b.y+=dy;break;}
            b.x+=dx*h.t+h.nx*.025f;b.y+=dy*h.t+h.ny*.025f;left*=1-h.t;
            if(h.paddle){float f=clamp((b.x-paddle)/(width/2),-.96f,.96f),a=f*1.12f;b.vx=(float)Math.sin(a)*level.speed;b.vy=-(float)Math.cos(a)*level.speed;if(Math.abs(b.vx)<level.speed*.09f){b.vx=(f<0?-1:1)*level.speed*.09f;b.vy=-(float)Math.sqrt(level.speed*level.speed-b.vx*b.vx);}b.quiet=0;events.sound(1);}
            else{if(h.nx!=0)b.vx=-b.vx;if(h.ny!=0)b.vy=-b.vy;}
            if(h.cell>=0){boolean brick=cells[h.cell]>0;if(brick)hit(h.cell);if(brick)b.quiet=0;}
            // Free a rare repeating axis-aligned orbit only after prolonged wall-only travel.
            if(b.quiet>9){float angle=(float)Math.atan2(b.vy,b.vx)+.09f;float speed=(float)Math.hypot(b.vx,b.vy);float nx=(float)Math.cos(angle)*speed,ny=(float)Math.sin(angle)*speed;if((h.nx==0||nx*h.nx>0)&&(h.ny==0||ny*h.ny>0)){b.vx=nx;b.vy=ny;}b.quiet=0;}

        }
    }
    private static void boundary(Contact h,float t,float nx,float ny){if(t>=-.00001f&&t<=1&&t<h.t){h.t=Math.max(0,t);h.nx=nx;h.ny=ny;h.cell=-1;h.paddle=false;}}
    private static void sweep(Contact h,float x,float y,float dx,float dy,float l,float top,float r,float bottom,int cell){
        float nearX=Float.NEGATIVE_INFINITY,farX=Float.POSITIVE_INFINITY,nearY=Float.NEGATIVE_INFINITY,farY=Float.POSITIVE_INFINITY;
        if(Math.abs(dx)<.000001f){if(x<l||x>r)return;}else{float a=(l-x)/dx,b=(r-x)/dx;nearX=Math.min(a,b);farX=Math.max(a,b);}
        if(Math.abs(dy)<.000001f){if(y<top||y>bottom)return;}else{float a=(top-y)/dy,b=(bottom-y)/dy;nearY=Math.min(a,b);farY=Math.max(a,b);}
        float enter=Math.max(nearX,nearY),exit=Math.min(farX,farY);
        if(enter>exit||exit<0||enter<-.0001f||enter>1||enter>=h.t)return;
        h.t=Math.max(0,enter);h.cell=cell;h.paddle=false;
        if(Math.abs(nearX-nearY)*Math.max(Math.abs(dx),Math.abs(dy))<.001f){h.nx=dx>0?-1:1;h.ny=dy>0?-1:1;}else if(nearX>nearY){h.nx=dx>0?-1:1;h.ny=0;}else{h.nx=0;h.ny=dy>0?-1:1;}
    }
    void hit(int i){
        if(cells[i]<=0)return;
        cells[i]--;lastX=16+(i%Levels.COLS+.5f)*Levels.CELL;lastY=146+(i/Levels.COLS+.5f)*Levels.CELL;
        if(cells[i]==0){remaining--;broken++;combo++;comboTime=1.6f;score+=10+Math.min(40,combo/3);
            if((broken==6||broken==18||broken%32==0)&&drops.size()<14){int k=broken==6?0:broken==18?1:rng.nextInt(5);drops.add(new Drop(lastX,lastY,k));}
            if(broken%90==0&&balls.size()<12)split(3);
        }
        events.sound(2);
    }
    public void power(int kind){
        switch(kind){
            case 0:{int old=balls.size();split(3);notice=balls.size()>old?"+"+(balls.size()-old)+" ШАРА":"ПРЕДЕЛ 8192 ШАРОВ";break;}
            case 1:{int old=balls.size();multiplyFlying();notice=balls.size()>old?"ШАРЫ "+old+" → "+balls.size():"ПРЕДЕЛ 8192 ШАРОВ";break;}
            case 2:wideTime=16;notice="ШИРОКАЯ ПЛАТФОРМА";break;
            case 3:slowTime=12;notice="ЗАМЕДЛЕНИЕ";break;
            case 4:shieldTime=6;notice="НИЖНЯЯ СТЕНА · 6 СЕК";break;
            default:return;
        }
        noticeTime=2;events.sound(3);
    }
    /** Every visible ball is an independent simulated ball, never a weighted group. */
    private void multiplyFlying(){
        int original=balls.size();
        for(int side=0;side<2;side++)for(int i=0;i<original&&balls.size()<MAX_BALLS;i++){
            Ball source=balls.get(i);float a=side==0?-.24f:.24f;
            balls.add(new Ball(source.x,source.y,source.vx*(float)Math.cos(a)-source.vy*(float)Math.sin(a),source.vx*(float)Math.sin(a)+source.vy*(float)Math.cos(a)));
        }
    }
    public int totalBalls(){return balls.size();}
    public String ballCountText(){return String.valueOf(balls.size());}
    private void split(int count){
        for(int i=0;i<count&&balls.size()<MAX_BALLS;i++){
            float angle=(rng.nextFloat()-.5f)*2.2f;balls.add(new Ball(paddle,PADDLE_Y-12,(float)Math.sin(angle)*level.speed,-(float)Math.cos(angle)*level.speed));
        }
    }
    private void win(){if(state==State.WON)return;state=State.WON;int stars=lives==3&&elapsed<level.targetSeconds?3:lives>=2?2:1;events.completed(level.number,stars,score);events.sound(5);}
    public int stars(){return lives==3&&elapsed<level.targetSeconds?3:lives>=2?2:1;}
    public float progress(){return 1-remaining/(float)level.blocks;}
    public static float clamp(float v,float lo,float hi){return Math.max(lo,Math.min(hi,v));}
}

package pro.brickstorm.game;
import java.util.ArrayList;

/** Shared visual composition: Android Canvas and desktop verification use this exact layout. */
public final class GameRenderer {
    public static final int BG=0xff233b59,PANEL=0xe52b526e,EDGE=0xff6384a5,WHITE=0xfff7fbff,MUTED=0xffbfd2e7,LIME=0xffffdc88,CYAN=0xff86edff,GOLD=0xffffde8c;
    public static final class Button {public final float x,y,w,h;public final String action;Button(float x,float y,float w,float h,String a){this.x=x;this.y=y;this.w=w;this.h=h;action=a;}public boolean contains(float px,float py){return px>=x&&px<=x+w&&py>=y&&py<=y+h;}}
    public final ArrayList<Button> buttons=new ArrayList<>();private Painter p;private AppModel app;
    public String hit(float x,float y){for(int i=buttons.size()-1;i>=0;i--)if(buttons.get(i).contains(x,y))return buttons.get(i).action;return null;}
    public void draw(Painter painter,AppModel model){p=painter;app=model;buttons.clear();background();switch(app.screen){case "home":home();break;case "levels":levels();break;case "settings":settings();break;default:game();break;}}
    private void background(){
        p.rect(0,0,400,820,0xff143853,0);p.artwork();
        if(!app.screen.equals("home")){p.rect(0,0,400,820,app.screen.equals("game")?0xeb14344e:0xda14344e,0);}
        else {p.rect(0,0,400,290,0x30112c46,0);for(int i=0;i<24;i++)p.rect(0,580+i*10,400,10,((int)(40+i*7)<<24)|0x00142e48,0);}
    }
    static int mix(int a,int b,float t){int red=(int)(((a>>16)&255)*(1-t)+((b>>16)&255)*t),green=(int)(((a>>8)&255)*(1-t)+((b>>8)&255)*t),blue=(int)((a&255)*(1-t)+(b&255)*t);return 0xff000000|(red<<16)|(green<<8)|blue;}
    private void text(String s,float x,float y,float size,int color,boolean bold){p.text(s,x,y,size,color,bold,false);}
    private void center(String s,float x,float y,float size,int color,boolean bold){p.text(s,x,y,size,color,bold,true);}
    private void button(float x,float y,float w,float h,String label,String action,boolean primary){
        p.rect(x,y+4,w,h,0x55102236,17);p.rect(x,y,w,h,primary?0xffe9b75e:0xff527a94,17);
        p.rect(x+1,y+1,w-2,h-3,primary?0xffffdb8a:0xf52a506d,16);
        p.rect(x+16,y+2,w-32,1,primary?0xaaffffff:0x707fcce9,0);
        center(label,x+w/2,y+h/2+5,16,primary?0xff163449:WHITE,true);buttons.add(new Button(x,y,w,h,action));
    }
    private void icon(float x,float y,String label,String action){button(x,y,44,44,label,action,false);}
    private void brand(float x,float y){for(int k=0;k<6;k++)p.rect(x+(k%3)*8,y+(k/3)*8,6,6,k==5?CYAN:LIME,1);text("БРИКС ШТОРМ",x+32,y+14,13,WHITE,true);}
    private void home(){
        center("200 УРОВНЕЙ · ЛАБИРИНТЫ И ОТСКОКИ",200,41,10,0xffbcecff,true);
        center("БРИКС",200,105,48,WHITE,true);center("ШТОРМ",200,156,48,GOLD,true);
        center("Один удар. Целый каскад.",200,185,14,0xffe6f4ff,false);
        button(44,216,312,58,app.hasSession&&app.game.state!=Engine.State.LOST?"ПРОДОЛЖИТЬ  ›":"ИГРАТЬ  ›","play",true);
        center("Уровень "+(app.hasSession?app.game.level.number:app.last)+"  /  200",200,299,12,WHITE,true);
        p.rect(25,641,350,142,0xb31c3c55,22);
        text("ТВОИ РЕЗУЛЬТАТЫ",43,669,10,CYAN,true);text("★ "+app.totalStars(),306,670,14,GOLD,true);
        p.rect(43,684,314,4,0xff46657a,2);p.rect(43,684,Math.max(4,app.completedCount()/(float)Levels.COUNT*314),4,GOLD,2);
        button(42,708,194,53,"Уровни","levels",false);button(246,708,112,53,"Настройки","settings",false);
        center("Играй в своём ритме · без интернета",200,808,10,0xffb5cfde,false);
    }
    private void header(String title,String subtitle){button(20,20,92,44,"‹ Меню","home",false);text(title,128,49,22,WHITE,true);text(subtitle,24,105,12,MUTED,false);}
    private void levels(){
        header("Уровни","Все 200 уровней доступны. Выбирай любой.");int start=app.page*AppModel.PAGE_SIZE+1;
        p.rect(24,125,352,63,PANEL,16);text("200 ЛАБИРИНТОВ · 1–3 ВЫХОДА",39,150,11,CYAN,true);
        text("Двойные стенки и узкие выходы к кирпичам",39,174,11,WHITE,false);
        for(int j=0;j<AppModel.PAGE_SIZE;j++){
            int n=start+j;if(n>Levels.COUNT)break;Levels.Layout l=Levels.make(n);float x=24+(j%4)*90,y=205+(j/4)*119;boolean active=n==app.last;
            p.rect(x,y,82,110,active?GOLD:0xff52758e,12);p.rect(x+1,y+1,80,108,0xff214663,11);
            text(String.format("%02d",n),x+7,y+17,12,active?GOLD:WHITE,true);
            if(l.artistic())text("МОЗАИКА",x+30,y+16,6,0xffe1b6fa,true);
            // Runtime cells form the thumbnail; no invented promotional layout.
            float cell=1.04f,ox=x+8,oy=y+23;
            p.rect(ox,oy,64*cell,68*cell,0xff12324b,1);
            p.thumbnail(l,ox,oy,64*cell,68*cell);
            for(int star=0;star<3;star++)center("★",x+25+star*16,y+105,10,app.stars[n]>star?GOLD:0xff58758e,false);
            buttons.add(new Button(x,y,82,110,"level:"+n));
        }
        if(app.page>0)icon(24,706,"‹","prevpage");center((app.page+1)+" / "+AppModel.PAGES,200,735,15,MUTED,true);if(app.page<AppModel.PAGES-1)icon(332,706,"›","nextpage");
        center("ПРОЙДЕНО "+app.completedCount()+" / "+Levels.COUNT+"   ·   ★ "+app.totalStars()+" / "+(Levels.COUNT*3),200,790,11,MUTED,false);
    }
    private void settings(){
        header("Настройки","Сделай шторм своим.");
        text("ЗВУК И ЭФФЕКТЫ",24,158,11,CYAN,true);
        button(24,185,352,64,"Звук                         "+(app.sound?"ВКЛ":"ВЫКЛ"),"sound",false);
        button(24,261,352,64,"Следы шара      "+(app.effects?"ВКЛ":"ВЫКЛ"),"effects",false);
        text("КАК ИГРАТЬ",24,384,11,CYAN,true);
        String[] lines={"Веди пальцем по полю — платформа следует.","Коснись поля, чтобы запустить шар.","Лови круглые бонусы платформой.","Направляй шары через проходы в стенах.","Жёлтый ×3 размножает все летящие шары.","Бонус стены защищает снизу 6 секунд.","Все уровни открыты. Скорость: ×1 / ×2 / ×4.","Каждый бонус добавляет отдельные шары."};
        for(int i=0;i<lines.length;i++)text(lines[i],24,419+i*26,13,MUTED,false);
        p.rect(24,636,352,91,PANEL,18);text("СОЗДАНО ДЛЯ КАСКАДОВ",42,665,11,LIME,true);text("Маршруты · отскоки · 200 уровней",42,697,12,WHITE,false);
        center("Брикс Шторм  ·  1.9.0",200,795,11,MUTED,false);
    }
    private void game(){
        Engine e=app.game;button(16,17,88,44,"‹ Меню","home",false);center("УРОВЕНЬ "+String.format("%03d",e.level.number),200,39,11,CYAN,true);center(e.level.title(),200,64,e.level.title().length()>20?15:18,WHITE,true);icon(340,17,e.state==Engine.State.PAUSED?"▶":"Ⅱ","pause");
        text("СЧЁТ",18,101,9,MUTED,true);text(String.format("%,d",e.score).replace(',',' '),18,124,21,WHITE,true);
        center("РАЗРУШЕНО",211,101,9,MUTED,true);center(Math.round(e.progress()*100)+"%",211,124,20,WHITE,true);
        text("ПОПЫТКИ",314,101,9,MUTED,true);for(int i=0;i<3;i++)p.circle(328+i*21,119,5,i<e.lives?0xffff8f9a:EDGE);
        p.rect(10,141,380,398,0x55203a50,8);
        p.rect(16,146,368,391,0xff0e2338,0);p.line(14,146,14,537,0xff83afc4,2);p.line(386,146,386,537,0xff83afc4,2);p.line(16,144,384,144,0xff83afc4,2);p.board(e);
        for(int floor=0;floor<e.level.floorSegments.length;floor+=2){
            float x=e.level.floorSegments[floor],w=e.level.floorSegments[floor+1]-x;
            p.rect(x,Levels.FLOOR_TOP,w,Levels.CELL,0xffa9bfce,0);p.line(x,Levels.FLOOR_TOP+.6f,x+w,Levels.FLOOR_TOP+.6f,0xffeef7ff,.8f);
        }
        for(int g=0;g<e.level.gates.length;g+=2){
            float gateL=e.level.gateLeft(g),gateR=e.level.gateRight(g);
            p.line(gateL-.5f,Levels.FLOOR_TOP,gateL-.5f,Levels.BOTTOM,0xffa5f5ee,.6f);p.line(gateR+.5f,Levels.FLOOR_TOP,gateR+.5f,Levels.BOTTOM,0xffa5f5ee,.6f);
            if(e.state==Engine.State.READY)center("↑ ВХОД ↑",(gateL+gateR)/2,Levels.BOTTOM+15,8,CYAN,true);
        }
        p.line(14,537,14,740,0xff74a5c8,2);p.line(386,537,386,740,0xff74a5c8,2);
        if(e.balls.size()>256)p.swarm(e);else for(Engine.Ball b:e.balls){if(app.effects&&e.balls.size()<180)for(int k=4;k>=0;k--)p.circle(b.tx[k],b.ty[k],2.8f-k*.35f,((80-k*12)<<24)|0x0064dcf4);p.circle(b.x,b.y,4.4f,0x307cecff);p.circle(b.x,b.y,Engine.RADIUS,WHITE);p.circle(b.x-.7f,b.y-.7f,1,0xffd2ffff);}
        String[] labels={"+3","×3","↔","S","▰"};int[] colors={CYAN,GOLD,0xffb69aff,0xff82b7ff,0xff8deac9};
        for(Engine.Drop d:e.drops){p.circle(d.x,d.y,11,colors[d.kind]);center(labels[d.kind],d.x,d.y+4,11,BG,true);}
        if(app.effects)p.rect(e.paddle-e.width/2-3,Engine.PADDLE_Y-3,e.width+6,13,0x33ffb64f,7);
        p.rect(e.paddle-e.width/2,Engine.PADDLE_Y,e.width,7,0xffffb64f,4);
        p.rect(e.paddle-e.width/2+3,Engine.PADDLE_Y,e.width-6,2,WHITE,1);
        if(e.state==Engine.State.READY){
            Engine.Ball serve=e.balls.get(0);float a=e.serveAngle();
            for(int i=0;i<10;i++){float distance=22+i*14;p.circle(serve.x+(float)Math.sin(a)*distance,serve.y-(float)Math.cos(a)*distance,1,0x88c7f8ff);}
            center("КОСНИСЬ ПОЛЯ ДЛЯ ЗАПУСКА",200,618,11,WHITE,true);
            center("Веди пальцем, чтобы двигать платформу",200,642,10,MUTED,false);
            center("Угол отскока зависит от места удара о платформу",200,663,8,MUTED,false);
        }
        if(e.shieldTime>0){
            p.rect(16,Engine.SHIELD_Y-2,368,8,0x335cefd7,4);p.rect(16,Engine.SHIELD_Y,368,4,0xffa1f0d9,2);
            center("СТЕНА  "+(int)Math.ceil(e.shieldTime)+" с",200,Engine.SHIELD_Y-8,9,0xffa1f0d9,true);
        }
        text("ШАРЫ  "+e.ballCountText(),18,565,10,MUTED,true);text(e.level.difficulty(),273,565,9,CYAN,true);
        if(e.noticeTime>0)center(e.notice,200,599,9,LIME,true);
        else if(e.state==Engine.State.READY)center(e.level.hint(),200,586,8,MUTED,false);
        p.line(16,750,384,750,EDGE,1);button(16,766,114,40,"Уровни","levels",false);button(142,766,72,40,"×1","speed1",app.speed==1);button(222,766,72,40,"×2","speed2",app.speed==2);button(302,766,80,40,"×4","speed4",app.speed==4);
        if(e.state==Engine.State.PAUSED)overlay("ПАУЗА","Уровень "+e.level.number+" · "+e.level.title(),"Продолжить","resume","Карта уровней","levels",false);
        else if(e.state==Engine.State.WON)overlay(e.level.number==Levels.COUNT?"ШТОРМ ПОКОРЁН":"УРОВЕНЬ ПРОЙДЕН","★ "+e.stars()+" / 3     ·     СЧЁТ "+e.score,e.level.number==Levels.COUNT?"Карта уровней":"Следующий уровень",e.level.number==Levels.COUNT?"levels":"next","Пройти ещё раз","retry",true);
        else if(e.state==Engine.State.LOST)overlay("ЕЩЁ ОДНА ПОПЫТКА?","Разрушено "+Math.round(e.progress()*100)+"% блоков","Попробовать снова","retry","Карта уровней","levels",false);
    }
    private void overlay(String title,String sub,String primary,String action,String second,String other,boolean success){
        buttons.clear();p.rect(0,0,400,820,0xd9294663,0);p.rect(20,238,360,376,PANEL,24);center(success?"✦":"Ⅱ",200,298,40,success?LIME:CYAN,true);center(title,200,340,19,WHITE,true);center(sub,200,371,12,MUTED,false);button(44,401,312,52,primary,action,true);button(44,465,312,48,second,other,false);center("Прогресс уровня сохранится до закрытия игры",200,594,10,MUTED,false);button(44,529,312,44,"В главное меню","home",false);
    }
}

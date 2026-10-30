package pro.brickstorm.game;
import java.util.ArrayList;

/** Shared visual composition: Android Canvas and desktop verification use this exact layout. */
public final class GameRenderer {
    public static final int BG=0xff071b59,PANEL=0xe52b526e,EDGE=0xff6384a5,WHITE=0xfff7fbff,MUTED=0xffbfd2e7,LIME=0xffffdc88,CYAN=0xff86edff,GOLD=0xffffde8c;
    public static final class Button {public final float x,y,w,h;public final String action;Button(float x,float y,float w,float h,String a){this.x=x;this.y=y;this.w=w;this.h=h;action=a;}public boolean contains(float px,float py){return px>=x&&px<=x+w&&py>=y&&py<=y+h;}}
    public final ArrayList<Button> buttons=new ArrayList<>();private Painter p;private AppModel app;
    public String hit(float x,float y){for(int i=buttons.size()-1;i>=0;i--)if(buttons.get(i).contains(x,y))return buttons.get(i).action;return null;}
    public void draw(Painter painter,AppModel model){p=painter;app=model;buttons.clear();background();switch(app.screen){case "home":home();break;case "levels":levels();break;case "settings":settings();break;case "progress":progress();break;case "giftinfo":giftInfo();break;case "rewards":rewards();break;case "adinfo":adInfo();break;default:game();break;}if(app.adBusy){buttons.clear();p.rect(0,0,400,820,0xde071b59,0);center("РЕКЛАМА",200,385,22,WHITE,true);center("Игра приостановлена",200,420,13,MUTED,false);}}
    private void background(){
        p.rect(0,0,400,820,BG,0);p.artwork(app.screen.equals("home"));
        // The playing field is unchanged. Only its surrounding wallpaper is new.
        if(!app.screen.equals("home")&&!app.screen.equals("game"))p.rect(0,0,400,820,0x95071745,0);
    }
    static int mix(int a,int b,float t){int red=(int)(((a>>16)&255)*(1-t)+((b>>16)&255)*t),green=(int)(((a>>8)&255)*(1-t)+((b>>8)&255)*t),blue=(int)((a&255)*(1-t)+(b&255)*t);return 0xff000000|(red<<16)|(green<<8)|blue;}
    private void text(String s,float x,float y,float size,int color,boolean bold){p.text(s,x,y,size,color,bold,false);}
    private void center(String s,float x,float y,float size,int color,boolean bold){p.text(s,x,y,size,color,bold,true);}
    private float fit(String value,float size,float width){return size*Math.min(1,width/Math.max(1,p.textWidth(value,size,true)));}
    private void button(float x,float y,float w,float h,String label,String action,boolean primary){
        button(x,y,w,h,label,action,primary,16);
    }
    private void button(float x,float y,float w,float h,String label,String action,boolean primary,float labelSize){
        p.rect(x,y+4,w,h,0x55102236,17);p.rect(x,y,w,h,primary?0xffe9b75e:0xff527a94,17);
        p.rect(x+1,y+1,w-2,h-3,primary?0xffffdb8a:0xf52a506d,16);
        p.rect(x+16,y+2,w-32,1,primary?0xaaffffff:0x707fcce9,0);
        center(label,x+w/2,y+h/2+5,labelSize,primary?0xff163449:WHITE,true);buttons.add(new Button(x,y,w,h,action));
    }
    private void icon(float x,float y,String label,String action){button(x,y,44,44,label,action,false);}
    private void disabled(float x,float y,float w,float h,String label){disabled(x,y,w,h,label,13);}
    private void disabled(float x,float y,float w,float h,String label,float size){p.rect(x,y,w,h,0xee23435c,13);center(label,x+w/2,y+h/2+4,size,0xffa8becd,true);}
    private void brand(float x,float y){for(int k=0;k<6;k++)p.rect(x+(k%3)*8,y+(k/3)*8,6,6,k==5?CYAN:LIME,1);text("БРИКС ШТОРМ",x+32,y+14,13,WHITE,true);}
    /** Artwork is the exact approved composition. Touch regions share its coordinates.
     * Only changing values are overpainted; no sample score/timer is presented as live data. */
    private void home(){
        p.gradient(49,24,27,25,0xff102d73,0xff101e5c,3);
        center(String.valueOf(app.totalStars()),62,44,app.totalStars()>99?13:19,WHITE,true);
        buttons.add(new Button(14,19,65,35,"progress"));
        buttons.add(new Button(349,18,37,37,"settings"));
        buttons.add(new Button(55,476,290,79,"play"));
        p.gradient(89,558,222,20,0xff091e62,0xff091b5a,5);
        boolean continuing=app.hasSession&&app.game.state!=Engine.State.LOST&&app.game.state!=Engine.State.WON;
        center((continuing?"Продолжить":"Начать")+" · уровень "+app.last,200,572,13,MUTED,false);
        buttons.add(new Button(24,592,171,49,"levels"));
        buttons.add(new Button(205,592,171,49,"progress"));
        // Free gift and rewarded video are separate, directly accessible controls.
        p.rect(28,660,344,68,0xff5488eb,15);p.gradient(29,661,342,66,0xff153c88,0xff0a205d,14);
        for(int i=0;i<3;i++){float x=50+(i==1?12:i==2?24:0),y=696+(i==1?-15:0);p.circle(x,y,10,0xff74a8ff);p.circle(x-1,y-2,9,WHITE);}
        text("Подарок ×3",91,684,13,WHITE,true);String stock="В запасе: "+app.gifts.charges();text(stock,91,705,fit(stock,10,92),MUTED,true);
        if(app.gifts.canClaim()){
            p.rect(192,670,84,44,0xffcb6414,10);p.gradient(193,671,82,41,0xffffdc8a,0xffff9e28,9);
            center("БЕСПЛАТНО",234,684,7,0xff663207,true);center("ЗАБРАТЬ ×3",234,702,9,0xff552509,true);
            buttons.add(new Button(192,670,84,44,"gift:claim"));
        }else{
            p.gradient(192,670,84,44,0xff214a89,0xff102b62,9);
            center("БЕСПЛАТНО",234,684,7,MUTED,true);
            center(app.gifts.freeCharges()==3?"ЗАПАС ПОЛОН":app.gifts.countdown(),234,702,9,MUTED,false);
        }
        chargeVideo(284,670,76,44);
        // Real completed count and stars replace the promotional 5/300 example.
        p.rect(28,739,344,33,0xff4387ed,9);p.gradient(29,740,342,31,0xff0a306f,0xff071b54,8);
        text(Levels.COUNT+" уровень",46,760,11,WHITE,true);
        p.rect(138,749,149,12,0xff5272b4,6);p.gradient(139,750,147,10,0xff234a8a,0xff112e68,5);
        if(app.completedCount()>0)p.gradient(139,750,Math.max(4,app.completedCount()/(float)Levels.COUNT*147),10,0xffffee7a,0xffffb321,5);
        center("★",305,761,20,GOLD,true);String starCount=app.totalStars()+" / "+(Levels.COUNT*3);text(starCount,319,759,fit(starCount,11,48),MUTED,true);
        p.gradient(28,781,344,31,0xff0a306f,0xff071b54,9);
        String status=app.adMessage.isEmpty()?"Прогресс сохраняется автоматически":app.adMessage;
        center(status,200,797,fit(status,10,340),MUTED,false);
    }
    /** Every confirmed video may grant another charge, independently of free gift limits. */
    private void chargeVideo(float x,float y,float w,float h){
        boolean eligible=app.gifts.canClaimAd(),ready=app.ads.rewardedReady();
        p.rect(x,y,w,h,eligible&&ready?0xffffc451:0xff4c76be,10);
        p.gradient(x+1,y+1,w-2,h-2,eligible&&ready?0xffffdc88:0xff20477e,eligible&&ready?0xffffa02c:0xff102b59,9);
        int color=eligible&&ready?0xff552509:WHITE;
        center("▶ ×3",x+w/2,y+17,15,color,true);
        String status=ready?"ЗА РЕКЛАМУ":"ЗАГРУЖАЕТСЯ";
        center(status,x+w/2,y+h-8,fit(status,7,w-8),color,true);
        if(eligible)buttons.add(new Button(x,y,w,h,"ad:charge"));
    }
    private void progress(){
        header("Мой прогресс","Все результаты сохраняются на этом устройстве.");
        p.rect(24,146,352,178,0xf00a2866,20);center("★ "+app.totalStars()+" / "+(Levels.COUNT*3),200,204,31,GOLD,true);
        center("Пройдено "+app.completedCount()+" из "+Levels.COUNT+" уровней",200,246,17,WHITE,true);
        p.rect(50,274,300,8,0xff244579,4);if(app.completedCount()>0)p.gradient(50,274,app.completedCount()/(float)Levels.COUNT*300,8,0xffffed9a,0xffffae2b,4);
        text("ПОСЛЕДНИЙ ВЫБРАННЫЙ",40,374,11,CYAN,true);
        Levels.Layout level=Levels.make(app.last);String selected="Уровень "+app.last+" · "+level.title();text(selected,40,406,fit(selected,15,320),WHITE,true);
        text("Лучший счёт: "+app.best[app.last]+"   ·   ★ "+app.stars[app.last]+" / 3",40,437,13,MUTED,false);
        button(40,481,320,55,"Выбрать уровень","levels",true);
        button(40,555,320,50,"В главное меню","home",false);
    }
    private void giftInfo(){
        header("Подарок ×3","Утраивает каждый летящий шар.");
        p.rect(24,146,352,390,0xf00a2866,20);
        center("×3",200,217,48,GOLD,true);String stock="В запасе: "+app.gifts.charges();center(stock,200,258,fit(stock,19,320),WHITE,true);
        String[] lines={"Бесплатный подарок — каждые 4 часа.","Бесплатно: до 3 получений за 24 часа.","Запас бесплатных зарядов: до 3.","Бесплатный заряд — один за попытку.","За рекламу — без суточного лимита.","Рекламные заряды — без лимита за уровень."};
        for(int i=0;i<lines.length;i++)text(lines[i],40,308+i*31,12,MUTED,false);
        if(app.gifts.canClaimAd())button(40,506,320,44,"Бонусы за рекламу  ▶","rewards",false);
        button(40,587,320,52,"В главное меню","home",true);
    }
    private void adInfo(){
        header("О рекламе","Рекламу предоставляет Рекламная сеть Яндекса.");
        p.rect(24,145,352,380,0xf00a2866,18);
        String[] lines={"Обычная реклама — между уровнями после 3-го.","Пауза между показами — не менее 180 секунд.","Бонусы за рекламу получаешь только по желанию.","Без сети можно продолжать обычную игру.","Сохранения и результаты хранятся на устройстве.","Яндекс обрабатывает технические данные", "для загрузки рекламы и измерения её показов.","Доступ к геолокации не запрашивается.","Реклама показывается без персонализации."};
        for(int i=0;i<lines.length;i++)text(lines[i],40,185+i*34,11,MUTED,false);
        button(40,552,320,44,"Политика игры","privacy:open",false);
        button(40,612,320,44,"Политика Яндекса","privacy:ads",false);
        button(40,680,320,50,"В главное меню","home",true);
    }
    private void adButton(float y,String title,String kind){
        if(app.rewardEligible(kind)&&app.ads.rewardedReady())button(40,y,320,48,title,"ad:"+kind,true);
        else disabled(40,y,320,48,app.rewardEligible(kind)?"Реклама пока недоступна":!app.hasSession||app.game.state==Engine.State.LOST||app.game.state==Engine.State.WON?"Доступно во время уровня":"Уже использовано в этой попытке");
    }
    private void rewards(){
        header("Бонусы","Награда после просмотра рекламы — на выбор.");
        p.rect(24,135,352,195,0xf00a2866,18);text("ЗАРЯД ×3",40,168,16,GOLD,true);
        text("Утраивает все летящие шары по кнопке в игре.",40,196,11,MUTED,false);
        String stock="В запасе: "+app.gifts.charges()+"  ·  За рекламу — без лимита";text(stock,40,225,fit(stock,12,320),WHITE,true);
        adButton(251,"Получить заряд ×3  ▶","charge");
        p.rect(24,352,352,190,0xf00a2866,18);text("ЗАЩИТНАЯ СТЕНА",40,387,16,CYAN,true);
        text("Защищает снизу 10 секунд активной игры.",40,416,12,MUTED,false);
        text("Один раз за попытку. Скорость ×4 не сокращает срок.",40,440,10,MUTED,false);
        adButton(466,"Включить стену  ▶","shield");
        if(!app.adMessage.isEmpty())center(app.adMessage,200,574,fit(app.adMessage,11,350),WHITE,false);
        button(40,609,320,50,app.rewardReturn.equals("game")?"Вернуться в игру":"Назад","rewardback",false);
        center("Если реклама недоступна, можно играть дальше",200,705,11,MUTED,false);
    }
    private void lostOverlay(){
        buttons.clear();p.rect(0,0,400,820,0xd9294663,0);p.rect(20,210,360,465,PANEL,24);
        center("ЕЩЁ ОДНА ПОПЫТКА?",200,267,20,WHITE,true);
        center("Разрушено "+Math.round(app.game.progress()*100)+"% блоков",200,302,13,MUTED,false);
        if(app.rewardEligible("revive")&&app.ads.rewardedReady())button(44,337,312,52,"Продолжить за рекламу  ▶","ad:revive",true);
        else disabled(44,337,312,52,app.reviveUsed?"Продолжение уже использовано":"Реклама пока недоступна");
        center("Вернётся 1 жизнь. Разбитые кирпичи сохранятся.",200,416,11,MUTED,false);
        button(44,443,312,50,"Начать заново","retry",false);button(44,507,312,48,"Карта уровней","levels",false);button(44,569,312,44,"В главное меню","home",false);
        if(!app.adMessage.isEmpty())center(app.adMessage,200,649,fit(app.adMessage,10,335),GOLD,false);
    }
    private void header(String title,String subtitle){button(20,20,92,44,"‹ Меню","home",false);text(title,128,49,22,WHITE,true);text(subtitle,24,105,12,MUTED,false);}
    private void levels(){
        header("Уровни","Пройди уровень, чтобы открыть следующий.");int start=app.page*AppModel.PAGE_SIZE+1;
        p.rect(24,125,352,63,PANEL,16);text(Levels.COUNT+" ЛАБИРИНТ · 1–3 ВЫХОДА",39,150,11,CYAN,true);
        text("Двойные стенки и узкие выходы к кирпичам",39,174,11,WHITE,false);
        for(int j=0;j<AppModel.PAGE_SIZE;j++){
            int n=start+j;if(n>Levels.COUNT)break;Levels.Layout l=Levels.make(n);float x=24+(j%4)*90,y=205+(j/4)*119;boolean active=n==app.last;
            p.rect(x,y,82,110,active?GOLD:0xff52758e,12);p.rect(x+1,y+1,80,108,0xff214663,11);
            text(String.format(java.util.Locale.ROOT,"%02d",n),x+7,y+17,12,active?GOLD:WHITE,true);
            if(l.artistic())text("МОЗАИКА",x+30,y+16,6,0xffe1b6fa,true);
            // Runtime cells form the thumbnail; no invented promotional layout.
            float cell=1.04f,ox=x+8,oy=y+23;
            p.rect(ox,oy,64*cell,68*cell,0xff12324b,1);
            p.thumbnail(l,ox,oy,64*cell,68*cell);
            if(app.isUnlocked(n)){
                for(int star=0;star<3;star++)center("★",x+25+star*16,y+105,10,app.stars[n]>star?GOLD:0xff58758e,false);
                buttons.add(new Button(x,y,82,110,"level:"+n));
            }else{
                p.rect(x+1,y+22,80,86,0xe0102440,10);float cx=x+41,cy=y+56;
                p.rect(cx-8,cy-14,16,18,MUTED,8);p.rect(cx-5,cy-11,10,16,0xff102440,5);
                p.rect(cx-11,cy-4,22,18,MUTED,4);p.circle(cx,cy+3,2,0xff102440);p.line(cx,cy+4,cx,cy+8,0xff102440,2);
                center("Пройди "+(n-1),cx,y+99,8,MUTED,false);
            }
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
        String[] lines={"Веди пальцем по полю — платформа следует.","Коснись поля, чтобы запустить шар.","Лови круглые бонусы платформой.","Направляй шары через проходы в стенах.","Жёлтый ×3 размножает все летящие шары.","Бонус стены защищает снизу 6 секунд.","Скорость игры: ×1 / ×2 / ×4.","Побеждай — открывай новые уровни."};
        for(int i=0;i<lines.length;i++)text(lines[i],24,419+i*26,13,MUTED,false);
        p.rect(24,636,352,91,PANEL,18);text("СОЗДАНО ДЛЯ КАСКАДОВ",42,665,11,LIME,true);text("Маршруты · отскоки · "+Levels.COUNT+" уровень",42,697,12,WHITE,false);
        button(24,739,352,38,"О рекламе и данных","adinfo",false);
        center("Брикс Шторм  ·  1.14.3",200,795,11,MUTED,false);
    }
    private void game(){
        Engine e=app.game;button(16,17,88,44,"‹ Меню","home",false);center("УРОВЕНЬ "+String.format(java.util.Locale.ROOT,"%03d",e.level.number),200,39,11,CYAN,true);center(e.level.title(),200,64,fit(e.level.title(),18,180),WHITE,true);icon(340,17,e.state==Engine.State.PAUSED?"▶":"Ⅱ","pause");
        text("СЧЁТ",18,101,9,MUTED,true);text(String.format(java.util.Locale.ROOT,"%,d",e.score).replace(',',' '),18,124,21,WHITE,true);
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
        if(e.shieldTime>0||app.adShieldTime>0){
            p.rect(16,Engine.SHIELD_Y-2,368,8,0x335cefd7,4);p.rect(16,Engine.SHIELD_Y,368,4,0xffa1f0d9,2);
            center("СТЕНА  "+(int)Math.ceil(app.adShieldTime>0?app.adShieldTime:e.shieldTime)+" с",200,Engine.SHIELD_Y-8,9,0xffa1f0d9,true);
        }
        text("ШАРЫ  "+e.ballCountText(),18,565,10,MUTED,true);text(e.level.difficulty(),273,565,9,CYAN,true);
        if(e.noticeTime>0)center(e.notice,200,599,9,LIME,true);
        else if(e.state==Engine.State.READY)center(e.level.hint(),200,586,8,MUTED,false);
        p.line(16,750,384,750,EDGE,1);button(16,766,80,40,"Уровни","levels",false);button(102,766,38,40,"×1","speed1",app.speed==1);button(146,766,38,40,"×2","speed2",app.speed==2);button(190,766,38,40,"×4","speed4",app.speed==4);
        String giftLabel=app.giftUsed&&app.gifts.adCharges()==0?"×3  ✓":"×3 · "+app.gifts.charges();
        if(app.canUseGift())button(234,766,68,40,giftLabel,"gift:use",true,fit(giftLabel,16,56));else disabled(234,766,68,40,giftLabel,fit(giftLabel,13,56));
        chargeVideo(308,766,76,40);
        if(!app.adMessage.isEmpty())center(app.adMessage,200,817,fit(app.adMessage,8,360),MUTED,false);
        if(e.state==Engine.State.PAUSED)overlay("ПАУЗА","Уровень "+e.level.number+" · "+e.level.title(),"Продолжить","resume","Карта уровней","levels",false);
        else if(e.state==Engine.State.WON)overlay(e.level.number==Levels.COUNT?"ШТОРМ ПОКОРЁН":"УРОВЕНЬ ПРОЙДЕН","★ "+e.stars()+" / 3     ·     СЧЁТ "+e.score,e.level.number==Levels.COUNT?"Карта уровней":"Следующий уровень",e.level.number==Levels.COUNT?"levels":"next","Пройти ещё раз","retry",true);
        else if(e.state==Engine.State.LOST)lostOverlay();
        if(e.state==Engine.State.PAUSED){chargeVideo(308,766,76,40);if(!app.adMessage.isEmpty())center(app.adMessage,200,817,fit(app.adMessage,8,360),MUTED,false);}
    }
    private void overlay(String title,String sub,String primary,String action,String second,String other,boolean success){
        buttons.clear();p.rect(0,0,400,820,0xd9294663,0);p.rect(20,app.game.state==Engine.State.PAUSED?205:238,360,app.game.state==Engine.State.PAUSED?480:376,PANEL,24);center(success?"✦":"Ⅱ",200,298,40,success?LIME:CYAN,true);center(title,200,340,19,WHITE,true);center(sub,200,371,12,MUTED,false);button(44,401,312,52,primary,action,true);button(44,465,312,48,second,other,false);center("Прогресс уровня сохранится до закрытия игры",200,app.game.state==Engine.State.PAUSED?657:594,10,MUTED,false);button(44,529,312,44,"В главное меню","home",false);
        if(app.game.state==Engine.State.PAUSED)button(44,589,312,44,"Бонусы за рекламу  ▶","rewards",false);
    }
}

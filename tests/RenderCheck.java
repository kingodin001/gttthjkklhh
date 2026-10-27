package pro.brickstorm.game;
import java.awt.*;import java.awt.geom.*;import java.awt.image.BufferedImage;import javax.imageio.ImageIO;import java.io.*;
/** Internal visual verification of shared screen geometry; never packaged in the Android app. */
public final class RenderCheck implements Painter {
    final BufferedImage image=new BufferedImage(800,1640,BufferedImage.TYPE_INT_RGB);final Graphics2D g=image.createGraphics();
    BufferedImage menuArt,gameArt;RenderCheck()throws Exception{g.scale(2,2);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);menuArt=ImageIO.read(new File("app/src/main/res/drawable-nodpi/arcade_menu.png"));gameArt=ImageIO.read(new File("app/src/main/res/drawable-nodpi/arcade_background.png"));}
    void color(int c){g.setColor(new Color(c,true));}
    public void rect(float x,float y,float w,float h,int c,float r){color(c);g.fill(new RoundRectangle2D.Float(x,y,w,h,r*2,r*2));}
    public void circle(float x,float y,float r,int c){color(c);g.fill(new Ellipse2D.Float(x-r,y-r,r*2,r*2));}
    public void line(float x,float y,float x2,float y2,int c,float w){color(c);g.setStroke(new BasicStroke(w));g.draw(new Line2D.Float(x,y,x2,y2));}
    public void text(String s,float x,float y,float size,int c,boolean bold,boolean centered){color(c);g.setFont(new Font("DejaVu Sans",bold?Font.BOLD:Font.PLAIN,1).deriveFont(size));if(centered)x-=g.getFontMetrics().stringWidth(s)/2f;g.drawString(s,x,y);}
    public float textWidth(String s,float size,boolean bold){g.setFont(new Font("DejaVu Sans",bold?Font.BOLD:Font.PLAIN,1).deriveFont(size));return g.getFontMetrics().stringWidth(s);}
    public void artwork(boolean home){g.drawImage(home?menuArt:gameArt,0,0,400,820,null);}
    public void gradient(float x,float y,float w,float h,int top,int bottom,float radius){
        Paint old=g.getPaint();g.setPaint(new GradientPaint(x,y,new Color(top,true),x,y+h,new Color(bottom,true)));
        g.fill(new RoundRectangle2D.Float(x,y,w,h,radius*2,radius*2));g.setPaint(old);
    }
    public void board(Engine e){for(int i=0;i<67*64;i++){
        float x=16+i%64*Levels.CELL,y=146+i/64*Levels.CELL;int hp=e.cells[i];
        if(hp<0){rect(x,y,Levels.CELL,Levels.CELL,0xffa9bfce,0);line(x,y+.6f,x+Levels.CELL,y+.6f,0xffdceaf1,.8f);}
        else if(hp>0){rect(x+.35f,y+.35f,Levels.CELL-.7f,Levels.CELL-.7f,e.level.colors[i],0);rect(x+.55f,y+.4f,Levels.CELL-1.1f,.65f,0x80ffffff,0);if(hp>1)rect(x+2,y+2,1.5f,1.5f,0xffeef5ff,0);}
    }}
    public static void main(String[] args)throws Exception{
        EngineTest.Memory earned=new EngineTest.Memory();for(int i=1;i<=Levels.COUNT;i++)earned.put("level_stars_"+Levels.make(i).id(),1);AppModel a=new AppModel(earned);GameRenderer r=new GameRenderer();int access=0;
        for(int page=0;page<AppModel.PAGES;page++){
            a.screen="levels";a.page=page;RenderCheck p=new RenderCheck();r.draw(p,a);ImageIO.write(p.image,"png",new File("verification/menu-page-"+(page+1)+".png"));
            for(int j=0;j<AppModel.PAGE_SIZE&&page*AppModel.PAGE_SIZE+j<Levels.COUNT;j++){int n=page*AppModel.PAGE_SIZE+j+1;float x=24+j%4*90+41,y=205+j/4*119+55;if(!("level:"+n).equals(r.hit(x,y)))throw new AssertionError("Wrong card "+n);a.action(r.hit(x,y));if(a.game.level.number!=n)throw new AssertionError("Wrong level launch");access++;}
            int empty=page*AppModel.PAGE_SIZE+AppModel.PAGE_SIZE-1;if(empty>=Levels.COUNT&&r.hit(335,617)!=null)throw new AssertionError("Phantom last-page card");
        }
        AppModel fresh=new AppModel(new EngineTest.Memory());
        for(int page=0;page<AppModel.PAGES;page++){
            fresh.screen="levels";fresh.page=page;RenderCheck locked=new RenderCheck();r.draw(locked,fresh);
            if(page==0||page==AppModel.PAGES-1)ImageIO.write(locked.image,"png",new File("verification/locked-page-"+(page+1)+".png"));
            for(int j=0;j<AppModel.PAGE_SIZE&&page*AppModel.PAGE_SIZE+j<Levels.COUNT;j++){
                int n=page*AppModel.PAGE_SIZE+j+1;String hit=r.hit(24+j%4*90+41,205+j/4*119+55);
                if(n==1?!"level:1".equals(hit):hit!=null)throw new AssertionError("Fresh locked card remains clickable "+n);
            }
        }
        fresh.completed(1,2,200);fresh.screen="levels";fresh.page=0;RenderCheck unlocked=new RenderCheck();r.draw(unlocked,fresh);
        if(!"level:2".equals(r.hit(155,260))||r.hit(245,260)!=null)throw new AssertionError("Win must unlock only the next card");
        ImageIO.write(unlocked.image,"png",new File("verification/unlocked-after-win.png"));
        for(int n:new int[]{1,16,25,100,107,199,200,201,202,203,204,205,206,207,208,209,210,225,250,275,299,300}){a.start(n);RenderCheck p=new RenderCheck();r.draw(p,a);ImageIO.write(p.image,"png",new File("verification/game-"+n+".png"));}
        a.screen="home";RenderCheck home=new RenderCheck();r.draw(home,a);ImageIO.write(home.image,"png",new File("verification/home.png"));
        if(!"gift:claim".equals(r.hit(293,691)))throw new AssertionError("Gift claim control is missing");
        if(!"play".equals(r.hit(200,510))||!"levels".equals(r.hit(100,615))||!"progress".equals(r.hit(285,615))||!"settings".equals(r.hit(365,36))||!"giftinfo".equals(r.hit(352,690)))throw new AssertionError("Approved menu artwork and live touch controls disagree");
        a.action("progress");RenderCheck progress=new RenderCheck();r.draw(progress,a);ImageIO.write(progress.image,"png",new File("verification/progress.png"));if(!"levels".equals(r.hit(200,508)))throw new AssertionError("Progress screen is not functional");a.back();
        a.action("giftinfo");RenderCheck info=new RenderCheck();r.draw(info,a);ImageIO.write(info.image,"png",new File("verification/gift-info.png"));if(!"home".equals(r.hit(200,590)))throw new AssertionError("Gift information cannot return home");a.back();
        a.action("gift:claim");RenderCheck claimed=new RenderCheck();r.draw(claimed,a);ImageIO.write(claimed.image,"png",new File("verification/home-claimed.png"));
        if(r.hit(293,691)!=null||a.gifts.charges()!=1)throw new AssertionError("Cooldown card must not claim again");
        a.start(201);RenderCheck ready=new RenderCheck();r.draw(ready,a);if(r.hit(330,785)!=null)throw new AssertionError("Gift use must wait for launched ball");
        a.game.launch();RenderCheck flying=new RenderCheck();r.draw(flying,a);ImageIO.write(flying.image,"png",new File("verification/gift-available.png"));
        if(!"gift:use".equals(r.hit(330,785)))throw new AssertionError("Gift use control is missing");
        if(!"speed4".equals(r.hit(250,785))||!"levels".equals(r.hit(60,785)))throw new AssertionError("Gift button overlaps existing controls");
        a.action("gift:use");RenderCheck used=new RenderCheck();r.draw(used,a);ImageIO.write(used.image,"png",new File("verification/gift-used.png"));
        if(r.hit(330,785)!=null||a.gifts.charges()!=0)throw new AssertionError("Spent gift remains actionable");
        a.screen="home";a.stars[1]=3;a.stars[107]=2;RenderCheck saved=new RenderCheck();r.draw(saved,a);ImageIO.write(saved.image,"png",new File("verification/home-with-progress.png"));if(!"progress".equals(r.hit(50,36)))throw new AssertionError("Live stars counter has no progress action");
        for(int i=1;i<=Levels.COUNT;i++)a.stars[i]=3;RenderCheck full=new RenderCheck();r.draw(full,a);ImageIO.write(full.image,"png",new File("verification/home-complete.png"));if(a.totalStars()!=900)throw new AssertionError("Wrong maximum star total");
        a.screen="settings";RenderCheck settings=new RenderCheck();r.draw(settings,a);ImageIO.write(settings.image,"png",new File("verification/settings.png"));
        System.out.println("PASS: all "+access+" earned level cards are clickable across "+AppModel.PAGES+" pages; fresh install locks, sequential unlock, approved menu controls, live progress, gift stock/timer and shared renderer screenshots verified internally.");
    }
}

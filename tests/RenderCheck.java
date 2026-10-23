package pro.brickstorm.game;
import java.awt.*;import java.awt.geom.*;import java.awt.image.BufferedImage;import javax.imageio.ImageIO;import java.io.*;
/** Internal visual verification of shared screen geometry; never packaged in the Android app. */
public final class RenderCheck implements Painter {
    final BufferedImage image=new BufferedImage(800,1640,BufferedImage.TYPE_INT_RGB);final Graphics2D g=image.createGraphics();
    BufferedImage art;RenderCheck()throws Exception{g.scale(2,2);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);art=ImageIO.read(new File("app/src/main/res/drawable-nodpi/menu_art.png"));}
    void color(int c){g.setColor(new Color(c,true));}
    public void rect(float x,float y,float w,float h,int c,float r){color(c);g.fill(new RoundRectangle2D.Float(x,y,w,h,r*2,r*2));}
    public void circle(float x,float y,float r,int c){color(c);g.fill(new Ellipse2D.Float(x-r,y-r,r*2,r*2));}
    public void line(float x,float y,float x2,float y2,int c,float w){color(c);g.setStroke(new BasicStroke(w));g.draw(new Line2D.Float(x,y,x2,y2));}
    public void text(String s,float x,float y,float size,int c,boolean bold,boolean centered){color(c);g.setFont(new Font("DejaVu Sans",bold?Font.BOLD:Font.PLAIN,1).deriveFont(size));if(centered)x-=g.getFontMetrics().stringWidth(s)/2f;g.drawString(s,x,y);}
    public void artwork(){int crop=(int)(art.getHeight()*400f/820f),left=(art.getWidth()-crop)/2;g.drawImage(art,0,0,400,820,left,0,left+crop,art.getHeight(),null);}
    public void board(Engine e){for(int i=0;i<e.cells.length;i++){
        float x=16+i%64*Levels.CELL,y=146+i/64*Levels.CELL;int hp=e.cells[i];
        if(hp<0){rect(x,y,Levels.CELL,Levels.CELL,0xffa9bfce,0);line(x,y+.6f,x+Levels.CELL,y+.6f,0xffdceaf1,.8f);}
        else if(hp>0){rect(x+.35f,y+.35f,Levels.CELL-.7f,Levels.CELL-.7f,e.level.colors[i],0);rect(x+.55f,y+.4f,Levels.CELL-1.1f,.65f,0x80ffffff,0);if(hp>1)rect(x+2,y+2,1.5f,1.5f,0xffeef5ff,0);}
    }}
    public static void main(String[] args)throws Exception{
        AppModel a=new AppModel(new EngineTest.Memory());GameRenderer r=new GameRenderer();int access=0;
        for(int page=0;page<AppModel.PAGES;page++){
            a.screen="levels";a.page=page;RenderCheck p=new RenderCheck();r.draw(p,a);ImageIO.write(p.image,"png",new File("verification/menu-page-"+(page+1)+".png"));
            for(int j=0;j<AppModel.PAGE_SIZE&&page*AppModel.PAGE_SIZE+j<Levels.COUNT;j++){int n=page*AppModel.PAGE_SIZE+j+1;float x=24+j%4*90+41,y=205+j/4*119+55;if(!("level:"+n).equals(r.hit(x,y)))throw new AssertionError("Wrong card "+n);a.action(r.hit(x,y));if(a.game.level.number!=n)throw new AssertionError("Wrong level launch");access++;}
            int empty=page*AppModel.PAGE_SIZE+AppModel.PAGE_SIZE-1;if(empty>=Levels.COUNT&&r.hit(335,617)!=null)throw new AssertionError("Phantom last-page card");
        }
        for(int n:new int[]{1,6,11,25,44,58}){a.start(n);RenderCheck p=new RenderCheck();r.draw(p,a);ImageIO.write(p.image,"png",new File("verification/game-"+n+".png"));}
        a.screen="home";RenderCheck home=new RenderCheck();r.draw(home,a);ImageIO.write(home.image,"png",new File("verification/home.png"));
        a.screen="settings";RenderCheck settings=new RenderCheck();r.draw(settings,a);ImageIO.write(settings.image,"png",new File("verification/settings.png"));
        System.out.println("PASS: all "+access+" real level cards are clickable across "+AppModel.PAGES+" pages; shared renderer screenshots exported for internal visual inspection.");
    }
}

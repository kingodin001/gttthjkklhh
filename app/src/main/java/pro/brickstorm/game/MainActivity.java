package pro.brickstorm.game;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import java.io.*;
import java.util.Arrays;

public final class MainActivity extends Activity {
    private GameView gameView;
    @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);gameView=new GameView(this);setContentView(gameView);}
    @Override protected void onResume(){super.onResume();gameView.resume();}
    @Override protected void onPause(){gameView.pause();super.onPause();}
    @Override protected void onDestroy(){gameView.audio.release();super.onDestroy();}
    @Override public void onBackPressed(){gameView.model.back();gameView.invalidate();}

    static final class GameView extends View implements AppModel.Storage {
        final SharedPreferences prefs;final Audio audio;final AppModel model;final GameRenderer renderer=new GameRenderer();final AndroidPainter painter=new AndroidPainter();
        private int insetTop,insetBottom,insetLeft,insetRight;private float scale=1,dx=0,dy=0;private long lastFrame=0;private boolean running;private String downAction;private int pointer=-1;
        GameView(Context c){super(c);prefs=c.getSharedPreferences("campaign_v1",Context.MODE_PRIVATE);audio=new Audio(c);model=new AppModel(this);painter.loadArtwork(c);setFocusable(true);setContentDescription("Брикс Шторм. Арканоид. Управление касанием и движением пальца.");setLayerType(View.LAYER_TYPE_HARDWARE,null);
            setOnApplyWindowInsetsListener((v,insets)->{if(Build.VERSION.SDK_INT>=30){android.graphics.Insets i=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());insetTop=i.top;insetBottom=i.bottom;insetLeft=i.left;insetRight=i.right;}else{insetTop=insets.getSystemWindowInsetTop();insetBottom=insets.getSystemWindowInsetBottom();insetLeft=insets.getSystemWindowInsetLeft();insetRight=insets.getSystemWindowInsetRight();}invalidate();return insets;});
        }
        void resume(){running=true;lastFrame=0;postInvalidateOnAnimation();}
        void pause(){running=false;if(model.game.state==Engine.State.PLAYING)model.game.togglePause();lastFrame=0;}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);long now=System.nanoTime();float dt=lastFrame==0?0:Math.min(.05f,(now-lastFrame)/1_000_000_000f);lastFrame=now;if(running&&model.screen.equals("game")){model.tick(dt);}
            canvas.drawColor(GameRenderer.BG);float availableW=getWidth()-insetLeft-insetRight,availableH=getHeight()-insetTop-insetBottom;
            scale=Math.min(availableW/400f,availableH/820f);dx=insetLeft+(availableW-400*scale)/2;dy=insetTop+(availableH-820*scale)/2;
            if(scale>0){canvas.save();canvas.translate(dx,dy);canvas.scale(scale,scale);canvas.clipRect(0,0,400,820);painter.canvas=canvas;renderer.draw(painter,model);canvas.restore();}if(running)postInvalidateOnAnimation();
        }
        @Override public boolean onTouchEvent(android.view.MotionEvent event){
            int act=event.getActionMasked();if(act==android.view.MotionEvent.ACTION_DOWN){pointer=event.getPointerId(0);float x=(event.getX()-dx)/scale,y=(event.getY()-dy)/scale;downAction=renderer.hit(x,y);if(downAction==null&&model.screen.equals("game")&&y>140&&y<751)model.game.aim(x);return true;}
            int index=event.findPointerIndex(pointer);if(index<0)return true;float x=(event.getX(index)-dx)/scale,y=(event.getY(index)-dy)/scale;
            if(act==android.view.MotionEvent.ACTION_MOVE){if(downAction==null&&model.screen.equals("game")&&y>140&&y<751)model.game.aim(x);return true;}
            if(act==android.view.MotionEvent.ACTION_UP||(act==android.view.MotionEvent.ACTION_POINTER_UP&&event.getPointerId(event.getActionIndex())==pointer)){
                String up=renderer.hit(x,y);if(downAction!=null&&downAction.equals(up))model.action(up);else if(downAction==null&&model.screen.equals("game")&&y>140&&y<751)model.game.launch();pointer=-1;downAction=null;performClick();return true;
            }
            if(act==android.view.MotionEvent.ACTION_CANCEL){pointer=-1;downAction=null;}return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
        @Override public int get(String k,int fallback){return prefs.getInt(k,fallback);}
        @Override public void put(String k,int value){prefs.edit().putInt(k,value).apply();}
        @Override public void sound(int kind){audio.play(kind);}
    }
    static final class AndroidPainter implements Painter {
        Canvas canvas;final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);final RectF rect=new RectF();Bitmap board;Canvas boardCanvas;final Paint pixel=new Paint();int[] previous;Engine lastEngine;int lastLevel=-1;
        Bitmap artwork;final Bitmap[] thumbnails=new Bitmap[Levels.COUNT];
        void loadArtwork(Context c){int id=c.getResources().getIdentifier("menu_art","drawable",c.getPackageName());if(id!=0)artwork=BitmapFactory.decodeResource(c.getResources(),id);}
        @Override public void artwork(){if(artwork==null)return;paint.setColor(Color.WHITE);paint.setFilterBitmap(true);rect.set(0,0,400,820);int crop=(int)(artwork.getHeight()*400f/820f);int left=(artwork.getWidth()-crop)/2;canvas.drawBitmap(artwork,new Rect(left,0,left+crop,artwork.getHeight()),rect,paint);}
        final Typeface regular=Typeface.create("sans-serif",Typeface.NORMAL),bold=Typeface.create("sans-serif",Typeface.BOLD);
        @Override public void rect(float x,float y,float w,float h,int color,float radius){paint.setColor(color);rect.set(x,y,x+w,y+h);canvas.drawRoundRect(rect,radius,radius,paint);}
        @Override public void circle(float x,float y,float r,int color){paint.setColor(color);canvas.drawCircle(x,y,r,paint);}
        @Override public void line(float x,float y,float x2,float y2,int color,float width){paint.setColor(color);paint.setStrokeWidth(width);canvas.drawLine(x,y,x2,y2,paint);}
        @Override public void text(String s,float x,float y,float size,int color,boolean heavy,boolean centered){paint.setColor(color);paint.setTextSize(size);paint.setTypeface(heavy?bold:regular);paint.setTextAlign(centered?Paint.Align.CENTER:Paint.Align.LEFT);canvas.drawText(s,x,y,paint);}
        @Override public void thumbnail(Levels.Layout l,float x,float y,float w,float h){
            Bitmap thumb=thumbnails[l.number-1];
            if(thumb==null){
                thumb=Bitmap.createBitmap(128,136,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(thumb);
                for(int i=0;i<l.cells.length;i++)if(l.cells[i]!=0){pixel.setColor(l.cells[i]<0?0xffb3cbdc:l.colors[i]);float px=i%64*2,py=i/64*2;c.drawRect(px,py,px+2,py+2,pixel);}
                thumbnails[l.number-1]=thumb;
            }
            paint.setColor(Color.WHITE);paint.setFilterBitmap(false);rect.set(x,y,x+w,y+h);canvas.drawBitmap(thumb,null,rect,paint);
        }
        @Override public void board(Engine e){
            if(board==null){board=Bitmap.createBitmap(368,391,Bitmap.Config.ARGB_8888);boardCanvas=new Canvas(board);previous=new int[e.cells.length];Arrays.fill(previous,Integer.MIN_VALUE);}
            if(lastLevel!=e.level.number||lastEngine!=e){Arrays.fill(previous,Integer.MIN_VALUE);lastLevel=e.level.number;lastEngine=e;}
            for(int i=0;i<e.cells.length;i++)if(previous[i]!=e.cells[i]){
                float x=i%64*Levels.CELL,y=i/64*Levels.CELL;pixel.setColor(0xff102f4b);boardCanvas.drawRect(x,y,x+Levels.CELL,y+Levels.CELL,pixel);
                int hp=e.cells[i];if(hp<0){
                    // One continuous dark steel face: unlike empty space, every pixel is solid.
                    pixel.setColor(0xff9fb5c6);boardCanvas.drawRect(x,y,x+Levels.CELL,y+Levels.CELL,pixel);
                    pixel.setColor(0xffeef7ff);boardCanvas.drawRect(x,y,x+Levels.CELL,y+.65f,pixel);
                    pixel.setColor(0xff566f85);boardCanvas.drawRect(x,y,x+.6f,y+Levels.CELL,pixel);
                }else if(hp>0){pixel.setColor(e.level.colors[i]);boardCanvas.drawRect(x+.35f,y+.35f,x+Levels.CELL-.35f,y+Levels.CELL-.35f,pixel);
                    pixel.setColor(0x80ffffff);boardCanvas.drawRect(x+.55f,y+.4f,x+Levels.CELL-.55f,y+1.05f,pixel);
                    pixel.setColor(0x40203450);boardCanvas.drawRect(x+.4f,y+Levels.CELL-1.05f,x+Levels.CELL-.4f,y+Levels.CELL-.35f,pixel);
                    if(hp>1){pixel.setColor(0xffeef5ff);boardCanvas.drawRect(x+2,y+2,x+3.5f,y+3.5f,pixel);}}
                previous[i]=hp;
            }
            canvas.drawBitmap(board,16,146,null);
            for(int y=0;y<68;y++){int x=0;while(x<64){if(e.cells[y*64+x]!=-1){x++;continue;}int start=x;while(x<64&&e.cells[y*64+x]==-1)x++;
                float l=16+start*Levels.CELL,t=146+y*Levels.CELL,w=(x-start)*Levels.CELL;
                rect(l,t,w,Levels.CELL,0xffa9bfce,0);line(l,t+.6f,l+w,t+.6f,0xffdceaf1,.8f);line(l,t+Levels.CELL-.6f,l+w,t+Levels.CELL-.6f,0xff566f85,1);
            }}
        }
    }
    static final class Audio {
        final SoundPool pool;final int[] ids=new int[6];long last=0;
        Audio(Context c){pool=new SoundPool.Builder().setMaxStreams(6).setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();
            int[] hz={520,340,760,1040,170,1320};for(int k=0;k<6;k++)try{File f=new File(c.getCacheDir(),"tone"+k+".wav");write(f,hz[k],k==5?.25:.055);ids[k]=pool.load(f.getAbsolutePath(),1);}catch(IOException ignored){}
        }
        void play(int kind){long now=System.nanoTime();if(kind==2&&now-last<45_000_000)return;last=now;pool.play(ids[Math.max(0,Math.min(5,kind))],.22f,.22f,1,0,1);}
        void release(){pool.release();}
        static void write(File f,int hz,double seconds)throws IOException{int n=(int)(22050*seconds);try(DataOutputStream o=new DataOutputStream(new FileOutputStream(f))){o.writeBytes("RIFF");le32(o,36+n*2);o.writeBytes("WAVEfmt ");le32(o,16);le16(o,1);le16(o,1);le32(o,22050);le32(o,44100);le16(o,2);le16(o,16);o.writeBytes("data");le32(o,n*2);for(int i=0;i<n;i++){double envelope=Math.pow(1-i/(double)n,3)*Math.min(1,i/50.0);short v=(short)(Math.sin(2*Math.PI*hz*i/22050)*envelope*18000);le16(o,v);}}}
        static void le16(DataOutputStream o,int v)throws IOException{o.writeByte(v&255);o.writeByte((v>>8)&255);}static void le32(DataOutputStream o,int v)throws IOException{le16(o,v);le16(o,v>>16);}
    }
}

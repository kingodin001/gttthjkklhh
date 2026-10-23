package pro.brickstorm.game;
public interface Painter {
    void rect(float x,float y,float w,float h,int color,float radius);
    void circle(float x,float y,float radius,int color);
    void line(float x,float y,float x2,float y2,int color,float width);
    void text(String value,float x,float baseline,float size,int color,boolean bold,boolean center);
    void board(Engine e);
    default void artwork(){}
    default void thumbnail(Levels.Layout l,float x,float y,float width,float height){
        float cw=width/Levels.COLS,ch=height/Levels.ROWS;
        for(int i=0;i<l.cells.length;i++)if(l.cells[i]!=0)rect(x+(i%64)*cw,y+(i/64)*ch,cw,ch,l.cells[i]<0?0xffb3cbdc:l.colors[i],0);
    }
}

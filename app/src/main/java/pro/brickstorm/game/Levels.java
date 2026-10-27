package pro.brickstorm.game;

/** 300 densely filled puzzles with fully enclosed maze corridors. */
public final class Levels {
    public static final int COUNT=300, COLS=64, ROWS=68;
    public static final float LEFT=16, TOP=146, CELL=5.75f, RIGHT=384, BOTTOM=TOP+ROWS*CELL, FLOOR_TOP=TOP+67*CELL;
    private static final Layout[] CACHE=new Layout[COUNT];
    public static final int[] PALETTE={0xffff705b,0xffffb344,0xffffe16b,0xff75d8bc,0xff42d5e4,0xff7998ef,0xffbfa1ff,0xfff695c5,0xffecf1fa,0xff7494b7,0xffb7e767,0xffe4ba70,0xffa8dffa,0xffed8a9b,0xff7bc8d5,0xffd6beec};
    public static final class Layout {
        public final int number; public final int[] cells,colors,gates; public final int blocks;
        public final int gateStart,gateEnd; public final float speed,paddleWidth,gateWidth; public final float[] floorSegments; public final int targetSeconds;
        Layout(int n,int[] c,int[] col,int count){number=n;cells=c;colors=col;blocks=count;gates=LevelData.GATES[n-1].clone();gateStart=gates[0];gateEnd=gates[1];gateWidth=LevelData.GATE_WIDTHS[n-1];floorSegments=new float[gates.length+2];float cursor=LEFT;for(int g=0;g<gates.length;g+=2){floorSegments[g]=cursor;floorSegments[g+1]=gateLeft(g);cursor=gateRight(g);}floorSegments[gates.length]=cursor;floorSegments[gates.length+1]=RIGHT;speed=190+(n-1)*.6f;paddleWidth=144-(n-1)*.24f;targetSeconds=240+(n-1)*5;}
        public int id(){return LevelData.IDS[number-1];}
        public float gateCenter(int g){return LEFT+(gates[g]+gates[g+1])*.5f*CELL;}
        public float gateLeft(int g){return gateCenter(g)-gateWidth/2;}
        public float gateRight(int g){return gateCenter(g)+gateWidth/2;}
        public String title(){return LevelData.TITLE[number-1];}
        public String hint(){return LevelData.HINT[number-1];}
        public String family(){return LevelData.FAMILY[number-1];}
        public boolean artistic(){return false;}
        public int portCount(){return LevelData.PORTS[number-1];}
        public String difficulty(){return number<=20?"ПЕРВЫЕ ОТСКОКИ":number<=60?"СРЕДНИЙ":number<=110?"ВЫШЕ СРЕДНЕГО":number<=160?"СЛОЖНЫЙ":"МАСТЕР";}
    }
    public static Layout make(int n){
        if(n<1||n>COUNT)throw new IllegalArgumentException("Level must be 1.."+COUNT);
        if(CACHE[n-1]!=null)return CACHE[n-1];
        String data=LevelData.CELLS[n-1];int[] cells=new int[COLS*ROWS],colors=new int[cells.length];int count=0;
        for(int i=0;i<cells.length;i++){char c=data.charAt(i);if(c=='#'){cells[i]=-1;colors[i]=0xffa8bfd2;}else if(c!='.'){boolean strong=c>='A'&&c<='P';int color=strong?c-'A':c-'a';cells[i]=strong?2:1;colors[i]=PALETTE[color];count++;}}
        return CACHE[n-1]=new Layout(n,cells,colors,count);
    }
    public static int positionOf(int id){for(int n=1;n<=COUNT;n++)if(LevelData.IDS[n-1]==id)return n;return 1;}
    public static int gateStart(int n){return LevelData.GATES[n-1][0];}
    public static int gateEnd(int n){return LevelData.GATES[n-1][1];}
    private Levels(){}
}

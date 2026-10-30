package pro.brickstorm.game;

/** Free gifts keep their limits; confirmed ad rewards have a separate unlimited inventory. */
public final class RewardBank {
    public static final int CAPACITY=3, DAILY_LIMIT=3;
    public static final long COOLDOWN=4*60*60, DAY=24*60*60;
    private final AppModel.Storage storage;
    private final long[] claims=new long[DAILY_LIMIT];
    private int stock;
    private long adStock;
    private long lastSeen;

    RewardBank(AppModel.Storage storage){
        this.storage=storage;stock=Math.max(0,Math.min(CAPACITY,storage.get("gift_stock",0)));
        adStock=Math.max(0,storage.getLong("gift_ad_stock",0));
        lastSeen=Math.max(0,storage.getLong("gift_last_seen",0));
        for(int i=0;i<claims.length;i++)claims[i]=Math.max(0,storage.getLong("gift_claim_"+i,0));
        java.util.Arrays.sort(claims);
    }
    private long now(){lastSeen=Math.max(lastSeen,Math.max(0,storage.nowSeconds()));return lastSeen;}
    public long charges(){return adStock>Long.MAX_VALUE-stock?Long.MAX_VALUE:stock+adStock;}
    public int freeCharges(){return stock;}
    public long adCharges(){return adStock;}
    public int claimsLeft(){long t=now();int recent=0;for(long claim:claims)if(claim>0&&t-claim<DAY)recent++;return DAILY_LIMIT-recent;}
    public long waitSeconds(){
        long t=now(),next=claims[2]>0?claims[2]+COOLDOWN:0;
        if(claims[0]>0&&t-claims[0]<DAY)next=Math.max(next,claims[0]+DAY);
        return Math.max(0,next-t);
    }
    public boolean canClaim(){return stock<CAPACITY&&waitSeconds()==0;}
    public boolean canClaimAd(){return true;}
    public boolean claimAd(){if(adStock<Long.MAX_VALUE)adStock++;now();persist();return true;}
    public boolean claim(){
        if(!canClaim())return false;
        long t=now();claims[0]=claims[1];claims[1]=claims[2];claims[2]=t;stock++;persist();return true;
    }
    public boolean canSpend(boolean freeAllowed){return (freeAllowed&&stock>0)||adStock>0;}
    boolean spend(){return spend(true);}
    boolean spend(boolean freeAllowed){if(freeAllowed&&stock>0)stock--;else if(adStock>0)adStock--;else return false;now();persist();return true;}
    private void persist(){storage.saveChargeState(stock,adStock,lastSeen,claims.clone());}
    public String countdown(){long t=waitSeconds();return String.format(java.util.Locale.ROOT,"%02d:%02d:%02d",t/3600,t/60%60,t%60);}
}

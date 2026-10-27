#!/usr/bin/env python3
"""200 mazes: 25 authored route families × eight nonlinear geometry variants.
Complete double banks, a single bottom inlet, exactly 1–3 brick-facing ports.
Structural connectivity does not constitute an automated campaign playthrough.
"""
from pathlib import Path
from collections import deque, Counter
import json, math
ROOT=Path(__file__).resolve().parents[1]
W,H=64,68
D4=((1,0),(-1,0),(0,1),(0,-1))
D8=tuple((x,y) for x in (-1,0,1) for y in (-1,0,1))
def route(title,*paths):return dict(title=title,paths=[list(p) for p in paths])
FAMILIES=[
route('Три контура',[(32,66),(32,58),(9,58),(9,19),(54,19),(54,49),(20,49),(20,30),(43,30),(43,40),(31,40)]),
route('Левый шлюз',[(9,66),(9,58),(34,58),(34,48),(8,48),(8,37),(42,37),(42,25),(18,25),(18,12),(48,12)]),
route('Двойной затвор',[(52,66),(52,16),(40,16),(40,57),(28,57),(28,14),(16,14),(16,49),(7,49)]),
route('Длинный серпантин',[(53,66),(53,58),(39,58),(39,45),(53,45),(53,32),(30,32),(30,19),(47,19),(47,7),(22,7)]),
route('Спиральный шлюз',[(32,66),(32,59),(7,59),(7,8),(56,8),(56,50),(17,50),(17,18),(46,18),(46,40),(27,40),(27,28),(36,28)]),
route('Ступени и обход',[(9,66),(9,57),(21,57),(21,45),(34,45),(34,33),(47,33),(47,20),(56,20),(56,8),(13,8),(13,22),(25,22)]),
route('Два маршрута',[(31,66),(31,57),(10,57),(10,14),(31,14),(31,7),(51,7)],[(31,57),(53,57),(53,43),(42,43),(42,29),(31,29),(31,14)]),
route('Двойной ромб',[(32,66),(32,59),(8,35),(32,9),(56,35),(32,59),(32,49),(46,35),(32,21),(18,35),(32,49)]),
route('Восьмёрка',[(32,66),(32,57),(9,43),(32,29),(55,16),(32,5),(9,16),(32,29),(55,43),(32,57)]),
route('Лестницы навстречу',[(8,66),(8,58),(21,58),(21,45),(10,45),(10,31),(31,31),(31,17),(50,17),(50,7)],[(31,31),(52,31),(52,48),(41,48),(41,58),(55,58)]),
route('Змеиный гребень',[(10,66),(10,8),(22,8),(22,54),(34,54),(34,18),(46,18),(46,55),(56,55),(56,7)]),
route('Зигзаг к вершине',[(12,66),(12,58),(51,45),(12,32),(51,19),(25,6)]),
route('Скошенная крепость',[(22,66),(22,59),(7,47),(13,10),(50,7),(57,48),(31,53),(23,26),(42,21),(46,38),(34,41)]),
route('Три крыла',[(31,66),(31,49),(12,37),(8,14),(27,20),(31,7)],[(31,49),(53,39),(56,15),(37,21),(31,7)],[(27,20),(31,31),(37,21)]),
route('Квадратный меандр',[(51,66),(51,57),(9,57),(9,44),(31,44),(31,31),(9,31),(9,9),(53,9),(53,22),(42,22),(42,44),(54,44)]),
route('Песочные ворота',[(31,66),(31,58),(9,46),(31,32),(54,18),(31,6),(9,18),(31,32),(54,46),(31,58)]),
route('Шесть граней',[(32,66),(32,58),(10,45),(10,20),(32,7),(54,20),(54,45),(32,58)],[(10,20),(32,32),(54,20)],[(32,32),(32,45)]),
route('Треугольный обход',[(32,66),(32,58),(8,58),(32,8),(56,58),(32,58)],[(20,33),(43,33),(32,19)]),
route('Два крючка',[(12,66),(12,58),(51,58),(51,11),(28,11),(28,25),(40,25),(40,44),(15,44),(15,14),(6,14)]),
route('Сердце маршрута',[(32,66),(32,58),(9,34),(9,16),(20,8),(32,20),(44,8),(55,16),(55,34),(32,58)],[(32,58),(32,37),(21,28)]),
route('Вилка с обходом',[(31,66),(31,53),(10,53),(10,11),(26,11),(26,34),(43,34),(43,9),(55,9)],[(31,53),(53,53),(53,44),(43,44),(43,34)]),
route('Коридоры цитадели',[(53,66),(53,58),(9,58),(9,9),(54,9),(54,47),(20,47),(20,20),(43,20),(43,36),(31,36)],[(9,36),(20,36)]),
route('Двойная молния',[(13,66),(13,58),(48,45),(15,31),(49,17),(27,6)],[(48,45),(55,35),(45,29)],[(15,31),(7,20),(19,12)]),
route('Разомкнутые орбиты',[(31,66),(31,58),(13,54),(6,39),(11,20),(26,8),(45,9),(56,23),(53,45),(39,51),(26,45),(20,31),(28,20),(40,20),(46,30),(39,39)]),
route('Коса поворотов',[(12,66),(12,57),(46,43),(15,29),(49,13),(49,6)],[(12,57),(50,57),(17,43),(48,29),(16,13),(16,6)])]

def line(a,b):
 x,y=a;xx,yy=b;out=[(x,y)];steps=max(abs(xx-x),abs(yy-y))
 for j in range(1,steps+1):
  nx=round(a[0]+(xx-a[0])*j/steps);ny=round(a[1]+(yy-a[1])*j/steps)
  if nx!=x:out.append((nx,y))
  if ny!=y:out.append((nx,ny))
  x,y=nx,ny
 return out
def components(points):
 remaining=set(points);out=[]
 while remaining:
  p=min(remaining);remaining.remove(p);part={p};q=deque([p])
  while q:
   x,y=q.popleft()
   for dx,dy in D4:
    p=x+dx,y+dy
    if p in remaining:remaining.remove(p);part.add(p);q.append(p)
  out.append(part)
 return out
def distances(mask,start):
 out={start:0};q=deque([start])
 while q:
  x,y=q.popleft()
  for dx,dy in D4:
   p=x+dx,y+dy
   if p in mask and p not in out:out[p]=out[x,y]+1;q.append(p)
 return out
def warped(family,variant,attempt):
 ax=(variant//2-1.5)*1.1+math.sin(attempt*1.7)*1.2
 ay=((variant*3)%7-3)*.55+math.cos(attempt*1.3)*.8
 def warp(p):
  x,y=p;x=round(x+ax*math.sin(math.pi*(x-3)/58));y=round(y+ay*math.sin(math.pi*(y-3)/63)) if y<63 else y
  return (63-x if variant%2 else x,y)
 paths=[[warp(p) for p in path] for path in family['paths']]
 # Later variants add real bends, not just a colour/size change. Only accept
 # their geometry if the same closed-bank and reachable-region checks pass.
 if variant>=2 and attempt<60:
  path=paths[0]
  options=[j for j,(a,b) in enumerate(zip(path,path[1:])) if a[1]<60 and b[1]<60 and math.dist(a,b)>24]
  if options:
   j=options[(variant+attempt)%len(options)];a,b=path[j:j+2];dx=b[0]-a[0];dy=b[1]-a[1];length=math.hypot(dx,dy)
   shift=(5+variant%3)*(1 if (attempt+variant)%2 else -1);nx=-dy/length*shift;ny=dx/length*shift
   c=(round(a[0]+dx*.32),round(a[1]+dy*.32));d=(round(a[0]+dx*.68),round(a[1]+dy*.68))
   offset=lambda p:(max(5,min(58,round(p[0]+nx))),max(5,min(59,round(p[1]+ny))))
   path[j+1:j+1]=[c,offset(c),offset(d),d]
 return paths
def build(index,fi,variant,attempt=0):
 family=FAMILIES[fi];paths=warped(family,variant,attempt);center=set()
 for path in paths:
  for a,b in zip(path,path[1:]):center.update(line(a,b))
 mask={(x+dx,y+dy) for x,y in center for dx,dy in D8 if 1<=x+dx<=62 and 1<=y+dy<=66}
 start=paths[0][0];dist=distances(mask,start)
 assert len(dist)==len(mask)
 shell={(x+dx,y+dy) for x,y in mask for dx,dy in D8 if 1<=x+dx<=62 and 1<=y+dy<=66}-mask
 bricks={(x,y) for y in range(1,67) for x in range(1,63)}-mask-shell
 parts=components(bricks)
 # Acute joins can leave a handful of isolated tiles between two banks. Close
 # those with steel instead of leaving unreachable bonus bricks in a pinhole.
 tiny=set().union(*(p for p in parts if len(p)<=40))
 bricks-=tiny;shell|=tiny;parts=[p for p in parts if len(p)>40]
 if len(parts)>3 or len(bricks)<2150:return None
 region={p:i for i,part in enumerate(parts) for p in part};candidates=[]
 for x,y in sorted(shell):
  for dx,dy in D4:
   tx,ty=-dy,dx;wall=[(x+tx*k,y+ty*k) for k in (-1,0,1)]
   inner=[(xx-dx,yy-dy) for xx,yy in wall];outer=[(xx+dx,yy+dy) for xx,yy in wall]
   if all(p in shell for p in wall) and all(p in mask for p in inner) and all(p in bricks for p in outer):
    regions={region[p] for p in outer}
    if len(regions)==1:candidates.append(dict(cells=wall,inner=inner,region=next(iter(regions)),distance=min(dist[p] for p in inner)))
   # A one-cell doorway still has 0.75 px of diameter clearance. This is used
   # on stepped diagonal banks where three collinear wall tiles do not exist.
   inside=(x-dx,y-dy);outside=(x+dx,y+dy)
   if inside in mask and outside in bricks:
    candidates.append(dict(cells=[(x,y)],inner=[inside],region=region[outside],distance=dist[inside]))
 chosen=[];used=set();wanted=max(len(parts),1+index%3)
 def select(pool):
  pool=[p for p in pool if p['distance']>=15 and all(abs(p['cells'][0][0]-q['cells'][0][0])+abs(p['cells'][0][1]-q['cells'][0][1])>=9 for q in chosen)]
  if not pool:return False
  p=max(pool,key=lambda p:p['distance']+len(p['cells'])*2+sum(min(30,abs(p['cells'][0][0]-q['cells'][0][0])+abs(p['cells'][0][1]-q['cells'][0][1]))*.5 for q in chosen))
  chosen.append(p);used.update(p['cells']);return True
 for r in sorted(range(len(parts)),key=lambda r:len(parts[r])):
  if not select([p for p in candidates if p['region']==r]):return None
 while len(chosen)<wanted:
  if not select([p for p in candidates if p['distance']>=max(dist.values())*.42]):return None
 pairs=[('b','c'),('e','f'),('a','g'),('k','c'),('g','h'),('d','e'),('a','b'),('f','m')]
 primary,secondary=pairs[(fi+variant)%len(pairs)]
 def color(x,y):
  mode=fi%8
  if mode==0:inside=(x-32)**2+(y-25)**2<16**2
  elif mode==1:inside=abs(x-32)+abs(y-29)<24
  elif mode==2:inside=13<x<51 and 10<y<26
  elif mode==3:inside=x>y*.7+8
  elif mode==4:inside=(x//16+y//22)%2==0
  elif mode==5:inside=abs(x-32)<8 or abs(y-27)<8
  elif mode==6:inside=abs(x-32)<(y+8)*.35
  else:inside=math.hypot((x-32)*1.3,y-30)<23
  ch=secondary if inside else primary
  return ch.upper() if index>=60 and (x*17+y*13+index)%max(24,54-index//8)==0 else ch
 cells=[['#' if x in (0,63) or y in (0,67) else color(x,y) for x in range(W)] for y in range(H)]
 for x,y in shell:cells[y][x]='#'
 for x,y in mask|used:cells[y][x]='.'
 entry=start[0];two=index>19 and index%4==0;a=entry-1;b=a+(2 if two else 3)
 for x in range(a,b):cells[67][x]='.'
 data=''.join(''.join(row) for row in cells)
 exposed={(x,y) for y in range(1,67) for x in range(1,63) if cells[y][x]=='.' and any(cells[y+dy][x+dx].isalpha() for dx,dy in D4)}
 if exposed!=used:return None
 return dict(number=index+1,id=20001+index,title=family['title']+' '+str(variant+1),hint=f'{len(chosen)} выхода к кирпичам. Пройди повороты между стенками.',family='maze:'+str(fi),variant=variant,routes=paths,gates=[(a,b)],gateWidths=[10.5 if two else 15.5],cells=data,blocks=len(bricks),ports=[p['cells'] for p in chosen],portDistances=[p['distance'] for p in chosen],tunnelLength=max(dist.values()),brickRegions=len(parts),mazeCells=len(mask),speed=round(190+index*.6,2),paddle=round(144-index*.24,2))
def write(catalog):
 out=['package pro.brickstorm.game;','/** 200 enclosed maze puzzles; generated by scripts/maze-levels.py. */','final class LevelData {']
 for field in ('title','hint','family','cells'):
  out+=['    static final String[] '+field.upper()+'={']+['        '+json.dumps(l[field],ensure_ascii=False)+',' for l in catalog]+['    };']
 out+=['    static final int[][] GATES={']+['        {'+','.join(str(x) for pair in l['gates'] for x in pair)+'},' for l in catalog]+['    };']
 out+=['    static final int[] IDS={'+','.join(str(l['id']) for l in catalog)+'};']
 out+=['    static final int[] PORTS={'+','.join(str(len(l['ports'])) for l in catalog)+'};']
 out+=['    static final float[] GATE_WIDTHS={'+','.join(str(l['gateWidths'][0])+'f' for l in catalog)+'};','    private LevelData(){}','}']
 (ROOT/'app/src/main/java/pro/brickstorm/game/LevelData.java').write_text('\n'.join(out)+'\n')
 (ROOT/'verification').mkdir(exist_ok=True)
 (ROOT/'verification/level-catalog.json').write_text(json.dumps(catalog,ensure_ascii=False,indent=2))
if __name__=='__main__':
 catalog=[];shapes=set()
 for variant in range(8):
  for family in range(len(FAMILIES)):
   index=len(catalog)
   for attempt in range(80):
    level=build(index,family,variant,attempt)
    if level:
     geometry=''.join(c if c in '.#' else 'x' for c in level['cells'])
     if geometry not in shapes:break
   else:raise RuntimeError(f'No valid unique layout for family {family}, variant {variant}')
   shapes.add(geometry);catalog.append(level)
 assert len(catalog)==200
 write(catalog)
 print('Wrote 200 new mazes; 25 route families × 8 nonlinear variants; ports',dict(Counter(len(l['ports']) for l in catalog)),'; bricks',min(l['blocks'] for l in catalog),'..',max(l['blocks'] for l in catalog),'; tunnel reach',min(l['tunnelLength'] for l in catalog),'..',max(l['tunnelLength'] for l in catalog))

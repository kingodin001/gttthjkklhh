#!/usr/bin/env python3
"""Fifty individually specified routes, two reference compositions and eight retained mosaics."""
from pathlib import Path
import json,math,collections
ROOT=Path(__file__).resolve().parents[1];W,H=64,68
PALETTE=['ff705b','ffb344','ffe16b','75d8bc','42d5e4','7998ef','bfa1ff','f695c5','ecf1fa','7494b7','b7e767','e4ba70','a8dffa','ed8a9b','7bc8d5','d6beec']
def arc(x,y,r,a,b):
 return [(round(x+r*math.cos(math.radians(a+(b-a)*i/24)),2),round(y+r*math.sin(math.radians(a+(b-a)*i/24)),2)) for i in range(25)]
def poly(*p):return list(p)
def spec(title,family,main,*branches):return dict(title=title,family=family,routes=[main,*branches])
# No chapter-based recoloring of the same ten layouts. All fifty route graphs are explicit.
S=[
 spec('Две половины','reference:halves',poly((31,67),(31,5),(15,5)),poly((31,5),(49,5))),
 spec('Диагональный спектр','reference:triangles',poly((48,67),(48,61),(43,61),(43,56),(34,56),(24,44),(44,24),(23,4)),poly((44,24),(59,6)),poly((24,44),(5,62))),
 spec('Три поворота','galleries',poly((55,67),(55,58),(8,58),(8,34),(53,34),(53,9),(26,9))),
 spec('Кристальная спираль','spiral',poly((32,67),(32,59),(7,59),(7,8),(56,8),(56,48),(19,48),(19,20),(44,20),(44,37),(31,37))),
 spec('Ломаный луч','diagonal-z',poly((12,67),(12,59),(50,43),(13,27),(51,8))),
 spec('Неравный обход','asymmetric-fork',poly((30,67),(30,56),(9,56),(9,16),(31,7)),poly((30,56),(52,56),(52,38),(37,38),(37,19),(31,7))),
 spec('Три луча','three-branches',poly((32,67),(32,43),(10,43),(10,10)),poly((32,43),(54,43),(54,10)),poly((32,43),(32,8))),
 spec('Кольцо приливов','round-loop',poly((32,67),(32,57))+arc(32,32,25,90,450),poly((32,57),(32,32),(15,32))),
 spec('Ромб развилок','diamond-loop',poly((32,67),(32,59),(7,33),(32,7),(57,33),(32,59)),poly((7,33),(32,33),(32,7))),
 spec('Серебряный каскад','five-galleries',poly((54,67),(54,59),(9,59),(9,46),(52,46),(52,33),(11,33),(11,19),(51,19),(51,6))),
 spec('Вертикальная молния','vertical-zigzag',poly((31,67),(31,58),(12,46),(49,34),(14,22),(48,8))),
 spec('Песочные ворота','hourglass',poly((32,67),(32,59),(9,48),(32,32),(54,17),(32,6)),poly((32,59),(55,48),(32,32),(10,17),(32,6))),
 spec('Три двора','courtyard-chain',poly((12,67),(12,59),(26,59),(26,43),(8,43),(8,27),(32,27),(32,9),(54,9),(54,28),(44,28))),
 spec('Вложенные грани','nested-diamonds',poly((32,67),(32,59),(7,33),(32,6),(57,33),(32,59),(19,45),(32,31),(45,18))),
 spec('Волна','wave',poly((12,67),(12,58),(24,51),(44,49),(53,42),(48,33),(27,29),(12,22),(18,12),(43,7))),
 spec('Открытая буква С','c-path',poly((49,67),(49,59),(9,59),(9,8),(53,8),(53,23),(25,23),(25,43),(45,43))),
 spec('Боковая лестница','wide-stairs',poly((9,67),(9,58),(22,58),(22,45),(36,45),(36,32),(52,32),(52,17),(36,17),(36,7))),
 spec('Гребень галерей','comb',poly((11,67),(11,10),(51,10)),poly((11,26),(48,26)),poly((11,43),(53,43)),poly((11,58),(35,58))),
 spec('Диагональный зигзаг','cross-slope',poly((52,67),(52,59),(12,42),(51,24),(13,7))),
 spec('Две орбиты','double-round',poly((32,67),(32,55))+arc(19,39,16,90,450),poly((32,55),(45,55))+arc(45,24,17,90,450)),
 spec('Шестиугольник','hexagon',poly((32,67),(32,59),(10,46),(10,20),(32,7),(54,20),(54,46),(32,59)),poly((10,20),(32,32),(54,20))),
 spec('Смещённые входы','offset-doors',poly((48,67),(48,58),(21,58),(21,47),(43,47),(43,34),(14,34),(14,21),(38,21),(38,7))),
 spec('Четыре края','corner-network',poly((32,67),(32,51),(8,51),(8,10),(32,10)),poly((32,51),(55,51),(55,10),(32,10)),poly((8,31),(55,31))),
 spec('Косая сердцевина','skew-spiral',poly((20,67),(20,58),(8,48),(14,10),(53,7),(57,48),(28,53),(22,26),(43,21),(45,38))),
 spec('Башни и мост','tower-bridge',poly((17,67),(17,8),(47,8),(47,58)),poly((17,33),(47,33)),poly((17,54),(32,54),(32,43))),
 spec('Дерево отскоков','y-branches',poly((32,67),(32,44),(12,24),(12,7)),poly((32,44),(52,24),(52,7)),poly((12,24),(31,19)),poly((52,24),(37,12))),
 spec('Треугольный маршрут','triangle-loop',poly((31,67),(31,59),(7,59),(31,7),(57,59),(31,59)),poly((19,34),(44,34))),
 spec('Встречные склоны','angled-snake',poly((10,67),(10,58),(53,46),(11,33),(52,20),(24,6))),
 spec('Полукруги','half-orbits',poly((9,67),(9,54))+arc(32,54,23,180,360)+poly((55,32))+arc(32,32,23,0,-180)+poly((9,10),(49,10))),
 spec('Открытые ограды','open-u',poly((31,67),(31,59),(8,59),(8,9),(55,9),(55,56),(21,56),(21,23),(43,23),(43,42),(32,42))),
 spec('Крылья маршрута','butterfly-path',poly((32,67),(32,55),(8,42),(13,13),(32,28),(53,10),(57,39),(32,55)),poly((32,28),(32,8))),
 spec('Каналы со сдвигом','canal-network',poly((48,67),(48,58),(12,58),(12,39),(30,39),(30,19),(53,19),(53,7)),poly((30,39),(50,39)),poly((12,39),(12,10))),
 spec('Обратная эс','reverse-s',poly((12,67),(12,58),(53,58),(53,34),(10,34),(10,8),(51,8))),
 spec('Три лепестка','trefoil',poly((32,67),(32,53))+arc(20,40,16,55,365),poly((32,53),(44,50))+arc(44,34,16,90,-220),poly((32,53),(32,25))+arc(32,16,10,90,450)),
 spec('Четыре рубежа','quadrants',poly((32,67),(32,55),(9,55),(9,32),(32,32),(32,9),(55,9),(55,32),(32,32),(32,55),(55,55)),poly((9,32),(9,9))),
 spec('Ступени к центру','inward-stairs',poly((53,67),(53,58),(10,58),(10,14),(48,14),(48,49),(21,49),(21,25),(36,25),(36,38))),
 spec('Воронка спектра','funnel',poly((32,67),(32,58),(8,43),(21,29),(8,9)),poly((32,58),(56,43),(43,29),(56,9)),poly((21,29),(32,17),(43,29))),
 spec('Зубчатый обход','toothed-path',poly((10,67),(10,58),(23,58),(23,48),(13,48),(13,36),(34,36),(34,24),(49,24),(49,11),(35,11),(35,5))),
 spec('Восьмёрка','figure-eight',poly((32,67),(32,56),(11,44),(32,31),(53,18),(32,5),(11,18),(32,31),(53,44),(32,56))),
 spec('Двойной серп','crescents',poly((15,67),(15,58),(8,44),(11,23),(24,8),(40,8),(52,21)),poly((15,58),(38,59),(54,46),(54,29),(39,20),(28,25),(26,39))),
 spec('Коса','braided-path',poly((16,67),(16,57),(48,43),(16,29),(48,13),(48,5)),poly((48,57),(16,43),(48,29),(16,13),(16,5)),poly((16,57),(48,57))),
 spec('Трапециевидная цитадель','trapezoid',poly((32,67),(32,59),(6,59),(15,9),(49,9),(58,59),(32,59)),poly((11,36),(32,23),(53,36))),
 spec('Два кармана','offset-pockets',poly((49,67),(49,58),(9,58),(9,9),(33,9),(33,24),(20,24),(20,41),(48,41),(48,13),(56,13))),
 spec('Три колодца','three-wells',poly((10,67),(10,8),(29,8),(29,53),(49,53),(49,9),(57,9)),poly((10,58),(29,58))),
 spec('Звёздный обход','star-route',poly((32,67),(32,58),(8,43),(22,34),(10,15),(29,19),(39,6),(43,26),(57,34),(43,44),(46,59),(32,58))),
 spec('Камеры по диагонали','sloped-chambers',poly((12,67),(12,59),(29,59),(29,45),(13,45),(13,30),(42,30),(42,15),(24,15),(24,5)),poly((42,30),(55,44),(55,57))),
 spec('Сломанные кольца','broken-rings',poly((32,67),(32,59),(8,59),(8,8),(55,8),(55,54),(20,54),(20,21),(43,21),(43,43),(31,43),(31,32))),
 spec('Развилка мастера','master-fork',poly((31,67),(31,55),(9,46),(14,27),(7,9)),poly((31,55),(54,48),(47,30),(56,10)),poly((14,27),(31,16),(47,30)),poly((31,16),(31,5))),
 spec('Пять связей','five-links',poly((53,67),(53,57),(10,57),(10,40),(32,40),(32,23),(53,23),(53,7)),poly((10,40),(10,8),(32,8),(32,23)),poly((32,40),(53,40))),
 spec('Сердце лабиринта','final-asymmetric',poly((17,67),(17,59),(7,49),(7,10),(31,6),(55,14),(56,49),(43,59),(30,51),(20,39),(22,22),(39,18),(46,31),(36,43),(31,33)))
]
assert len(S)==50

def distance(px,py,a,b):
 ax,ay=a;bx,by=b;dx,dy=bx-ax,by-ay;t=max(0,min(1,((px-ax)*dx+(py-ay)*dy)/(dx*dx+dy*dy))) if dx or dy else 0
 return math.hypot(px-ax-t*dx,py-ay-t*dy)
def inside(x,y,points):
 sign=False;j=len(points)-1
 for i,(xx,yy) in enumerate(points):
  xj,yj=points[j]
  if (yy>y)!=(yj>y) and x<(xj-xx)*(y-yy)/(yj-yy)+xx:sign=not sign
  j=i
 return sign
class Board:
 def __init__(self,n):
  self.n=n;self.c=[['.' for x in range(W)] for y in range(H)];self.mask=set()
  for y in range(1,67):
   for x in range(1,63):self.c[y][x]=chr(97+[0,2,6,4][(x//16+y//24+n//5)%4])
 def paint_line(self,a,b,width,ch=None):
  r=width/2
  for y in range(max(1,math.floor(min(a[1],b[1])-r)),min(67,math.ceil(max(a[1],b[1])+r+1))):
   for x in range(max(1,math.floor(min(a[0],b[0])-r)),min(63,math.ceil(max(a[0],b[0])+r+1))):
    if distance(x,y,a,b)<=r:
     if ch is None:self.mask.add((x,y))
     else:self.c[y][x]=ch
 def route(self,points,width):
  for a,b in zip(points,points[1:]):self.paint_line(a,b,width)
 def rail(self,a,b,side,width):
  dx,dy=b[0]-a[0],b[1]-a[1];length=math.hypot(dx,dy)
  if length<7:return
  trim=min(3,length/4);nx,ny=-dy/length*(width/2+1)*side,dx/length*(width/2+1)*side
  aa=(a[0]+dx/length*trim+nx,a[1]+dy/length*trim+ny);bb=(b[0]-dx/length*trim+nx,b[1]-dy/length*trim+ny)
  self.paint_line(aa,bb,1.1,'#')
 def finish(self,entry,two=False):
  for x,y in self.mask:self.c[y][x]='.'
  # A real landing chamber, never a one-row brick strip squeezed against the floor.
  for y in range(63,67):
   for x in range(1,63):
    if self.c[y][x]=='#':self.c[y][x]=chr(97+[0,2,6,4][(x//16+self.n)%4])
  for x in range(W):self.c[0][x]=self.c[67][x]='#'
  for y in range(H):self.c[y][0]=self.c[y][63]='#'
  count=2 if two else 3;start=max(2,min(60,int(entry)));self.gates=[(start,start+count)];self.gateWidths=[10.5 if two else 15.5]
  for y in range(63,67):
   for x in range(max(1,start-1),min(63,start+count+1)):self.c[y][x]='.'
  for x in range(start,start+count):self.c[67][x]='.'
  # Remove any internal horizontal wall which leaves only one row before another wall.
  for repeat in range(4):
   remove=set()
   for x in range(1,63):
    walls=[y for y in range(68) if self.c[y][x]=='#']
    for a,b in zip(walls,walls[1:]):
     if b-a==2:remove.add((x,a if a>0 else b))
   if not remove:break
   for x,y in remove:self.c[y][x]='.'
  for y in range(1,67):
   for x in range(1,63):
    ch=self.c[y][x]
    if ch.islower() and self.n>12 and (x*11+y*7+self.n)%max(7,26-self.n//3)==0:self.c[y][x]=ch.upper()
  return ''.join(''.join(row) for row in self.c)

def make(n):
 s=S[n-1];b=Board(n);width=4 if n in [1,2,5,11,19,24,28,39,41,45] else 5
 for r in s['routes']:b.route(r,width)
 if n==1:
  for y in range(1,67):
   for x in range(1,63):b.c[y][x]='k' if x<31 else 'c'
  for y in range(8,62):b.c[y][28]=b.c[y][34]='#'
 elif n==2:
  for y in range(1,67):
   for x in range(1,63):
    color='a'
    if inside(x,y,[(23,1),(62,1),(44,24)]):color='i'
    elif x>44-(y-24)*1.02:color='g'
    if inside(x,y,[(3,65),(24,44),(42,65)]):color='b'
    b.c[y][x]=color
    if any((x+dx,y+dy) in b.mask for dx,dy in [(1,0),(-1,0),(0,1),(0,-1)]) and (x,y) not in b.mask:b.c[y][x]='e'
  for a,z in zip(s['routes'][0][:5],s['routes'][0][1:5]):b.rail(a,z,1,width)
 else:
  for ri,r in enumerate(s['routes']):
   for j,(a,z) in enumerate(zip(r,r[1:])):
    if j==0 or j%3==n%3:continue
    b.rail(a,z,1 if (j+n+ri)%2 else -1,width)
    if n in [4,10,13,16,22,30,33,36,38,43,47,49,50]:b.rail(a,z,-1 if (j+n+ri)%2 else 1,width)
 one=n>6 and n%3==0;data=b.finish(s['routes'][0][0][0],one)
 return dict(number=n,id=n,title=s['title'],hint='Узкий вход удерживает шары внутри. Ищи отскоки.',family=s['family'],gates=b.gates,gateWidths=b.gateWidths,cells=data,blocks=sum(c.isalpha() for c in data),speed=round(190+(n-1)*2.3,2),paddle=round(144-(n-1)*1.05,2),routes=s['routes'])

def normalize_art(l,n):
 b=Board(n);data=l['cells'];b.c=[list(data[y*64:(y+1)*64]) for y in range(68)]
 # Retain the actual art but apply the same bottom clearance and precise narrow entrance.
 for y in range(63,67):
  for x in range(1,63):b.c[y][x]='.'
 for y in range(65,67):
  for x in range(2,62):b.c[y][x]='e' if x<32 else 'g'
 for y in range(56,67):
  for x in range(30,35):b.c[y][x]='.'
 d=b.finish(32,n%3==0);l.update(number=n,id=n,cells=d,gates=b.gates,gateWidths=b.gateWidths,blocks=sum(c.isalpha() for c in d),speed=round(190+(n-1)*2.3,2),paddle=round(144-(n-1)*1.05,2));return l

def write(catalog):
 out=['package pro.brickstorm.game;','/** Fifty explicit route graphs and exact world-space floor apertures. */','final class LevelData {']
 for field in ['title','hint','family','cells']:
  out+=['    static final String[] '+field.upper()+'={']+['        '+json.dumps(l[field],ensure_ascii=False)+',' for l in catalog]+['    };']
 out+=['    static final int[][] GATES={']+['        {'+','.join(str(x) for pair in l['gates'] for x in pair)+'},' for l in catalog]+['    };']
 out+=['    static final int[] IDS={'+','.join(str(l['id']) for l in catalog)+'};']
 out+=['    static final float[] GATE_WIDTHS={'+','.join(str(l['gateWidths'][0])+'f' for l in catalog)+'};','    private LevelData(){}','}']
 (ROOT/'app/src/main/java/pro/brickstorm/game/LevelData.java').write_text('\n'.join(out)+'\n')
 (ROOT/'verification/level-catalog.json').write_text(json.dumps(catalog,ensure_ascii=False,indent=2))

def extended(index):
 import copy
 base=3+(index%48);variant=1+index//48;original=S[base-1]
 def warp(p):
  x,y=p;xx=x+(2.3+variant*.35)*math.sin(math.pi*(x-1)/62)*math.sin((y+variant*9)*.052)
  yy=y if y>=63 else y+(variant%3-1)*2.3*math.sin(math.pi*y/67)
  if variant%2==0:xx=63-xx
  return (round(max(3,min(60,xx)),2),round(max(3,min(67,yy)),2))
 routes=[]
 for path in original['routes']:
  route=[warp(p) for p in path]
  # Add a staggered detour to a long straight segment in selected variants.
  if index%3==0:
   for j,(a,b) in enumerate(zip(route,route[1:])):
    if abs(a[1]-b[1])<1 and abs(a[0]-b[0])>20 and 12<a[1]<51:
     x1=a[0]+(b[0]-a[0])*.33;x2=a[0]+(b[0]-a[0])*.67;yy=a[1]+(5 if variant%2 else -5)
     route[j+1:j+1]=[(x1,a[1]),(x1,yy),(x2,yy),(x2,b[1])];break
  routes.append(route)
 modified=copy.deepcopy(original);modified['routes']=routes
 S[base-1]=modified
 try:l=make(base)
 finally:S[base-1]=original
 l.update(id=1000+index,title=original['title']+' · '+str(variant+1),family='extended:'+str(index),routes=routes)
 return l

if __name__=='__main__':
 import runpy
 old={n:make(n) for n in range(1,51)}
 refs=runpy.run_path(str(ROOT/'scripts/reference-levels.py'))['build_reference']
 art=[normalize_art(l,51+j) for j,l in enumerate(json.loads((ROOT/'scripts/art-mosaics.json').read_text()))]
 # Interleave each new chamber among familiar routes, and move a few early routes around.
 order=[1,2,7,3,6,4,5,*range(8,51)]
 ref_at=[2,5,7,10,13,15,18,21,24,27,30,33,36,39,42,45,48,50]
 art_at=[6,12,18,24,30,36,42,48]
 catalog=[];ri=ai=0
 for pos,old_id in enumerate(order,1):
  catalog.append(old[old_id])
  if pos in ref_at:
   catalog.append(refs(len(catalog)+1,ri,Board,inside));ri+=1
  if pos in art_at:catalog.append(art[ai]);ai+=1
 for i in range(224):catalog.append(extended(i))
 for n,l in enumerate(catalog,1):
  l.update(number=n,speed=round(190+min(n-1,75)*2+max(0,n-76)*.22,2),paddle=round(144-min(n-1,75)*.8-max(0,n-76)*.08,2))
 write(catalog);print('Wrote 300 levels; first 76 retained, 224 extended route puzzles')

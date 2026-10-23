#!/usr/bin/env python3
"""Fifty full-field, connected route puzzles. Geometry is shipped as runtime cells."""
from pathlib import Path
import json, math
ROOT=Path(__file__).resolve().parents[1]; W,H=64,68
PALETTE=['ff705b','ffb344','ffe16b','75d8bc','42d5e4','7998ef','bfa1ff','f695c5','ecf1fa','7494b7','b7e767','e4ba70','a8dffa','ed8a9b','7bc8d5','d6beec']
class Board:
 def __init__(self,n):
  self.n=n;self.c=[['.' for x in range(W)] for y in range(H)];self.mask=set();self.routes=[]
  for y in range(2,66):
   for x in range(2,62):
    colors=[0,2,6,4];band=(x//16+(n-1)%4)%4
    if (n-1)%3==1:band=(y//17+(n-1)//10)%4
    if (n-1)%3==2:band=min(3,(x+y)//32)
    self.c[y][x]=chr(97+colors[band])
 def rect(self,x,y,w,h,ch):
  for j in range(max(1,y),min(67,y+h)):
   for i in range(max(1,x),min(63,x+w)):self.c[j][i]=ch
 def passage(self,points,width):
  self.routes.append(points);r=width//2
  for (ax,ay),(bx,by) in zip(points,points[1:]):
   assert ax==bx or ay==by
   for y in range(max(1,min(ay,by)-r),min(67,max(ay,by)+r+1)):
    for x in range(max(1,min(ax,bx)-r),min(63,max(ax,bx)+r+1)):self.mask.add((x,y))
 def rail(self,a,b,side,width,trim=2):
  # A silver rail alongside a lane, never across its clear floor.
  ax,ay=a;bx,by=b;offset=width//2+1
  if ay==by:
   l,r=sorted([ax,bx]);self.rect(l+trim,ay+side*offset,max(0,r-l-2*trim+1),1,'#')
  else:
   t,bottom=sorted([ay,by]);self.rect(ax+side*offset,t+trim,1,max(0,bottom-t-2*trim+1),'#')
 def finish(self,entry,width):
  for x,y in self.mask:self.c[y][x]='.'
  for x in range(W):self.c[0][x]='#';self.c[67][x]='#'
  for y in range(H):self.c[y][0]='#';self.c[y][63]='#'
  for x in range(W):self.c[66][x]='#'
  a,b=entry-width//2,entry+width//2+1
  for y in range(64,68):
   for x in range(a,b):self.c[y][x]='.'
  for y in range(2,66):
   for x in range(2,62):
    c=self.c[y][x]
    if c.islower() and self.n>10 and (x*11+y*7+self.n)%max(6,25-self.n//3)==0:self.c[y][x]=c.upper()
  self.gates=[(a,b)];return ''.join(''.join(row) for row in self.c)

# Each of the fifty entries has an explicit topology and a different arrangement.
# Rail frequency and route turns grow through five chapters, without an empty first half.
NAMES=[
'Три поворота','Путь к сердцу','Двойной обход','Лестница света','Серебряная змейка','Встречные пути','Квадратный вихрь','Развилка спектра','Пять галерей','Внутренний двор',
'Ворота янтаря','Спираль приливов','Две цитадели','Ступени к ядру','Маршрут молнии','Разделённый каскад','Сердце кристалла','Пути преломления','Смещённые рубежи','Нефритовый лабиринт',
'Четыре затвора','Золотая сердцевина','Два маршрута','Лестница отскоков','Длинная змейка','Встречные галереи','Спираль спектра','Три развилки','Серебряный каскад','Путь сквозь крепость',
'Пять затворов','Тайна сердцевины','Двойной фронт','Ступени мастера','Ломаная галерея','Верхнее слияние','Двойной вихрь','Перекрёсток','Стальные галереи','Тройной двор',
'Шесть затворов','Сердце шторма','Два пути к короне','Последняя лестница','Шесть поворотов','Встречный шторм','Янтарная спираль','Последняя развилка','Рубеж мастера','Абсолютный шторм']

def serpentine(chapter,mode):
 count=[3,3,4,4,5][chapter] if mode==0 else [4,4,4,5,5][chapter]
 ys=[60-round(i*51/(count-1)) for i in range(count)]
 left=8+chapter%2;right=55-chapter%3
 if mode==4:left+=2;right-=2
 if mode==8:left=12-chapter%3;right=52+chapter%2
 pts=[(right,67),(right,ys[0])];x=right
 for j,y in enumerate(ys):
  x=left if x==right else right
  pts.append((x,y))
  if j+1<len(ys):pts.append((x,ys[j+1]))
 return pts

def spiral(chapter,mode):
 # Alternating direction. The entrance connects to the OUTERMOST lane.
 pts=[(32,67),(32,60),(7,60),(7,7),(56,7),(56,49),(19,49),(19,19),(44,19),(44,37),(31,37),(31,29)]
 if chapter>=3:pts[-1]=(31,26)
 if chapter%2:pts=[(63-x,y) for x,y in pts]
 if mode==6:
  # A second legitimate entry branch joins the outer spiral on the right.
  return [pts,[(32 if chapter%2==0 else 31,67),(32 if chapter%2==0 else 31,62),(58 if chapter%2==0 else 5,62),(58 if chapter%2==0 else 5,8)]]
 return [pts]

def twin(chapter,mode):
 stem=[(32,67),(32,58)];left=8+chapter%3;right=55-chapter%3
 # Connected bottom fork and stepped outside lanes which meet near the golden goal.
 a=stem+[(left,58),(left,43),(20,43),(20,30),(left+3,30),(left+3,10),(32,10),(32,5)]
 b=stem+[(right,58),(right,43),(44,43),(44,30),(right-3,30),(right-3,10),(32,10)]
 if mode==5:
  a=stem+[(12,58),(12,47),(25,47),(25,34),(10,34),(10,9),(32,9),(32,4)]
  b=stem+[(52,58),(52,47),(39,47),(39,34),(54,34),(54,9),(32,9)]
 if mode==5:
  delta=chapter%3
  a=[(x+delta if x<30 else x,y) for x,y in a]
  b=[(x-delta if x>34 else x,y) for x,y in b]
 if mode==7:
  # Central cross-lane opens a choice of upper or outer route, with protected inner banks.
  return [a,b,[(20,43),(44,43)],[(20,30),(32,30),(32,18)]]
 return [a,b]

def stairs(chapter):
 mirror=chapter%2==1;shift=chapter%3
 pts=[(10,67),(10,56),(23,56),(23,43),(36,43),(36,29),(52,29),(52,9),(37,9)]
 if chapter>=2:pts=[(10,67),(10,58),(22,58),(22,46),(35,46),(35,34),(51,34),(51,20),(37,20),(37,7),(17,7)]
 pts[-1]=(pts[-1][0]+chapter,pts[-1][1])
 if mirror:pts=[(63-x,y) for x,y in pts]
 return [pts]

def courtyards(chapter):
 # Open nested squared path, with a different access direction in each chapter.
 pts=[(32,67),(32,60),(8,60),(8,9),(55,9),(55,56),(21+chapter%3,56),(21+chapter%3,23),(43-chapter%2,23),(43-chapter%2,43),(31+chapter%3,43)]
 if chapter%2:pts=[(63-x,y) for x,y in pts]
 return [pts]

def make(n):
 c=(n-1)//10;kind=(n-1)%10;width=7 if c<2 else 5
 b=Board(n)
 if kind in [0,4,8]:routes=[serpentine(c,kind)]
 elif kind in [1,6]:routes=spiral(c,kind)
 elif kind in [2,5,7]:routes=twin(c,kind)
 elif kind==3:routes=stairs(c)
 else:routes=courtyards(c)
 for points in routes:b.passage(points,width)
 # Long rails create real ricochet galleries; later puzzles line BOTH lane faces.
 for ri,points in enumerate(routes):
  for j,(a,z) in enumerate(zip(points,points[1:])):
   if j==0:continue
   side=1 if (j+ri+c)%2 else -1
   b.rail(a,z,side,width,trim=2)
   if c>=2 or kind in [2,5,7,9]:b.rail(a,z,-side,width,trim=3)
 # Additional corner shields end before the next turn. Carve the route LAST so no steel blocks it.
 entry=routes[0][0][0];data=b.finish(entry,width)
 hint='Войди снизу и выбирай угол отскока на поворотах.'
 if kind in [1,6,9]:hint='Обойди стенки по открытому маршруту к сердцевине.'
 if kind in [2,5,7]:hint='Выбери обход: два пути сходятся у верхних блоков.'
 return dict(number=n,title=NAMES[n-1],hint=hint,family=['route:gates','route:spiral','route:twin','route:stairs','route:snake','route:merge','route:vortex','route:fork','route:galleries','route:courtyard'][kind],gates=b.gates,cells=data,blocks=sum(ch.isalpha() for ch in data),speed=round(190+(n-1)*2.3,2),paddle=round(144-(n-1)*1.05,2),routes=routes)

def write(catalog):
 out=['package pro.brickstorm.game;','/** Reproducible connected route catalog; generated by scripts/build-levels.py. */','final class LevelData {']
 for field in ['title','hint','family','cells']:
  out.append('    static final String[] '+field.upper()+'={')
  for l in catalog:out.append('        '+json.dumps(l[field],ensure_ascii=False)+',')
  out.append('    };')
 out.append('    static final int[][] GATES={')
 for l in catalog:out.append('        {'+','.join(str(i) for pair in l['gates'] for i in pair)+'},')
 out.extend(['    };','    private LevelData(){}','}'])
 (ROOT/'app/src/main/java/pro/brickstorm/game/LevelData.java').write_text('\n'.join(out)+'\n')
 (ROOT/'verification/level-catalog.json').write_text(json.dumps(catalog,ensure_ascii=False,indent=2))

if __name__=='__main__':
 catalog=[make(n) for n in range(1,51)]
 art=json.loads((ROOT/'scripts/art-mosaics.json').read_text())
 for j,l in enumerate(art):
  n=51+j;l.update(number=n,speed=round(190+(n-1)*2.3,2),paddle=round(144-(n-1)*1.05,2));catalog.append(l)
 write(catalog);print('Wrote 50 full-field route puzzles + 8 retained mosaics')

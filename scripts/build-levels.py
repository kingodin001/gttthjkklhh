#!/usr/bin/env python3
"""Authored, deterministic brick layouts. Writes the runtime catalog, no image overlays."""
from pathlib import Path
import math,json
ROOT=Path(__file__).resolve().parents[1]
W,H=64,68
# Jewel tones with bright specular brick faces; colors are actual breakable cells.
PALETTE=['ff705b','ffb344','ffe16b','75d8bc','42d5e4','7998ef','bfa1ff','f695c5','ecf1fa','7494b7','b7e767','e4ba70','a8dffa','ed8a9b','7bc8d5','d6beec']
class Board:
 def __init__(self,n):
  self.n=n;self.c=[['.' for x in range(W)] for y in range(H)];self.route=[]
 def rect(self,x,y,w,h,ch):
  for j in range(max(0,y),min(H,y+h)):
   for i in range(max(0,x),min(W,x+w)):self.c[j][i]=ch
 def line(self,points,width,ch):
  for (ax,ay),(bx,by) in zip(points,points[1:]):
   l=max(abs(bx-ax),abs(by-ay))*3+1
   for k in range(l+1):
    t=k/l;x=ax+(bx-ax)*t;y=ay+(by-ay)*t
    for j in range(max(0,int(y-width)),min(H,math.ceil(y+width+1))):
     for i in range(max(0,int(x-width)),min(W,math.ceil(x+width+1))):
      if (i-x)**2+(j-y)**2<=(width/2)**2:self.c[j][i]=ch
 def fill(self,style=0,top=3,bottom=64):
  for y in range(top,bottom):
   for x in range(2,62):
    if style%5==0:col=[0,2,4,6][min(3,x//16)]
    elif style%5==1:col=[4,5,6,2][min(3,y//17)]
    elif style%5==2:col=[0,1,2,6][min(3,(x+y)//32)]
    elif style%5==3:col=[6,4,0,2][min(3,int(abs(x-31.5)/8))]
    else:col=[4,6,2,0][(x//16+y//18)%4]
    self.c[y][x]=chr(97+col)
 def border(self,gates):
  self.rect(0,0,W,1,'#');self.rect(0,0,1,H,'#');self.rect(63,0,1,H,'#');self.rect(0,66,W,2,'#')
  for a,b in gates:self.rect(a,64,b-a,4,'.')
  self.gates=gates
 def shelves(self,ys,gaps,wide):
  for y,gap in zip(ys,gaps):
   # A real passage, with a generous empty turning gallery on both faces.
   self.rect(1,y-2,62,6,'.');self.rect(1,y,62,2,'#');self.rect(gap-wide//2,y,wide,2,'.')
   self.rect(gap-wide//2,y-4,wide,10,'.')
 def final(self):
  # Progressive reinforcement is visible as a small diamond, never an invisible HP change.
  for y in range(2,64):
   for x in range(2,62):
    c=self.c[y][x]
    if c.islower() and self.n>14 and (x*13+y*7+self.n)%max(6,24-self.n//4)==0:self.c[y][x]=c.upper()
  return ''.join(''.join(r) for r in self.c)

# Fifty individually specified puzzle compositions, not random mazes.
# Family, title, short practical observation, authored parameters.
S=[
('shelves','Первый проход','Войди снизу и отбей шар в левый карман.',([38],[44])),
('shelves','Левый поворот','Проход слева открывает верхнюю галерею.',([43],[16])),
('split','Два маршрута','Выбери левый или правый вход к блокам.',(29,)),
('shelves','Смещённые ворота','Меняй угол между правыми и левыми воротами.',([43,24],[47,17])),
('u','Открытая крепость','Зайди в крепость снизу, затем отбей к краям.',(12,)),
('dogleg','Двойной поворот','Обойди торец перегородки и направь шар вверх.',([(43,66),(43,46),(18,46),(18,12)],)),
('shelves','Три галереи','Проведи шар через чередующиеся ворота.',([49,32,15],[48,16,46])),
('fork','Развилка','Разные входы ведут к разным половинам.',(19,45)),
('pinwheel','Четыре крыла','Отскок вокруг центра открывает четыре сектора.',(12,)),
('spiral','Спираль к ядру','Проследи открытый путь от входа к центру.',(0,)),
('stair','Лестница отскоков','Поднимайся по ступеням через свободные торцы.',(0,)),
('shelves','Ворота со сдвигом','Средний проём смещён относительно нижнего.',([50,32,14],[17,43,23])),
('u','Внутренний двор','Обойди боковые стены открытого двора.',(17,)),
('split','Две башни','Центральный коридор разветвляется на два крыла.',(34,)),
('dogleg','Маршрут молнии','Выбирай отскок на каждом повороте.',([(17,66),(17,51),(45,51),(45,30),(21,30),(21,7)],)),
('spiral','Золотая сердцевина','Внутренние стенки защищают центр с трёх сторон.',(1,)),
('shelves','Обратный каскад','Левый вход ведёт к правому верхнему карману.',([52,37,22,9],[16,46,20,44])),
('fork','Три решения','Разбери боковые карманы перед центральным.',(16,48)),
('pinwheel','Мельница','У каждой лопасти есть открытый торец.',(15,)),
('stair','Диагональная галерея','Используй боковую стену для следующего пролёта.',(1,)),
('spiral','Двойная спираль','Два открытых входа, два пути к внутренним блокам.',(2,)),
('shelves','Пять отсечек','У каждой перегородки свой смещённый проём.',([54,43,32,21,10],[47,17,44,20,42])),
('dogleg','Кристальный зигзаг','Не стреляй в сталь: ищи свободный угол.',([(47,66),(47,54),(17,54),(17,36),(45,36),(45,18),(19,18),(19,4)],)),
('u','Крепость в крепости','Проёмы внешнего и внутреннего двора смещены.',(21,)),
('pinwheel','Ротор','Четыре поворота вокруг защищённого центра.',(18,)),
('fork','Встречные ворота','Проходы не лежат на одной прямой.',(21,43)),
('spiral','Лабиринт спектра','Вход снизу связан с каждой частью спирали.',(3,)),
('stair','Восходящий каскад','Чередуй удары от боков и торцов ступеней.',(2,)),
('shelves','Ломаная перспектива','Дальние секции требуют косых отскоков.',([54,43,32,21,10],[21,45,16,43,24])),
('split','Цитадели','Две башни прикрыты смещёнными щитами.',(25,)),
('dogleg','Шесть поворотов','Построй путь через все повороты коридора.',([(16,66),(16,54),(46,54),(46,38),(19,38),(19,22),(44,22),(44,5)],)),
('spiral','Центр притяжения','Разрушай стенки из блоков, сокращая путь к ядру.',(4,)),
('shelves','Серебряные затворы','Стальные полки отражают, цветные клетки разрушаются.',([55,44,33,22,11],[47,16,46,18,44])),
('pinwheel','Роза маршрутов','Последовательно открывай сектора за лопастями.',(21,)),
('u','Тройной двор','Три вложенные ограды остаются открытыми снизу.',(25,)),
('stair','Высокое напряжение','Проводить шар нужно через торцы лестницы.',(3,)),
('spiral','Двойной вихрь','Выбери вход и расчисти две спиральные ветви.',(5,)),
('fork','Скрытые галереи','Боковые карманы соединены обходами вокруг щитов.',(15,49)),
('dogleg','Стальная змейка','Повороты проходят между противоположными торцами.',([(46,66),(46,55),(17,55),(17,42),(45,42),(45,29),(19,29),(19,15),(44,15),(44,4)],)),
('shelves','Галереи мастера','За каждым проёмом нужно снова менять направление.',([55,44,33,22,11],[20,45,17,46,22])),
('pinwheel','Карусель отскоков','Четыре открытые камеры обходятся вокруг центра.',(24,)),
('spiral','Янтарный лабиринт','Внутреннее ядро прикрыто поворотными щитами.',(6,)),
('u','Последняя крепость','Не пропускай блоки в боковых дворах.',(27,)),
('stair','Лестница мастера','Проходы узкие, но торцы доступны с двух сторон.',(4,)),
('fork','Тройной рубеж','Внешние пути ведут к разным защищённым секциям.',(20,44)),
('dogleg','Длинный обход','Нижний вход связан со всей ломаной галереей.',([(17,66),(17,56),(46,56),(46,44),(19,44),(19,32),(44,32),(44,20),(21,20),(21,5)],)),
('spiral','Сердце шторма','Ищи короткие отскоки через открытые торцы.',(7,)),
('shelves','Шесть ворот','Шесть смещённых ворот ведут к верхней галерее.',([56,47,38,29,20,11],[47,17,45,19,43,22])),
('pinwheel','Последний ротор','Расчисти крылья и открой центральные блоки.',(27,)),
('final','Абсолютный шторм','Сочетай обход перегородок и точные отскоки.',())
]
ART=[('Янтарный дракон','dragon'),('Компас приливов','compass'),('Нефритовый храм','temple'),('Лунный мотылёк','moth'),('Песочные часы','hourglass'),('Танец карпов','koi'),('Золотой механизм','gears'),('Корона спектра','crown')]

def puzzle(n,idx,s):
 family,title,hint,param=s;b=Board(n);gap=max(8,19-n//5);entry=max(10,23-n//5);center=32
 # First levels are compact. Later fields progressively occupy the full height.
 top=max(3,48-idx*6);b.fill(idx,top,64)
 if family=='shelves':
  ys,gs=param;b.shelves(ys,gs,gap);center=gs[0];b.rect(center-gap//2,ys[0],gap,66-ys[0],'.')
 elif family=='dogleg':
  pts=param[0];center=pts[0][0];b.line(pts,max(gap,9),'.')
  # Alternating full-width metal shelves force the bends, with wide turning bays.
  bends=pts[1::2]
  for j,(x,y) in enumerate(bends[:-1]):
   nxt=pts[min(2*j+3,len(pts)-1)][0];gy=int((y+pts[2*j+2][1])/2) if pts[2*j+2][1]!=y else y-5
   b.shelves([gy],[nxt],gap+2)
 elif family=='split':
  k=param[0];center=32;b.rect(27,5,10,61,'.');b.rect(31,8,2,42,'#');b.rect(1,k-3,62,8,'.')
  b.rect(9,k,46,2,'#');b.rect(24,k-3,16,8,'.');b.rect(1,57,62,7,'.')
  for x in [12,44]:b.rect(x,43,8,21,'.')
 elif family=='fork':
  left,right=param;b.rect(27,43,10,23,'.');b.rect(left-4,38,right-left+9,10,'.')
  b.rect(left-4,9,9,40,'.');b.rect(right-4,9,9,40,'.');b.rect(left-4,6,right-left+9,8,'.')
  b.rect(25,17,14,26,'#');b.rect(28,13,8,35,'.')
  for y in [25,49]:b.shelves([y],[left if y==49 else right],gap+2)
 elif family=='pinwheel':
  arm=param[0];center=32;b.rect(27,1,10,65,'.');b.rect(1,29,62,10,'.')
  b.rect(28,29-arm,2,arm,'#');b.rect(35,39,2,min(22,arm),'#');b.rect(max(3,28-arm),36,arm,2,'#');b.rect(37,29,min(24,arm+3),2,'#')
  # Central open pinwheel, with no closed ring or intersecting walls.
  b.rect(30,31,4,5,'.');b.rect(1,60,62,6,'.')
  for x,y in [(8,13),(43,12),(9,45),(44,46)]:b.rect(x,y,10,7,'.')
  if arm>19:b.shelves([55],[18 if idx%2 else 46],gap+2);center=18 if idx%2 else 46
 elif family=='u':
  depth=param[0];b.rect(27,4,10,62,'.')
  for j in range(1+(depth>=20)+(depth>=26)):
   l=7+j*9;r=56-j*9;t=9+j*9;bottom=57-j*6
   b.rect(l,t,r-l+1,2,'#');b.rect(l,t,2,bottom-t,'#');b.rect(r-1,t,2,bottom-t,'#')
   # Each U opens toward the bottom; no sealed nested perimeter.
   b.rect(l-4,bottom-2,7,5,'.');b.rect(r-2,bottom-2,7,5,'.')
  b.rect(25,51,14,15,'.');b.rect(2,60,60,6,'.')
 elif family=='stair':
  variant=param[0];mirror=variant%2==1;pts=[]
  coords=[(12,66),(12,55-variant),(24+variant,55-variant),(24+variant,40-variant),(37+variant,40-variant),(37+variant,26-variant),(51,26-variant),(51,7)]
  if mirror:coords=[(63-x,y) for x,y in coords]
  center=coords[0][0];b.line(coords,gap+2,'.')
  for j,(x,y) in enumerate(coords[1:-1:2]):
   # The rail is only alongside a turn and always has two exposed ends.
   sx=(63-x if mirror else x);yy=y-4
   if not mirror:b.rect(2,yy,max(2,x-gap//2-2),2,'#')
   else:b.rect(x+gap//2,yy,62-x-gap//2,2,'#')
 elif family=='spiral':
  v=param[0];mirror=v%2==1;center=32
  pts=[(32,66),(32,59),(8,59),(8,8),(55,8),(55,49),(19,49),(19,20),(44,20),(44,38),(30,38),(30,30)]
  if mirror:pts=[(63-x,y) for x,y in pts];center=31
  b.line(pts,max(5,8-n//20),'.')
  # Only three partial walls; never complete metal rings.
  for x,y,w,h in [(13,15,2,25),(24,43,22,2),(48,22,2,20)]:
   if mirror:x=64-x-w
   b.rect(x,y,w,h,'#')
  if v in [2,5]:
   b.line([(50,66),(50,57),(57,57),(57,9)],6,'.');b.extra_gate=(46,55)
 elif family=='final':
  center=46;b.shelves([55,42,29],[46,18,44],9)
  b.rect(42,55,9,11,'.');b.line([(44,27),(44,9),(12,9),(12,22),(30,22),(30,16)],8,'.')
  b.rect(19,14,2,9,'#');b.rect(28,26,25,2,'#')
 # Never hide the entry: clear a whole landing gallery immediately above bottom wall.
 b.rect(1,64,62,2,'.')
 gates=[(max(2,center-entry//2),min(62,center+(entry+1)//2))]
 if hasattr(b,'extra_gate'):gates.append(b.extra_gate)
 b.border(gates)
 return b,title,hint,family

def art(n,title,kind):
 b=Board(n);b.art=kind
 for y in range(4,62):
  for x in range(3,61):
   u=(x-31.5)/29;v=(y-32)/28;r=math.hypot(u,v);a=math.atan2(v,u);inside=False;col=0
   if kind=='dragon':
    # A long curled body, pointed head, back spines and paired wings.
    body=abs(u-.42*math.sin(v*4.4))<.16 and abs(v)<.85
    wing=abs(u)>.18 and abs(u)<(.92-.65*abs(v+.05)) and -.48<v<.27
    head=((u-.42)/.29)**2+((v+.68)/.18)**2<1
    spine=abs(u-.42*math.sin(v*4.4))<.27 and int((v+1)*15)%3==0 and abs(v)<.7
    inside=body or wing or head or spine;col=2 if body or head else 1 if spine else 0
    if head and .42<u<.56 and -.76<v<-.69:col=8
   elif kind=='compass':
    ring=.72<r<.91;ray=r<(.83 if int((a+math.pi)/(math.pi/8))%2==0 else .52) and abs(math.sin(a*4))<.23
    inside=ring or ray or r<.17;col=2 if ray or r<.17 else 4 if int((a+math.pi)*8)%2 else 6
   elif kind=='temple':
    roof=abs(v+.51)<.15 and abs(u)<.93-2.1*abs(v+.55)
    roof2=abs(v+.26)<.10 and abs(u)<.75
    cols=-.19<v<.57 and any(abs(u-k)<.11 for k in [-.57,-.19,.19,.57])
    base=.58<v<.78 and abs(u)<.90 or .45<v<.59 and abs(u)<.76
    inside=roof or roof2 or cols or base;col=3 if cols else 2 if base else 4
   elif kind=='moth':
    wing=(((abs(u)-.49)/.47)**2+((v+.20)/.45)**2<1 or ((abs(u)-.39)/.35)**2+((v-.40)/.32)**2<1)
    body=abs(u)<.075 and abs(v)<.78;antenna=abs(abs(u)-(-v-.68)*.8)<.06 and -.95<v<-.66
    inside=wing or body or antenna;eye=((abs(u)-.50)/.13)**2+((v+.16)/.16)**2<1
    col=2 if body or eye else 6 if v<0 else 4
   elif kind=='hourglass':
    cap=abs(v)>.77 and abs(v)<.87 and abs(u)<.78
    body=abs(v)<.76 and abs(u)<.12+.72*abs(v)
    inside=cap or body;col=8 if cap or abs(u)>.02+.64*abs(v) else 2 if v>.10 or v<-.42 else 4
   elif kind=='koi':
    # Two curved, opposing fish around an empty center; fins and tails included.
    for sign in [-1,1]:
     uu=u*sign;vv=v*sign;aa=math.atan2(vv,uu);rr=math.hypot(uu,vv)
     body=.35<rr<.79 and -.35<aa<2.12
     tail=abs(uu+.39)<.27 and .30<vv<.88 and abs(uu+.39)<(vv-.23)*.65
     if body or tail:
      inside=True;col=0 if sign==1 else 8
      if body and int((aa+.4)*7)%4==0:col=2 if sign==1 else 4
      if body and uu>.46 and -.07<vv<.08:col=9
   elif kind=='gears':
    for cx,cy,rad,colr in [(-.37,-.36,.46,2),(.39,.27,.48,4),(-.42,.56,.29,6)]:
     rr=math.hypot(u-cx,v-cy);aa=math.atan2(v-cy,u-cx);outer=rad*(1 if int((aa+math.pi)*16/math.pi)%2==0 else .84)
     if .30*rad<rr<outer:inside=True;col=colr
     if .15*rad<rr<.27*rad:inside=True;col=8
   elif kind=='crown':
    base=abs(u)<.81 and .20<v<.65
    peak=-.65<v<.22 and abs(u)<.91 and (abs(u)<.17*(v+.80) or abs(abs(u)-.50)<.22*(v+.90) or abs(abs(u)-.83)<.18*(v+.88))
    inside=base or peak;gem=any(((u-cx)/.12)**2+((v-.39)/.13)**2<1 for cx in [-.47,0,.47]);col=6 if gem else 2 if v<.52 else 1
   if inside:b.c[y][x]=chr(97+col)
 # Small open-ended silver reflectors make art levels playable puzzles too.
 b.rect(8,57,15,2,'#');b.rect(41,57,15,2,'#');b.rect(1,60,62,6,'.');b.rect(27,54,10,12,'.')
 # Two disconnected short shields; open bypasses around all four ends.
 if n>25:b.rect(11,34,2,10,'#');b.rect(51,19,2,10,'#')
 b.border([(26,38)])
 return b,title,'Ищи отскоки вокруг щитов и разбирай фигуру по частям.','art:'+kind

# Art pieces are interleaved; each position has a stricter handling profile.
insert={6:0,12:1,18:2,24:3,30:4,36:5,44:6,52:7}
catalog=[];pi=0
for n in range(1,59):
 if n in insert:
  title,kind=ART[insert[n]];b,title,hint,family=art(n,title,kind)
 else:
  b,title,hint,family=puzzle(n,pi,S[pi]);pi+=1
 # Very first encounter retains a short clear goal, with a real corner puzzle.
 if n==1:
  for y in range(H):
   for x in range(W):
    if b.c[y][x].islower():b.c[y][x]='.'
  for x,y,w,h,col in [(6,28,17,6,'c'),(38,28,17,6,'e'),(9,46,17,6,'g')]:b.rect(x,y,w,h,col)
 data=b.final();catalog.append(dict(number=n,title=title,hint=hint,family=family,gates=b.gates,cells=data,blocks=sum(c.isalpha() for c in data),speed=round(190+(n-1)*2.3,2),paddle=round(144-(n-1)*1.05,2)))
assert pi==50
out=['package pro.brickstorm.game;','/** Generated by scripts/build-levels.py from the authored catalog. */','final class LevelData {']
for field in ['title','hint','family','cells']:
 out.append('    static final String[] '+field.upper()+'={')
 for l in catalog:
  if field=='cells':out.append('        '+json.dumps(l[field])+',')
  else:out.append('        '+json.dumps(l[field],ensure_ascii=False)+',')
 out.append('    };')
out.append('    static final int[][] GATES={')
for l in catalog:out.append('        {'+','.join(str(i) for pair in l['gates'] for i in pair)+'},')
out.extend(['    };','    private LevelData(){}','}'])
(ROOT/'app/src/main/java/pro/brickstorm/game/LevelData.java').write_text('\n'.join(out)+'\n')
(ROOT/'verification/level-catalog.json').write_text(json.dumps(catalog,ensure_ascii=False,indent=2))
print('Wrote',len(catalog),'levels:',pi,'puzzles +',len(ART),'art; bricks',min(l['blocks'] for l in catalog),'..',max(l['blocks'] for l in catalog))

"""Playable chamber layouts inspired by the eighteen user-supplied reference images."""
import math
NAMES=['Поперечный шлюз','Зелёная буква Р','Т-образная камера','Тройной замок','Янтарный угол','Красный крюк','Косая единица','Левый лабиринт','Камера сердца','Изумрудная эс','Четвёрка','Три опоры','Звёздная камера','Восемь лучей','Синие гребни','Зал сокровищ','Синяя скважина','Изумрудный поезд']
SOURCES=['webp-H','images (1).jpg','images (1).png','images (10).jpg','images (11).jpg','images (12).jpg','images (13).jpg','images (14).jpg','images (2).jpg','images (3).jpg','images (4).jpg','images (5).jpg','images (6).jpg','images (7).jpg','images (8).jpg','images (9).jpg','images.jpg','images.png']
def build_reference(n,k,Board,inside):
 b=Board(n);color=['b','k','k','b','b','a','a','a','k','k','k','m','k','a','f','a','m','k'][k]
 for y in range(1,67):
  for x in range(1,63):b.c[y][x]=color
 def rect(x,y,w,h):
  for yy in range(y,y+h):
   for xx in range(x,x+w):
    if 0<xx<63 and 0<yy<67:b.mask.add((xx,yy))
 def polygon(points):
  for y in range(1,63):
   for x in range(1,63):
    if inside(x,y,points):b.mask.add((x,y))
 def circle(cx,cy,r):
  for y in range(1,63):
   for x in range(1,63):
    if (x-cx)**2+(y-cy)**2<=r*r:b.mask.add((x,y))
 def wall(points):
  for a,z in zip(points,points[1:]):b.paint_line(a,z,1.1,'#')
  b.mask.difference_update((x,y) for y in range(1,63) for x in range(1,63) if b.c[y][x]=='#')
 features=[]
 if k==0:
  main=[(32,67),(32,27)];branches=[[(15,32),(15,45)],[(49,32),(49,45)],[(15,39),(49,39)]];ports=[[(32,27),(32,18)]]
 elif k==1:
  rect(20,16,7,37);circle(31,27,13);rect(24,16,9,24)
  main=[(23,67),(23,48),(23,27),(34,27)];branches=[];ports=[[(23,29),(6,29)]]
 elif k==2:
  rect(12,16,40,9);rect(28,23,9,31)
  main=[(32,67),(32,21)];branches=[[(17,20),(47,20)]];ports=[[(32,20),(32,5)]]
 elif k==3:
  rect(12,40,41,22);main=[(31,67),(31,60),(44,60),(44,57),(26,57),(26,53),(32,53)];branches=[];ports=[[(32,53),(32,35)]]
  features=[[(12,61),(12,40),(52,40),(52,61),(12,61)],[(16,58),(16,44),(48,44),(48,58),(16,58)],[(20,55),(20,48),(44,48),(44,55),(20,55)]]
 elif k==4:
  main=[(48,67),(48,43),(12,43)];branches=[];ports=[[(12,43),(6,43)]]
 elif k==5:
  main=[(29,67),(29,44),(40,44)];branches=[];ports=[[(40,44),(48,44)]]
 elif k==6:
  main=[(37,67),(37,38),(21,51)];branches=[];ports=[[(21,51),(15,51)]]
 elif k==7:
  rect(3,43,29,19);main=[(17,67),(17,60),(24,60),(24,57),(14,57),(14,53),(18,53)];branches=[];ports=[[(18,53),(18,37)]]
  features=[[(3,61),(3,43),(31,43),(31,61),(3,61)],[(7,58),(7,47),(27,47),(27,58),(7,58)],[(11,55),(11,51),(23,51),(23,55),(11,55)]]
 elif k==8:
  points=[]
  for i in range(96):
   t=i*2*math.pi/96;points.append((32+1.18*16*math.sin(t)**3,29-1.12*(13*math.cos(t)-5*math.cos(2*t)-2*math.cos(3*t)-math.cos(4*t))))
  polygon(points);main=[(32,67),(32,45),(32,29)];branches=[];ports=[[(32,27),(32,12),(32,5)]]
 elif k==9:
  main=[(19,67),(19,50),(45,50),(45,38),(19,27),(19,14),(47,14)];branches=[];ports=[[(47,14),(57,14)]]
 elif k==10:
  main=[(37,67),(37,21)];branches=[[(15,14),(15,39),(37,39)]];ports=[[(37,21),(37,7)]]
 elif k==11:
  main=[(32,67),(32,20)];branches=[[(16,47),(16,20),(49,20),(49,47)]];ports=[[(32,20),(32,7)]]
 elif k==12:
  polygon([(32+(22 if j%2==0 else 10)*math.cos(-math.pi/2+j*math.pi/5),31+(22 if j%2==0 else 10)*math.sin(-math.pi/2+j*math.pi/5)) for j in range(10)])
  main=[(32,67),(32,45),(32,31)];branches=[];ports=[[(37,30),(55,30)]]
 elif k==13:
  rect(26,25,13,14);main=[(32,67),(32,32)];branches=[]
  for j in range(8):
   a=j*math.pi/4;branches.append([(32,32),(32+22*math.cos(a),32+22*math.sin(a))])
  ports=[[(32,12),(32,5)]]
 elif k==14:
  rect(18,39,28,23);main=[(22,67),(22,43),(28,43),(28,59),(35,59),(35,43),(42,43),(42,58)];branches=[];ports=[[(42,52),(52,52)]]
  features=[[(25,48),(25,61)],[(32,40),(32,55)],[(39,48),(39,61)]]
 elif k==15:
  for y in range(12,32):
   for x in range(10,55):b.c[y][x]='c'
  rect(18,42,28,20);main=[(22,67),(22,45),(28,45),(28,59),(35,59),(35,45),(41,45),(41,57)];branches=[];ports=[[(32,45),(32,32)]]
  features=[[(25,49),(25,61)],[(32,43),(32,56)],[(38,49),(38,61)]]
 elif k==16:
  rect(27,41,12,21);main=[(32,67),(32,59),(35,59),(35,45),(29,45),(29,57)];branches=[];ports=[[(29,57),(20,57)]];features=[[(32,48),(32,59)]]
 else:
  rect(12,30,40,13);rect(33,16,19,20);rect(18,18,6,15);circle(21,18,4);circle(21,44,4);circle(45,44,4)
  main=[(32,67),(32,40),(21,40),(21,20)];branches=[[(21,37),(45,37),(45,30)]];ports=[[(21,20),(21,7)]]
  features=[[(38,21),(46,21),(46,27),(38,27),(38,21)]]
 # Broad hollow silhouette with a steel outline, surrounded by real dense breakable cells.
 for route in [main,*branches]:b.route(route,7 if k in [9,13] else 5)
 room=set(b.mask)
 for x,y in room:
  for dx,dy in [(1,0),(-1,0),(0,1),(0,-1)]:
   xx,yy=x+dx,y+dy
   if 0<xx<63 and 0<yy<63 and (xx,yy) not in room:b.c[yy][xx]='#'
 for feature in features:wall(feature)
 # Explicit outlets prevent the unbreakable silhouette rim from sealing the field off.
 for route in [main,*branches,*ports]:b.route(route,3 if k in [3,7,14,15,16] else 4)
 data=b.finish(main[0][0],n%4==0)
 return dict(number=n,id=101+k,title=NAMES[k],hint='Войди в камеру и найди выход к внешним кирпичам.',family='archive:'+str(k+1),reference=SOURCES[k],gates=b.gates,gateWidths=b.gateWidths,cells=data,blocks=sum(c.isalpha() for c in data),routes=[main,*branches,*ports])

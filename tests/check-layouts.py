#!/usr/bin/env python3
"""Independent structure/port checks, not an automated level playthrough."""
from pathlib import Path
from collections import deque,Counter
import json
root=Path(__file__).resolve().parents[1]
levels=json.loads((root/'verification/level-catalog.json').read_text())
assert len(levels)==200
shapes=set();ids=set();families=Counter();counts=Counter()
def neighbors(i):
 x,y=i%64,i//64
 return [j for xx,yy,j in ((x-1,y,i-1),(x+1,y,i+1),(x,y-1,i-64),(x,y+1,i+64)) if 0<=xx<64 and 0<=yy<68]
def flood(starts,allowed):
 seen=set(starts);q=deque(starts)
 while q:
  i=q.popleft()
  for j in neighbors(i):
   if j not in seen and allowed(j):seen.add(j);q.append(j)
 return seen
for n,l in enumerate(levels,1):
 d=l['cells'];assert len(d)==4352 and l['number']==n
 assert l['id']>=20001 and l['id'] not in ids;ids.add(l['id'])
 assert l['family'].startswith('maze:');families[l['family']]+=1
 assert 1<=len(l['ports'])<=3;counts[len(l['ports'])]+=1
 assert all(d[x]=='#' for x in range(64)),(n,'top is open')
 assert all(d[y*64]=='#' and d[y*64+63]=='#' for y in range(68)),(n,'side is open')
 assert 10<l['gateWidths'][0]<=16
 gate={67*64+x for a,b in l['gates'] for x in range(a,b)}
 assert {i for i in range(67*64,68*64) if d[i]!='#'}==gate,(n,'extra bottom opening')
 empty=flood(gate,lambda i:d[i]=='.')
 assert empty=={i for i,c in enumerate(d) if c=='.'},(n,'disconnected empty corridor')
 reachable=flood(gate,lambda i:d[i]!='#')
 assert all(i in reachable for i,c in enumerate(d) if c.isalpha()),(n,'sealed bricks')
 actual={i for i,c in enumerate(d) if c=='.' and any(d[j].isalpha() for j in neighbors(i))}
 declared={y*64+x for p in l['ports'] for x,y in p}
 assert actual==declared,(n,'unintended brick exposure: wall is decorative or missing',actual^declared)
 remaining=set(actual);window_count=0
 while remaining:
  part=flood([min(remaining)],lambda i:i in actual);remaining-=part;window_count+=1
 assert window_count==len(l['ports']),(n,'window grouping')
 assert sum(c.isalpha() for c in d)==l['blocks']>=2150,(n,'field is too empty')
 assert min(l['portDistances'])>=15
 assert l['tunnelLength']>=70,(n,'route is too short')
 # Every authored centerline has a full three-cell cross-section. The route
 # follows four-connected raster stairs at angled segments, never a corner crack.
 for path in l['routes']:
  for a,b in zip(path,path[1:]):
   steps=max(abs(b[0]-a[0]),abs(b[1]-a[1]))
   for k in range(steps+1):
    x=round(a[0]+(b[0]-a[0])*k/max(1,steps));y=round(a[1]+(b[1]-a[1])*k/max(1,steps))
    for dx in (-1,0,1):
     for dy in (-1,0,1):
      if 1<=y+dy<=66:assert d[(y+dy)*64+x+dx]=='.',(n,'narrowed corridor',x,y)
 shape=''.join(c if c in '.#' else 'x' for c in d)
 assert shape not in shapes,(n,'repeated wall geometry');shapes.add(shape)
assert len(families)==25 and all(v==8 for v in families.values())
print(f'PASS: 200 unique enclosed mazes, 25 route families, 1–3 actual brick-facing windows {dict(counts)}; no other exposed bricks, closed top/sides, connected ball-width routes and all bricks structurally reachable.')

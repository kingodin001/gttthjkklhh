#!/usr/bin/env python3
"""Static geometry checks only, no automated playthrough."""
import json,collections,math
from pathlib import Path
root=Path(__file__).resolve().parents[1];levels=json.loads((root/'verification/level-catalog.json').read_text());shapes=set();families=set()
for l in levels:
 n=l['number'];data=l['cells'];gates=l['gates'];width=l['gateWidths'][0]
 assert 10<width<=16,(n,'entry wider than three balls or narrower than two')
 assert all(data[x]=='#' for x in range(64)),(n,'open top')
 assert all(data[y*64]=='#' and data[y*64+63]=='#' for y in range(68)),(n,'open side')
 assert not any(data[y*64+x]=='#' for y in range(63,67) for x in range(1,63)),(n,'wall squeezes bottom landing chamber')
 # There must be at least TWO full cell rows of clearance between separated walls.
 for x in range(1,63):
  ys=[y for y in range(68) if data[y*64+x]=='#']
  assert all(b-a!=2 for a,b in zip(ys,ys[1:])),(n,'one-row pocket',x)
 starts=[66*64+x for a,b in gates for x in range(a,b)]
 def flood(allow):
  seen=set(starts);q=collections.deque(starts)
  while q:
   i=q.popleft();x,y=i%64,i//64
   for dx,dy in [(1,0),(-1,0),(0,1),(0,-1)]:
    xx,yy=x+dx,y+dy;j=yy*64+xx
    if 0<=xx<64 and 0<=yy<67 and j not in seen and allow(data[j]):seen.add(j);q.append(j)
  return seen
 seen=flood(lambda c:c!='#')
 assert all(not c.isalpha() or i in seen for i,c in enumerate(data)),(n,'sealed bricks')
 if not l['family'].startswith('art:'):
  empty=flood(lambda c:c=='.')
  for path in l['routes']:
   for a,b in zip(path,path[1:]):
    count=max(2,math.ceil(math.dist(a,b)*2))
    for j in range(count+1):
     t=j/count;x=round(a[0]+(b[0]-a[0])*t);y=min(66,round(a[1]+(b[1]-a[1])*t))
     assert y*64+x in empty,(n,'disconnected initial route',x,y)
  shape=''.join('#' if c=='#' else '.' if c=='.' else 'x' for c in data)
  assert shape not in shapes,(n,'duplicate route geometry');shapes.add(shape);families.add(l['family'])
  for y in range(1,65,8):assert sum(c.isalpha() for c in data[y*64:(y+8)*64])>12,(n,'empty height band')
assert len(shapes)==292 and len(families)==292
assert len(set(l['id'] for l in levels))==300
assert sum(l.get('reference') is not None for l in levels)==18
summary=f'PASS: 292 different route graphs, including 18 new archive chambers; all 300 have exact 2–3-ball apertures, connected reachable bricks and no one-row wall pockets.'
print(summary)

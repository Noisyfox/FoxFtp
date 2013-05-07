#coding:utf-8
import os, sys,re
from os.path import join, getsize

of = open(join(sys.argv[2], sys.argv[3]), 'w')
r_r = re.compile(r'[\r\n\t]+')

for root, dirs, files in os.walk(sys.argv[1]):
    for name in files:
        _f = join(root, name)
        _t = unicode(_f, 'gbk', 'ignore')
        if(not r_r.search(_t)):
            of.write('%s\t%s\t%d\n'%(_t.encode('utf8'), os.path.split(_t)[1].encode('utf8'), os.stat(_f).st_size))
    if(root != sys.argv[1]):
        _f = root
        _t = unicode(_f, 'gbk', 'ignore')
        if(not r_r.search(_t)):
            of.write('%s\t%s\t%d\n'%(_t.encode('utf8'), os.path.split(_t)[1].encode('utf8'), -1))
    
of.close()


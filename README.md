ant l2 -Darg1=./problem/L2/reverse.json

ant deep -Dapp=./problem/DeepCoder/prog5.json -Ddepth=3

# Neo version
Without learning
ant neodeep -Dapp=./problem/DeepCoder/prog2.json -Ddepth=3 -Dlearn=false

With learning
ant neodeep -Dapp=./problem/DeepCoder/prog2.json -Ddepth=3 -Dlearn=true

Note: Neo depth is -1 than deep (e.g. if deep depth is 4, neo depth is 3)

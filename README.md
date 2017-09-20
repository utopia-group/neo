ant l2 -Darg1=./problem/L2/reverse.json

# Outdated
ant deep -Dapp=./problem/DeepCoder/prog5.json -Ddepth=3

# Neo version
Without learning and statistical model
ant neodeep -Dapp=./problem/DeepCoder/prog7.json -Ddepth=4 -Dlearn=false -Dstat=false

# Outdated
With learning and statistical model
ant neoDeepOLD -Dapp=./problem/DeepCoder/prog7.json -Ddepth=4 -Dlearn=true -Dstat=true

Note: Neo depth is -1 than deep (e.g. if deep depth is 4, neo depth is 3)

New version of Neo:
ant neoDeep -Dapp=./problem/DeepCoder/prog2.json -Ddepth=2 -Dlearn=false -Dstat=false

Note: Neo2 searches by number of lines. Still experimental version that only enumerates partial programs.

# Neo for Morpheus

Without n-gram information:
ant neoMorpheus -Dapp=./problem/Morpheus/r4.json -Ddepth=3 -Dlearn=false -Dstat=false -Dfile=""

With n-gram information using a file:
ant neoMorpheus -Dapp=./problem/Morpheus/r4.json -Ddepth=3 -Dlearn=false -Dstat=false -Dfile="./sketches/sketches-size3.txt"

# Set up neural net model
#
# requires:
# - Python 2.7
# - NumPy and Tensorflow
#
# The latter can be installed using the following commands:

pip install numpy
pip install tensorflow

# Then, run org.genesys.clients.DeepCoderDeciderMain to test the Python decider.
#
# If a python interpreter other than the default should be used, then create
# a text file ./model/tmp/python_path.txt and include the path. For example,
# to use /usr/local/bin/python, include "/usr/local/bin/" in this file.

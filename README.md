ant l2 -Darg1=./problem/L2/reverse.json

# Outdated
ant deep -Dapp=./problem/DeepCoder/prog5.json -Ddepth=3

# Neo version
Without learning and statistical model
ant neodeep -Dapp=./problem/DeepCoder/prog7.json -Ddepth=4 -Dlearn=false -Dstat=false

With learning and statistical model
ant neodeep -Dapp=./problem/DeepCoder/prog7.json -Ddepth=4 -Dlearn=true -Dstat=true

Note: Neo depth is -1 than deep (e.g. if deep depth is 4, neo depth is 3)

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

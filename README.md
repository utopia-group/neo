# Neo for DeepCode 
Original deepCode: no learning + statistical model:

ant neoDeep -Dapp=./problem/DeepCoder-pldi18/prog1.json -Ddepth=4 -Dlearn=false -Dstat=true -Dfile=""

# Neo for Morpheus

Without n-gram information:

ant neoMorpheus -Dapp=./problem/Morpheus/r4.json -Ddepth=3 -Dlearn=false -Dstat=false -Dfile=""

With n-gram information using a file:

ant neoMorpheus -Dapp=./problem/Morpheus/r4.json -Ddepth=3 -Dlearn=false -Dstat=false -Dfile="./sketches/sketches-size3.txt"

# Set up neural net model

 requires:
 - Python 2.7
 - NumPy and Tensorflow

 The latter can be installed using the following commands:

pip install numpy
pip install tensorflow

 Then, run org.genesys.clients.DeepCoderDeciderMain to test the Python decider.

 If a python interpreter other than the default should be used, then create
 a text file ./model/tmp/python_path.txt and include the path. For example,
 to use /usr/local/bin/python, include "/usr/local/bin/" in this file.

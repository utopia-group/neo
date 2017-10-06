import io
import random
import numpy as np

from consts import *
from gen_io import *

# filename: str (path of the dataset to read)
# funcs_filename: str (path of the set of DSL operators)
# num_vals: int (the number of possible values in the input-output examples)
# max_len: int (the maximum length of an input-output example)
# return: [([int], [int], [int], [int])] (an array of (input value, output value, dsl operator ngram, label) tuples)
def read_deep_coder_train_dataset(filename, funcs_filename, num_vals, max_len):
    # Step 1: Read functions
    f = open(DATA_PATH + '/' + funcs_filename)
    funcs = {line[:-1]: i for (i, line) in enumerate(f)}
    f.close()

    # Step 2: Read programs

    f = open(DATA_PATH + '/' + filename)

    dataset = []
    counter = 0
    total_read = 0

    lines = []
    for line in f:
        lines.append(line)

    # Step 3: Shuffle dataset
    random.shuffle(lines)

    f.close()

    # Step 4: Build data set
    
    # helper function
    def process(ex):
        if type(ex) is np.int64 or type(ex) is int:
            partial_value = np.zeros(1, dtype=np.int64) + int(ex + num_vals/2)
        elif type(ex) is list:
            partial_value = np.array(ex, dtype=np.int64) + num_vals/2
        else:
            raise Exception('Not recognized: ' + str(type(ex)))
        value = np.zeros(max_len, dtype=np.int64) + num_vals
        value[:len(partial_value)] = partial_value
        return value.tolist()

    for line in lines:

        if counter > 400000:
            break

        if counter%10000 == 0:
            print 'Reading:', counter
        counter += 1
        
        # Step 4a: Convert to source code accepted by DeepCoder compiler
        source_code = '\n'.join(line.strip().split(' | '))

        # Step 4b: Compile the program
        program = compiler(source_code, num_vals/2, max_len)

        # Step 4c: Generate the IO examples
        try:
            io_examples = generate_IO_examples([program], 5, max_len)[0]
        except:
            continue

        # construct the input value
        input_value_0 = []
        input_value_1 = []
        output_value = []
        
        for i in range(5):
            if len(io_examples[i][0]) == 2:
                input_value_0.append(process(io_examples[i][0][0]))
                input_value_1.append(process(io_examples[i][0][1]))
            elif len(io_examples[i][0]) == 1:
                input_value_0.append(process(io_examples[i][0][0]))
                input_value_1.append(process([]))
            else:
                raise Exception('Invalid input example: ' + str(io_examples[i][0]))

            # construct the output value
            output_value.append(process(io_examples[i][1]))

        # construct the label by iterating over functions in the program
        ngram = [len(funcs), len(funcs)]
        for func in program.src.split():
            if func in funcs:
                label = np.zeros([len(funcs)], dtype=np.int64).tolist()
                label[funcs[func]] = 1
                dataset.append(tuple([x for x in input_value_0] + [x for x in input_value_1] + [x for x in output_value] + [ngram, label]))
                ngram = [ngram[1], funcs[func]]

        total_read += 1
        
    print 'Total read:', total_read

    return dataset

# filename: str (path of the dataset to read)
# dataset: [([int], [int], [int], [int])] (an array of (input value, output value, dsl operator ngram, label) tuples)
def write_deep_coder_train_dataset(filename, dataset):

    f = open(DATA_PATH + '/' + filename, 'w')

    counter = 0

    for datapoint in dataset:

        if counter%10000 == 0:
            print 'Writing: ' + str(counter)
        counter += 1
        
        f.write(str(datapoint) + '\n')

    f.close()

# filename: str (path of the dataset to read)
# num_dsl_ops: int (number of DSL operators)
# return: (np.array([num_points, input_length], int),
#          np.array([num_points, input_length], int),
#          np.array([num_points, output_length], int),
#          np.array([num_points, n_gram_length], int),
#          np.array([num_points, num_dsl_ops], int))
#         (a (input values, output values, dsl ops, label) tuple)
def read_train_dataset(filename, num_dsl_ops):
    
    f = open(DATA_PATH + '/' + filename)

    input_values_0 = []
    input_values_1 = []
    output_values = []

    for i in range(5):
        input_values_0.append([])
        input_values_1.append([])
        output_values.append([])
    
    dsl_ops = []
    labels = []

    counter = 0

    for line in f:

        if counter%100000 == 0:
            print 'Read ' + str(counter)
        counter += 1

        # Obtain (DSL ops, input values, output values) tuples
        toks = line[2:-3].split('], [')

        for i in range(5):
            input_values_0[i].append(_process_list(toks[i]))
            input_values_1[i].append(_process_list(toks[i+5]))
            output_values[i].append(_process_list(toks[i+10]))

        dsl_ops.append(_process_list(toks[15]))
        labels.append(_process_list(toks[16]))

    print 'Total read: ' + str(len(labels))

    return (np.array(input_values_0), np.array(input_values_1), np.array(output_values), np.array(dsl_ops), np.array(labels))

# filename: str (path of the dataset to read)
# return: (np.array([num_points, input_length], int),
#          np.array([num_points, output_length], int))
#         (a (input values, output values) tuple)
def read_test_dataset(filename):
    f = open(TMP_PATH + '/' + filename)

    input_values_0 = []
    input_values_1 = []
    output_values = []
    for i in range(5):
        input_values_0.append([])
        input_values_1.append([])
        output_values.append([])
    dsl_ops = []
    
    for line in f:
        # Step 1: Obtain (DSL ops, input values, output values) tuples
        toks = line[2:-3].split('], [')

        # Step 2: Process values
        for i in range(5):
            input_values_0[i].append(_process_list(toks[i]))
            input_values_1[i].append(_process_list(toks[i+5]))
            output_values[i].append(_process_list(toks[i+10]))
        
        dsl_ops.append(_process_list(toks[15]))

    return (np.array(input_values_0), np.array(input_values_1), np.array(output_values), np.array(dsl_ops))
    
# s: str
# return: [int]
def _process_list(s):
    return [int(v) for v in s.split(', ')]

# dataset: (np.array([num_points, input_length], int),
#           np.array([num_points, input_length], int),
#           np.array([num_points, output_length], int),
#           np.array([num_points, n_gram_length], int))
#          (a (input values, output values, label) tuple)
# train_frac: float (proportion of points to use for training)
# return: (train_dataset, test_dataset) where train_dataset, test_dataset each have the same type as dataset
def split_train_test(dataset, train_frac):
    input_values_0 = [_split_train_test_single(input_value_0, train_frac) for input_value_0 in dataset[0]]
    input_values_1 = [_split_train_test_single(input_value_1, train_frac) for input_value_1 in dataset[1]]
    output_values = [_split_train_test_single(output_value, train_frac) for output_value in dataset[2]]
    dsl_ops = _split_train_test_single(dataset[3], train_frac)
    labels = _split_train_test_single(dataset[4], train_frac)
    return tuple(tuple([[input_values_0[i][t] for i in range(5)],
                        [input_values_1[i][t] for i in range(5)],
                        [output_values[i][t] for i in range(5)],
                        dsl_ops[t],
                        labels[t]]) for t in range(2))

# dataset_single: np.array([num_points, num_vals], int)
# train_frac: float (proportion of points to use for training)
def _split_train_test_single(dataset_single, train_frac):
    n_train = int(train_frac*len(dataset_single))
    return (dataset_single[:n_train], dataset_single[n_train:])

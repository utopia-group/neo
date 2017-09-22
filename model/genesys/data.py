import io
import numpy as np

from consts import *
from gen_io import *

# filename: str (path of the dataset to read)
# funcs_filename: str (path of the set of DSL operators)
# num_vals: int (the number of possible values in the input-output examples)
# max_len: int (the maximum length of an input-output example)
# return: [([int], [int], [int])] (an array of (input value, output value, label) tuples)
def read_deep_coder_train_dataset(filename, funcs_filename, num_vals, max_len):
    # Step 1: Read functions
    f = open(DATA_PATH + '/' + funcs_filename)
    funcs = {line[:-1]: i for (i, line) in enumerate(f)}
    f.close()

    # Step 2: Read training set

    f = open(DATA_PATH + '/' + filename)

    dataset = []
    counter = 0
    for line in f:

        if counter%10000 == 0:
            print 'Reading:', counter
        counter += 1
        
        # Step 2a: Convert to source code accepted by DeepCoder compiler
        source_code = '\n'.join(line.strip().split(' | '))

        # Step 2b: Compile the program
        program = compiler(source_code, num_vals/2, max_len)

        # Step 2c: Generate the IO examples
        try:
            io_examples = generate_IO_examples([program], 1, max_len)[0]
        except:
            continue

        # helper function
        def process(ex):
            if type(ex) is np.int64 or type(ex) is int:
                return int(ex + num_vals/2)
            elif type(ex) is list:
                partial_value = np.array(ex, dtype=np.int64) + num_vals/2
                value = np.zeros(max_len, dtype=np.int64) + num_vals
                value[:len(partial_value)] = partial_value
                return value.tolist()
            else:
                raise Exception('Not recognized: ' + str(type(ex)))

        # iterate over functions in the program
        for func in program.src.split():
            if func in funcs:
                # construct the input value
                if len(io_examples[0][0]) == 2:
                    input_value = [process(io_examples[0][0][0]), process(io_examples[0][0][1])]
                elif len(io_examples[0][0]) == 1:
                    input_value = process(io_examples[0][0][0])
                else:
                    raise Exception('Invalid input example: ' + str(io_examples[0][0]))

                # construct the output value
                output_value = process(io_examples[0][1])

                # construct the label
                label = np.zeros([len(funcs)], dtype=np.int64)
                label[funcs[func]] = 1
                label = label.tolist()

                dataset.append((input_value, output_value, label))

                break

    f.close()

    return dataset

# filename: str (path of the dataset to read)
# dataset: [([int], [int], [int])] (an array of (input value, output value, label) tuples)
def write_deep_coder_train_dataset(filename, dataset):

    f = open(DATA_PATH + '/' + filename, 'w')

    counter = 0

    for (input_value, output_value, label) in dataset:

        if counter%10000 == 0:
            print 'Writing: ' + str(counter)
        counter += 1
        
        f.write('(' + str(input_value) + ', ' + str(output_value) + ', ' + str(label) + ')\n')

    f.close()

# filename: str (path of the dataset to read)
# num_dsl_ops: int (number of DSL operators)
# return: (np.array([num_points, input_length], int),
#          np.array([num_points, output_length], int),
#          np.array([num_points, n_gram_length], int))
#         (a (input values, output values, label) tuple)
def read_train_dataset(filename, num_dsl_ops):
    
    f = open(DATA_PATH + '/' + filename)

    input_values = []
    output_values = []
    labels = []

    counter = 0

    for line in f:
        if counter%100000 == 0:
            print 'Read ' + str(counter)
        counter += 1

        if counter > 100000:
            break

        try:
            # Step 1: Obtain (DSL ops, input values, output values) tuples
            toks = line[2:-3].split('], [')

            input_value = _process_list(toks[0])
            output_value = _process_list(toks[1])
            label = _process_list(toks[2])

            # Step 2: Process values
            input_values.append(input_value)
            output_values.append(output_value)
            labels.append(label)

        except:
            pass

    print 'Total read: ' + str(len(labels))

    return (np.array(input_values), np.array(output_values), np.array(labels))

# filename: str (path of the dataset to read)
# return: (np.array([num_points, input_length], int),
#          np.array([num_points, output_length], int))
#         (a (input values, output values) tuple)
def read_test_dataset(filename):
    f = open(TMP_PATH + '/' + filename)

    input_values = []
    output_values = []
    for line in f:
        # Step 1: Obtain (DSL ops, input values, output values) tuples
        toks = line[2:-3].split('], [')

        # Step 2: Process values
        input_values.append(_process_list(toks[0]))
        output_values.append(_process_list(toks[1]))

    return (np.array(input_values), np.array(output_values))
    
# s: str
# return: [int]
def _process_list(s):
    return [int(v) for v in s.split(', ')]

# dataset: (np.array([num_points, input_length], int),
#           np.array([num_points, output_length], int),
#           np.array([num_points, n_gram_length], int))
#          (a (input values, output values, label) tuple)
# train_frac: float (proportion of points to use for training)
# return: (train_dataset, test_dataset) where train_dataset, test_dataset each have the same type as dataset
def split_train_test(dataset, train_frac):
    split_dataset = tuple(_split_train_test_single(dataset_single, train_frac) for dataset_single in dataset)
    return (tuple(split_dataset[i][0] for i in range(3)), tuple(split_dataset[i][1] for i in range(3)))

# dataset_single: np.array([num_points, num_vals], int)
# train_frac: float (proportion of points to use for training)
def _split_train_test_single(dataset_single, train_frac):
    n_train = int(train_frac*len(dataset_single))
    return (dataset_single[:n_train], dataset_single[n_train:])

import io
import numpy as np

from consts import *
from gen_io import *

# filename: str (path of the dataset to read)
# funcs_filename: str (path of the set of DSL operators)
# num_vals: int (the number of possible values in the input-output examples)
# max_len: int (the maximum length of an input-output example)
# return: (np.array([num_points, input_length], int),
#          np.array([num_points, output_length], int),
#          np.array([num_points, n_gram_length], int))
#         (a (input values, output values, label) tuple)
def read_train_dataset(filename, funcs_filename, num_vals, max_len):
    # Step 1: Read functions
    f = open(DATA_PATH + '/' + funcs_filename)
    funcs = {line[:-1]: i for (i, line) in enumerate(f)}
    f.close()

    # Step 2: Read training set

    f = open(DATA_PATH + '/' + filename)

    input_values = []
    output_values = []
    labels = []
    counter = 0
    for line in f:

        if counter > 2000000:
            break

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

        # Step 2d: Currently limited to programs with a single input
        if len(io_examples[0][0]) != 1:
            continue

        # Step 2e: Obtain labels
        for func in program.src.split():
            if func in funcs:
                # construct the label
                label = np.zeros(len(funcs))
                label[funcs[func]] = 1.0

                # construct the input value
                input_value_partial = np.array(io_examples[0][0][0]) + num_vals/2
                input_value = np.zeros(max_len) + num_vals
                input_value[:len(input_value_partial)] = input_value_partial

                # construct the output value
                try:
                    output_value_partial = np.array(io_examples[0][1]) + num_vals/2
                    output_value = np.zeros(max_len) + num_vals
                    output_value[:len(output_value_partial)] = output_value_partial
                except:
                    continue
                
                input_values.append(input_value)
                output_values.append(output_value)
                labels.append(label)

    f.close()
    
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

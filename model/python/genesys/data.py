import io
import numpy as np

from consts import *

# filename: str (path of the dataset to read)
# return: (np.array([num_points, input_length], int),
#          np.array([num_points, output_length], int),
#          np.array([num_points, n_gram_length], int),
#          np.array([num_points, num_dsl_ops], int))
#         (a (DSL op n-grams, input values, output values, label) tuple)
def read_dataset(filename):
    f = open(DATA_PATH + '/' + filename)

    dsl_ops = []
    input_values = []
    output_values = []
    labels = []
    for line in f:
        # Step 1: Obtain (DSL ops, input values, output values, labels) tuples
        toks = line[2:-3].split('], [')

        # Step 2: Process values
        dsl_ops.append(_process_list(toks[0]))
        input_values.append(_process_list(toks[1]))
        output_values.append(_process_list(toks[2]))
        labels.append(_process_list(toks[3]))

    f.close()

    return (np.array(input_values), np.array(output_values), np.array(dsl_ops), np.array(labels))

# s: str
# return: [int]
def _process_list(s):
    return [int(v) for v in s.split(', ')]

# dataset: (np.array([num_points, input_length], int),
#           np.array([num_points, output_length], int),
#           np.array([num_points, n_gram_length], int),
#           np.array([num_points, num_dsl_ops], int))
#          (a (DSL op n-grams, input values, output values, label) tuple)
# train_frac: float (proportion of points to use for training)
# return: (train_dataset, test_dataset) where train_dataset, test_dataset each have the same type as dataset
def split_train_test(dataset, train_frac):
    split_dataset = tuple(_split_train_test_single(dataset_single, train_frac) for dataset_single in dataset)
    return (tuple(split_dataset[i][0] for i in range(4)), tuple(split_dataset[i][1] for i in range(4)))

# dataset_single: np.array([num_points, num_vals], int)
# train_frac: float (proportion of points to use for training)
def _split_train_test_single(dataset_single, train_frac):
    n_train = int(train_frac*len(dataset_single))
    return (dataset_single[:n_train], dataset_single[n_train:])

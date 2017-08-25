import io
import numpy as np

# filename: str (path of the dataset to read)
# return: (np.array([num_points, n_gram_length], int),
#          np.array([num_points, input_length], int),
#          np.array([num_points, output_length], int),
#          np.array([num_points, num_dsl_ops], int))
#         (a (DSL op n-grams, input values, output values, label) tuple)
def read_dataset(filename):
    f = open(filename)

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

    return (np.array(dsl_ops), np.array(input_values), np.array(output_values), np.array(labels))

# s: str
# return: [int]
def _process_list(s):
    return [int(v) for v in s.split(', ')]

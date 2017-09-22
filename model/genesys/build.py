import random

from data import *
from params import *

def main():
    if input_length != output_length:
        raise Exception('Input and output lengths must be equal!')

    # Step 1: Read dataset
    dataset = read_deep_coder_train_dataset(deep_coder_dataset_filename, deep_coder_funcs_filename, num_vals, input_length)

    # Step 2: Shuffle dataset
    random.shuffle(dataset)

    # Step 3: Write dataset
    write_deep_coder_train_dataset(deep_coder_processed_dataset_filename, dataset)

if __name__ == '__main__':
    main()

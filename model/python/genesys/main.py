from data import *
from nn import *

def main():

    # Step 1: Parameters

    # Step 1a: Input/output value parameters
    num_vals = 21              # values are ints in [-10, 10]
    val_embedding_dim = 20     # dimension of the vector embedding of values
    input_length = 5           # lengths of input lists
    output_length = 5          # lengths of output lists

    # Step 1b: DSL Operator parameters
    num_dsl_ops = 40           # number of DSL operators
    dsl_op_embedding_dim = 20 # dimension of the vector embedding of DSL operators
    n_gram_length = 2          # length of DSL operator n-grams

    # Step 1c: Neural net parameters
    hidden_layer_dim = 256     # number of nodes in each hidden layer)

    # Step 1d: Path parameters
    l2_dataset_filename = '../data/l2.txt'

    # Step 1d: Build parameters
    params = DeepCoderModelParams(num_vals, val_embedding_dim, input_length, output_length, num_dsl_ops, dsl_op_embedding_dim, n_gram_length, hidden_layer_dim)

    # Step 2: Build neural net
    model = DeepCoderModel(params)

    # Step 3: Read dataset
    dataset = read_dataset(l2_dataset_filename)
    print dataset
    
if __name__ == '__main__':
    main()

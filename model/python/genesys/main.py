from data import *
from nn import *

def main():

    # Step 1: Parameters

    # Step 1a: Input/output value parameters
    num_vals = 22             # values are ints in [-10, 10]
    val_embedding_dim = 20    # dimension of the vector embedding of values
    input_length = 5          # lengths of input lists
    output_length = 5         # lengths of output lists

    # Step 1b: DSL Operator parameters
    num_dsl_ops = 40          # number of DSL operators
    dsl_op_embedding_dim = 20 # dimension of the vector embedding of DSL operators
    n_gram_length = 2         # length of DSL operator n-grams

    # Step 1c: Neural net parameters
    hidden_layer_dim = 256    # number of nodes in each hidden layer)

    # Step 1d: Build model parameters
    model_params = DeepCoderModelParams(num_vals, val_embedding_dim, input_length, output_length, num_dsl_ops, dsl_op_embedding_dim, n_gram_length, hidden_layer_dim)

    # Step 1e: Train parameters
    num_epochs = 12
    batch_size = 50
    step_size = 1e-2
    save_path = 'deep_coder_model.ckpt'
    load_prev = True
    train_params = DeepCoderTrainParams(num_epochs, batch_size, step_size, save_path, load_prev)

    # Step 1f: Dataset parameters
    l2_dataset_filename = 'l2.txt'
    train_frac = 0.7

    # Step 2: Build neural net
    model = DeepCoderModel(model_params)

    # Step 3: Read dataset
    dataset = read_dataset(l2_dataset_filename)
    (train_dataset, test_dataset) = split_train_test(dataset, train_frac)

    # Step 4: Train model
    model.train(train_dataset[0], train_dataset[1], train_dataset[2], train_dataset[3], test_dataset[0], test_dataset[1], test_dataset[2], test_dataset[3], train_params)
    
if __name__ == '__main__':
    main()

from data import *
from nn import *
from params import *

def main():
    # Step 1: Build parameters
    model_params = DeepCoderModelParams(num_vals, val_embedding_dim, input_length, output_length, hidden_layer_dim, num_dsl_ops)
    train_params = DeepCoderTrainParams(num_epochs, batch_size, step_size, save_path, load_prev)

    # Step 2: Build neural net
    model = DeepCoderModel(model_params)

    # Step 3: Read dataset
    if input_length != output_length:
        raise Exception('Input and output lengths must be equal!')
    dataset = read_train_dataset(deep_coder_dataset_filename, deep_coder_funcs_filename, num_vals, input_length)
    (train_dataset, test_dataset) = split_train_test(dataset, train_frac)

    # Step 4: Train model
    model.train(train_dataset[0], train_dataset[1], train_dataset[2], test_dataset[0], test_dataset[1], test_dataset[2], train_params)
    
if __name__ == '__main__':
    main()

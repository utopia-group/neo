from data import *
from nn import *
from params import *

def main():
    # Step 1: Build parameters
    model_params = DeepCoderModelParams(num_vals, val_embedding_dim, input_length, output_length, num_dsl_ops, dsl_op_embedding_dim, n_gram_length, hidden_layer_dim)
    test_params = DeepCoderTestParams(save_path)
    
    # Step 2: Build neural net
    model = DeepCoderModel(model_params)

    # Step 3: Read dataset
    dataset = read_test_dataset(l2_dataset_filename)

    # Step 4: Train model
    results = model.test(dataset[0], dataset[1], dataset[2], test_params)
    for result in results:
        print 'RESULT:', list(result)

if __name__ == '__main__':
    main()

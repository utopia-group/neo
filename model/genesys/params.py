# Input/output value parameters
num_vals = 21             # values are ints in [-N/2, N/2)
val_embedding_dim = 20    # dimension of the vector embedding of values
input_length = 5          # lengths of input lists
output_length = 5         # lengths of output lists

# DSL Operator parameters
num_dsl_ops = 34          # number of DSL operators

# Neural net parameters
hidden_layer_dim = 256    # number of nodes in each hidden layer)

# Train parameters
num_epochs = 100
batch_size = 50
step_size = 1e-6
save_path = 'deep_coder_model.ckpt'
load_prev = True

# Dataset parameters
deep_coder_dataset_filename = 'deep_coder.txt'
deep_coder_funcs_filename = 'deep_coder_funcs.txt'
train_frac = 0.9

# Test parameters
deep_coder_test_filename = 'deep_coder.txt'

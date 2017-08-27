# Input/output value parameters
num_vals = 22             # values are ints in [-10, 10]
val_embedding_dim = 20    # dimension of the vector embedding of values
input_length = 5          # lengths of input lists
output_length = 5         # lengths of output lists

# DSL Operator parameters
num_dsl_ops = 44          # number of DSL operators
dsl_op_embedding_dim = 20 # dimension of the vector embedding of DSL operators
n_gram_length = 2         # length of DSL operator n-grams

# Neural net parameters
hidden_layer_dim = 256    # number of nodes in each hidden layer)

# Train parameters
num_epochs = 100
batch_size = 50
step_size = 1e-4
save_path = 'deep_coder_model.ckpt'
load_prev = True

# Dataset parameters
l2_dataset_filename = 'deep_coder.txt'
train_frac = 0.7

# Test parameters
l2_test_filename = 'deep_coder.txt'

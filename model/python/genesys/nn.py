import numpy as np
import tensorflow as tf

# num_vals:              int (number of possible input values)
# val_embedding_dim:     int (dimension of the vector embedding of values)
# input_length:          int (length of input lists)
# output_length:         int (length of output lists)
# num_dsl_ops:           int (number of DSL operators)
# dsl_op_embedding_dim: int (dimension of the vector embedding of DSL operators)
# n_gram_length:         int (length of DSL operator n-grams)
# hidden_layer_dim:      int (number of nodes in each hidden layer)
class DeepCoderModelParams:
    def __init__(self, num_vals, val_embedding_dim, input_length, output_length, num_dsl_ops, dsl_op_embedding_dim, n_gram_length, hidden_layer_dim):
        self.num_vals = num_vals
        self.val_embedding_dim = val_embedding_dim
        self.input_length = input_length
        self.output_length = output_length
        self.num_dsl_ops = num_dsl_ops
        self.dsl_op_embedding_dim = dsl_op_embedding_dim
        self.n_gram_length = n_gram_length
        self.hidden_layer_dim = hidden_layer_dim

# input_values:  tensorflow tensor of dimension [?, input_length] and values in {0, 1, ..., num_vals}
# output_values: tensorflow tensor of dimension [?, output_length] and values in {0, 1, ..., num_vals}
# dsl_op_scores: tensorflow tensor of dimension [?, n_gram_length] and values in {0, 1, ..., num_dsl_ops}
# labels:        tensorflow tensor of dimension [?, num_dsl_ops] and values in {0.0, 1.0}
# loss:          tensorflow loss function
class DeepCoderModel:
    # params: DeepCoderModelParams (parameters for the deep coder model)
    def __init__(self, params):
        # Step 1: Inputs (input list, output list, DSL operator n-gram)
        self.input_values = tf.placeholder(tf.int32, [None, params.input_length])
        self.output_values = tf.placeholder(tf.int32, [None, params.output_length])
        self.dsl_ops = tf.placeholder(tf.int32, [None, params.n_gram_length])

        # Step 2: Embedding layers
        
        # input value embedding
        input_value_embeddings = tf.get_variable('input_value_embeddings', [params.num_vals, params.val_embedding_dim])
        embedded_input_values = tf.nn.embedding_lookup(input_value_embeddings, self.input_values)

        # output value embedding
        output_value_embeddings = tf.get_variable('output_value_embeddings', [params.num_vals, params.val_embedding_dim])
        embedded_output_values = tf.nn.embedding_lookup(output_value_embeddings, self.output_values)

        # dsl op n-gram embedding
        dsl_op_embeddings = tf.get_variable('dsl_op_embeddings', [params.num_dsl_ops, params.dsl_op_embedding_dim])
        embedded_dsl_ops = tf.nn.embedding_lookup(dsl_op_embeddings, self.dsl_ops)

        # Step 3: Concatenation layer
        merged = tf.concat([embedded_input_values, embedded_output_values, embedded_dsl_ops], 1)

        # Step 4: Hidden layer
        hidden0 = tf.layers.dense(inputs=merged, units=params.hidden_layer_dim, activation=tf.nn.relu)
        hidden1 = tf.layers.dense(inputs=hidden0, units=params.hidden_layer_dim, activation=tf.nn.relu)
        hidden2 = tf.layers.dense(inputs=hidden1, units=params.hidden_layer_dim, activation=tf.nn.relu)

        # Step 5: Logits
        dsl_op_logits = tf.layers.dense(inputs=hidden2, units=params.num_dsl_ops, activation=None)

        # Step 6: Output (probability of each DSL operator)
        self.dsl_op_scores = tf.nn.softmax(dsl_op_logits)

        # Step 7: Loss layer
        self.labels = tf.placeholder(tf.float32, [None, params.num_dsl_ops])
        self.loss = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(labels=self.labels, logits=dsl_op_logits))

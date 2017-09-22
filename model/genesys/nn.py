import os
import numpy as np
import tensorflow as tf

from consts import *

# num_vals:             int (number of possible input values)
# val_embedding_dim:    int (dimension of the vector embedding of values)
# input_length:         int (length of input lists)
# output_length:        int (length of output lists)
# hidden_layer_dim:     int (number of nodes in each hidden layer)
# num_dsl_ops:          int (number of dsl ops)
class DeepCoderModelParams:
    def __init__(self, num_vals, val_embedding_dim, input_length, output_length, hidden_layer_dim, num_dsl_ops):
        self.num_vals = num_vals
        self.val_embedding_dim = val_embedding_dim
        self.input_length = input_length
        self.output_length = output_length
        self.hidden_layer_dim = hidden_layer_dim
        self.num_dsl_ops = num_dsl_ops

# num_epochs: int (number of training epochs)
# batch_size: int (number of datapoints per batch)
# step_size:  float (step length in gradient descent)
# save_path:  str (path to save the neural net)
# load_prev:  bool (whether to load an existing save file)
class DeepCoderTrainParams:
    def __init__(self, num_epochs, batch_size, step_size, save_path, load_prev):
        self.num_epochs = num_epochs
        self.batch_size = batch_size
        self.step_size = step_size
        self.save_path = save_path
        self.load_prev = load_prev

# save_path:  str (path where the neural net is saved)
class DeepCoderTestParams:
    def __init__(self, save_path):
        self.save_path = save_path
        
# input_values:  tensorflow tensor of dimension [?, input_length] and values in {0, 1, ..., num_vals}
# output_values: tensorflow tensor of dimension [?, output_length] and values in {0, 1, ..., num_vals}
# dsl_op_scores: tensorflow tensor of dimension [?, n_gram_length] and values in {0, 1, ..., num_dsl_ops}
# labels:        tensorflow tensor of dimension [?, num_dsl_ops] and values in {0.0, 1.0}
# loss:          tensorflow loss function
# accuracy:      tensorflow accuracy function
class DeepCoderModel:
    # params: DeepCoderModelParams (parameters for the deep coder model)
    def __init__(self, params):
        # Step 1: Inputs (input list, output list, DSL operator n-gram)
        self.input_values = tf.placeholder(tf.int32, [None, params.input_length])
        self.output_values = tf.placeholder(tf.int32, [None, params.output_length])

        # Step 2: Embedding layers
        
        # input value embedding
        input_value_embeddings = tf.get_variable('input_value_embeddings', [params.num_vals + 1, params.val_embedding_dim])
        embedded_input_values = tf.nn.embedding_lookup(input_value_embeddings, self.input_values)
        embedded_input_values_flat = tf.reshape(embedded_input_values, [-1, params.input_length * params.val_embedding_dim])

        # output value embedding
        output_value_embeddings = tf.get_variable('output_value_embeddings', [params.num_vals + 1, params.val_embedding_dim])
        embedded_output_values = tf.nn.embedding_lookup(output_value_embeddings, self.output_values)
        embedded_output_values_flat = tf.reshape(embedded_output_values, [-1, params.output_length * params.val_embedding_dim])

        # Step 3: Concatenation layer
        merged = tf.concat([embedded_input_values_flat, embedded_output_values_flat], 1)

        # Step 4: Hidden layer
        hidden0 = tf.layers.dense(inputs=merged, units=params.hidden_layer_dim, activation=tf.nn.relu)
        hidden1 = tf.layers.dense(inputs=hidden0, units=params.hidden_layer_dim, activation=tf.nn.relu)
        hidden2 = tf.layers.dense(inputs=hidden1, units=params.hidden_layer_dim, activation=tf.nn.relu)

        # Step 5: Logits
        dsl_op_logits = tf.layers.dense(inputs=hidden2, units=params.num_dsl_ops, activation=None)

        # Step 6: Output (probability of each DSL operator)
        self.dsl_op_scores = tf.nn.sigmoid(dsl_op_logits)

        # Step 7: Loss layer
        self.labels = tf.placeholder(tf.float32, [None, params.num_dsl_ops])
        self.loss = tf.reduce_mean(tf.nn.sigmoid_cross_entropy_with_logits(labels=self.labels, logits=dsl_op_logits))

        # Step 8: Accuracy
        self.accuracy = tf.reduce_mean(tf.cast(tf.equal(tf.cast(tf.round(self.dsl_op_scores), tf.int32), tf.cast(self.labels, tf.int32)), tf.float32))

    # input_values_train:  np.array([num_train, input_length])
    # output_values_train: np.array([num_train, output_length])
    # labels_train:        np.array([num_train, num_dsl_ops])
    # input_values_test:   np.array([num_test, input_length])
    # output_values_test:  np.array([num_test, output_length])
    # labels_test:         np.array([num_test, num_dsl_ops])
    # params:              DeepCoderTrainParams
    def train(self, input_values_train, output_values_train, labels_train, input_values_test, output_values_test, labels_test, params):
        # Step 1: Save path
        save_path = self.save_path(params)
        
        # Step 2: Compute number of batches
        num_batches = len(input_values_train)/params.batch_size

        # Step 3: Training step
        train_step = tf.train.AdamOptimizer(params.step_size).minimize(self.loss)

        # Step 4: Training
        with tf.Session() as sess:
            # Step 4a: Global variables initialization
            sess.run(tf.global_variables_initializer())

            # Step 4b: Load existing model
            if params.load_prev and tf.train.checkpoint_exists(save_path):
                tf.train.Saver().restore(sess, save_path)
                print 'Loaded deep coder model in: %s' % save_path

            min_loss = None

            for i in range(params.num_epochs):
                print 'epoch: %d' % i
                for j in range(num_batches):

                    if j%1000 == 0:
                        print 'Batch:', j
                    
                    # Step 4c: Compute batch bounds
                    lo = j*params.batch_size
                    hi = (j+1)*params.batch_size
                    
                    # Step 4d: Compute batch
                    feed_dict = {
                        self.input_values: input_values_train[lo:hi],
                        self.output_values: output_values_train[lo:hi],
                        self.labels: labels_train[lo:hi],
                    }

                    # Step 4e: Run training step
                    sess.run(train_step, feed_dict=feed_dict)

                    if j%1000 == 0:

                        # Step 4f: Test set accuracy
                        (loss, accuracy) = self.test(input_values_test, output_values_test, labels_test, params)
                        
                        # Step 4g: save model
                        if min_loss is None or loss <= min_loss:
                            tf.train.Saver().save(sess, save_path)
                            print 'Saved deep coder neural net in: %s' % save_path
                            min_loss = loss
                            
    # input_values:  np.array([num_test, input_length])
    # output_values: np.array([num_test, output_length])
    # labels_test:   np.array([num_test, num_dsl_ops])
    # params:        DeepCoderTrainParams | DeepCoderTestParams
    def test(self, input_values, output_values, labels, params):
        with tf.Session() as sess:
            # Step 1: Directory path
            save_path = self.save_path(params)

            # Step 2: Load neural net
            tf.train.Saver().restore(sess, save_path)
            print 'Loaded deep coder model in: %s' % save_path

            # Test neural net
            feed_dict = {
                self.input_values: input_values,
                self.output_values: output_values,
                self.labels: labels,
            }
            loss = sess.run(self.loss, feed_dict=feed_dict)
            print 'Loss: %g' % loss
            accuracy = sess.run(self.accuracy, feed_dict=feed_dict)
            print 'Accuracy: %g' % accuracy

        return (loss, accuracy)

    # input_values:  np.array([num_run, input_length])
    # output_values: np.array([num_run, output_length])
    # params:        DeepCoderTestParams
    def run(self, input_values, output_values, params):

        with tf.Session() as sess:
            # Step 1: Directory path
            save_path = self.save_path(params)

            # Step 2: Load neural net
            tf.train.Saver().restore(sess, save_path)
            print 'Loaded deep coder model in: %s' % save_path

            # Step 3: Build inputs
            feed_dict = {
                self.input_values: input_values,
                self.output_values: output_values,
            }

            # Step 4: Run prediction
            scores = sess.run(self.dsl_op_scores, feed_dict=feed_dict)

        return scores

    # params: DeepCoderTrainParams | DeepCoderTestParams
    def save_path(self, params):
        return DATA_PATH + '/' + params.save_path

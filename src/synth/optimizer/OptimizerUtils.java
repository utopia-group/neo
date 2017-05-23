package synth.optimizer;

import java.util.Random;

import synth.utils.Utils.Maybe;
import synth.utils.Utils.Pair;

public class OptimizerUtils {
	public static interface OptimizationInstance<T> {
		public Pair<T,Double> sample(Maybe<T> t);
	}
	
	public static interface Scorer<T> {
		public double score(T t);
	}
	
	public static interface Sampler<T> {
		public T sample(Maybe<T> t);
	}
	
	public static class ScoredOptimizationInstance<T> implements OptimizationInstance<T> {
		private final Sampler<T> sampler;
		private final Scorer<T> scorer;
		public ScoredOptimizationInstance(Sampler<T> sampler, Scorer<T> scorer) {
			this.sampler = sampler;
			this.scorer = scorer;
		}
		@Override
		public Pair<T,Double> sample(Maybe<T> t) {
			T candidate = this.sampler.sample(t);
			return new Pair<T,Double>(candidate, this.scorer.score(candidate));
		}
	}
	
	public static class LinearCombinationScorer<T> implements Scorer<T> {
		private final Pair<Scorer<T>,Double>[] scorers;
		@SafeVarargs
		public LinearCombinationScorer(Pair<Scorer<T>,Double> ... scorers) {
			this.scorers = scorers;
		}
		@Override
		public double score(T t) {
			double score = 0.0;
			for(Pair<Scorer<T>,Double> scorer : this.scorers) {
				score += scorer.t1 * scorer.t0.score(t);
			}
			return score;
		}
	}
	
	public static interface Optimizer {
		public <T> T optimize(OptimizationInstance<T> instance);
	}
	
	public static class MCMCOptimizer implements Optimizer {
		private final int numIterations;
		private final Random random;
		public MCMCOptimizer(int numIterations, Random random) {
			this.numIterations = numIterations;
			this.random = random;
		}
		@Override
		public <T> T optimize(OptimizationInstance<T> instance) {
			Pair<T,Double> cur = instance.sample(new Maybe<T>());
			for(int i=0; i<this.numIterations; i++) {
				Pair<T,Double> candidate = instance.sample(new Maybe<T>(cur.t0));
				if(candidate.t1/cur.t1 >= this.random.nextDouble()) {
					cur = candidate;
				}
			}
			return cur.t0;
		}
	}
}

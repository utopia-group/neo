package org.genesys.ml;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.genesys.type.AbstractList;
import org.genesys.type.AbstractType;
import org.genesys.type.Cons;
import org.genesys.type.EmptyList;
import org.genesys.type.IntType;
import org.genesys.type.ListType;

public class L2InputSampler implements Sampler<Object> {
	
	private final AbstractType type;
	private final L2InputSamplerParameters parameters;
	private final Random random;
	
	public L2InputSampler(AbstractType type, L2InputSamplerParameters parameters, Random random) {
		this.type = type;
		this.parameters = parameters;
		this.random = random;
	}

	@Override
	public Object sample() {
		return this.sample(this.type);
	}
	
	private Object sample(AbstractType type) {
		if(type instanceof ListType) {
			int length = random.nextInt(this.parameters.maxLength);
			List<Object> list = new ArrayList<Object>();
			for(int i=0; i<length; i++) {
				list.add(this.sample(((ListType)type).type));
			}
			return convert(list);
		} else if(type instanceof IntType) {
			return (Integer)this.parameters.minInt + this.random.nextInt((this.parameters.maxInt - this.parameters.minInt));
		} else {
			throw new RuntimeException("Type not yet handled: " + type);
		}
	}
	
	// Reverses the list, but that is fine for our purposes
	private static <T> AbstractList convert(List<Object> list) {
		AbstractList abstractList = new EmptyList();
		for(Object obj : list) {
			abstractList = new Cons(obj, abstractList);
		}
		return abstractList;
	}
}

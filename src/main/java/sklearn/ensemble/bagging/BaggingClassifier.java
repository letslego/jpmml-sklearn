/*
 * Copyright (c) 2015 Villu Ruusmann
 *
 * This file is part of JPMML-SkLearn
 *
 * JPMML-SkLearn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-SkLearn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-SkLearn.  If not, see <http://www.gnu.org/licenses/>.
 */
package sklearn.ensemble.bagging;

import java.util.List;
import java.util.Set;

import org.dmg.pmml.DataType;
import org.dmg.pmml.DefineFunction;
import org.dmg.pmml.MiningFunctionType;
import org.dmg.pmml.MiningModel;
import org.dmg.pmml.MultipleModelMethodType;
import org.dmg.pmml.OpType;
import org.dmg.pmml.Output;
import org.jpmml.converter.Schema;
import org.jpmml.converter.ModelUtil;
import sklearn.Classifier;
import sklearn.EstimatorUtil;

public class BaggingClassifier extends Classifier {

	public BaggingClassifier(String module, String name){
		super(module, name);
	}

	@Override
	public boolean requiresContinuousInput(){
		Classifier baseEstimator = getBaseEstimator();

		return baseEstimator.requiresContinuousInput();
	}

	@Override
	public DataType getDataType(){
		Classifier baseEstimator = getBaseEstimator();

		return baseEstimator.getDataType();
	}

	@Override
	public OpType getOpType(){
		Classifier baseEstimator = getBaseEstimator();

		return baseEstimator.getOpType();
	}

	@Override
	public MiningModel encodeModel(Schema schema){
		List<? extends Classifier> estimators = getEstimators();
		List<List<Integer>> estimatorsFeatures = getEstimatorsFeatures();

		MultipleModelMethodType multipleModelMethod = MultipleModelMethodType.AVERAGE;

		for(Classifier estimator : estimators){

			if(!estimator.hasProbabilityDistribution()){
				multipleModelMethod = MultipleModelMethodType.MAJORITY_VOTE;

				break;
			}
		}

		Output output = ModelUtil.createProbabilityOutput(schema);

		MiningModel miningModel = BaggingUtil.encodeBagging(estimators, estimatorsFeatures, multipleModelMethod, MiningFunctionType.CLASSIFICATION, schema)
			.setOutput(output);

		return miningModel;
	}

	@Override
	public Set<DefineFunction> encodeDefineFunctions(){
		Classifier baseEstimator = getBaseEstimator();

		return baseEstimator.encodeDefineFunctions();
	}

	public Classifier getBaseEstimator(){
		Object baseEstimator = get("base_estimator_");

		return EstimatorUtil.asClassifier(baseEstimator);
	}

	public List<? extends Classifier> getEstimators(){
		List<?> estimators = (List)get("estimators_");

		return EstimatorUtil.asClassifierList(estimators);
	}

	public List<List<Integer>> getEstimatorsFeatures(){
		List<?> estimatorsFeatures = (List)get("estimators_features_");

		return BaggingUtil.transformEstimatorsFeatures(estimatorsFeatures);
	}
}
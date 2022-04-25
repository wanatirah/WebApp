package com.example.WebApp.Model;

import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.NumberedFileInputSplit;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.common.io.ClassPathResource;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.evaluation.classification.ROC;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;

public class Training {

    public static final int BatchSize = 57449;
    public static final int numLabelClasses = -1;

    public static void main(String[] args) throws IOException, InterruptedException {

        File baseTrainDir = new ClassPathResource("Data/Train").getFile();
        File featuresTrainDir = new File(baseTrainDir, "features");
        File labelsTrainDir= new File(baseTrainDir, "labels");

        File baseTestDir = new ClassPathResource("Data/Test").getFile();
        File featuresTestDir = new File(baseTestDir, "features");
        File labelsTestDir = new File(baseTestDir, "labels");

        //load training data
        SequenceRecordReader trainFeatures = new CSVSequenceRecordReader(0, ",");
        trainFeatures.initialize(new NumberedFileInputSplit(featuresTrainDir.getAbsolutePath() + "/temp_features_%d.csv", 0, 0));
        SequenceRecordReader trainLabels = new CSVSequenceRecordReader();
        trainLabels.initialize(new NumberedFileInputSplit(labelsTrainDir.getAbsolutePath() + "/temp_labels_%d.csv", 0, 0));
        DataSetIterator trainData = new SequenceRecordReaderDataSetIterator(trainFeatures, trainLabels, BatchSize, numLabelClasses, false);


        //load testing data
        SequenceRecordReader testFeatures = new CSVSequenceRecordReader(0, ",");
        testFeatures.initialize(new NumberedFileInputSplit(featuresTestDir.getAbsolutePath() + "/temp_features_%d.csv", 0, 0));
        SequenceRecordReader testLabels = new CSVSequenceRecordReader();
        testLabels.initialize(new NumberedFileInputSplit(labelsTestDir.getAbsolutePath() + "/temp_labels_%d.csv", 0, 0));
        DataSetIterator testData = new SequenceRecordReaderDataSetIterator(testFeatures, testLabels, BatchSize, numLabelClasses, false);

        int numInputs = 1;
        int numOutput = 1;
        int epochs = 1;
        int seedNumber = 123;
        double learningRate = 0.01;

        ComputationGraphConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(seedNumber)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(learningRate))
                .graphBuilder()
                .addInputs("trainFeatures")
                .setOutputs("predictTemperature")
                .addLayer("layer0", new LSTM.Builder()
                                .nIn(numInputs)
                                .nOut(100)
                                .activation(Activation.TANH)
                                .build(),
                        "trainFeatures")
                .addLayer("predictTemperature", new RnnOutputLayer.Builder()
                                .dataFormat(RNNFormat.NCW)
                                .nIn(100)
                                .nOut(numOutput)
                                .lossFunction(LossFunctions.LossFunction.XENT)
                                .activation(Activation.SIGMOID)
                                .build(),
                        "layer0")
                .backpropType(BackpropType.Standard)
                .build();


        StatsStorage storage = new InMemoryStatsStorage();
        UIServer server = UIServer.getInstance();
        server.attach(storage);

        ComputationGraph model = new ComputationGraph(config);
        model.init();
        model.setListeners(new StatsListener(storage, 1));

        int evalStep = 5;

        for(int i = 0; i < epochs; ++i)
        {
            model.fit(trainData);

            if(i % evalStep == 0)
            {

                ROC roc = new ROC(100);
                while (testData.hasNext())
                {
                    DataSet batch = testData.next();
                    INDArray[] output = model.output(batch.getFeatures());
                    roc.evalTimeSeries(batch.getLabels(), output[0]);

                }

                System.out.println("EPOCH " + i + " VALID AUC: " + roc.calculateAUC());
                testData.reset();

                /*
                Save the model
                */
                File locationToSave = new File(new ClassPathResource("Data/").getFile().getAbsolutePath().toString() + "_" + i + ".zip");
                ModelSerializer.writeModel(model, locationToSave, false);
                System.out.println("Model at epoch " + i + " save at " + locationToSave.toString());
            }
        }

        //ROC
        /*
        Evaluate the results
        */
        ROC roc = new ROC(100);
        while (testData.hasNext())
        {
            DataSet batch = testData.next();
            INDArray[] output = model.output(batch.getFeatures());
            roc.evalTimeSeries(batch.getLabels(), output[0], batch.getLabelsMaskArray());
        }
        System.out.println("***** ROC Test Evaluation *****");
        System.out.println(roc.calculateAUC());

        testData.reset();

        //Evaluation
        System.out.println("***** Test Evaluation *****");
        Evaluation eval = new Evaluation(numLabelClasses);

        while(testData.hasNext())
        {
            DataSet testDataSet = testData.next();
            INDArray[] predicted = model.output(testDataSet.getFeatures());
            INDArray labels = testDataSet.getLabels();
            eval.evalTimeSeries(labels, predicted[0], testDataSet.getLabelsMaskArray());
        }

        System.out.println(eval.confusionToString());
        System.out.println(eval.stats());
    }
}
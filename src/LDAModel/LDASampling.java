package LDAModel;

import PreProcess.Corpus;
import FollowProcess.FollowWork;
//import weka.clusterers.SimpleKMeans;

/*
 *  Created on March 15 14:02:14 2015
 * author: Xiangfu Song 
 * email : bintasong@gmail.com
 * 
 * 
 * */
public class LDASampling{
	public static int topicNum = 6;
	public static int iteration = 1000;
	
	public static void main(String[] argv){
		
		Corpus corpus =new Corpus(topicNum,50.0/topicNum,0.1);
		corpus.addDoc("C:/Users/binta/Desktop/lda_java/LDA/Data/Documents");
		System.out.println("add document to memory done");
		System.out.printf("the number of words: %d\n",corpus.V);
		corpus.init();
		System.out.println("init done");
		System.out.println("iteration...");
		corpus.sampling(iteration);
		System.out.println("iteration done");
		FollowWork fw = new FollowWork(corpus,"C:/Users/binta/Desktop/lda_java/LDA/Data/LDAResults/document-topic.txt","C:/Users/binta/Desktop/lda_java/LDA/Data/LDAResults/topic-word.txt","C:/Users/binta/Desktop/lda_java/LDA/Data/LDAResults/theta.txt");
		fw.sortMKandSave();
		fw.sortKWandSave();
		fw.writeTheta();
		System.out.println("lda done");
	}
}

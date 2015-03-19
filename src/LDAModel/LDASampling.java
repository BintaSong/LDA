package LDAModel;

import PreProcess.Corpus;
import FollowProcess.FollowWork;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;
/* 
 * Created on March 15 14:02:14 2015
 * author: Xiangfu Song 
 * email : bintasong@google.com
 * 
 * */
public class LDASampling{
	public static int topicNum = 14;
	public static int iteration = 100;
	
	public static void main(String[] argv){
		
		Corpus corpus =new Corpus(topicNum,50.0/topicNum,0.1);
		corpus.addDoc("C:/Users/binta/Desktop/Data/Documents");
		System.out.println("add document to memory done");
		System.out.printf("the number of words: %d\n",corpus.V);
		corpus.init();
		System.out.println("init done");
		System.out.println("iteration...");
		corpus.sampling(iteration);
		System.out.println("iteration done");
		FollowWork fw = new FollowWork(corpus,"C:/Users/binta/Desktop/Data/LDAResults/document-topic.txt","C:/Users/binta/Desktop/Data/LDAResults/topic-word.txt");
		fw.sortMKandSave();
		fw.sortKWandSave();	
		System.out.println("lda done");
	}
}

package PreProcess;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;
import java.util.Comparator;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
/* 
 * Created on March 12 14:02:14 2015
 * author: Xiangfu Song 
 * email : bintasong@google.com
 * 
 * */
public class Corpus{
	public static ArrayList<String> vacabulary;//文本集字典列表
	public ArrayList<Document> docs;//文档集合
	public static HashMap<String,Integer> wordtoindex;//词语到索引的hash表
	public static ArrayList<String> noiselist;//噪声单词列表
	
	public double alpha ; //通常情况是 50 / K
	public double beta ;//通常 0.1
	public int V;//字典词汇量
	public int M;//文档数量
	public int K;//主题数目
	public int [][] z;//对每个词语的主题标注
	public int [][] nmk;//每行对应某个文档上主题的个数的分布 M*K
	public int [][] nkw;//每行对应某个主题上词语的个数分布. K*V
	public int [] nmkSum;//文档-主题 每行的求和
	public int [] nkwSum;//主题-词语 每行的总和

	public Corpus(int topicnum,double alpha,double beta){
		vacabulary = new ArrayList<String>();
		docs = new ArrayList<Document>();
		wordtoindex = new HashMap<String,Integer>();
		noiselist = new ArrayList<String>();
		this.alpha = alpha;
		this.beta = beta;
		K = topicnum;
		V = 0;
		M = 0;
		addNoise("./Data/StopWords/stopword.txt");
	}
	
	public void addNoise(String noisefilepath){
		//从nlist向noiselist添加噪声词语
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(new File(noisefilepath))); 
			String line = null;
			while( (line = reader.readLine()) != null){
				//我们的噪声文件每行存储一个词语
				line = line.trim();
				noiselist.add(line);
			}
		}catch(FileNotFoundException e){
			System.out.println("噪声文件没有找到！");
			e.printStackTrace();
		}catch(IOException e){
			System.out.println("文件IO错误！");
			e.printStackTrace();
		}finally{
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					System.out.println("关闭噪声文件错误！");
					e.printStackTrace();
				}
			}
		}
	}
	
	public List<Term> splitWords(String words){
		//返回分词后的List<Term>列表
		List<Term> termlist= ToAnalysis.parse(words);
		removeNoise(termlist);
		return termlist;
	}
	
	public void removeNoise(List<Term> termlist){
		//将分词后的List<Term>移除掉noise
		for(int i = 0;i < termlist.size();i++ ){
			if(noiselist.contains(termlist.get(i).getName())){
				termlist.remove(i);
				i--;
			}
		}
	}	
	
	public void addDoc(String docspath){
		for(File docFile : new File(docspath).listFiles()){
			BufferedReader reader = null;
			String words = "";
			String line = "";
			try{
				reader = new BufferedReader(new FileReader(docFile)); 
				while( (line = reader.readLine()) != null ){
					//我们的文件每行存储一个词语
					words = words + line.trim();
				}
			}catch(IOException e){
				System.out.println("读取文档错误");
				e.printStackTrace();				
			}finally{
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						System.out.println("关闭文档错误!");
						e.printStackTrace();
					}
				}
			}
			List<Term> termslist = splitWords(words);
			Document doc = new Document(docFile.getAbsolutePath(),termslist);
			docs.add(doc);
			M += 1;
		}	
	}
	
	public void init(){
		nmk = new int [M][K];
		nkw = new int[K][V];
		nmkSum = new int[M];
		nkwSum = new int[K];
		
		z = new int[M][];
		//随机主题赋值
		for(int i = 0;i < M;i++ ){
			int N = docs.get(i).docwords.length;
			z[i] = new int[N];
			for (int j = 0;j < N;j++){
				int randtopic = (int)(Math.random()*K);
				z[i][j] = randtopic;
				nmk[i][randtopic] += 1;
				nkw[randtopic][docs.get(i).docwords[j]] += 1;
				nkwSum[randtopic] += 1;
			}
			nmkSum[i] += N;
		}		
	}

	public int choose(double p[],int topicnum){
		double a = Math.random();
		int k = 0;
		for( k = 0;k < p.length;k++ ){
			a -= p[k];
			if(a <= 0){
				break;
			}
		}
		return k;
	}
	
	public int newTopic(int m,int n){
		int oldtopic = z[m][n];
		nmk[m][oldtopic] -= 1;
		nkw[oldtopic][docs.get(m).docwords[n]] -= 1;
		nmkSum[m] -= 1;
		nkwSum[oldtopic] -= 1;
		
		double p[] = new double[K];
		double sum = 0.0;
		for(int k = 0;k < K;k++){
			p[k] = (nkw[k][docs.get(m).docwords[n]] + beta) / (nkwSum[k] + V * beta) * (nmk[m][k] + alpha) / (nmkSum[m] + K * alpha);
			sum += p[k];
		}
		//System.out.printf("before sum of p[k] is:%f\n",sum);
		for(int k =0;k < K;k++){
			p[k] = p[k]/sum;
		}
		sum = 0;
		for(int k = 0;k < K;k++){
			sum += p[k];
		}
		//System.out.printf("after sum of p[k] is:%f\n",sum);
		int newtopic = choose(p,K);
		nmk[m][newtopic] += 1;
		nkw[newtopic][docs.get(m).docwords[n]] += 1;
		nmkSum[m] += 1;
		nkwSum[newtopic] += 1;
 		
		return newtopic;
	}
	
	public void sampling(int iteration){
		for(int i = 0;i < iteration;i++){
			//System.out.println("iteration #" );
			//System.out.println(this.vacabulary);
			for(int m = 0;m < M;m++ ){
				int N = docs.get(m).docwords.length; 
				for(int n = 0;n < N;n++){
					z[m][n] = newTopic(m,n);
				}
			}
		}
	}
	
	public String getWordByIndex(int index){
		//通过索引获取词语
		return vacabulary.get(index);
	}
	
	public class Document{
		public String docname;//文档名字
		public int[] docwords;//文档词语列表，存储词语在字典的索引值
		
		public Document(String dname,List<Term> words){
			//words:已经将噪声词语删除的字符列表
			this.docname = dname;
			docwords = new int[words.size()];
			for(int i = 0;i < words.size();i++ ){
				String word = words.get(i).getName();
				if(wordtoindex.containsKey(word)){
					//hash表中已经包含了该词语
					docwords[i] = wordtoindex.get(word);
				}
				else{//否则，将新词汇加入词汇字典和hash表
					int newindex = wordtoindex.size();
					vacabulary.add(word);
					wordtoindex.put(word, newindex);
					docwords[i] = newindex;
					V += 1;
				}
			}			
		}
	}

}
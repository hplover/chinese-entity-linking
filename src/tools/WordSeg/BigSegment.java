package tools.WordSeg;

import java.util.List;


import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLSentence;
import com.hankcs.hanlp.dependency.nnparser.NeuralNetworkDependencyParser;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.CRF.CRFSegment;
import com.hankcs.hanlp.seg.NShort.NShortSegment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;

public class BigSegment {
	// private static Logger logger =
	// Logger.getLogger(BigSegment.class.getName());
	private Segment nShortSegment = new NShortSegment();
	private static Boolean isInited = false;

	public BigSegment(String path) {
		super();
		HanLP.Config.ShowTermNature = true;
		nShortSegment.enableCustomDictionary(false).enablePlaceRecognize(true)
				.enableOrganizationRecognize(true);
		synchronized (isInited) {
			if (!isInited) {
				isInited = true;
				String prefix = path ;
				HanLP.Config.BiGramDictionaryPath = prefix
						+ HanLP.Config.BiGramDictionaryPath;
				HanLP.Config.CharTablePath = prefix
						+ HanLP.Config.CharTablePath;
				HanLP.Config.CharTypePath = prefix + HanLP.Config.CharTypePath;
				HanLP.Config.CoreDictionaryPath = prefix
						+ HanLP.Config.CoreDictionaryPath;
				HanLP.Config.CoreDictionaryTransformMatrixDictionaryPath = prefix
						+ HanLP.Config.CoreDictionaryTransformMatrixDictionaryPath;
				HanLP.Config.CoreStopWordDictionaryPath = prefix
						+ HanLP.Config.CoreStopWordDictionaryPath;
				HanLP.Config.CoreSynonymDictionaryDictionaryPath = prefix
						+ HanLP.Config.CoreSynonymDictionaryDictionaryPath;
				HanLP.Config.CRFDependencyModelPath = prefix
						+ HanLP.Config.CRFDependencyModelPath;
				HanLP.Config.CRFSegmentModelPath = prefix
						+ HanLP.Config.CRFSegmentModelPath;
				HanLP.Config.CustomDictionaryPath = new String[] {
						prefix + "data/dictionary/custom/CustomDictionary.txt",
						prefix + "data/dictionary/custom/现代汉语补充词库.txt",
						prefix + "data/dictionary/custom/全国地名大全.txt",
						prefix + "data/dictionary/custom/人名词典.txt",
						prefix + "data/dictionary/custom/机构名词典.txt",
						prefix + "data/dictionary/custom/上海地名.txt" };
				HanLP.Config.HMMSegmentModelPath = prefix
						+ HanLP.Config.HMMSegmentModelPath;
				HanLP.Config.JapanesePersonDictionaryPath = prefix
						+ HanLP.Config.JapanesePersonDictionaryPath;
				HanLP.Config.MaxEntModelPath = prefix
						+ HanLP.Config.MaxEntModelPath;
				HanLP.Config.NNParserModelPath = prefix
						+ HanLP.Config.NNParserModelPath;
				HanLP.Config.OrganizationDictionaryPath = prefix
						+ HanLP.Config.OrganizationDictionaryPath;
				HanLP.Config.OrganizationDictionaryTrPath = prefix
						+ HanLP.Config.OrganizationDictionaryTrPath;
				HanLP.Config.PersonDictionaryPath = prefix
						+ HanLP.Config.PersonDictionaryPath;
				HanLP.Config.PersonDictionaryTrPath = prefix
						+ HanLP.Config.PersonDictionaryTrPath;
				HanLP.Config.PinyinDictionaryPath = prefix
						+ HanLP.Config.PinyinDictionaryPath;
				HanLP.Config.PlaceDictionaryPath = prefix
						+ HanLP.Config.PlaceDictionaryPath;
				HanLP.Config.PlaceDictionaryTrPath = prefix
						+ HanLP.Config.PlaceDictionaryTrPath;
				HanLP.Config.SYTDictionaryPath = prefix
						+ HanLP.Config.SYTDictionaryPath;
				HanLP.Config.TraditionalChineseDictionaryPath = prefix
						+ HanLP.Config.TraditionalChineseDictionaryPath;
				HanLP.Config.TranslatedPersonDictionaryPath = prefix
						+ HanLP.Config.TranslatedPersonDictionaryPath;
				HanLP.Config.WordNatureModelPath = prefix
						+ HanLP.Config.WordNatureModelPath;
			}
		}
	}

	/**
	 * HanLP 分词器
	 */
	public List<Term> getHanLPSegment(int num, String line) {
		// line = line.replaceAll("\\pP|\\pS", " ").replaceAll("\\s{1,}",
		// " ").trim();//去掉标点以及多余的空格

		if (num == 0) {
			return HanLP.segment(line);
		} else if (num == 1) {
			return NLPTokenizer.segment(line);
		} else if (num == 2) {
			return nShortSegment.seg(line);
		} else if (num == 3) {
			Segment segment = new CRFSegment();
			segment.enablePartOfSpeechTagging(true);
			return segment.seg(line);
		} else {
			// logger.error("参数选择错误！");
			return null;
		}
	}

	/**
	 * @param line
	 *            : 需要解析的句子
	 * @return 返回一个CoNLLSentence对象，是CoNLLWord的集合
	 */
	public CoNLLSentence getDependencyParser(String line) {
		if (line == null)
			return null;
		CoNLLSentence ret =  NeuralNetworkDependencyParser.compute(line);
		String [][] matrix = ret.getEdgeArray();
		for (int i = 0;i<matrix.length;i++){
			for (int j=0;j<matrix[i].length;j++){
				System.out.print(matrix[i][j]);
			}
			System.out.println();
		}
		System.out.println();
		return ret;
	}
}

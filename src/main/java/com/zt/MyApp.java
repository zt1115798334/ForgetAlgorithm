package com.zt;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MyApp {

    static private final Logger logger = LoggerFactory.getLogger(MyApp.class);

    public static void main(String[] args) {
        File filePath = new File("E:\\BaiduNetdiskDownload\\WeiBo");
        List<String> fileList = getFileList(filePath);
        StringBuilder sb = new StringBuilder();
        fileList.stream().forEach(str -> {
            System.out.println(str);
            File file = new File(str);
            String textStr = txt2String(file);
            textStr = textStr.replaceAll("\\s*", "");
            textStr = textStr.replaceAll("[^\\u4E00-\\u9FA5]", "");
            sb.append(textStr);
        });

        String sbToStr = sb.toString();
//        System.out.println("textStr = " + textStr);
        int textLength = sbToStr.length();
        BigDecimal textLengthBigDecimal = new BigDecimal(textLength);
        System.out.println(textLength);

        Map<String, BigDecimal> wordStatisticsMap = saveWordStatistics(sbToStr, textLength);
        Map<String, BigDecimal> phraseStatisticsMap = savePhraseStatistics(sbToStr, textLength);

//        readMap(wordStatisticsMap);
        readMap(phraseStatisticsMap);
        Map<String, BigDecimal> wordProbabilityMap = saveCharacterProbability(wordStatisticsMap, textLengthBigDecimal);
        Map<String, BigDecimal> phraseProbabilityMap = saveCharacterProbability(phraseStatisticsMap, textLengthBigDecimal);

//        readMap(wordProbabilityMap);
        readMap(phraseProbabilityMap);

        Map<String, TextInfo> textSplitActual = cuttingPhrase(sbToStr, textLength, wordProbabilityMap, phraseProbabilityMap);
        List<TextInfo> textInfos = mapToList(textSplitActual);
        readList(textInfos);
        System.out.println("textLength = " + textLength);
    }

    private static List<TextInfo> saveTextList(BigDecimal countAll, Map<String, BigDecimal> textPhraseMap) {
        return textPhraseMap.entrySet().stream().map(entry -> {
            String textSplit = entry.getKey();
            BigDecimal textCount = entry.getValue();
            BigDecimal textProportion = textCount.divide(countAll, 2, BigDecimal.ROUND_HALF_UP);
            return new TextInfo(textSplit, textCount, textProportion);
        }).collect(Collectors.toList());
    }

    public static List<String> getFileList(File file) {
        List<String> result = new ArrayList<String>();
        if (!file.isDirectory()) {
            System.out.println(file.getAbsolutePath());
            result.add(file.getAbsolutePath());
        } else {
            File[] directoryList = file.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    if (file.isFile() && file.getName().indexOf("txt") > -1) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            for (int i = 0; i < directoryList.length; i++) {
                result.add(directoryList[i].getPath());
            }
        }
        return result;
    }

    /**
     * 读取txt文件的内容
     *
     * @param file 想要读取的文件对象
     * @return 返回文件内容
     */
    public static String txt2String(File file) {
        StringBuilder result = new StringBuilder();
        if (file.isFile() && file.exists()) {
            try {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");
                BufferedReader br = new BufferedReader(read);//构造一个BufferedReader类来读取文件
                String s = null;
                while ((s = br.readLine()) != null) {//使用readLine方法，一次读一行
                    result.append(System.lineSeparator() + s);
                }
                br.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return result.toString();
    }

    public static void appendMethodA(String fileName, String content) {
        try {
            // 打开一个随机访问文件流，按读写方式
            RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
            // 文件长度，字节数
            long fileLength = randomFile.length();
            //将写文件指针移到文件尾。
            randomFile.seek(fileLength);
            randomFile.writeBytes(content);
            randomFile.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void method1(String file, String conent) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            out.write(conent);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveMap(Map<String, BigDecimal> textWordMap, String textSplit) {
        if (textWordMap.containsKey(textSplit)) {
            BigDecimal textCount = textWordMap.get(textSplit);
            textWordMap.put(textSplit, textCount.add(BigDecimal.ONE));
        } else {
            textWordMap.put(textSplit, BigDecimal.ONE);
        }
    }

    /**
     * 统计字个数
     *
     * @param textStr    字符
     * @param textLength 字符长度
     * @return
     */
    private static Map<String, BigDecimal> saveWordStatistics(String textStr, int textLength) {
        Map<String, BigDecimal> textWordMap = Maps.newHashMap();
        for (int i = 0; i < textLength; i++) {
            String textSplit = textStr.substring(i, i + 1);
            saveMap(textWordMap, textSplit);
        }
        return textWordMap;
    }

    /**
     * 统计词个数
     *
     * @param textStr    字符
     * @param textLength 字符长度
     * @return
     */
    private static Map<String, BigDecimal> savePhraseStatistics(String textStr, int textLength) {
        Map<String, BigDecimal> textWordMap = Maps.newHashMap();
        for (int i = 0; i < textLength - 1; i++) {
            String textSplit = textStr.substring(i, i + 2);
            saveMap(textWordMap, textSplit);
        }
        return textWordMap;
    }


    /**
     * 计算字符出现的概率
     *
     * @param characterStatistics  字符统计个数map
     * @param textLengthBigDecimal 字符长度
     * @return
     */
    private static Map<String, BigDecimal> saveCharacterProbability(Map<String, BigDecimal> characterStatistics
            , BigDecimal textLengthBigDecimal) {
        Map<String, BigDecimal> result = Maps.newHashMap();
        characterStatistics.entrySet().forEach(entry -> {
            BigDecimal textCount = entry.getValue();
            result.put(entry.getKey(), textCount.divide(textLengthBigDecimal, 8, BigDecimal.ROUND_HALF_UP));
        });
        return result;
    }

    private static Map<String, TextInfo> cuttingPhrase(String textStr,
                                                       int textLength,
                                                       Map<String, BigDecimal> wordProbabilityMap,
                                                       Map<String, BigDecimal> phraseProbabilityMap) {
        int lastPosition = 0;
        double second = 0D;
        Map<String, TextInfo> result = Maps.newHashMap();
        for (int i = 0; i < textLength - 1; i++) {
            String textSplit = textStr.substring(i, i + 2);

            BigDecimal phraseProbability = phraseProbabilityMap.get(textSplit);
            String[] wordArr = textSplit.split("");

            String wordA = wordArr[0];
            String wordB = wordArr[1];

            BigDecimal wordProbabilityA = wordProbabilityMap.get(wordA);
            BigDecimal wordProbabilityB = wordProbabilityMap.get(wordB);
            if (phraseProbability.compareTo(wordProbabilityA.multiply(wordProbabilityB)) == -1) {
                int nowPosition = i + 1;
                String textSplitActual = textStr.substring(lastPosition, nowPosition);

                if (i % 7 == 0) { //过一秒
                    second++;
                }
                if (result.containsKey(textSplitActual)) {
                    TextInfo textInfo = result.get(textSplitActual);
                    double beforeSecond = textInfo.getSecond();
                    double interval = second - beforeSecond;
                    double coefficient = calcCoefficient();
                    double calcNumber = calcNetonCooling(coefficient, interval);
                    BigDecimal textCount = textInfo.getTextCount().multiply(new BigDecimal(calcNumber)).add(BigDecimal.ONE).setScale(4, BigDecimal.ROUND_HALF_UP);
                    textInfo.setSecond(second);
                    textInfo.setTextCount(textCount);
                    result.put(textSplitActual, textInfo);
                } else {
                    TextInfo textInfo = new TextInfo(textSplitActual, BigDecimal.ONE, second);
                    result.put(textSplitActual, textInfo);
                }
                if (i % (7 * 60 * 60 * 24 * 6) == 0) {
                    clearMap(result);
                }

                lastPosition = nowPosition;
            }
        }
        return result;
    }

    public static void clearMap(Map<String, TextInfo> textInfoMap) {
        textInfoMap.entrySet().forEach(entry -> {
            String textSplitActual = entry.getKey();
            TextInfo textInfo = entry.getValue();
            if (textInfo.getTextCount().compareTo(new BigDecimal(0.254)) == -1) {
                textInfoMap.remove(textSplitActual);
            }
        });
    }

    public static void readMap(Map<String, BigDecimal> textMap) {
        List<Map.Entry<String, BigDecimal>> textListMap = textMap
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue())
                .collect(Collectors.toList());

        textListMap.forEach(entry -> {
            String textSplit = entry.getKey();
            BigDecimal textCount = entry.getValue();
            logger.info("内容为：" + textSplit + ",个数为：" + textCount.toPlainString());
        });
    }

    public static List<TextInfo> mapToList(Map<String, TextInfo> textMap) {
        return textMap.entrySet().stream().map(entry -> entry.getValue()).collect(Collectors.toList());
    }

    public static void readList(List<TextInfo> list) {
        List<TextInfo> sortTextInfo = list.stream()
                .sorted(Comparator.comparing(TextInfo::getTextCount).reversed())
                .collect(Collectors.toList());
        String fileName = "d://text.txt";
        File file = new File(fileName);
        try {
            if (file.createNewFile()) {
                System.out.println("Create file successed");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        System.out.println("开始写入:");
        sortTextInfo.forEach(str -> {
            String content = "字组为：[" + str.getText() + "]" + "比例为:[" + str.getTextCount() + "]\n";
            method1(fileName, content);
        });
    }

    public static double calcCoefficient() {
        return Math.log(0.254) / (7 * 60 * 60 * 24 * 6);
    }

    /**
     * @param coefficient 冷却系数
     * @param interval    时间间隔
     * @return
     */
    public static double calcNetonCooling(double coefficient, double interval) {
        return Math.exp(-1 * coefficient * interval);
    }
}

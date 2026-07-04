package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.entity.ExamInfo;
import com.example.demo.entity.WordDict;
import com.example.demo.mapper.ExamInfoMapper;
import com.example.demo.mapper.WordDictMapper;
import com.example.demo.service.ExamService;
import com.example.demo.vo.ExamPaperVO;
import com.example.demo.vo.QuestionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private ExamInfoMapper examInfoMapper;

    @Autowired
    private WordDictMapper wordDictMapper;

    @Override
    public Long createExam(String title, String startTime, String endTime, int questionCount) {
        // 1. 查询所有单词库的 ID
        List<WordDict> allWords = wordDictMapper.selectList(null);
        if (allWords.size() < questionCount) {
            throw new RuntimeException("题库单词数量不足以生成考试！");
        }

        // 2. 将所有单词在内存中打乱随机抽取前 questionCount 个 ID
        Collections.shuffle(allWords);
        List<Long> selectedIds = allWords.subList(0, questionCount).stream()
                .map(WordDict::getId)
                .collect(Collectors.toList());

        // 3. 构建考试信息写入数据库
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ExamInfo examInfo = new ExamInfo();
        examInfo.setTitle(title);
        examInfo.setStartTime(LocalDateTime.parse(startTime, df));
        examInfo.setEndTime(LocalDateTime.parse(endTime, df));
        examInfo.setQuestionIds(selectedIds);
        examInfo.setCreateTime(LocalDateTime.now());

        examInfoMapper.insert(examInfo);
        return examInfo.getId();
    }

    @Override
    public ExamPaperVO getExamPaper(Long examId) {
        // 1. 获取考试基本信息
        ExamInfo examInfo = examInfoMapper.selectById(examId);
        if (examInfo == null) {
            throw new RuntimeException("考试不存在！");
        }

        // 校验考试时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(examInfo.getStartTime())) {
            throw new RuntimeException("考试尚未开始！");
        }
        if (now.isAfter(examInfo.getEndTime())) {
            throw new RuntimeException("考试已结束！");
        }

        // 2. 根据记录的 questionIds 查出正确的考题单词列表
        List<Long> questionIds = examInfo.getQuestionIds();
        List<WordDict> targetWords = wordDictMapper.selectBatchIds(questionIds);

        // 3. 为了高效生成干扰项，我们一次性查出系统中所有的单词释义作为“干扰项池”
        // （对于课程设计万级以内的词库，载入内存极其快且减轻数据库 IO 压力）
        List<WordDict> allWords = wordDictMapper.selectList(null);
        List<String> allMeanings = allWords.stream()
                .map(WordDict::getMeaningCn)
                .collect(Collectors.toList());

        List<QuestionVO> questionVOList = new ArrayList<>();
        Random random = new Random();

        // 4. 为每个单词构建选择题（1个正确答案 + 3个干扰答案）
        for (WordDict target : targetWords) {
            QuestionVO vo = new QuestionVO();
            vo.setWordId(target.getId());
            vo.setWordEn(target.getWordEn());

            // 构建选项列表并加入正确答案
            Set<String> optionsSet = new HashSet<>();
            optionsSet.add(target.getMeaningCn());

            // 从池子中随机抽3个不同的干扰项（利用 Set 去重）
            while (optionsSet.size() < 4 && allMeanings.size() >= 4) {
                int randomIndex = random.nextInt(allMeanings.size());
                String randomMeaning = allMeanings.get(randomIndex);
                optionsSet.add(randomMeaning);
            }

            // 将 Set 转为 List 并使用 Collections.shuffle 随机打乱 ABCD 选项顺序！
            List<String> shuffledOptions = new ArrayList<>(optionsSet);
            Collections.shuffle(shuffledOptions);

            vo.setOptions(shuffledOptions);
            questionVOList.add(vo);
        }

        // 5. 【核心需求：题目乱序】
        // 考卷包含的题目内容大家一致，但对每个考生随机打乱题序！
        Collections.shuffle(questionVOList);

        // 6. 组装最终试卷返回
        ExamPaperVO paperVO = new ExamPaperVO();
        paperVO.setExamId(examInfo.getId());
        paperVO.setTitle(examInfo.getTitle());
        paperVO.setStartTime(examInfo.getStartTime());
        paperVO.setEndTime(examInfo.getEndTime());
        paperVO.setQuestions(questionVOList);

        return paperVO;
    }
}
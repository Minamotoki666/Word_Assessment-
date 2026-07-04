package com.example.demo.vo;

import lombok.Data;
import java.util.List;

@Data
public class QuestionVO {
    private Long wordId;         // 单词ID
    private String wordEn;       // 英文单词(题目)
    private List<String> options;// 4个打乱顺序的中文释义选项
}
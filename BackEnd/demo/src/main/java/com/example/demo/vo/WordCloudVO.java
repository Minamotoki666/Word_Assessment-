package com.example.demo.vo;

import lombok.Data;

@Data
public class WordCloudVO {
    private String name;  // 单词拼写
    private Integer value; // 答错次数(作为权重，次数越多字体越大)
}
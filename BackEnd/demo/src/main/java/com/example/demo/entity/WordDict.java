package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("word_dict")
public class WordDict {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String wordEn;

    private String meaningCn;

    private String category;

    private LocalDateTime createTime;
}
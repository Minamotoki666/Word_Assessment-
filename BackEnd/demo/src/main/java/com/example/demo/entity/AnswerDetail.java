package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("answer_detail")
public class AnswerDetail {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long examRecordId;

    private String studentId;

    private Long wordId;

    private String wordEn;

    /**
     * 对应数据库中的 tinyint(1)，在 Java 中用 Boolean 接收更符合面向对象习惯
     * true 代表答对(1)，false 代表答错(0)
     */
    private Boolean isCorrect;

    private LocalDateTime createTime;
}
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
// 开启 autoResultMap 以便让 TypeHandler 在 XML 或自动生成的查询中生效
@TableName(value = "exam_info", autoResultMap = true)
public class ExamInfo {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /**
     * 本次考试包含的单词ID集合。
     * 在数据库中以 JSON 数组存储，如 [1, 2, 35, 42]。
     * 这里通过 JacksonTypeHandler 自动转换为 Java 的 List<Long>。
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> questionIds;

    private LocalDateTime createTime;
}
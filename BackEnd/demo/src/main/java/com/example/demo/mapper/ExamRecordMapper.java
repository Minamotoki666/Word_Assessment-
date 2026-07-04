package com.example.demo.mapper;
// 3. ExamRecordMapper.java
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.ExamRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {}
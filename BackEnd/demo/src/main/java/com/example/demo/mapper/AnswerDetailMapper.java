package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.AnswerDetail;
import com.example.demo.vo.WordCloudVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AnswerDetailMapper extends BaseMapper<AnswerDetail> {

    /**
     * 统计某个学生最近 7 天内错题频次最多的前 30 个单词
     * is_correct = 0 代表答错
     */
    @Select("SELECT word_en AS name, COUNT(*) AS value " +
            "FROM answer_detail " +
            "WHERE student_id = #{studentId} " +
            "  AND is_correct = 0 " +
            "  AND create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "GROUP BY word_en " +
            "ORDER BY value DESC " +
            "LIMIT 30")
    List<WordCloudVO> selectWeeklyErrorWordCloud(@Param("studentId") String studentId);
}
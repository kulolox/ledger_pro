package com.example.back_end.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.back_end.entity.Transaction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {

    @Select("SELECT DATE_FORMAT(trans_time, '%Y-%m') AS ym, type, SUM(amount) AS total "
            + "FROM transaction "
            + "WHERE book_id = #{bookId} AND deleted = 0 "
            + "AND trans_time >= #{start} AND trans_time < #{end} "
            + "GROUP BY DATE_FORMAT(trans_time, '%Y-%m'), type "
            + "ORDER BY ym")
    List<Map<String, Object>> sumByMonthAndType(@Param("bookId") Long bookId,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Select("SELECT type, category_id, SUM(amount) AS total, COUNT(*) AS count "
            + "FROM transaction "
            + "WHERE book_id = #{bookId} AND deleted = 0 "
            + "AND trans_time >= #{start} AND trans_time < #{end} "
            + "GROUP BY type, category_id "
            + "ORDER BY type ASC, SUM(amount) DESC")
    List<Map<String, Object>> sumByCategoryAndType(@Param("bookId") Long bookId,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}


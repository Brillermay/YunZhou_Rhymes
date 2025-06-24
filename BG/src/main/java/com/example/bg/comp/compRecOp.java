package com.example.bg.comp;

import com.example.bg.ConnetMySQL;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;

@CrossOrigin(
        origins = {
                "http://localhost:8080",
                "http://localhost:8081",
                "http://127.0.0.1:8081",
                "http://117.72.88.23:8081",
        },
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        maxAge = 3600,
        allowCredentials = "true"
)
@RestController
@RequestMapping(value = "/compRec")
public class compRecOp extends ConnetMySQL {

    @PostMapping("/submit")
    public String submitRecord(@RequestBody compRec rec) {
        SqlSession session = null;
        InputStream in = null;
        try {
            System.out.println("收到前端提交: " + rec);

            in = Resources.getResourceAsStream("SqlMapConfig.xml");
            System.out.println("成功加载SqlMapConfig.xml");

            session = getSession(in);
            System.out.println("成功获取数据库Session");

            compRecOpMapper mapper = session.getMapper(compRecOpMapper.class);

            System.out.println("开始查询历史记录...");
            compRec oldRec = mapper.getHistory(rec.UID, rec.Difficulty, rec.Sum);
            System.out.println("查询结果: " + oldRec);

            if (oldRec == null) {
                System.out.println("无历史记录，准备插入新纪录");
                mapper.addHistory(rec);
                session.commit();
                System.out.println("插入成功");
                return "Record added successfully";
            } else {
                // 修改逻辑：Max更大 或 Max相等且Mintime更小才更新
                if (rec.Max > oldRec.Max || (rec.Max == oldRec.Max && rec.Mintime < oldRec.Mintime)) {
                    System.out.println("新纪录更优，准备更新");
                    mapper.updateHistory(rec);
                    session.commit();
                    System.out.println("更新成功");
                    return "Record updated successfully";
                } else {
                    System.out.println("新纪录不优于历史记录，无需更新");
                    return "No update needed";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (session != null) session.rollback();
            System.out.println("发生异常: " + e.getMessage());
            return "Error: " + e.getMessage();
        } finally {
            try {
                if (in != null) in.close();
                if (session != null) session.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @PostMapping("/rank")
    public List<compRec> getRank(@RequestBody compRec rec) {
        SqlSession session = null;
        InputStream in = null;
        try {
            in = Resources.getResourceAsStream("SqlMapConfig.xml");
            session = getSession(in);
            compRecOpMapper mapper = session.getMapper(compRecOpMapper.class);
            return mapper.getRank(rec.Difficulty, rec.Sum);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error: " + e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
                if (session != null) session.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

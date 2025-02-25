package com.leyuz.bbs.infrastructure;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Collections;


@Slf4j
public class MyBatisPlusGenerator {

    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/nx-forum?useUnicode=true&characterEncoding=utf-8",
                        "root", "123456")
                .globalConfig(builder -> {
                    builder.author("walker") // 设置作者
                            //.enableSwagger() // 开启 swagger 模式
                            .outputDir(new File("").getAbsolutePath() + "\\nx-forum-infrastructure\\src\\main\\java"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("com.leyuz.bbs") // 设置父包名
                            //.moduleName("infrastructure") // 设置父包模块名
                            .entity("infrastructure.dataobject")
                            .service("domain.gateway")
                            .serviceImpl("infrastructure")
                            .mapper("infrastructure.mapper")
                            .xml("mapper.xml")
                            //.controller("adapter")
                            .pathInfo(Collections.singletonMap(OutputFile.xml,
                                    new File("").getAbsolutePath() + "\\nx-forum-infrastructure\\src\\main\\resources\\mapper")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude("bbs_thread_properties") // 设置需要生成的表名
                            .addTablePrefix("t_", "c_", "bbs_")// 设置过滤表前缀
                            .addTableSuffix("s");
                })
                //.templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }
}

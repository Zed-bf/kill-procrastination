package com.procrastination.killer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 拖延症杀手 —— 启动类
 *
 * AI-Powered Procrastination Killer
 * 帮助用户将宏观目标自动拆解为具体、可执行的微小任务，
 * 通过可视化的任务列表和进度追踪激励用户行动。
 */
@SpringBootApplication
public class ProcrastinationKillerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProcrastinationKillerApplication.class, args);
    }
}

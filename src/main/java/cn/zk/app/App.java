package cn.zk.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * <br/>
 * Created on 2018/6/3 13:53.
 *
 * @author zhubenle
 */
@SpringBootApplication
@ComponentScan(value = {"cn.zk"})
@EntityScan(value = {"cn.zk.entity"})
@EnableJpaRepositories(value = {"cn.zk.repository"})
@Slf4j
public class App {
    public static void main(String[] args) throws UnknownHostException {
        SpringApplication application = new SpringApplication(App.class);
        Environment env = application.run(args).getEnvironment();
        log.info("\n----------------------------------------------------------\n\t" +
                        "com.chehejia.core Application '{}' is running! Access URLs:\n\t" +
                        "访问地址: \t\thttp://{}:{}\n\t" +
                        "h2-console: \thttp://{}:{}/h2-console\n" +
                        "----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"));
    }
}

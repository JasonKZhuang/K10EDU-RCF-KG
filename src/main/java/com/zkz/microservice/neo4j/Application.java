package com.zkz.microservice.neo4j;

import com.zkz.microservice.neo4j.configuration.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Arrays;

@Slf4j
@SpringBootApplication
@EnableTransactionManagement
@RequiredArgsConstructor
public class Application {
    private final AppProperties appProperties;
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        //If you don’t want command line properties to be added to the Environment you can disable them using
        //application.setAddCommandLineProperties(false);
        //print out all the arguments
        System.out.println("\n ===>The application is running with parameters:");
        for (String arg : args) {
            System.out.println(arg);
        }

        //run the application instance
        application.run(args);
    }


    /**
     * Spring Boot will run ALL CommandLineRunner beans once the application context is loaded.
     * Interface used to indicate that a bean should run when it is contained within a SpringApplication.
     * Multiple CommandLineRunner beans can be defined within the same application context and can be ordered using the Ordered interface or @Order annotation.
     *
     * @param ctx
     * @return
     */
    @Bean
    @Order(1)
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {

        log.info("===> The Google API Key:" + appProperties.getGcpKey());

        return args -> {
            System.out.println("1 ===================================>>> Let's inspect the beans provided by Spring Boot <<<====");
            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                // System.out.println(beanName);
            }
        };
    }

}

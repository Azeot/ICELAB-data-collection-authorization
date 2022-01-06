package it.univr.graal;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Program {

    public static void main(String[] argv) {
        final var ctx = new AnnotationConfigApplicationContext(Config.class);
        final var rabbitmqListener = ctx.getBean(RabbitmqListener.class);
        rabbitmqListener.listen();
    }

}

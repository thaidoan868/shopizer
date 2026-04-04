package vn.io.oldmoon.shopizer.rabbitmq;

public interface RabbitConstants {
  String MAIN_EXCHANGE = "app.main.exchange";
  String USER_CREATED_QUEUE = "user.created.queue";
  String USER_CREATED_KEY = "user.created.key";

  String DLX_EXCHANGE = "app.dlx.exchange";
  String DLQ = "app.dead.letter.queue";
  String DEAD_LETTER_ROUTING_KEY = "deadLetter";
}

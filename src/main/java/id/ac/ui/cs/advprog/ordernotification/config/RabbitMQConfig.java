package id.ac.ui.cs.advprog.ordernotification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "auction.finished.queue";
    public static final String EXCHANGE_NAME = "auction.exchange";
    public static final String ROUTING_KEY = "auction.finished.key";

    public static final String WALLET_QUEUE_NAME = "wallet.notification.queue";
    public static final String WALLET_EXCHANGE_NAME = "bidmart.wallet.exchange";
    public static final String WALLET_ROUTING_KEY = "wallet.event.*";
    public static final String AUTH_QUEUE_NAME = "auth.notification.queue";
    public static final String AUTH_EXCHANGE_NAME = "bidmart.auth.exchange";

    @Bean
    public Queue auctionFinishedQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public TopicExchange auctionExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding auctionFinishedBinding(
            @Qualifier("auctionFinishedQueue") Queue queue,
            @Qualifier("auctionExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public Queue walletNotificationQueue() {
        return new Queue(WALLET_QUEUE_NAME, true);
    }

    @Bean
    public TopicExchange walletExchange() {
        return new TopicExchange(WALLET_EXCHANGE_NAME);
    }

    @Bean
    public Binding walletNotificationBinding(
            @Qualifier("walletNotificationQueue") Queue queue,
            @Qualifier("walletExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(WALLET_ROUTING_KEY);
    }

    @Bean
    public Queue authQueue() {
        return new Queue(AUTH_QUEUE_NAME, true);
    }

    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(AUTH_EXCHANGE_NAME);
    }

    @Bean
    public Binding authAccountDisabledBinding(
            @Qualifier("authQueue") Queue authQueue,
            @Qualifier("authExchange") TopicExchange authExchange) {
        return BindingBuilder.bind(authQueue).to(authExchange).with("auth.event.account_disabled");
    }

    @Bean
    public Binding authRoleCreatedBinding(
            @Qualifier("authQueue") Queue authQueue,
            @Qualifier("authExchange") TopicExchange authExchange) {
        return BindingBuilder.bind(authQueue).to(authExchange).with("auth.event.role_created");
    }

    @Bean
    public Binding authPermissionCreatedBinding(
            @Qualifier("authQueue") Queue authQueue,
            @Qualifier("authExchange") TopicExchange authExchange) {
        return BindingBuilder.bind(authQueue).to(authExchange).with("auth.event.permission_created");
    }

    @Bean
    public Binding authRolePermissionChangedBinding(
            @Qualifier("authQueue") Queue authQueue,
            @Qualifier("authExchange") TopicExchange authExchange) {
        return BindingBuilder.bind(authQueue).to(authExchange).with("auth.event.role_permission_changed");
    }

    @Bean
    public Binding authUserRoleChangedBinding(
            @Qualifier("authQueue") Queue authQueue,
            @Qualifier("authExchange") TopicExchange authExchange) {
        return BindingBuilder.bind(authQueue).to(authExchange).with("auth.event.user_role_changed");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

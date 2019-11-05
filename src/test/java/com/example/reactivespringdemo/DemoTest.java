package com.example.reactivespringdemo;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static reactor.core.scheduler.Schedulers.parallel;

@Log4j2
class DemoTest {

    private static void millis(FluxSink<Object> fluxSink) {
        while (true) {
            fluxSink.next(System.currentTimeMillis());
        }
    }

    @Test
    void pubsub() {
        // one publisher
        var publisher = Flux.just(1, 2, 3, 4)
                .log()
                .subscribeOn(parallel()) // execute on other than main
                .map(i -> i * 2);

        // more subscribers/subscriptions to one stream
        publisher.subscribe(add);
        publisher.subscribe(subscriber);
        publisher.subscribe(log::info, log::error, () -> log.info("Done"), s -> s.request(2));
        publisher.subscribe(subBackPressure);

        assertThat(elements).containsExactly(2, 4, 6, 8);
    }

    @Test
    void hotStream() {
        ConnectableFlux<Object> hot =
                Flux.create(DemoTest::millis)
                        .sample(ofSeconds(2))
                        .publish();

        hot.subscribe(System.out::println); // stream doesnt start yet
        hot.subscribe(System.out::println);

        hot.connect(); // only now it starts running
    }

    private List<Integer> elements = new ArrayList<>();
    private Consumer<Integer> add = elements::add;
    private Subscriber<Integer> subscriber = new Subscriber<>() {
        @Override public void onSubscribe(Subscription subscription) {
            subscription.request(10);
        }

        @Override public void onNext(Integer i) {
            log.info("my OnNext({}) ", i);
        }

        @Override public void onError(Throwable throwable) {
            log.error("my Error: {}", throwable.getMessage());
        }

        @Override public void onComplete() {
            log.info("My Done");
        }
    };
    private Subscriber<Integer> subBackPressure = new Subscriber<>() {
        private Subscription s;
        int onNextAmount;

        @Override
        public void onSubscribe(Subscription s) {
            this.s = s;
            s.request(2);
        }

        @Override
        public void onNext(Integer integer) {
            onNextAmount++;
            if (onNextAmount % 2 == 0) {
                s.request(2);
            }
        }

        @Override
        public void onError(Throwable t) {}

        @Override
        public void onComplete() {}
    };

}
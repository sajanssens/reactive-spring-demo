package com.example.reactivespringdemo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Component
@AllArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public Flux<Reservation> getAll() {
        return reservationRepository.findAll()/*.subscribe(reservationRepository::save)*/;
    }

}

@Repository
interface ReservationRepository extends ReactiveMongoRepository<Reservation, String> {

}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
class Reservation {
    @Id
    private String id;
    private String name;

}

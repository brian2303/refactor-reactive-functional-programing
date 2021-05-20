package com.example;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class app {

    public static void main(String[] args) {
        var line = "a";
        var list = List.of(line.split(";"));
        System.out.println(list.size());

//        Optional.of(Map.of(1,2,3,4))
//            .map(Map::values);

//        var columns = Flux.fromIterable()
//                .filter(Objects::isNull)
//                .map(column -> column = "");


//        String a = "";
//        Integer[] numbers = {1,3,4};
//        List l = List.of(numbers);
//        Optional.of(List.of("111",";code"))
//        .filter(li -> li.contains(";"));
//        var l = List.of(";code");
//        System.out.println(l.contains(";"));
//
//        l.get(0);
//        System.out.println(!Objects.isNull(a));
//        Optional.of(3L)
//                .filter(num -> num > 3L)
//                .map(num -> 0L)
//                .orElseGet(() -> 3L);

//        var list = "";
//        var nueva = List.of(list.split(";"));
//
//        nueva.forEach(n -> {
//            System.out.println(n.isBlank());
//        });
    }
}

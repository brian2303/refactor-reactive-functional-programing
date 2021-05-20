package com.example;


import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;


public class HelperKata {
    private static final String EMPTY_STRING = "";
    private static String ANTERIOR_BONO = null;
    private static String characterSeparated = FileCSVEnum.CHARACTER_DEFAULT.getId();
    private static Set<String> codes = new HashSet<>();
    private static AtomicInteger counter = new AtomicInteger(0);



    public static Flux<CouponDetailDto> getListFromBase64File(final String fileBase64) {

        return createFluxFrom(fileBase64)
                .map(HelperKata::toCouponDetail)
                .map(HelperKata::toMapWithCouponDetailAndDto)
                .map();


//        return Flux.fromIterable(
//                bufferedReader.lines().skip(1)
//                        .map(line -> getTupleOfLine(line,line.split(characterSeparated), characterSeparated))
//                        .map(tuple -> {
//                            String dateValidated = null;
//                            String errorMessage = null;
//                            String bonoForObject = null;
//                            String bonoEnviado;
//
//                            if (tuple.getT1().isBlank() || tuple.getT2().isBlank()) {
//                                errorMessage = ExperienceErrorsEnum.FILE_ERROR_COLUMN_EMPTY.toString();
//                            } else if (!codes.add(tuple.getT1())) {
//                                errorMessage = ExperienceErrorsEnum.FILE_ERROR_CODE_DUPLICATE.toString();
//                            } else if (!validateDateRegex(tuple.getT2())) {
//                                errorMessage = ExperienceErrorsEnum.FILE_ERROR_DATE_PARSE.toString();
//                            } else if (validateDateIsMinor(tuple.getT2())) {
//                                errorMessage = ExperienceErrorsEnum.FILE_DATE_IS_MINOR_OR_EQUALS.toString();
//                            } else {
//                                dateValidated = tuple.getT2();
//                            }
//
//                            bonoEnviado = tuple.getT1();
//                            if (ANTERIOR_BONO == null || ANTERIOR_BONO.equals("")) {
//                                ANTERIOR_BONO = typeBono(bonoEnviado);
//                                if (ANTERIOR_BONO == "") {
//                                    bonoForObject = null;
//                                } else {
//                                    bonoForObject = bonoEnviado;
//                                }
//                            } else if (ANTERIOR_BONO.equals(typeBono(bonoEnviado))) {
//                                bonoForObject = bonoEnviado;
//                            } else if (!ANTERIOR_BONO.equals(typeBono(bonoEnviado))) {
//                                bonoForObject = null;
//                            }
//
//                            return CouponDetailDto.aCouponDetailDto()
//                                    .withCode(bonoForObject)
//                                    .withDueDate(dateValidated)
//                                    .withNumberLine(counter.incrementAndGet())
//                                    .withMessageError(errorMessage)
//                                    .withTotalLinesFile(1)
//                                    .build();
//                        }).collect(Collectors.toList())
//        );
        return null;
    }

    private static Map<CouponDetail,CouponDetailDto> toMapWithCouponDetailAndDto(CouponDetail couponDetail) {
        return Map.of(couponDetail,CouponDetailDto.aCouponDetailDto());
    }


    private static CouponDetail toCouponDetail(String line) {
        var columns = List.of(line.split(characterSeparated));
        return Optional.of(columns)
                .filter(HelperKata::hasAllColumns)
                .map(columnsFields -> new CouponDetail(columnsFields.get(0),columnsFields.get(1)))
                .orElseGet(() -> toCouponDetailWithColumnEmpty(line));
    }

    private static CouponDetail toCouponDetailWithColumnEmpty(String line){
        var columns = List.of(line.split(characterSeparated));
        return Optional.of(line)
                .filter(HelperKata::hasCode)
                .map(lineWithCode -> new CouponDetail(columns.get(0),EMPTY_STRING))
                .orElseGet(() -> new CouponDetail(EMPTY_STRING,columns.get(0)));
    }



    private static Flux<String> createFluxFrom(String fileBase64) {
        return Flux.using(
                () -> new BufferedReader(new InputStreamReader(
                        new ByteArrayInputStream(decodeBase64(fileBase64))
                )).lines().skip(1),
                Flux::fromStream,
                Stream::close
        );
    }

    public static String typeBono(String bonoIn) {
        if (bonoIn.chars().allMatch(Character::isDigit)
                && bonoIn.length() >= 12
                && bonoIn.length() <= 13) {
            return ValidateCouponEnum.EAN_13.getTypeOfEnum();
        }
        if (bonoIn.startsWith("*")
                && bonoIn.replace("*", "").length() >= 1
                && bonoIn.replace("*", "").length() <= 43) {
            return ValidateCouponEnum.EAN_39.getTypeOfEnum();

        } else {
            return ValidateCouponEnum.ALPHANUMERIC.getTypeOfEnum();
        }
    }

    public static boolean validateDateRegex(String dateForValidate) {
        String regex = FileCSVEnum.PATTERN_DATE_DEFAULT.getId();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(dateForValidate);
        return matcher.matches();
    }

    private static byte[] decodeBase64(final String fileBase64) {
        return Base64.getDecoder().decode(fileBase64);
    }

    public static boolean validateDateIsMinor(String dateForValidate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(FileCSVEnum.PATTERN_SIMPLE_DATE_FORMAT.getId());
            Date dateActual = sdf.parse(sdf.format(new Date()));
            Date dateCompare = sdf.parse(dateForValidate);
            return dateCompare.compareTo(dateActual) <= 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean hasAllColumns(List<String> columns) {
        return columns
                .stream()
                .noneMatch(String::isBlank);
    }

    private static boolean hasCode(String line) {
        return !line.startsWith(characterSeparated);
    }
}

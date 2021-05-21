package com.example;


import reactor.core.publisher.Flux;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;



public class HelperKata {
    private static final String EMPTY_STRING = "";
    private static String firstCoupon;
    private static final String CHARACTER_SEPARATED = FileCSVEnum.CHARACTER_DEFAULT.getId();
    private static final Set<String> codes = new HashSet<>();
    private static final AtomicInteger counter = new AtomicInteger(0);

    private HelperKata() {
    }

    public static Flux<CouponDetailDto> getListFromBase64File(final String fileBase64) {
        return createFluxFrom(fileBase64)
                .map(HelperKata::toCouponDetail)
                .map(HelperKata::validateColumnBlank)
                .map(HelperKata::validateCoupon)
                .map(HelperKata::dtoValidateCodeRepeated)
                .map(HelperKata::validateDate)
                .map(HelperKata::validateDateIsMinor);
    }

    private static CouponDetail toCouponDetail(String line) {
        var columns = List.of(line.split(CHARACTER_SEPARATED));
        return Optional.of(columns)
                .filter(HelperKata::hasAllColumns)
                .map(columnsFields -> new CouponDetail(columnsFields.get(0),columnsFields.get(1)))
                .orElseGet(() -> toCouponDetailWithColumnEmpty(line));
    }

    public static CouponDetailDto validateColumnBlank(CouponDetail couponDetail){
        return Optional.of(couponDetail)
                .filter(coupon -> coupon.getCode().isBlank() || coupon.getDueDate().isBlank())
                .map(c -> CouponDetailDto
                        .aCouponDetailDto()
                        .withCode(null)
                        .withTotalLinesFile(1)
                        .withNumberLine(counter.incrementAndGet())
                        .withDueDate(null)
                        .withMessageError(ExperienceErrorsEnum.FILE_ERROR_COLUMN_EMPTY.toString()))
                .orElseGet(() -> CouponDetailDto
                        .aCouponDetailDto()
                        .withCode(couponDetail.getCode())
                        .withTotalLinesFile(1)
                        .withNumberLine(counter.incrementAndGet())
                        .withDueDate(couponDetail.getDueDate())
                        .withMessageError(""));
    }

    private static CouponDetailDto validateCoupon(CouponDetailDto couponDetailDto){
        assignOnlyFirstCoupon(couponDetailDto.getCode());
        return Optional.ofNullable(couponDetailDto.getCode())
                .filter(code -> !firstCoupon.equals(typeBono(code)))
                .map(c -> couponDetailDto.withCode(null))
                .orElseGet(() -> couponDetailDto);
    }

    private static CouponDetailDto dtoValidateCodeRepeated(CouponDetailDto couponDetailDto){
        return Optional.ofNullable(couponDetailDto.getCode())
                .filter(code -> !codes.add(code))
                .map(c -> couponDetailDto
                        .withMessageError(ExperienceErrorsEnum.FILE_ERROR_CODE_DUPLICATE.toString()))
                .orElseGet(() -> couponDetailDto);
    }

    private static CouponDetailDto validateDate(CouponDetailDto couponDetailDto){
        return Optional.ofNullable(couponDetailDto.getDueDate())
                .filter(date -> !validateDateRegex(date))
                .map(date -> couponDetailDto.withDueDate(null))
                .filter(coupon -> couponDetailDto.getMessageError().isBlank())
                .map(c -> couponDetailDto
                        .withMessageError(ExperienceErrorsEnum.FILE_ERROR_DATE_PARSE.toString()))
                .orElseGet(() -> couponDetailDto);
    }

    private static CouponDetailDto validateDateIsMinor(CouponDetailDto couponDetailDto){
        return Optional.ofNullable(couponDetailDto.getDueDate())
                .filter(HelperKata::validateDateIsMinor)
                .map(date -> couponDetailDto.withDueDate(null))
                .filter(coupon -> coupon.getMessageError().isBlank())
                .map(c -> couponDetailDto
                        .withMessageError(ExperienceErrorsEnum.FILE_DATE_IS_MINOR_OR_EQUALS.toString())
                        .build())
                .orElseGet(couponDetailDto::build);
    }


    private static String typeBono(String bonoIn) {
        if (isTypeEan13(bonoIn)) {
            return ValidateCouponEnum.EAN_13.getTypeOfEnum();
        }
        if (isTypeEan39(bonoIn)) {
            return ValidateCouponEnum.EAN_39.getTypeOfEnum();
        }
        return ValidateCouponEnum.ALPHANUMERIC.getTypeOfEnum();
    }


    private static CouponDetail toCouponDetailWithColumnEmpty(String line){
        var columns = List.of(line.split(CHARACTER_SEPARATED));
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

    private static boolean validateDateRegex(String dateForValidate) {
        String regex = FileCSVEnum.PATTERN_DATE_DEFAULT.getId();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(dateForValidate);
        return matcher.matches();
    }

    private static byte[] decodeBase64(final String fileBase64) {
        return Base64.getDecoder().decode(fileBase64);
    }

    private static boolean validateDateIsMinor(String dateForValidate) {
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
        return !line.startsWith(CHARACTER_SEPARATED);
    }

    private static void assignOnlyFirstCoupon(String code){
        if(firstCoupon == null){
            firstCoupon = typeBono(code);
        }
    }

    private static boolean isTypeEan13(String bono){
        return bono.chars().allMatch(Character::isDigit) && bono.length() >= 12 && bono.length() <= 13;
    }

    private static boolean isTypeEan39(String bono){
        return bono.startsWith("*")
                && bono.replace("*", "").length() >= 1
                && bono.replace("*", "").length() <= 43;
    }
}

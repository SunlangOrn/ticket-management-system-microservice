package com.mentor.paymentservice.payment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = PaymentStatus.class)
public interface PaymentMapper {

    @Mapping(target = "qrString", expression = "java(payment.getStatus() == PaymentStatus.PENDING ? payment.getQrString() : null)")
    @Mapping(target = "deeplink", expression = "java(payment.getStatus() == PaymentStatus.PENDING ? payment.getDeeplink() : null)")
    @Mapping(target = "qrImage", ignore = true)
    PaymentResponse toResponse(Payment payment);
}